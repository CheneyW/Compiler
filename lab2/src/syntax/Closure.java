package syntax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Closure {
	private static final String epsilon = "ε";
	private static Map<String, Set<Production>> productionsDict;
	private static First first;

	private Set<LR1Item> initialItem = new HashSet<LR1Item>();
	private Set<LR1Item> items = new HashSet<LR1Item>();

	public Closure(LR1Item[] initialItem) {
		for (LR1Item item : initialItem) {
			this.initialItem.add(item);
			items.add(item);
		}
		generate();
	}

	public void set(Map<String, Set<Production>> productionsDict, First first) {
		Closure.productionsDict = productionsDict;
		Closure.first = first;
	}

	private void generate() {
		Map<String, List<LR1Item>> left2Item = new HashMap<String, List<LR1Item>>();
		while (true) {
			boolean update = false;
			Iterator<LR1Item> iterator = items.iterator();
			while (iterator.hasNext()) {
				LR1Item it = iterator.next();
				if (it.visited) {
					continue;
				}
				it.visited = true;

				String B = it.getNext();
				String beta = it.getNextNext();
				if (B == null) {
					continue;
				}
				// 计算FIRST(βa)
				List<String> expectedSymbol = new ArrayList<String>();
				if (beta == null) {
					for (String s : it.getExpectedSymbol()) {
						expectedSymbol.add(s);
					}
				} else {
					Set<String> firstBeta = first.getFirst(beta);
					for (String s : firstBeta) {
						if (!s.equals(epsilon)) {
							expectedSymbol.add(s);
						}
					}
					if (firstBeta.contains(epsilon)) {
						for (String s : it.getExpectedSymbol()) {
							if (!expectedSymbol.contains(s)) {
								expectedSymbol.add(s);
							}
						}
					}
				}

				// 加入
				if (left2Item.containsKey(B)) {
					for (LR1Item i : left2Item.get(B)) {// 更新展望符
						if (i.addExpectedSymbol(expectedSymbol)) {
							update = true;
						}
					}
				} else {// 加入项目集
					List<LR1Item> list = new ArrayList<LR1Item>();
					for (Production p : productionsDict.get(B)) {
						LR1Item i = new LR1Item(p, expectedSymbol, 0);
						items.add(i);
						list.add(i);
						update = true;
					}
					left2Item.put(B, list);
				} // end if

			} // end while

			if (!update) {
				break;
			}
		} // end while
	}

	public Set<LR1Item> getItems() {
		return items;
	}

	public Set<LR1Item> getInitialItem() {
		return initialItem;
	}

	public Closure GOTO(String X) {
		List<LR1Item> itemList = new ArrayList<LR1Item>();
		for (LR1Item it : items) {
			if (it.getNext().equals(X)) {
				LR1Item newIt = new LR1Item(it.getLeft(), it.getRight(), it.getExpectedSymbol(), it.getDot() + 1);
				itemList.add(newIt);
			}
		}

		LR1Item[] itemArr = new LR1Item[itemList.size()];
		for (int i = 0; i < itemList.size(); i++) {
			itemArr[i] = itemList.get(i);
		}
		return new Closure(new LR1Item[] {});
	}

	public boolean isLike(Closure c) {
		if (c.getInitialItem().size() != initialItem.size()) {
			return false;
		}
		for (LR1Item it : c.getInitialItem()) {
			if (!initialItem.contains(it)) {
				return false;
			}
		}
		return true;
	}

}
