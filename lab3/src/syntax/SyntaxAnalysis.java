package syntax;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.swing.table.DefaultTableModel;

import lexical.Symbol;

import java.util.Map;

public class SyntaxAnalysis {
	private static final String epsilon = "ε";

	// 产生式集,终结符集,非终结符集
	private List<Production> productions = new ArrayList<Production>();
	private Set<String> terminals = new HashSet<String>();
	private Set<String> nonterminals = new HashSet<String>();

	private Map<String, Set<Production>> productionsDict = new HashMap<String, Set<Production>>();

	private List<Closure> closures = new ArrayList<Closure>();
	private List<Map<String, Integer>> GOTO;
	private List<Map<String, String>> ACTION;

	public SyntaxAnalysis() {
		// 产生式 终结符 非终结符
		getProduction();
		getNonterminal();
		getTerminal();
		getProductionDict();

		// FIRST
		First first = new First(productionsDict, terminals, nonterminals);
		// 从非终结符集中去除epsilon
		terminals.remove(epsilon);
		writeProduction();
		writeTerminal();
		writeNonterminal();
		// 去除产生式右部的epsilon
		Iterator<Production> iterator = productions.iterator();
		while (iterator.hasNext()) {
			Production p = iterator.next();
			String[] right = p.getRight();
			if (right.length == 1 && right[0].equals(epsilon)) {
				p.setRight(new String[] {});
			}
		}
		first.writeFirst(terminals, nonterminals);

		// CLOSURE
		Closure.set(productionsDict, first);
		getAllClosure();
		writeClosure();

		// 分析表
		AnalysisTable table = new AnalysisTable(closures, terminals, nonterminals);
		GOTO = table.getGoto();
		ACTION = table.getAction();
	}

	public List<String> analyze(List<Symbol> tokens, DefaultTableModel errorTbMd, DefaultTableModel threeAddrTbMd) {
		// 分析器
		Analyzer analyzer = new Analyzer(GOTO, ACTION, tokens, productions, errorTbMd, threeAddrTbMd);
		List<String> actions = analyzer.getActions();
		return actions;
	}

	// 构造LR(1)项集族
	private void getAllClosure() {
		// P' -> P
		List<String> expectedSymbol = new ArrayList<String>();// 展望符
		expectedSymbol.add("#");
		LR1Item initialItem = new LR1Item(0, "P'", new String[] { "P" }, expectedSymbol, 0);
		Closure initialClosure = new Closure(new LR1Item[] { initialItem });
		initialClosure.generate();
		closures.add(initialClosure);

		Set<String> symbolSet = new HashSet<String>();
		symbolSet.addAll(terminals);
		symbolSet.addAll(nonterminals);
		boolean update = true;
		while (update) {
			update = false;
			Iterator<Closure> iterator = closures.iterator();
			Set<Closure> temp = new HashSet<Closure>();
			while (iterator.hasNext()) {
				Closure I = iterator.next();
				if (I.visited) {
					continue;
				}
				I.visited = true;

				for (String symbol : symbolSet) {
					Closure newClosure = I.GO(symbol);
					if (newClosure == null) {
						continue;
					}
					boolean contains = false;
					for (Closure c : closures) {
						if (c.contains(newClosure)) {
							contains = true;
						}
					}
					for (Closure c : temp) {
						if (c.contains(newClosure)) {
							contains = true;
						}
					}
					if (!contains) {
						newClosure.generate();
						temp.add(newClosure);
						update = true;
					}
				} // end for
			} // end while
			for (Closure c : temp) {
				closures.add(c);
			}
		} // end while
	}

	// 获得产生式
	private void getProduction() {
		try {
			BufferedReader in = new BufferedReader(new FileReader("./data/grammar.txt"));
			String line;
			while ((line = in.readLine()) != null) {
				line = line.trim();
				if (line.contentEquals(""))
					continue;
				String[] p = line.split("->");
				String left = p[0].trim();
				String[] right = p[1].trim().split("\\s+");
				productions.add(new Production(productions.size(), left, right));
			}
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 获得非终结符
	private void getNonterminal() {
		for (Production p : productions) {
			String symbol = p.getLeft();
			if (!nonterminals.contains(symbol)) {
				nonterminals.add(symbol);
			}
		}
	}

	// 获得终结符
	private void getTerminal() {
		for (Production p : productions) {
			for (String symbol : p.getRight()) {
				if (!nonterminals.contains(symbol) && !terminals.contains(symbol)) {
					terminals.add(symbol);
				}
			}
		}
	}

	// 建立以产生式左部非终结符为key，以产生式为value 的map
	private void getProductionDict() {
		for (String s : nonterminals) {
			productionsDict.put(s, new HashSet<Production>());
		}
		for (Production p : productions) {
			productionsDict.get(p.getLeft()).add(p);
		}
	}

	// 打印closure到文件
	private void writeClosure() {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("./data/CLOSURE.txt"));
			for (int i = 0; i < closures.size(); i++) {
				Closure c = closures.get(i);
				out.write("I" + Integer.toString(i));
				out.newLine();
				for (LR1Item it : c.getItems()) {
					out.write(it.toString());
					out.newLine();
				}
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
			System.out.println("ERROR when write CLOSURE.");
		}
	}

	private void writeProduction() {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("./data/production.txt"));
			for (int i = 0; i < productions.size(); i++) {
				out.write(Integer.toString(i) + "\t" + productions.get(i));
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
			System.out.println("ERROR when write Production.");
		}
	}

	private void writeTerminal() {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("./data/terminal.txt"));
			for (String t : terminals) {
				out.write(t + "\n");
			}
			out.close();
		} catch (IOException e) {
			System.out.println("ERROR when write Terminal.");
		}
	}

	private void writeNonterminal() {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("./data/nonterminal.txt"));
			for (String n : nonterminals) {
				out.write(n + "\n");
			}
			out.close();
		} catch (IOException e) {
			System.out.println("ERROR when write Nonterminal.");
		}
	}

}
