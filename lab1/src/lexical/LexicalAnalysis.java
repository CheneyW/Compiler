package lexical;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

public class LexicalAnalysis {
	private static final int KEYWORD = 1;// 关键字
	private static final int IDENTIFIER = 2;// 标识符
	private static final int CONSTANT = 3;// 常数
	private static final int OPERATOR = 4;// 运算符
	private static final int SEPARATOR = 5;// 分界符
	private static final int COMMENT = 6;// 注释

	private static final List<String> keyWords = Arrays.asList("int", "float", "double", "if", "else", "switch", "do",
			"while", "for", "void", "return");
	private Map<String, String> operatorCOF = new HashMap<String, String>() {
		{
			put("+", "400");
			put("-", "401");
			put("*", "402");
			put("/", "403");
			put("&", "404");
			put("|", "405");
			put("!", "406");
			put("&&", "407");
			put("||", "408");
			put("==", "409");
			put("!=", "410");
			put("<", "411");
			put(">", "412");
			put("<=", "413");
			put(">=", "414");
		}
	};
	private Map<String, String> separatorCOF = new HashMap<String, String>() {
		{
			put("=", "500");
			put(";", "501");
			put("(", "502");
			put(")", "503");
			put("[", "504");
			put("]", "505");
			put("{", "506");
			put("}", "507");
		}
	};

	private MyBuffer buffer;
	private DefaultTableModel tokenTbMd, errorTbMd, symbolTbMd;
	private List<String> symbolTable = new ArrayList<String>();

	public LexicalAnalysis(JTextArea inputText, DefaultTableModel tokenTbMd, DefaultTableModel errorTbMd,
			DefaultTableModel symbolTbMd) {
		buffer = new MyBuffer(inputText.getText());
		this.tokenTbMd = tokenTbMd;
		this.errorTbMd = errorTbMd;
		this.symbolTbMd = symbolTbMd;
	}

	public void run() {
		while (scanToken()) {
		}
	}

	private boolean scanToken() {
		char ch = buffer.getChar();
		while (ch == ' ' || ch == '\t') {
			ch = buffer.getChar();
		}
		while (ch == '\n') {
			buffer.nextLine();
			ch = buffer.getChar();
		}

		// 字母开头
		if (isLetter(ch)) {
			do {
				buffer.push(ch);
				ch = buffer.getChar();
			} while (isLetter(ch) || isDigit(ch));
			buffer.retract();
			String token = buffer.getToken();
			if (keyWords.contains(token)) {
				acceptToken(KEYWORD, token, buffer.getRowNum());
			} else {
				acceptToken(IDENTIFIER, token, buffer.getRowNum());
			}
			buffer.clear();
			return true;
		}
		// 数字开头
		if (isDigit(ch)) {
			do {
				buffer.push(ch);
				ch = buffer.getChar();
			} while (isDigit(ch));
			buffer.retract();
			acceptToken(CONSTANT, buffer.getToken(), buffer.getRowNum());
			buffer.clear();
			return true;
		}

		switch (ch) {
		case '/':
			ch = buffer.getChar();
			if (ch != '*') { // '/'
				buffer.retract();
				acceptToken(OPERATOR, "" + '/', buffer.getRowNum());
			} else { // 注释
				buffer.push('/');
				while (true) {
					do {
						buffer.push(ch);
						ch = buffer.getChar();
					} while (ch != '*');
					buffer.push(ch);
					ch = buffer.getChar();
					if (ch == '/') {
						buffer.push(ch);
						break;
					}
				}
				acceptToken(COMMENT, buffer.getToken(), buffer.getRowNum());
				buffer.clear();
			}
			return true;

		case '*':
		case '+':
		case '-':
			acceptToken(OPERATOR, "" + ch, buffer.getRowNum());
			return true;

		case '&':
		case '|':
			buffer.push(ch);
			char nextch = buffer.getChar();
			if (nextch == ch) { // && ||
				buffer.push(nextch);
			} else { // & |
				buffer.retract();
			}
			acceptToken(OPERATOR, buffer.getToken(), buffer.getRowNum());
			buffer.clear();
			return true;

		case '!':
		case '>':
		case '<':
			buffer.push(ch);
			ch = buffer.getChar();
			if (ch == '=') {
				buffer.push(ch);
			} else {
				buffer.retract();
			}
			acceptToken(OPERATOR, buffer.getToken(), buffer.getRowNum());
			buffer.clear();
			return true;

		case '=':
			buffer.push(ch);
			ch = buffer.getChar();
			if (ch == '=') {
				buffer.push(ch);
				acceptToken(OPERATOR, buffer.getToken(), buffer.getRowNum());
			} else {
				buffer.retract();
				acceptToken(SEPARATOR, buffer.getToken(), buffer.getRowNum());
			}
			buffer.clear();
			return true;

		case ';':
		case '(':
		case ')':
		case '[':
		case ']':
		case '{':
		case '}':
			acceptToken(SEPARATOR, "" + ch, buffer.getRowNum());
			return true;

		case '\0':
			return false;
		default:
			outputError("" + ch, buffer.getRowNum());
			return true;
		}
	}

	private void acceptToken(int type, String token, int row) {
		switch (type) {
		case KEYWORD:
			outputToken(row, token, Integer.toString(100 + keyWords.indexOf(token)), "");
			break;
		case IDENTIFIER:
			if (!symbolTable.contains(token)) {
				symbolTable.add(token);
				outputSymbol(symbolTable.size()-1, token);
			}
			outputToken(row, token, "200", Integer.toString(symbolTable.indexOf(token)));
			break;
		case CONSTANT:
			outputToken(row, token, "300", token);
			break;
		case OPERATOR:
			outputToken(row, token, operatorCOF.get(token), "");
			break;
		case SEPARATOR:
			outputToken(row, token, separatorCOF.get(token), "");
			break;
		case COMMENT:
			if (token.length() > 5) {
				token = token.substring(0, 5) + "...";
			}
			outputToken(row, token, "600", token);
			break;
		}
	}

	private void outputToken(int row, String token, String CodeOfKind, String attr) {
		this.tokenTbMd.addRow(new String[] { Integer.toString(row), token, CodeOfKind, attr });
	}

	private void outputError(String description, int row) {
		String str = "ERROR: Unrecognized character '" + description + "' , line " + Integer.toString(row) + ".";
		this.errorTbMd.addRow(new String[] { str });
	}

	private void outputSymbol(int idx, String name) {
		this.symbolTbMd.addRow(new String[] { Integer.toString(idx), name });
	}

	private boolean isDigit(char c) {
		if (c >= '0' && c <= '9') {
			return true;
		}
		return false;
	}

	private boolean isLetter(char c) {
		if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_') {
			return true;
		}
		return false;
	}

}
