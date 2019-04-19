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

	public boolean visited = false;

	public Closure(LR1Item[] initialItem) {
		for (LR1Item item : initialItem) {
			this.initialItem.add(item);
			items.add(item);
		}
	}

	public static void set(Map<String, Set<Production>> productionsDict, First first) {
		Closure.productionsDict = productionsDict;
		Closure.first = first;
	}

	public void generate() {
		Map<String, List<LR1Item>> left2Item = new HashMap<String, List<LR1Item>>();
		boolean update = true;
		while (update) {
			update = false;
			Iterator<LR1Item> iterator = items.iterator();
			Set<LR1Item> temp = new HashSet<LR1Item>();
			while (iterator.hasNext()) {

				LR1Item it = iterator.next();
				if (it.visited) {
					continue;
				}
				it.visited = true;

				String B = it.getNext();
				if (B == null) {
					continue;
				}
				List<String> beta = it.getBeta();
				// 计算FIRST(βa)
				List<String> expectedSymbol = new ArrayList<String>();
				if (beta.size() == 0) {
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
				} else if (productionsDict.containsKey(B)) {// 加入项目集
					List<LR1Item> list = new ArrayList<LR1Item>();
					for (Production p : productionsDict.get(B)) {
						LR1Item i = new LR1Item(p, expectedSymbol, 0);
						temp.add(i);
						list.add(i);
						update = true;
					}
					left2Item.put(B, list);
				}
			} // end iterator
			for (LR1Item i : temp) {
				items.add(i);
			}
		} // end while
	}

	public Set<LR1Item> getItems() {
		return items;
	}

	public Set<LR1Item> getInitialItem() {
		return initialItem;
	}

	public Closure GO(String X) {
		List<LR1Item> itemList = new ArrayList<LR1Item>();
		for (LR1Item it : items) {
			if (it.getNext() != null && it.getNext().equals(X)) {
				LR1Item newIt = new LR1Item(it.getID(), it.getLeft(), it.getRight(), it.getExpectedSymbol(),
						it.getDot() + 1);
				itemList.add(newIt);
			}
		}
		if (itemList.size() == 0) {
			return null;
		}

		LR1Item[] itemArr = new LR1Item[itemList.size()];
		for (int i = 0; i < itemList.size(); i++) {
			itemArr[i] = itemList.get(i);
		}
		Closure c = new Closure(itemArr);
		return c;
	}

	public boolean contains(Closure c) {

		for (LR1Item it : c.getInitialItem()) {
			if (!initialItem.contains(it)) {
				return false;
			}
		}
		return true;
	}

}
