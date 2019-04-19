package lexical;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Encoding {
	public static final int CONSTANT = 0;// 常数
	public static final int IDENTIFIER = 1;// 标识符
	public static final int KEYWORD = 2;// 关键字
	public static final int OPERATOR = 3;// 运算符
	public static final int SEPARATOR = 4;// 分界符
	public static final int COMMENT = 5;// 注释

	public static final List<String> keyWords = Arrays.asList("if", "else", "for", "do", "while", "switch", "case",
			"default", "break", "continue", "return", "void", "int", "boolean", "char", "short", "long", "unsigned",
			"float", "double", "record");

	public static final List<String> codeOfKind = Arrays.asList("id", "const", "if", "else", "for", "do", "while",
			"switch", "case", "default", "break", "continue", "return", "void", "int", "boolean", "char", "short",
			"long", "unsigned", "float", "double", "record", "%", "+", "-", "*", "/", ":", ";", ",", "{", "}", "(", ")",
			"[", "]", "=", "&&", "||", "!", ">", ">=", "<", "<=", "!=", "==");

	public static void main(String[] args) {// test
		System.out.println(codeOfKind.size());
		System.out.println(codeOfKind.indexOf("if"));
		System.out.println(codeOfKind.indexOf("record"));
	}

	public static List<String> TokenToSymbol(List<Integer> tokenList) {
		List<String> tokens = new ArrayList<String>();
		for (int idx : tokenList) {
			tokens.add(codeOfKind.get(idx));
		}
		return tokens;
	}
}
