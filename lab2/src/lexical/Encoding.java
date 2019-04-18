package lexical;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Encoding {
	public static final int KEYWORD = 0;// 关键字
	public static final int IDENTIFIER = 1;// 标识符
	public static final int CONSTANT = 2;// 常数
	public static final int OPERATOR = 3;// 运算符
	public static final int SEPARATOR = 4;// 分界符
	public static final int COMMENT = 5;// 注释

	public static final List<String> keyWords = Arrays.asList("int", "float", "double", "if", "else", "switch", "do",
			"while", "for", "void", "return");

	@SuppressWarnings("serial")
	public static final Map<String, Integer> operatorAttrVal = new HashMap<String, Integer>() {
		{
			put("+", 0);
			put("-", 1);
			put("*", 2);
			put("/", 3);
			put("&", 4);
			put("|", 5);
			put("!", 6);
			put("&&", 7);
			put("||", 8);
			put("==", 9);
			put("!=", 10);
			put("<", 11);
			put(">", 12);
			put("<=", 13);
			put(">=", 14);
		}
	};

	@SuppressWarnings("serial")
	public static final Map<String, Integer> separatorAttrVal = new HashMap<String, Integer>() {
		{
			put("=", 0);
			put(";", 1);
			put("(", 2);
			put(")", 3);
			put("[", 4);
			put("]", 5);
			put("{", 6);
			put("}", 7);
		}
	};

	public static List<String> Token2Symbol(List<Token> tokenList) {

		List<String> tokens = new ArrayList<String>();
		return tokens;

	}
}
