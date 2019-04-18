package syntax;

import java.util.HashSet;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;

public class First {
	private static final String epsilon = "ε";
	private Map<String, Set<String>> first = new HashMap<String, Set<String>>();

	public First(Map<String, Set<Production>> productionsDict, Set<String> terminals, Set<String> nonterminals) {

		// 终结符
		for (String x : terminals) {
			Set<String> set = new HashSet<String>();
			set.add(x);
			first.put(x, set);
		}
		// 非终结符
		for (String x : nonterminals) {
			Set<String> set = new HashSet<String>();
			first.put(x, set);
		}
		for (String x : nonterminals) {
			Set<String> set = new HashSet<String>();
			Set<Production> productions = productionsDict.get(x);
			for (Production p : productions) {
				String symbol = p.getRight()[0];
				if (terminals.contains(symbol) && !set.contains(symbol)) {
					set.add(symbol);
				}
			}
			first.put(x, set);
		}
		while (true) {
			boolean flag = true;
			for (String x : nonterminals) {
				Set<Production> productions = productionsDict.get(x);
				for (Production p : productions) {
					String[] right = p.getRight();
					int idx = 0;
					for (; idx < right.length; idx++) {
						Set<String> firstY = first.get(right[idx]);
						for (String a : firstY) {
							if (!first.get(x).contains(a) && !a.equals(epsilon)) {
								first.get(x).add(a);
								flag = false;
							}
						}
						if (!firstY.contains(epsilon)) {
							break;
						}
					}
					if (idx == right.length && !first.get(x).contains(epsilon)) {
						first.get(x).add(epsilon);
						flag = false;
					}
				}
			}
			if (flag) {
				break;
			}
		}
		first.remove(epsilon);
	}

	public Set<String> getFirst(String x) {
		return first.get(x);
	}

	public Set<String> getFirst(String[] alpha) {
		Set<String> firstAlpha = new HashSet<String>();
		int idx = 0;
		for (; idx < alpha.length; idx++) {
			Set<String> firstY = first.get(alpha[idx]);
			for (String a : firstY) {
				if (!firstAlpha.contains(a) && !a.equals(epsilon)) {
					firstAlpha.add(a);
				}
			}
			if (!firstY.contains(epsilon)) {
				break;
			}
		}
		if (idx == alpha.length) {
			firstAlpha.add(epsilon);
		}
		return firstAlpha;
	}

	public void writeFirst(Set<String> terminals, Set<String> nonterminals) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("./data/FIRST.txt"));
			for (String symbol : nonterminals) {
				Set<String> set = first.get(symbol);
				out.write(symbol + " \t");
				for (String s : set) {
					out.write(s + " ");
				}
				out.newLine();
			}
			for (String symbol : terminals) {
				Set<String> set = first.get(symbol);
				out.write(symbol + " \t");
				for (String s : set) {
					out.write(s + " ");
				}
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
			System.out.println("ERROR when write FIRST.");
		}
	}

}
