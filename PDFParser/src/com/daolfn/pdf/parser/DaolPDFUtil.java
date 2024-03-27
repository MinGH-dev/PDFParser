package com.daolfn.pdf.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.daolfn.pdf.parser.obj.Page;
import com.daolfn.pdf.parser.obj.Row;
import com.daolfn.pdf.parser.obj.TextStyle;

public class DaolPDFUtil {
	/**
	 * 특정 PDF파일에서 키워드 검색
	 * @param pdfPath
	 * @param keywords
	 * @return
	 */
	public static JSONArray search (String pdfPath, String ...keywords) {
		JSONArray result = new JSONArray();
		
		
		return result;
	}
	
	/**
	 * 전체 PDF에서 키워드 검색
	 * @param strings
	 * @return
	 */
	public static JSONArray search (String ...strings) {
		JSONArray result = new JSONArray();
		
		
		return result;
	}
	
	
	
	
}
