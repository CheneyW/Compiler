package syntax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AnalysisTable {
	private List<Map<String, Integer>> GOTO;
	private List<Map<String, String>> ACTION;
	private List<Closure> closures;
	private List<Production> productions;
	private Set<String> terminals;
	private Set<String> nonterminals;

	public AnalysisTable(List<Closure> closures, List<Production> productions, Set<String> terminals,
			Set<String> nonterminals) {
		this.closures = closures;
		this.productions = productions;
		this.terminals = terminals;
		this.nonterminals = nonterminals;

		// 初始化分析表
		GOTO = new ArrayList<Map<String, Integer>>();
		ACTION = new ArrayList<Map<String, String>>();
		for (int i = 0; i < closures.size(); i++) {
			GOTO.add(new HashMap<String, Integer>());
			ACTION.add(new HashMap<String, String>());
		}
		constructGoto();
		constructAction();
	}

	public List<Map<String, Integer>> getGoto() {
		return GOTO;
	}

	public List<Map<String, String>> getAction() {
		return ACTION;
	}

	// 构造GOTO表
	private void constructGoto() {
		for (int from = 0; from < closures.size(); from++) {
			Closure src = closures.get(from);
			for (LR1Item it : src.getItems()) {
				String nextSymbol = it.getNext();
				if (nextSymbol == null || !nonterminals.contains(nextSymbol)) {
					continue;
				}
				Closure dest = src.GO(nextSymbol);
				for (int to = 0; to < closures.size(); to++) {
					if (closures.get(to).isLike(dest)) {
						GOTO.get(from).put(nextSymbol, to);
						break;
					}
				}
			}
		}
	}

	// 构造ACTION表
	private void constructAction() {
		for (int from = 0; from < closures.size(); from++) {
			Closure src = closures.get(from);
			for (LR1Item it : src.getItems()) {
				String nextSymbol = it.getNext();
				// 移进
				if (nextSymbol != null) {
					if (!terminals.contains(nextSymbol)) {
						continue;
					}
					Closure dest = src.GO(nextSymbol);
					for (int to = 0; to < closures.size(); to++) {
						if (closures.get(to).isLike(dest)) {
							ACTION.get(from).put(nextSymbol, "s" + Integer.toString(to));
							break;
						}
					}
				}
				// 规约
				else if (!it.getLeft().equals("P'")) {
					for (String expectedSymbol : it.getExpectedSymbol()) {
						ACTION.get(from).put(expectedSymbol, getProductionIdx(it));
					}
				}
				// 接受
				else if (it.isAccept()) {
					ACTION.get(from).put("#", "acc");// 接受 值为len
				}
			}
		}
	}

	private String getProductionIdx(LR1Item it) {
		String left = it.getLeft();
		String[] right = it.getRight();
		for (int i = 0; i < productions.size(); i++) {
			Production p = productions.get(i);
			if (left.equals(p.getLeft()) && Arrays.equals(right, p.getRight())) {
				return "r" + Integer.toString(i);
			}
		}
		return null;
	}
}
