package com.daolfn.pdf.parser.obj;

import org.json.simple.JSONObject;

public class TextStyle {
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
