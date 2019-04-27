package semantic;

import java.util.ArrayList;

import lexical.Symbol;

public class SymbolStack extends ArrayList<Symbol> {
	private static final long serialVersionUID = 1L;

	public void push(Symbol s) {
		this.add(s);
	}

	public Symbol get(int idx) {
		return this.get(idx);
	}

	public Symbol peek() {
		return this.get(size()-1);
	}

	public Symbol peek(int idx) {
		return this.get(size()-1-idx);
	}
	
	public Symbol pop() {
		Symbol s = get(size()-1);
		remove(size()-1);
		return s;
	}

}
