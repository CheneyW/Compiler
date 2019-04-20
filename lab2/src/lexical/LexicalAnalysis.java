package lexical;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

public class LexicalAnalysis {
	public static final int CONSTANT = 0;// 常数
	public static final int IDENTIFIER = 1;// 标识符
	public static final int KEYWORD = 2;// 关键字
	public static final int OPERATOR = 3;// 运算符
	public static final int SEPARATOR = 4;// 分界符
	public static final int COMMENT = 5;// 注释

	private static final List<String> keyWords = Token.keyWords;
	private static final List<String> codeOfKind = Token.codeOfKind;

	private List<Token> tokenList = new ArrayList<Token>();

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

	public List<Token> run() {
		while (scanToken()) {
		}
		return tokenList;
	}

	private boolean scanToken() {
		char ch = buffer.getChar();
		while (ch == ' ' || ch == '\t' || ch == '\n') {
			if (ch == '\n')
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
						if (ch == '\n') {
							outputError(String.format("Error at Line %3d: Comment is not closed.", buffer.getRowNum()));
							buffer.clear();
							return true;
						}
					} while (ch != '*');

					buffer.push(ch);
					ch = buffer.getChar();
					if (ch == '\n') {
						outputError(String.format("Error at Line %3d: Comment is not closed.", buffer.getRowNum()));
						buffer.clear();
						return true;
					}

					if (ch == '/') {
						buffer.push(ch);
						break;
					}
				}
				acceptToken(COMMENT, buffer.getToken(), buffer.getRowNum());
				buffer.clear();
			}
			return true;

		case '+':
		case '-':
		case '*':
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
		case ':':
		case ',':
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
			outputError(
					String.format("Error at Line %3d: Unrecognized character '" + "" + ch + "'.", buffer.getRowNum()));
			return true;
		}
	}

	private void acceptToken(int type, String token, int row) {
		switch (type) {
		case IDENTIFIER:
			if (!symbolTable.contains(token)) {
				symbolTable.add(token);
				outputSymbol(symbolTable.size() - 1, token);
			}
			outputToken(row, token, codeOfKind.indexOf("id"), Integer.toString(symbolTable.indexOf(token)));
			break;
		case CONSTANT:
			outputToken(row, token, codeOfKind.indexOf("const"), token);
			break;
		case KEYWORD:
		case OPERATOR:
		case SEPARATOR:
			outputToken(row, token, codeOfKind.indexOf(token), "");
		}
	}

	private void outputToken(int row, String token, int codeOfKind, String attr) {
		tokenList.add(new Token(codeOfKind, row));
		this.tokenTbMd.addRow(new String[] { Integer.toString(row), token, Integer.toString(codeOfKind), attr });
	}

	private void outputError(String description) {
		this.errorTbMd.addRow(new String[] { description });
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
