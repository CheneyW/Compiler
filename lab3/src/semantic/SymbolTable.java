package semantic;

import java.util.ArrayList;
import java.util.List;

import lexical.Symbol;

public class SymbolTable extends ArrayList<Symbol> {

	private static final long serialVersionUID = 1L;

	private List<SymbolTable> children = new ArrayList<SymbolTable>();

	public String name;
	public SymbolTable previous;
	public int width;

	public SymbolTable(SymbolTable previous) {
		this.previous = previous;
	}

	public void enterproc(SymbolTable newTable) {
		this.add(new Symbol("SymbolTable", Integer.toString(children.size()), 0));
		children.add(newTable);
	}

	public Symbol lookup(String name) {
		for (Symbol it : this) {
			if (it.val.equals(name)) {
				return it;
			}
		}
		return null;
	}

	public List<String> getSymbols() {
		List<String> list = new ArrayList<String>();
		String head = "SymbolTable [" + name + "]\t offset = " + Integer.toString(width);
		list.add(head);
		int count = 0;
		for (Symbol s : this) {
			if (s.symbol.equals("SymbolTable")) {
				SymbolTable child = children.get(Integer.parseInt(s.val));
				for (String childSymbol : child.getSymbols()) {
					list.add("\t" + childSymbol);
				}
			} else {
				String inf = String.format("id=%-4d name=%-8s offset=%-4d type=%-8s", count++, s.val, s.offset, s.type);
//				String inf = "id=" + Integer.toString(count++);
//				inf += "    name=" + s.val;
//				inf += "    offset=" + s.offset;
//				inf += "    width=" + s.width;
//				inf += "    type=" + s.type;
				list.add(inf);
			}
		}
		return list;
	}
}
