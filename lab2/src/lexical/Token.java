package lexical;

public class Token {
	private int codeOfKind = 0;// 种别码
	private int attrVal = 0;// 属性值

	public Token(int codeOfKind, int attrVal) {
		super();
		this.codeOfKind = codeOfKind;
		this.attrVal = attrVal;
	}

	public int getCodeOfKind() {
		return codeOfKind;
	}

	public int getAttrVal() {
		return attrVal;
	}
}
