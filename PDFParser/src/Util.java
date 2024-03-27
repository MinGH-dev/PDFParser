import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.TreeSet;

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

    
}
