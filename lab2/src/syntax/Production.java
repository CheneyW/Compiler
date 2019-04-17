package syntax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Production {
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + Arrays.hashCode(right);
		return result;
	}

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
		for (String s : right) {
			str += s + " ";
		}
		return str;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Production other = (Production) obj;
		if (left == null) {
			if (other.left != null)
				return false;
		} else if (!left.equals(other.left))
			return false;
		if (!Arrays.equals(right, other.right))
			return false;
		return true;
	}

	// test
	public static void main(String[] args) {
		List<String> symbols1 = new ArrayList<String>();
		symbols1.add("a");

		LR1Item it1 = new LR1Item("A", new String[] { "1", "3" }, symbols1, 0);
		Production p = new Production("A", new String[] { "1", "3" });

		System.out.println(it1.left.equals(p.getLeft()) && Arrays.equals(it1.right, p.getRight()));
		System.out.println(p.equals(it1));

	}
}