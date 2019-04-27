package semantic;

import java.util.ArrayList;

public class SymbolTable extends ArrayList<SymbolTable.Item> {

	private static final long serialVersionUID = 1L;

	// 填入符号表
	public void enter(String name, String type, int offset) {
		add(new Item(name, type, offset));
	}

	public String lookup(String name) {
		for (Item it : this) {
			if (it.name.equals(name)) {
				return name;
			}
		}
		return null;
	}

	class Item {
		String name;
		String type;
		int offset;

		public Item(String name, String type, int offset) {
			this.name = name;
			this.type = type;
			this.offset = offset;
		}
	}
}
