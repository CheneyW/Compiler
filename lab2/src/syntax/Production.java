package syntax;

import java.util.Arrays;

public class Production {
	protected int ID = 0;
	protected String left; // 产生式左边部分
	protected String[] right;// 产生式右边部分

	public Production(int ID, String left, String[] right) {
		this.left = left;
		this.right = right;
		this.ID = ID;
	}

	public int getID() {
		return ID;
	}

	public String[] getRight() {
		return right;
	}

	public void setRight(String[] right) {
		this.right = right;
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + Arrays.hashCode(right);
		return result;
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
}