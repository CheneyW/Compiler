package lexical;

public class MyBuffer {

	private StringBuffer tokenBuffer = new StringBuffer();
	private String text = "";
	private int rowNum = 1;
	private int idx = 0;

	public MyBuffer(String text) {
		this.text = text + '\0';
	}

	public void push(char ch) {
		tokenBuffer.append(ch);
	}

	public void clear() {
		tokenBuffer = new StringBuffer();
	}

	public String getToken() {
		return tokenBuffer.toString();
	}

	public void nextLine() {
		rowNum++;
	}

	public int getRowNum() {
		return rowNum;
	}

	public char getChar() {
		return text.charAt(idx++);
	}

	// 回退
	public void retract() {
		idx -= 1;
	}

}
