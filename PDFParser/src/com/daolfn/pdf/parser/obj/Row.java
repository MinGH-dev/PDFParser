package com.daolfn.pdf.parser.obj;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Row {
	float startY;
    StringBuilder text = new StringBuilder();
    List<TextStyle> textStyles = new ArrayList<>();

    public Row(float startY) {
        this.startY = startY;
    }
    
    public float getStartY () {
    	return startY;
    }

    public void addText(String text, TextStyle textStyle) {
        this.text.append(text);
        this.textStyles.add(textStyle);
    }
    
    public List<TextStyle> getStyles () {
    	return textStyles;
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
