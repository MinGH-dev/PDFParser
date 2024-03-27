import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//Word 클래스 정의
class Word extends ArrayList<TextPosition> {
    private float fontSize;
    private float x, y, width, height;
    private String text;
    private int wordIndex; // 행 내 순서

    public Word(TextPosition textPosition, int wordIndex) { // 생성자에 wordIndex 추가
        this.fontSize = textPosition.getFontSize();
        this.x = textPosition.getX();
        this.y = textPosition.getY();
        this.width = textPosition.getWidth();
        this.height = textPosition.getHeight();
        this.text = textPosition.getUnicode();
        this.wordIndex = wordIndex; // 순서 설정
        this.add(textPosition);
    }
    
    public String getText () {
    	return text;
    }
    
    public float getX () {
    	return x;
    }
    
    public float getY () {
    	return y;
    }
    
    public float getWidth () {
    	return width;
    }
    
    public float getHeight () {
    	return height;
    }

    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("fontSize", fontSize);
        obj.put("text", text);
        obj.put("x", x);
        obj.put("y", y);
        obj.put("width", width);
        obj.put("height", height);
        obj.put("wordIndex", wordIndex); // JSON에 순서 정보 추가
        return obj;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }
}

//Row 클래스 정의
class Row extends ArrayList<Word> {
	private int rowIndex;
	private StringBuilder rowSb;
	private Alignment alignment = null;
	private JSONObject contextInfo = null;
    public Row(int rowIndex) {
        this.rowIndex = rowIndex;
        this.rowSb = new StringBuilder();
        this.contextInfo = new JSONObject();
    }
    
    public int getRowNo () {
    	return rowIndex;
    }
    
    @Override
    public boolean add (Word word) {
    	rowSb.append(word.getText());
    	return super.add(word);
    }
    
    public String getText () {
    	return rowSb.toString();
    }
    
	@Override
    public String toString() {
		return toJson().toString();
    }
	
	public JSONObject toJson () {
		JSONObject obj = new JSONObject();
        obj.put("rowIndex", rowIndex);
        JSONArray words = new JSONArray();
        this.forEach(word -> words.add(word.toJson()));
        obj.put("words", words);
        if (alignment != null) {
        	obj.put("alignment", alignment.toString());
        }
        
        if (!contextInfo.isEmpty()) {
        	obj.put("context", contextInfo);
        }
        
        return obj;
    }
	
	public void setContext (ContextType type, String value) {
		contextInfo.put(type.toString(), value);
	}
	
	public void setAlignment (float pageStart, float pageEnd) {
    	float left = Float.MAX_VALUE;
        float right = 0;
        
        for (Word word : this) {
            if (word.getX() < left) left = word.getX();
            if ((word.getX() + word.getWidth()) > right) right = word.getX() + word.getWidth();
        }
        
        
        float pageWidth = pageEnd;
        
        float rowCenter = left + ((right - left) / 2);
        
        // 간단한 로직으로 좌측, 중앙, 우측 판단 (나중에 수정 가능)
        if (rowCenter <= pageWidth * 1 / 3 || left == pageStart) {
        	alignment = Alignment.LEFT;
        } else if (rowCenter <= pageWidth * 2 / 3) {
        	alignment = Alignment.CENTER;
        } else {
        	alignment = Alignment.RIGHT;
        }
        
        
    }
	
	public Alignment getAlignment () {
		return alignment;
	}
	
	public void updateDBRow (DBRow dbRow) {
		
	}
}

//Page 클래스 정의
class Page extends ArrayList<Row> {
	private int pageIndex;
	private float pageWidth;
	
    public Page(int pageIndex) {
        this.pageIndex = pageIndex;
    }
    
    public int getPageNo () {
    	return pageIndex;
    }
    
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("pageIndex", pageIndex);
        JSONArray rows = new JSONArray();
        this.forEach(row -> rows.add(row.toJson()));
        obj.put("rows", rows);
        return obj;
    }
    
    @Override
    public String toString() {
    	return toJson().toString();
    }
    
    public boolean isFirstOrLastRow(int rowNum) {
    	if (size() == rowNum || rowNum == 0) {
    		return true;
    	} else {
    		return false;
    	}
    }
}

//RulePDF 클래스 정의
class RulePDF extends ArrayList<Page> {
	@Override
    public String toString() {
		return toJson().toString();
    }
	
	public JSONArray toJson () {
		JSONArray pages = new JSONArray();
        this.forEach(page -> pages.add(page.toJson()));
        return pages;
    }
}

enum Alignment {
	LEFT, CENTER, RIGHT
}

enum FontLevel {
	GREATE, LARGE, MEDIUM, SMALL
} 

class CustomPDFTextStripper extends PDFTextStripper {
    private RulePDF rulePDF = new RulePDF();
    private Page currentPage = null;
    private int pageIndex = 0;
    private int rowIndex = 0;
    private float textStart = Float.MAX_VALUE;
    private float textEnd = 0.0f;

    public CustomPDFTextStripper() throws IOException {}

    public RulePDF getRulePDF() {
        return rulePDF;
    }

    
    @Override
    protected void startPage(PDPage page) throws IOException {
        super.startPage(page);
        currentPage = new Page(pageIndex++); // 새 페이지 시작 시 새 Page 객체 생성
    }

    @Override
    protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
        super.writeString(string, textPositions);
        Row row = new Row(rowIndex++);
        TextPosition firstPosition = textPositions.get(0);
       
        int wordIndex = 0; // 행 내에서의 단어 순서
        for (TextPosition textPosition : textPositions) {
            Word word = new Word(textPosition, wordIndex++); // 단어 생성 시 순서 전달
            row.add(word); // Word를 Row에 추가
            
            if (!"".equals(word.getText().trim())) {
            	float rowStart = textPosition.getX();
                float rowEnd = textPosition.getX() + textPosition.getWidth();
                
                if (rowStart < textStart) {
                	textStart = rowStart;
                }
                
                if (rowEnd > textEnd) {
                	textEnd = rowEnd;
                }
            }
        }
        currentPage.add(row); // Row를 현재 Page에 추가
    }

    @Override
    protected void endPage(PDPage page) {
    	//페이지 종료 시 각 row의 alignment를 세팅함
    	for (Row row : currentPage) {
    		if (!"".equals(row.getText().trim())) {
    			row.setAlignment(textStart, textEnd);
    		}
    		
    	}
    	
    	textStart = 0.0f;
    	textEnd = 0.0f;
        rulePDF.add(currentPage); // 페이지 종료 시 현재 Page를 RulePDF에 추가
        for (Row row : currentPage) {
    		if (!"".equals(row.getText().trim())) {
    			for (ContextType type : ContextType.values()) {
    				
        			String text = type.getCondition().getPatternText(rulePDF, currentPage.getPageNo(), row.getRowNo());
        			if (text != null) {
        				row.setContext(type, text);
        			}
        		} 
    			System.out.println(row.getText() + "\t" + row.toJson());
    		}
    		
    	}
    }
    
    
}

class RulePDFContextParser {
	
}

class DBRow {
	private String filePath = null;
	private String part = null; //문서 타이틀 / 편
	private String chapter = null; //장
	private String section = null; //절
	private String article = null; //조
	private String paragraph = null; //항
	private String subParagraph = null; //호
	private String item = null;//목
	private String generalProvisions = null; //총칙
	
	private String content = null;

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getPart() {
		return part;
	}

	public void setPart(String part) {
		this.part = part;
	}

	public String getChapter() {
		return chapter;
	}

	public void setChapter(String chapter) {
		this.chapter = chapter;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public String getArticle() {
		return article;
	}

	public void setArticle(String article) {
		this.article = article;
	}

	public String getParagraph() {
		return paragraph;
	}

	public void setParagraph(String paragraph) {
		this.paragraph = paragraph;
	}

	public String getSubParagraph() {
		return subParagraph;
	}

	public void setSubParagraph(String subParagraph) {
		this.subParagraph = subParagraph;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public String getGeneralProvisions() {
		return generalProvisions;
	}

	public void setGeneralProvisions(String generalProvisions) {
		this.generalProvisions = generalProvisions;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	
}

class DBData extends ArrayList<DBRow> {
	
}

//문맥 종류
enum ContextType {
	PAGE_NUMBER("페이지 번호", new ContextCondition() {

		@Override
		public String getPatternText(RulePDF pdf, int pageNo, int rowNo) {
			// 페이지의 Row 객체를 얻습니다. 예시에서는 Row 객체를 직접 가져오는 방법이 명시되지 않았으므로,
            // 이 부분은 가정에 기반한 것입니다. 실제 구현에서는 해당 Row를 어떻게 가져올지 결정해야 합니다.
            Row row = pdf.get(pageNo).get(rowNo);

            String rowText = row.getText(); // 가정: Row 객체에서 전체 텍스트를 얻는 메서드
            Alignment alignment = row.getAlignment(); // 가정: Row 객체가 중앙 정렬인지 확인하는 메서드
            
            // 페이지 번호 패턴 정규식
            String pageNumberPattern = "\\d+|-(\\d+)-";
            Pattern pattern = Pattern.compile(pageNumberPattern);
            Matcher matcher = pattern.matcher(rowText);

            // 첫 번째 또는 마지막 줄인지 확인 (가정)
            boolean isFirstOrLastRow = pdf.get(pageNo).isFirstOrLastRow(rowNo);

            if (alignment == Alignment.CENTER && isFirstOrLastRow && matcher.find()) {
                return matcher.group(); // 일치하는 페이지 번호 텍스트 반환
            }
            return null; // 인식 조건에 맞지 않으면 null 반환
		}
		
	}),
	PART("편", new ContextCondition() {
        @Override
        public String getPatternText(RulePDF pdf, int page, int titleRow) {
            // 조건에 따라, 첫 페이지를 기준으로 제목을 인식합니다.
            if (page != 0) {
                // 첫 페이지가 아니면 제목으로 간주하지 않습니다.
                return null;
            }
            
            // 첫 페이지에서 중앙 정렬되고, 페이지 번호를 제외한 첫 번째 라인을 찾습니다.
            // 여기서는 pdf 객체로부터 첫 페이지의 모든 Row를 순회하며 조건에 맞는 Row를 찾는 것으로 가정합니다.
            List<Row> rows = pdf.get(0); // 가정: 페이지의 모든 Row를 얻는 메서드
            
            for (Row row : rows) {
                boolean isCenterAligned = row.getAlignment() == Alignment.CENTER; // 가정: Row가 중앙 정렬된 경우
                if (isCenterAligned) {
                    // 페이지 번호를 제외한 조건을 만족하는 첫 번째 Row가 제목으로 간주됩니다.
                    // 여기서는 단순화를 위해 모든 Row가 페이지 번호가 아니라고 가정합니다.
                    // 실제 구현에서는 페이지 번호 인식 로직을 추가하여 해당 Row를 제외시켜야 합니다.
                    boolean isPageNumber = false; // 페이지 번호 인식 로직에 따라 설정
                    if (!isPageNumber) {
                        return row.getText(); // 가정: Row 객체에서 전체 텍스트를 얻는 메서드
                    }
                }
            }
            return null; // 조건에 맞는 제목이 없으면 null 반환
        }
    }),
	FILE_INDEX("목차", new ContextCondition() {
        @Override
        public String getPatternText(RulePDF pdf, int page, int titleRow) {
        	return null;
        }
	}),
	REVISED_HISTORY("제/개정", new ContextCondition() {
        @Override
        public String getPatternText(RulePDF pdf, int page, int titleRow) {
        	StringBuilder detectedIndexes = new StringBuilder();

        	List<Row> rows = pdf.get(page);

            for (Row row : rows) {
                String rowText = row.getText(); // 가정: Row 객체에서 전체 텍스트를 얻는 메서드

                // 조건1: 텍스트가 오른쪽에 치우친 경우
                boolean isRightAligned = row.getAlignment() == Alignment.RIGHT; // 가정: Row가 오른쪽 정렬된 경우

                // 조건2: 특수 문자를 제외한 row라인의 패턴 인식
                String indexPattern = "(제 정|개정\\[\\d+\\]|전면 개정\\[\\d+\\])\\s+\\d{4}\\.\\s*\\d{2}\\.\\s*\\d{2}\\.";
                Pattern pattern = Pattern.compile(indexPattern);
                Matcher matcher = pattern.matcher(rowText);

                // 제목을 이미 찾았고, 제개정 내역이 여러 라인일 수 있다고 가정
                // 조건3: 항상 제목이 등장한 이후에 등장하지는 않음
                if (matcher.find() && isRightAligned) {
                    // 일치하는 내용 추가
                    detectedIndexes.append(matcher.group()).append("\n");
                }
            }

            return detectedIndexes.toString().isEmpty() ? null : detectedIndexes.toString();
        }
	}),
	CHAPTER("장", new ContextCondition() {
        @Override
        public String getPatternText(RulePDF pdf, int page, int titleRow) {
        	StringBuilder detectedChapters = new StringBuilder();

        	Row row = pdf.get(page).get(titleRow);

        	String rowText = row.getText(); // 가정: Row 객체에서 전체 텍스트를 얻는 메서드
            
            // ‘제 [숫자] 장 [장제목]’ 형식의 패턴 인식
            // 띄어쓰기에 유연하도록 정규식 패턴을 설계
            String chapterPattern = "제\\s*\\d+\\s*장\\s+[\\p{L}\\p{M}*\\s]+";
            Pattern pattern = Pattern.compile(chapterPattern);
            Matcher matcher = pattern.matcher(rowText);

            // 중앙 또는 왼쪽 정렬인지 확인
            boolean isCenterOrLeftAligned = row.getAlignment()  == Alignment.CENTER || row.getAlignment()  == Alignment.LEFT;

            // 조건에 일치하는 경우 추출 및 저장
            if (isCenterOrLeftAligned && matcher.find()) {
                detectedChapters.append(matcher.group()).append("\n");
            }

            // detectedChapters가 비어있지 않으면, 이를 반환
            return detectedChapters.toString().isEmpty() ? null : detectedChapters.toString();
        }
	}),
	SECTION("절", new ContextCondition() {
        @Override
        public String getPatternText(RulePDF pdf, int page, int titleRow) {
        	return null;
        }
	}),
	ARTICLE("조", new ContextCondition() {
        @Override
        public String getPatternText(RulePDF pdf, int page, int titleRow) {
        	return null;
        }
	}),
	PARAGRAPH("항", new ContextCondition() {
        @Override
        public String getPatternText(RulePDF pdf, int page, int titleRow) {
        	return null;
        }
	}),
	SUB_PARAGRAPH("호", new ContextCondition() {
        @Override
        public String getPatternText(RulePDF pdf, int page, int titleRow) {
        	return null;
        }
	}),
	ITEM("목", new ContextCondition() {
        @Override
        public String getPatternText(RulePDF pdf, int page, int titleRow) {
        	return null;
        }
	})
	
	;
	private ContextCondition condition = null;
	private String name = null;
	
	private ContextType (String name, ContextCondition condition) {
		this.name = name;
		this.condition = condition;
	}
	
	public ContextCondition getCondition () {
		return condition;
	}
	
}

interface ContextCondition {
	public String getPatternText(RulePDF pdf, int page, int row);
}


public class PDFBoxExam {
	
	
	public static void main (String[] args) {
//		PDDocument document = null;
		try {
            PDDocument document = PDDocument.load(new File(Const.FILE_PATH));
            CustomPDFTextStripper stripper = new CustomPDFTextStripper();
            stripper.getText(document); // PDF에서 텍스트 추출 및 구조화
            RulePDF rulePDF = stripper.getRulePDF();
            // 추출된 데이터(rulePDF)를 사용하는 로직
            document.close();
            StringBuilder sb = new StringBuilder();
            Util.prettyPrint(sb, rulePDF.toJson(), 0);
            System.out.println(sb.toString());
            
            
        } catch (IOException e) {
            e.printStackTrace();
        }
		
	}

}
