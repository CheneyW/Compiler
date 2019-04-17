package syntax;

import java.util.Arrays;

public class Production {
	protected String left; // 产生式左边部分
	protected String[] right;// 产生式右边部分

	public Production(String left, String[] right) {
		this.left = left;
		this.right = right;
	}

	public String[] getRight() {
		return right;
	}

	public String getLeft() {
		return left;
	}

	@Override
	public String toString() {
		String str = left + " -> ";
		for(String s : right) {
			str += s+" ";
		}
		return str;
	}
}