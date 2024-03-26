import java.io.File;
import java.util.List;

import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;


public class TabulaExam {
	public static void main(String[] args) {
        // PDF 파일 경로
        String pdfFilePath = "C:\\DEV\\GRE\\1-1_당사규정지침\\장애관리지침[4]_변경후_175.pdf";

        try {
            // Tabula 객체를 이용해 PDF 파일 로드
            org.apache.pdfbox.pdmodel.PDDocument pdDocument = org.apache.pdfbox.pdmodel.PDDocument.load(new File(pdfFilePath));
            
            
            pdDocument.getPage(1);
            
            technology.tabula.ObjectExtractor oe = new technology.tabula.ObjectExtractor(pdDocument);
            
            // 페이지 번호 설정 (예: 첫 번째 페이지)
            technology.tabula.Page page = oe.extract(2); // 1은 첫 번째 페이지를 의미

            System.out.println("page : " + page.toString());
            
            // 표 추출 알고리즘 사용
            SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
            List<Table> tableList = sea.extract(page);
            
            for(Table table : tableList) {
            	System.out.println("table : " + table.toString());
                // 테이블의 모든 행을 반복 처리
                for (List<RectangularTextContainer> row : table.getRows()) {
                    // 각 셀의 텍스트를 콘솔에 출력
                    for (RectangularTextContainer cell : row) {
                    	
                        System.out.print(cell.isEmpty() + "\t|" + cell.getText() + "\t|");
                        System.out.println(cell.toString());
                    }
                    System.out.println();
                }
                System.out.println("----------------------------------------");
            }
            
            oe.close();
            pdDocument.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
