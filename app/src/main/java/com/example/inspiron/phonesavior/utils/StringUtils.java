package com.example.inspiron.phonesavior.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 判断给定的字符串是纯中文还是纯英文
 * 
 */
public class StringUtils {

	/**
	 * 是否是英文
	 * 
	 * @param charaString
	 * @return
	 */
	public static boolean isEnglish(String charaString) {
		return charaString.matches("^[a-z.A-Z]*");
	}

	// GENERAL_PUNCTUATION 判断中文的"号
	// CJK_SYMBOLS_AND_PUNCTUATION 判断中文的。号
	// HALFWIDTH_AND_FULLWIDTH_FORMS 判断中文的，号
	/**
	 * 判断字符是否是中文
	 * 
	 * @param c
	 * @return
	 */
	private boolean isChineseChar(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	/**
	 * 判断字符串是否含有中文
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isChinese(String str) {
		String regEx = "[\\u4e00-\\u9fa5]+";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		if (m.find())
			return true;
		else
			return false;
	}
}
