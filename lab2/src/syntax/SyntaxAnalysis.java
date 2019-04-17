package syntax;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;

public class SyntaxAnalysis {
	private static final String grammarFile = "./data/grammar.txt";

	private String startState = "P'";

	// 产生式集,终结符集,非终结符集
	private Set<Production> productions = new HashSet<Production>();
	private Set<String> terminals = new HashSet<String>();
	private Set<String> nonterminals = new HashSet<String>();

	private Map<String, Set<Production>> productionsDict = new HashMap<String, Set<Production>>();

	public static void main(String[] args) {
		SyntaxAnalysis a = new SyntaxAnalysis();
	}

	public SyntaxAnalysis() {

		getProduction();
		getNonterminal();
		getTerminal();
		getProductionDict();

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
