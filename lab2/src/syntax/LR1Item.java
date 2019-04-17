package syntax;

import java.util.ArrayList;
import java.util.List;

public class LR1Item extends Production {

	private List<String> expectedSymbol = new ArrayList<String>();// 展望符
	private int dot = 0;// 位置标记 分析进展状态

	public LR1Item(String left, String[] right, List<String> expectedSymbol, int dot) {
		super(left, right);
		for (String s : expectedSymbol) {
			this.expectedSymbol.add(s);
		}
		this.dot = dot;
	}

	public LR1Item(Production p, List<String> expectedSymbol, int dot) {
		super(p.getLeft(), p.getRight());
		for (String s : expectedSymbol) {
			this.expectedSymbol.add(s);
		}
		this.dot = dot;
	}

	public List<String> getExpectedSymbol() {
		List<String> newList = new ArrayList<String>();
		for (String s : expectedSymbol) {
			newList.add(s);
		}
		return newList;
	}

	public int getDot() {
		return dot;
	}

	public boolean isAccept() {
		return dot == this.right.length;
	}

	@Override
	public String toString() {
		String str = left + " -> ";
		for (int i = 0; i < right.length; i++) {
			if (dot == i) {
				str += '.' + right[i];
			} else {
				str += ' ' + right[i];
			}
		}
		if (dot == right.length) {
			str += ".";
		}
		str += " \t";
		for (String s : expectedSymbol) {
			str += s + ",";
		}
		return str;
	}

}
