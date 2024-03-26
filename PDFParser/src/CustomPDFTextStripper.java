import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class CustomPDFTextStripper {
    static class Row {
        float startY;
        StringBuilder text = new StringBuilder();
        List<TextStyle> textStyles = new ArrayList<>();

        public Row(float startY) {
            this.startY = startY;
        }

        public void addText(String text, TextStyle textStyle) {
            this.text.append(text);
            this.textStyles.add(textStyle);
        }

        public JSONObject toJSON() {
            JSONObject row = new JSONObject();
            row.put("text", text.toString());

            JSONArray styles = new JSONArray();
            for (TextStyle textStyle : textStyles) {
                styles.add(textStyle.toJSON());
            }
            row.put("styles", styles);

            return row;
        }
    }

    static class TextStyle {
        float fontSize;
        boolean isBold;
        boolean isItalic;

        public TextStyle(float fontSize, boolean isBold, boolean isItalic) {
            this.fontSize = fontSize;
            this.isBold = isBold;
            this.isItalic = isItalic;
        }

        public JSONObject toJSON() {
            JSONObject style = new JSONObject();
            style.put("fontSize", fontSize);
            style.put("isBold", isBold);
            style.put("isItalic", isItalic);
            return style;
        }
    }

    public static void main(String[] args) {
        try {
            PDDocument document = PDDocument.load(new File(Const.FILE_PATH));
            PDFTextStripper stripper = new PDFTextStripper() {
                List<Row> rows = new ArrayList<>();
                final float lineThreshold = 1f; // Adjust as necessary for your document

                @Override
                protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
                    if (!textPositions.isEmpty()) {
                        float startY = textPositions.get(0).getYDirAdj();
                        // Find if there is an existing row with a startY within the threshold
                        Row currentRow = rows.stream()
                                .filter(row -> Math.abs(row.startY - startY) < lineThreshold)
                                .findFirst()
                                .orElseGet(() -> {
                                    Row newRow = new Row(startY);
                                    rows.add(newRow);
                                    return newRow;
                                });
                        
                        
                        
                        float fontSize = textPositions.get(0).getFontSizeInPt();
                        boolean isBold = textPositions.get(0).getFont().getName().toLowerCase().contains("bold");
                        boolean isItalic = textPositions.get(0).getFont().getName().toLowerCase().contains("italic");
                        
                        currentRow.addText(text, new TextStyle(fontSize, isBold, isItalic));
                        System.out.println(currentRow.toJSON());
                    }
                }

                @Override
                public String getText(PDDocument doc) throws IOException {
                    super.getText(doc);
                    JSONArray jsonArray = new JSONArray();
                    for (Row row : rows) {
                        jsonArray.add(row.toJSON());
                    }
                    return jsonArray.toString();
                }
            };
            String jsonText = stripper.getText(document);
            System.out.println(jsonText);
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}