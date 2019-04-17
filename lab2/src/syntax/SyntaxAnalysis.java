package syntax;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;

public class SyntaxAnalysis {
	private static final String grammarFile = "./data/grammar.txt";

	private String startState = "P'";

	// 产生式集,终结符集,非终结符集
	private List<Production> productions = new ArrayList<Production>();
	private Set<String> terminals = new HashSet<String>();
	private Set<String> nonterminals = new HashSet<String>();

	private Map<String, Set<Production>> productionsDict = new HashMap<String, Set<Production>>();

	private List<Closure> closures = new ArrayList<Closure>();
	private List<Map<String, Integer>> GOTO;
	private List<Map<String, String>> ACTION;

	public static void main(String[] args) {
		SyntaxAnalysis a = new SyntaxAnalysis();
	}

	public SyntaxAnalysis() {
		// 产生式 终结符 非终结符
		getProduction();
		getNonterminal();
		getTerminal();
		getProductionDict();

		// 项目集
		First first = new First(productionsDict, terminals, nonterminals);
		Closure.set(productionsDict, first);
		getAllClosure();

		// 分析表
		AnalysisTable table = new AnalysisTable(closures, productions, terminals, nonterminals);
		GOTO = table.getGoto();
		ACTION = table.getAction();

	}

	// 构造LR(1)项集族
	private void getAllClosure() {
		// P' -> P
		List<String> expectedSymbol = new ArrayList<String>();// 展望符
		expectedSymbol.add("#");
		LR1Item initialItem = new LR1Item("P'", new String[] { "P" }, expectedSymbol, 0);
		Closure initialClosure = new Closure(new LR1Item[] { initialItem });
		initialClosure.generate();
		closures.add(initialClosure);

		boolean update = true;
		while (update) {
			update = false;
			Iterator<Closure> iterator = closures.iterator();
			while (iterator.hasNext()) {
				Closure I = iterator.next();
				if (I.visited) {
					continue;
				}
				I.visited = true;

				for (String symbol : terminals) {
					Closure newClosure = I.GO(symbol);
					if (newClosure == null) {
						continue;
					}
					boolean contains = false;
					for (Closure c : closures) {
						if (c.isLike(newClosure)) {
							contains = true;
						}
					}
					if (!contains) {
						newClosure.generate();
						closures.add(newClosure);
						update = true;
					}
				} // end for
			} // end while
		} // end while
	}

	// 获得产生式
	private void getProduction() {
		try {
			BufferedReader in = new BufferedReader(new FileReader(grammarFile));
			String line;
			while ((line = in.readLine()) != null) {
				line = line.trim();
				if (line.contentEquals(""))
					continue;
				String[] p = line.split("->");
				String left = p[0].trim();
				String[] right = p[1].trim().split("\\s+");
				productions.add(new Production(left, right));
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

}
