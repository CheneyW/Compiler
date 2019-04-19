package syntax;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AnalysisTable {
	private List<Map<String, Integer>> GOTO;
	private List<Map<String, String>> ACTION;
	private List<Closure> closures;
	private List<Production> productions;
	private Set<String> terminals = new HashSet<String>();
	private Set<String> nonterminals;

	public AnalysisTable(List<Closure> closures, List<Production> productions, Set<String> terminals,
			Set<String> nonterminals) {
		this.closures = closures;
		this.productions = productions;
		this.terminals.addAll(terminals);
		this.terminals.add("#");
		this.nonterminals = nonterminals;

		// 初始化分析表
		GOTO = new ArrayList<Map<String, Integer>>();
		ACTION = new ArrayList<Map<String, String>>();
		for (int i = 0; i < closures.size(); i++) {
			GOTO.add(new HashMap<String, Integer>());

			HashMap<String, String> map = new HashMap<String, String>();
			for (String s : this.terminals) {
				map.put(s, "err");
			}
			ACTION.add(map);
		}
		constructGoto();
		constructAction();
		writeTable();
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
					if (closures.get(to).contains(dest)) {
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
						if (closures.get(to).contains(dest)) {
							if (!ACTION.get(from).get(nextSymbol).equals("err")
									&& !ACTION.get(from).get(nextSymbol).equals("s" + Integer.toString(to))) {
								System.out.println("[" + from + "," + nextSymbol + "]\t"
										+ ACTION.get(from).get(nextSymbol) + "\t->\ts" + Integer.toString(to));
							}
							ACTION.get(from).put(nextSymbol, "s" + Integer.toString(to));
							break;
						}
					}
				}
				// 规约
				else if (!it.getLeft().equals("P'")) {
					for (String expectedSymbol : it.getExpectedSymbol()) {
						if (!ACTION.get(from).get(expectedSymbol).equals("err")
								&& !ACTION.get(from).get(expectedSymbol).equals("r" + Integer.toString(it.getID()))) {
							System.out.println("[" + from + "," + expectedSymbol + "]\t"
									+ ACTION.get(from).get(expectedSymbol) + "\t->\tr" + Integer.toString(it.getID()));
						}
						ACTION.get(from).put(expectedSymbol, "r" + Integer.toString(it.getID()));
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

	private void writeTable() {
		// write ACTION
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("./data/ACTION.txt"));
			for (String s : terminals) {
				out.write("\t" + s);
			}
			for (int i = 0; i < closures.size(); i++) {
				out.newLine();
				out.write(Integer.toString(i));
				for (String s : terminals) {
					out.write("\t" + ACTION.get(i).get(s));
				}
			}
			out.close();
		} catch (IOException e) {
			System.out.println("ERROR when write ACTION.");
		}

		// write GOTO
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("./data/GOTO.txt"));
			for (String s : nonterminals) {
				if (s.equals("P'")) {
					continue;
				}
				out.write("\t" + s);
			}
			for (int i = 0; i < closures.size(); i++) {
				out.newLine();
				out.write(Integer.toString(i));
				for (String s : nonterminals) {
					if (s.equals("P'")) {
						continue;
					}
					out.write("\t");
					if (GOTO.get(i).containsKey(s)) {
						out.write(Integer.toString(GOTO.get(i).get(s)));
					}
				}
			}
			out.close();
		} catch (IOException e) {
			System.out.println("ERROR when write GOTO.");
		}
	}
}
