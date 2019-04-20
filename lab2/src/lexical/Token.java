package lexical;

import java.util.Arrays;
import java.util.List;

public class Token {
	public static final List<String> keyWords = Arrays.asList("if", "else", "for", "do", "while", "switch", "case",
			"default", "break", "continue", "return", "void", "int", "boolean", "char", "short", "long", "unsigned",
			"float", "double", "record");

	public static final List<String> codeOfKind = Arrays.asList("id", "const", "if", "else", "for", "do", "while",
			"switch", "case", "default", "break", "continue", "return", "void", "int", "boolean", "char", "short",
			"long", "unsigned", "float", "double", "record", "%", "+", "-", "*", "/", ":", ";", ",", "{", "}", "(", ")",
			"[", "]", "=", "&&", "||", "!", ">", ">=", "<", "<=", "!=", "==");
	private String token = "";
	private int row = 0;

	public Token(int cof, int row) {
		super();
		this.token = codeOfKind.get(cof);
		this.row = row;
	}

	public Token(String token, int row) {
		this.token = token;
		this.row = row;
	}

	public String getToken() {
		return token;
	}

	public int getRow() {
		return row;
	}

}
