package com.daolfn.prototype;
import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.daolfn.prototype.Util.Rectangle;

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
    private float pageHeight;

    public Word(TextPosition textPosition, int wordIndex, float pageHeight) { // 생성자에 wordIndex 추가
        this.fontSize = textPosition.getFontSize();
        this.x = textPosition.getX();
        this.y = textPosition.getY();
        this.width = textPosition.getWidth();
        this.height = textPosition.getHeight();
        this.text = textPosition.getUnicode();
        this.wordIndex = wordIndex; // 순서 설정
        this.pageHeight = pageHeight;
        this.add(textPosition);
        
    }
    
    public Rectangle getViewerRactangle (float scalingFactor) {
    	return Util.transformToReactPDFViewerCoordinates(x, y, height, width, pageHeight, scalingFactor);
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
//        obj.putAll(getViewerRactangle(0.3f).toJson());
        obj.put("x", x);
        obj.put("y", y);
        obj.put("width", width);
        obj.put("height", height);
        obj.put("pageHeight", pageHeight);
        obj.put("fontSize", fontSize);
        obj.put("text", text);
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
	
	public JSONObject getContextInfo () {
		return contextInfo;
	}
	
	public String getContextInfoStr () {
//		StringBuilder sb = new StringBuilder();
//		sb.append("{");
//		boolean isFirst = true;
//		for (ContextType type : ContextType.values()) {
//			String typeStr = type.toString();
//			if (contextInfo.containsKey(typeStr)) {
//				String contextStr = contextInfo.get(typeStr).toString();
//				if (!isFirst) {
//					sb.append(", ");
//				}
//				sb.append("\"" + typeStr + "\"").append(":").append("\"" + contextStr + "\"");
//				isFirst = false;
//			}
//		}
//		sb.append("}");
//		return sb.toString();
		return contextInfo.toJSONString();
	}
	
	public JSONObject toJson () {
		JSONObject obj = new JSONObject();
        obj.put("rowIndex", rowIndex);
        JSONArray words = new JSONArray();
        this.forEach(word -> words.add(word.toJson()));
        obj.put("words", words);
        obj.put("text", getText());
        if (alignment != null) {
        	obj.put("alignment", alignment.toString());
        }
        
        if (!contextInfo.isEmpty()) {
        	obj.put("context", contextInfo);
        }
        
        return obj;
    }
	final static String PATTERN_TITLE = "\\[(.*?)\\]|\\((.*?)\\)";
	public void setContext (ContextType type, String value) {
		switch (type) {
		case CHAPTER : 
		case ARTICLE : 
		case SECTION : 
		case PARAGRAPH : 
		case SUB_PARAGRAPH :
			JSONObject titleJson = Util.splitTitle(type, value);
			Object[] keyArr = titleJson.keySet().toArray();
			for (Object key : keyArr) {
				Object v = titleJson.get(key);
				
		        Pattern pattern = Pattern.compile(PATTERN_TITLE);
		        Matcher matcher = pattern.matcher(v.toString());
		        String vStr = null;
		        while (matcher.find()) {
		            // matcher.group()은 전체 매치를 반환하지만,
		            // 우리는 괄호 안의 내용만 관심이 있으므로, group(1)과 group(2)를 확인합니다.
		            // 이때 한 그룹이 null일 수 있으므로, 둘 중 하나만 출력합니다.
		            if (matcher.group(1) != null) {
		            	vStr = matcher.group(1);
		            } else if (matcher.group(2) != null) {
		            	vStr = matcher.group(2);
		            }
		        }
		        if (vStr != null) {
		        	contextInfo.put(key.toString(), vStr);
		        } else {
		        	contextInfo.put(key.toString(), v.toString());
		        }
				
			}
			Util.splitTitle(type, value).forEach((k, v) -> {
				
			});
			break;
			default :
				contextInfo.put(type.toString(), value);
				break;
		}
		
	}
	
	public void setContext (JSONObject contextInfo) {
		this.contextInfo = new JSONObject(contextInfo);
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
	private boolean findPageNumber = false;
	
    public Page(int pageIndex) {
        this.pageIndex = pageIndex;
    }
    
    public int getPageNo () {
    	return pageIndex;
    }
    
    public void findPageNumber() {
    	findPageNumber = true;
    }
    
    public boolean isSetPageNumber () {
    	return findPageNumber;
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
	private String part = null;
	
	@Override
    public String toString() {
		return toJson().toString();
    }
	
	public void setPart (String part) {
		this.part = part;
	}
	
	public String getPart () {
		return this.part;
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
    private float pageHeight = 0;
    private int rowIndex = 0;
    private float textStart = Float.MAX_VALUE;
    private float textEnd = 0.0f;
    
    private boolean findFileIndex = false;
    private boolean findPageNumber = false;
    private ContextType beforeContextType = null;
    

    public CustomPDFTextStripper() throws IOException {}

    public RulePDF getRulePDF() {
        return rulePDF;
    }

    
    @Override
    protected void startPage(PDPage page) throws IOException {
        super.startPage(page);
        pageHeight = page.getMediaBox().getHeight();
        currentPage = new Page(pageIndex++); // 새 페이지 시작 시 새 Page 객체 생성
    }

    @Override
    protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
        super.writeString(string, textPositions);
        Row row = new Row(rowIndex++);
       
        int wordIndex = 0; // 행 내에서의 단어 순서
        for (TextPosition textPosition : textPositions) {
            Word word = new Word(textPosition, wordIndex++, pageHeight); // 단어 생성 시 순서 전달
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
//        System.out.println(row.getRowNo() + "/" + currentPage.size());
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
    	
    	
    	
    	rowIndex = 0;
    	textStart = 0.0f;
    	textEnd = 0.0f;
        rulePDF.add(currentPage); // 페이지 종료 시 현재 Page를 RulePDF에 추가
        for (Row row : currentPage) {
        	//직전 page, row를 찾는다
			int beforePageNo = -1;
			int beforeRowNo = -1;
			if (row.getRowNo() == 0) {
				beforePageNo = currentPage.getPageNo() -1;
				if (beforePageNo >= 0) {
					beforeRowNo = rulePDF.get(beforePageNo).size() -1;
				}
				
			} else {
				beforePageNo = currentPage.getPageNo();
				beforeRowNo = row.getRowNo() -1;
			}
			
			boolean findContext = false;
			for (ContextType type : ContextType.values()) {
    			String text = type.getCondition().getPatternText(rulePDF, currentPage.getPageNo(), row.getRowNo());
    			
    			if (text != null) {
    				if (type == ContextType.PAGE_NUMBER) {
    					currentPage.findPageNumber();
    				}
    				findContext = true;
    				
    				//직전row의 현재 기준 상위 context를 물려받는다.
    				if (beforePageNo >= 0 && beforeRowNo >= 0) {
    					//직전 Row를 찾을 수 없음
    					Page beforePage = rulePDF.get(beforePageNo);
    					if (beforePage != null) {
    						Row beforeRow = beforePage.get(beforeRowNo);
        					if (beforeRow != null) {
        						for (ContextType beforeType : ContextType.values()) {
        							//직전 페이지와 같은 경우에는 아래 조건에 따라 context를 상속받는다.
        							if (beforePage.getPageNo() == currentPage.getPageNo()) {
        								if (beforeType == type) {
                							break;
                						}
        							}
        							
        							//제/개정 이력은 상속 대상에서 항상 제외한다.
            						if (ContextType.REVISED_HISTORY == beforeType) {
            							continue;
            						}
        							
            						if (beforeRow.getContextInfo().containsKey(beforeType.toString())) {
            							try {
            								row.setContext(
                									beforeType, 
                									beforeRow.getContextInfo().get(
                											beforeType.toString()
                											)
                									.toString()
                									);
            							} catch (Exception e) {
//            								System.out.println(beforeRow.getContextInfo());
//            								System.out.println("not found data : " + beforeType.toString());
            								e.printStackTrace();
            							}
            							
            						}
            					}
        					}
    					}
    				}
    				row.setContext(type, text.trim());
//    				break;
    			}
    		} 
			
			//문맥을 찾지 못한 경우 직전의 Context를 물려받는다.
			if (!findContext) {
				if (beforePageNo >= 0 && beforeRowNo >= 0) {
					//직전 Row를 찾을 수 없음
					Row beforeRow = rulePDF.get(beforePageNo).get(beforeRowNo);
					JSONObject beforeContext = beforeRow.getContextInfo();
					row.setContext(beforeContext);
				}
			}
//			System.out.println("(" + row.getRowNo() + ")\t" + row.getText() + "\t" + row.getContextInfoStr());
			
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
            Row row = null;
            try {
            	row = pdf.get(pageNo).get(rowNo);
            } catch (Exception e) {
//            	System.out.println("pageNo : " + pageNo + ", rowNo : " + rowNo);
            	e.printStackTrace();
            }
            
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
            
            
            // 첫 페이지에서 중앙 정렬되고, 페이지 번호를 제외한 첫 번째 라인을 찾습니다.
            // 여기서는 pdf 객체로부터 첫 페이지의 모든 Row를 순회하며 조건에 맞는 Row를 찾는 것으로 가정합니다.
        	
            Row row = pdf.get(page).get(titleRow);
            String rowStr = row.getText().trim();
            //공백 라인인 경우 제외
            if (rowStr == null || "".equals(rowStr)) {
            	return null;
            }
            
            //페이지의 가장 첫 번째 라인이 페이지 번호인 경우 제외
            if ((titleRow == 0 && row.getContextInfo().containsKey(PAGE_NUMBER.toString()))) {
            	return null;
            } 
            
            //pdf 파일에 part가 이미 세팅된 경우 제외
            if (pdf.getPart() != null) {
            	return null;
            }
            
            
            
            boolean isCenterAligned = row.getAlignment() == Alignment.CENTER; // 가정: Row가 중앙 정렬된 경우
            if (isCenterAligned) {
            	pdf.setPart(row.getText());
            	return row.getText();
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
        	Row row = pdf.get(page).get(titleRow); // 페이지에서 해당 row 가져오기

            String rowText = row.getText(); // row에서 텍스트 가져오기

            // 제/개정 형식을 포착하는 정규식 설정
            String revisedHistoryPattern = "\\b(?:제 ?정|개정\\[\\d+\\]|전면 개정\\[\\d+\\]|개 ?정\\[\\d+\\]|전면 개정 \\[\\d+\\])\\s*\\d{4}\\.\\s*\\d{1,2}\\.\\s*\\d{1,2}\\.";
            Pattern pattern = Pattern.compile(revisedHistoryPattern);
            Matcher matcher = pattern.matcher(rowText);
            
            if(matcher.find()) {
                String matchedRevision = matcher.group(); // 일치하는 제/개정 포맷 발견
                // 결과 정제: 날짜 제거 및 공백 정리
                matchedRevision = matchedRevision.replaceAll("\\s*\\d{4}\\.\\s*\\d{1,2}\\.\\s*\\d{1,2}\\.", "").replaceAll("\\s", "");
                return matchedRevision; // 결과 반환
            }

            return null; // 조건에 맞지 않으면 null 반환
        }
	}),
	CHAPTER("장", new ContextCondition() {
        @Override
        public String getPatternText(RulePDF pdf, int page, int titleRow) {
        	StringBuilder detectedChapters = new StringBuilder();

        	Row row = pdf.get(page).get(titleRow);

        	String rowText = row.getText(); // 가정: Row 객체에서 전체 텍스트를 얻는 메서드
            String removeSpaceText = rowText.trim().replaceAll(" ", "");
            // ‘제 [숫자] 장 [장제목]’ 형식의 패턴 인식
            // 띄어쓰기에 유연하도록 정규식 패턴을 설계
            String chapterPattern = "제\\s*\\d+\\s*장\\s+[\\p{L}\\p{M}*\\s]+";
            Pattern pattern = Pattern.compile(chapterPattern);
            Matcher matcher = pattern.matcher(rowText);

            // 중앙 또는 왼쪽 정렬인지 확인
            boolean isCenterOrLeftAligned = row.getAlignment()  == Alignment.CENTER || row.getAlignment()  == Alignment.LEFT;

            // 조건에 일치하는 경우 추출 및 저장
            if (isCenterOrLeftAligned) {
            	
                if (matcher.find()) {
                	detectedChapters.append(matcher.group()).append("\n");
                }
                else if (removeSpaceText.startsWith("부칙")) {
                	detectedChapters.append(removeSpaceText).append("\n");
                }
            }

            // detectedChapters가 비어있지 않으면, 이를 반환
            return detectedChapters.toString().isEmpty() ? null : detectedChapters.toString();
        }
	}),
	SECTION("절", new ContextCondition() {
        @Override
        public String getPatternText(RulePDF pdf, int page, int titleRow) {
        	Row row = pdf.get(page).get(titleRow); // 페이지에서 해당 row 가져오기

            String rowText = row.getText(); // row에서 텍스트 가져오기

            // ‘제 [숫자] 절 [절제목]’ 형식의 패턴 인식을 위한 정규식 설정
            // 숫자와 ‘절’ 사이의 공백이 여러 개 있을 수 있으며, 절 제목에는 문자와 공백, 기호가 포함될 수 있다는 점을 반영
            String sectionPattern = "^제\\s*\\d+\\s*절\\s*[\\p{L}\\p{M}*\\s,.]+";
            Pattern pattern = Pattern.compile(sectionPattern);
            Matcher matcher = pattern.matcher(rowText);

            // 중앙 또는 왼쪽 정렬 확인
            boolean isCenterOrLeftAligned = row.getAlignment()  == Alignment.CENTER || row.getAlignment()  == Alignment.LEFT;

            // 조건에 일치하는 경우 문자열 반환
            if (isCenterOrLeftAligned && matcher.find()) {
                return matcher.group(); // 일치하는 절 제목 및 번호 반환
            }

            return null; // 조건에 맞지 않으면 null 반환
        }
	}),
	ARTICLE("조", new ContextCondition() {
        @Override
        public String getPatternText(RulePDF pdf, int page, int titleRow) {
        	Row row = pdf.get(page).get(titleRow); // 페이지에서 해당 row 가져오기

            String rowText = row.getText(); // row에서 텍스트 가져오기

            // '제 [숫자] 조 [목적]' 형식의 패턴 인식을 위한 정규식 설정
            // 숫자와 ‘조’ 사이의 공백이 여러 개 있을 수 있으며,
            // 조제목에는 문자와 공백, 기호가 포함될 수 있다는 점을 반영하되, 추가 내용은 포함하지 않음
            String articlePattern = "제\\s*\\d+\\s*조[\\s\\[\\(][가-힣A-Za-z\\s\\-\\[\\]]+[\\)\\]]";
            Pattern pattern = Pattern.compile(articlePattern);
            Matcher matcher = pattern.matcher(rowText);

            // 조건에 일치하는 경우 문자열 반환
            if (matcher.find()) {
                return matcher.group(); // 일치하는 조 제목 및 번호 반환
            }

            return null; // 조건에 맞지 않으면 null 반환
        }
	}),
	PARAGRAPH("항", new ContextCondition() {
        @Override
        public String getPatternText(RulePDF pdf, int page, int titleRow) {
            Row row = pdf.get(page).get(titleRow); // 페이지에서 해당 row 가져오기

            String rowText = row.getText(); // row에서 텍스트 가져오기

            // '①', '②', '③',... 및 '(1)', '(2)', '(3)',... 항 식별자를 인식하는 정규식 설정
            String paragraphPattern = "(①|②|③|④|⑤|⑥|⑦|⑧|⑨|⑩|⑪|⑫|⑬|⑭|⑮|⑯|⑰|⑱|⑲|⑳|㉑|㉒|㉓|㉔|㉕|㉖|㉗|㉘|㉙|㉚|\\(1\\)|\\(2\\)|\\(3\\)|\\(4\\)|\\(5\\)|\\(6\\)|\\(7\\)|\\(8\\)|\\(9\\)|\\(10\\)|\\(11\\)|\\(12\\)|\\(13\\)|\\(14\\)|\\(15\\)|\\(16\\)|\\(17\\)|\\(18\\)|\\(19\\)|\\(20\\)|\\(21\\)|\\(22\\)|\\(23\\)|\\(24\\)|\\(25\\)|\\(26\\)|\\(27\\)|\\(28\\)|\\(29\\)|\\(30\\))";
            Pattern pattern = Pattern.compile(paragraphPattern);
            Matcher matcher = pattern.matcher(rowText);
            
            if (matcher.find()) {
            	String matchStr = matcher.group();
            	String resultStr = null;
            	for (int i = 0 ; i < Const.PARAGRAPH.length ; i++) {
            		for (int j = 0 ; j < Const.PARAGRAPH[i].length ; j++) {
            			if (Const.PARAGRAPH[i][j].equals(matchStr)) {
            				resultStr = (j+1)+"";
            				break;
            			}
            		}
            	}
            	
                return "제" + resultStr + "항"; // 일치하는 항의 시작 식별자 반환
            }

            return null; // 조건에 맞지 않으면 null 반환
        }
	}),
	SUB_PARAGRAPH("호", new ContextCondition() {
        @Override
        public String getPatternText(RulePDF pdf, int page, int titleRow) {
        	Row row = pdf.get(page).get(titleRow); // 페이지에서 해당 row 가져오기

            String rowText = row.getText(); // row에서 텍스트 가져오기

            // 호 식별자 '(1).', '(2).' 등을 포착하는 정규식 설정
            String subParagraphPattern = "^\\b(\\d+)\\.\\s";
            Pattern pattern = Pattern.compile(subParagraphPattern);
            Matcher matcher = pattern.matcher(rowText);
            
            if (matcher.find()) {
                int subParagraphNumber = Integer.parseInt(matcher.group(1));
                return "제" + subParagraphNumber + "호"; // 일치하는 호의 번호 반환
            }

            return null; // 조건에 맞지 않으면 null 반환
        }
	}),
	ITEM("목", new ContextCondition() {
        @Override
        public String getPatternText(RulePDF pdf, int page, int titleRow) {
        	Row row = pdf.get(page).get(titleRow); // 페이지에서 해당 row 가져오기

            String rowText = row.getText(); // row에서 텍스트 가져오기

            // '목' 식별자 '가.', '나.', ..., 로마 숫자, 알파벳을 포착하는 정규식 설정
            String itemPattern = "\\b([가-힣]|[a-z]|[ivxlcdm]+)\\.";
            Pattern pattern = Pattern.compile(itemPattern);
            Matcher matcher = pattern.matcher(rowText);
            
            StringBuilder items = new StringBuilder();
            while(matcher.find()) {
                String matchedItem = matcher.group(1); // 일치하는 '목'의 식별자 추출
                items.append(matchedItem).append("목\n"); // '가목', '나목', ... 형식으로 추가
            }

            // 일치하는 '목'이 있었다면 반환, 없으면 null 반환
            return items.length() > 0 ? items.toString() : null;
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
	
	public String getContextName () {
		return name;
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
