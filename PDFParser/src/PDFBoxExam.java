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

    public Row(int rowIndex) {
        this.rowIndex = rowIndex;
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
        return obj;
    }
}

//Page 클래스 정의
class Page extends ArrayList<Row> {
	private int pageIndex;

    public Page(int pageIndex) {
        this.pageIndex = pageIndex;
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

class CustomPDFTextStripper extends PDFTextStripper {
    private RulePDF rulePDF = new RulePDF();
    private Page currentPage = null;
    private int pageIndex = 0;
    private int rowIndex = 0;

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
        int wordIndex = 0; // 행 내에서의 단어 순서
        for (TextPosition textPosition : textPositions) {
            Word word = new Word(textPosition, wordIndex++); // 단어 생성 시 순서 전달
            row.add(word); // Word를 Row에 추가
        }
        currentPage.add(row); // Row를 현재 Page에 추가
    }

    @Override
    protected void endPage(PDPage page) {
        rulePDF.add(currentPage); // 페이지 종료 시 현재 Page를 RulePDF에 추가
    }
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
