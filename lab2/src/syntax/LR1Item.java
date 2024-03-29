package syntax;

import java.util.ArrayList;
import java.util.List;

public class LR1Item extends Production {

	private List<String> expectedSymbol = new ArrayList<String>();// 展望符
	private int dot = 0;// 位置标记 分析进展状态

	public boolean visited = false;

	public LR1Item(int ID, String left, String[] right, List<String> expectedSymbol, int dot) {
		super(ID, left, right);
		for (String s : expectedSymbol) {
			this.expectedSymbol.add(s);
		}
		this.dot = dot;
	}

	public LR1Item(Production p, List<String> expectedSymbol, int dot) {
		super(p.getID(), p.getLeft(), p.getRight());
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

	// P' -> P·
	public boolean isAccept() {
		return dot == right.length && left.equals("P'");
	}

	public String getNext() {
		if (dot < right.length) {
			return right[dot];
		}
		return null;
	}

	public List<String> getBeta() {// A -> a . X Beta
		List<String> beta = new ArrayList<String>();
		for (int i = dot + 1; i < right.length; i++) {
			beta.add(right[i]);
		}
		return beta;
	}
//
//	public boolean addExpectedSymbol(List<String> newSymbol) {
//		boolean flag = false;
//		for (String s : newSymbol) {
//			if (!expectedSymbol.contains(s)) {
//				expectedSymbol.add(s);
//				flag = true;
//			}
//		}
//		return flag;
//	}

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
		str += "  \t,  ";
		for (String s : expectedSymbol) {
			str += s + " ";
		}
		return str;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + dot;
		result = prime * result + ((expectedSymbol == null) ? 0 : expectedSymbol.hashCode());
		if (expectedSymbol != null) {
			result = prime * result;
			for (String s : expectedSymbol) {
				result += s.hashCode();
			}
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		LR1Item other = (LR1Item) obj;
		if (dot != other.dot)
			return false;
		if (expectedSymbol == null) {
			if (other.expectedSymbol != null)
				return false;
		} else {
			if (other.expectedSymbol.size() != expectedSymbol.size()) {
				return false;
			}
			for (String s : other.expectedSymbol) {
				if (!expectedSymbol.contains(s)) {
					return false;
				}
			}
		}
		return true;
	}
}
