import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;

public class PDFBoxExam {
	public static void main (String[] args) {
//		PDDocument document = null;
		File file = new File("C:\\DEV\\GRE\\1-1_당사규정지침\\장애관리지침[4]_변경후_175.pdf");
		try (PDDocument document = PDDocument.load(file);) {
			int pageCount = document.getNumberOfPages();
			System.out.println("페이지 수: " + pageCount);
			
			PDFTextStripper pdfStripper = new PDFTextStripper();
			
			PDPage page = document.getPage(1);
			
			
			
			String text = pdfStripper.getText(document);
			System.out.println("=======================================================");
			System.out.println(text);
			System.out.println("=======================================================");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
