package lexical;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Symbol {
	public static final List<String> keyWords = Arrays.asList("if", "else", "for", "do", "while", "switch", "case",
			"default", "break", "continue", "return", "void", "int", "boolean", "char", "short", "long", "unsigned",
			"true", "false", "String");

	public static final List<String> codeOfKind = Arrays.asList("id", "const", "if", "else", "for", "do", "while", "switch", "case",
			"default", "break", "continue", "return", "void", "int", "boolean", "char", "short", "long", "unsigned",
			"true", "false", "String", "str", "%", "+", "-", "*", "/", ":", ";", ",", "{", "}", "(", ")", "[",
			"]", "=", "&&", "||", "!", ">", ">=", "<", "<=", "!=", "==");

	public String symbol; // 符号
	public String val = ""; // 非终结符的token值
	public int row = 0;

	public String addr = "";
	public String type = "";
	public int width = 0;

	public int quad = 0;

	public Set<Integer> falselist = new HashSet<Integer>();
	public Set<Integer> truelist = new HashSet<Integer>();
	public Set<Integer> nextlist = new HashSet<Integer>();

	public String op = ""; // relop

	public Symbol(String s) {
		symbol = s;
	}

	public Symbol(String s, int row) {
		this.symbol = s;
		this.row = row;
	}

	public Symbol(String symbol, String val, int row) {
		super();
		this.symbol = symbol;
		this.val = val;
		this.row = row;
	}

	public String getSymbol() {
		return symbol;
	}

	public int getRow() {
		return row;
	}

	public String getVal() {
		return val;
	}

}
