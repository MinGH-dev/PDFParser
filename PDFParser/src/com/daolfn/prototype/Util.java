package com.daolfn.prototype;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.yhdatabase.yfds.util.FdsUtil;

public class Util {
	/*
	public static void prettyPrint(StringBuilder sb, Object obj, int indent) {
		if (obj instanceof String) {
			Object toJson = null;
        	try {
				toJson = new JSONParser().parse(FdsUtil.nvl(obj));
				prettyPrint(sb, toJson, indent);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				String[] lines = ((String) obj).split("\n");
                // 첫 줄은 이미 들여쓰기가 적용된 상태이므로 바로 출력
				sb.append("\"");
				sb.append(lines[0]);
                // 두 번째 줄부터 들여쓰기 적용하여 출력
                for (int i = 1; i < lines.length; i++) {
                	sb.append("\n");
                    addIndent(sb, indent);
                    sb.append(lines[i]);
                }
                sb.append("\"");
			}
		} else 
		if (obj instanceof JSONObject) {
            JSONObject json = (JSONObject) obj;
            // TreeSet을 사용하여 key를 내림차순으로 정렬
            TreeSet<String> sortedKeys = new TreeSet<>(Collections.reverseOrder());
            sortedKeys.addAll(json.keySet());

            for (String key : sortedKeys) {
                addIndent(sb, indent);
                if (sb.length() == 0) {
                	sb.append("\"" + key + "\": ");
                } else {
                	sb.append("\n,\"" + key + "\": ");
                }
                
                prettyPrint(sb, json.get(key), indent + 1);
            }
        } else if (obj instanceof JSONArray) {
            JSONArray array = (JSONArray) obj;
            int thisIndent = indent;
            addIndent(sb, thisIndent);
            sb.append("[\n");
            int i = 0;
            for (Object element : array) {
            	addIndent(sb, indent); // 각 요소 전에 들여쓰기 추가
//                prettyPrint(sb, element, indent);
            	prettyPrint(sb, element, indent + 1);
                if (i++ < array.size() - 1) {
                    sb.append(",");
                }
                sb.append("\n"); // 요소 처리 후 줄바꿈
            }
            addIndent(sb, thisIndent);
            sb.append("\n]");
        } else {
            // 문자열값에 줄바꿈 문자가 있을 경우 처리
            if (obj instanceof String && ((String) obj).contains("\n")) {
            	Object toJson = null;
            	try {
					toJson = new JSONParser().parse(FdsUtil.nvl(obj));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	
            	if (!FdsUtil.isNullOrEmpty(toJson)) {
					prettyPrint(sb, toJson, indent + 1);
				} else {
					String[] lines = ((String) obj).split("\n");
	                // 첫 줄은 이미 들여쓰기가 적용된 상태이므로 바로 출력
					sb.append("\"");
					sb.append(lines[0]);
	                // 두 번째 줄부터 들여쓰기 적용하여 출력
	                for (int i = 1; i < lines.length; i++) {
	                	sb.append("\n");
	                    addIndent(sb, indent);
	                    sb.append(lines[i]);
	                }
	                sb.append("\"");
				}
            } else {
            	sb.append("\"");
            	sb.append(obj);
            	sb.append("\"");
            }
        }
    }
	 */
	
	public static JSONObject splitTitle (ContextType type, String contextText) {
		JSONObject obj = new JSONObject();
		
		Pattern chapterPattern = Pattern.compile("제\\s*(\\d+)\\s*"+type.getContextName()+"\\s*([^\\n\\r]*)");
        Matcher chapterMatcher = chapterPattern.matcher(contextText);

        while (chapterMatcher.find()) {
            String chapter = chapterMatcher.group(1);
            obj.put(type.toString(), "제" + chapter + type.getContextName());
            
            String chapterName = chapterMatcher.group(2).trim();
            obj.put(type.toString()+"_NAME", chapterName);
//            System.out.println("{\""+type+"\":\"제" + chapter + type.getContextName() + "\", \""+type+"_NAME\":\"" + chapterName + "\"}");
        }
        
        return obj;
	}
	
	
    public static void prettyPrint(StringBuilder sb, Object obj, int indent) {
        if (obj instanceof String) {
            Object toJson = null;
            try {
                toJson = new JSONParser().parse(FdsUtil.nvl(obj));
                prettyPrint(sb, toJson, indent);
            } catch (ParseException e) {
                // 문자열이 JSON 형태가 아니면 그대로 출력
                sb.append("\"");
                String[] lines = ((String) obj).split("\n");
                for (int i = 0; i < lines.length; i++) {
                    if (i > 0) {
                        sb.append("\n");
                        addIndent(sb, indent);
                    }
                    sb.append(lines[i]);
                }
                sb.append("\"");
            }
        } else if (obj instanceof JSONObject) {
            JSONObject json = (JSONObject) obj;
            sb.append("{\n");
            TreeSet<String> sortedKeys = new TreeSet<>(Collections.reverseOrder());
            sortedKeys.addAll(json.keySet());

            boolean first = true;
            for (String key : sortedKeys) {
                if (!first) {
                    sb.append(",\n");
                }
                addIndent(sb, indent + 1);
                sb.append("\"").append(key).append("\": ");
                prettyPrint(sb, json.get(key), indent + 1);
                first = false;
            }
            sb.append("\n");
            addIndent(sb, indent);
            sb.append("}");
        } else if (obj instanceof JSONArray) {
            JSONArray array = (JSONArray) obj;
            sb.append("[\n");
            for (int i = 0; i < array.size(); i++) {
                addIndent(sb, indent + 1);
                prettyPrint(sb, array.get(i), indent + 1);
                if (i < array.size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
            }
            addIndent(sb, indent);
            sb.append("]");
        } else {
            // 기본 타입(문자열, 숫자 등) 처리
            sb.append("\"").append(obj.toString()).append("\"");
        }
    }
	
	public static void addIndent(StringBuilder sb, int indent) {
	    for (int i = 0; i < indent; i++) {
	        sb.append("    "); // 들여쓰기를 위해 4개의 공백을 추가
	    }
	}

    public static String readFileContent(String filePath) {
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }
    
    /**
     * PDFBox의 좌표와 크기 정보를 React PDF Viewer 좌표계로 변환.
     *
     * @param pdfBoxX        PDFBox에서 추출한 x 좌표
     * @param pdfBoxY        PDFBox에서 추출한 y 좌표
     * @param pdfBoxHeight   PDFBox에서 추출한 요소의 높이
     * @param pdfBoxWidth    PDFBox에서 추출한 요소의 너비
     * @param pageHeight     PDF 페이지의 높이
     * @param scalingFactor  스케일링 비율 또는 줌 레벨
     * @return               변환된 좌표와 크기를 포함하는 Rectangle 객체
     */
    public static Rectangle transformToReactPDFViewerCoordinates(
        float pdfBoxX, float pdfBoxY, float pdfBoxHeight, float pdfBoxWidth,
        float pageHeight, float scalingFactor) {
        
        // 변환된 Y 좌표는 페이지 높이에서 원본 Y 좌표와 높이를 빼고, 스케일링을 적용합니다.
        float transformedY = (pageHeight - pdfBoxY - pdfBoxHeight) * scalingFactor;
        // X 좌표와 너비에 스케일링 적용
        float transformedX = pdfBoxX * scalingFactor;
        float transformedWidth = pdfBoxWidth * scalingFactor;
        float transformedHeight = pdfBoxHeight * scalingFactor;

        // 변환된 좌표와 크기를 가진 Rectangle 반환
        return new Rectangle(transformedX, transformedY, transformedWidth, transformedHeight);
    }

    /**
     * 좌표와 크기를 나타내는 Rectangle 클래스.
     */
    public static class Rectangle {
        public final float x;
        public final float y;
        public final float width;
        public final float height;

        public Rectangle(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        public JSONObject toJson () {
        	return new JSONObject() {{
        		put("x", x);
        		put("y", y);
        		put("width", width);
        		put("height", height);
        	}};
        }
        
        @Override
        public String toString() {
            return String.format("Rectangle{x=%.2f, y=%.2f, width=%.2f, height=%.2f}", x, y, width, height);
        }
    }
    
}
