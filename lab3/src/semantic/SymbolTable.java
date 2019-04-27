package semantic;

import java.util.ArrayList;

public class SymbolTable extends ArrayList<SymbolTable.Item> {

	private static final long serialVersionUID = 1L;

	// 填入符号表
	public void enter(String name, String type, int len, int offset) {
		add(new Item(name, type, len, offset));
	}

	public String lookup(String name) {
		for (Item it : this) {
			if (it.name.equals(name)) {
				return name;
			}
		}
		return null;
	}

	public String getType(String name) {
		for (Item it : this) {
			if (it.name.equals(name)) {
				return it.type;
			}
		}
		return null;
	}

	public int getLen(String name) {
		for (Item it : this) {
			if (it.name.equals(name)) {
				return it.len;
			}
		}
		return 0;
	}

	class Item {
		String name;
		String type;
		int len;
		int offset;

		public Item(String name, String type, int len, int offset) {
			this.name = name;
			this.type = type;
			this.len = len;
			this.offset = offset;
		}
	}
}
