package lexical;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

public class LexicalAnalysis {
	private static final int KEYWORD = 1;// 关键字
	private static final int IDENTIFIER = 2;// 标识符
	private static final int CONSTANT = 3;// 常数
	private static final int OPERATOR = 4;// 运算符
	private static final int SEPARATOR = 8;// 分界符
	private static final int COMMENT = 9;// 注释

	private static final List<String> keyWords = Arrays.asList("int", "short", "long", "char", "float", "double",
			"enum", "if", "else", "switch", "do", "while", "for", "printf", "scanf", "main", "void", "return");

	private MyBuffer buffer;
	private DefaultTableModel tokenTbMd, errorTbMd, symbolTbMd;
	private Set<String> symbolTable = new HashSet<String>();

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
			outputToken(row, token, token.toUpperCase(), "");
			break;
		case IDENTIFIER:
			outputToken(row, token, "IDN", token);
			if (!symbolTable.contains(token)) {
				symbolTable.add(token);
				outputSymbol(token);
			}
			break;
		case CONSTANT:
			outputToken(row, token, "CONST", token);
			break;
		case OPERATOR:
			outputToken(row, token, kindOfOperator(token), "");
			break;
		case SEPARATOR:
			outputToken(row, token, kindOfSeparator(token), "");
			break;
		case COMMENT:
			if (token.length() > 5) {
				token = token.substring(0, 5) + "...";
			}
			outputToken(row, token, "COMMENT", token);
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

	private void outputSymbol(String name) {
		this.symbolTbMd.addRow(new String[] { name });
	}

	private String kindOfOperator(String token) {
		switch (token) {
		case "+":
			return "PLUS";
		case "-":
			return "MINUS";
		case "*":
			return "MULTI";
		case "/":
			return "RDIV";
		case "&":
			return "AND";
		case "|":
			return "OR";
		case "!":
			return "NOT";
		case "&&":
			return "RE_AND";
		case "||":
			return "RE_OR";
		case "==":
			return "EQ";
		case "!=":
			return "NE";
		case ">":
			return "GT";
		case "<":
			return "LT";
		case ">=":
			return "GE";
		case "<=":
			return "LE";
		default:
			return "";
		}
	}

	private String kindOfSeparator(String token) {
		switch (token) {
		case ";":
			return "SEMIC";
		case "(":
			return "LR_BRAC";
		case ")":
			return "RR_BRAC";
		case "[":
			return "LS_BRAC";
		case "]":
			return "RS_BRAC";
		case "{":
			return "LB_BRAC";
		case "}":
			return "RB_BRAC";
		case "=":
			return "ASSIGN";
		default:
			return "";
		}
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
