package syntax;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Analyzer {

	private List<Map<String, Integer>> GOTO;
	private List<Map<String, String>> ACTION;
	private Stack<Integer> stateStack = new Stack<Integer>();
	private Stack<String> symbolStack = new Stack<String>();
	private List<String> actions = new ArrayList<String>();

	private List<String> tokens;
	private int inputIdx = 0;
	private List<Production> productions;

	public Analyzer(List<Map<String, Integer>> Goto, List<Map<String, String>> Action, List<String> tokens,
			List<Production> productions) {
		GOTO = Goto;
		ACTION = Action;
		this.tokens = tokens;
		this.tokens.add("#");
		this.productions = productions;

		symbolStack.push("#");
		stateStack.push(0);
		analyze();
	}

	public List<String> getActions() {
		return actions;
	}

	private void analyze() {
		while (true) {
			String symbol = tokens.get(inputIdx);
			int state = stateStack.peek();
			String act = ACTION.get(state).get(symbol);
			// 移进
			if (act.startsWith("s")) {
				stateStack.push(Integer.parseInt(act.substring(1)));
				symbolStack.push(symbol);
				inputIdx++;
			}
			// 规约
			else if (act.startsWith("r")) {
				actions.add(productions.get(Integer.parseInt(act.substring(1))).toString());
				Production p = productions.get(Integer.parseInt(act.substring(1)));
				for (int i = 0; i < p.getRight().length; i++) {
					symbolStack.pop();
					stateStack.pop();
				}
				symbolStack.push(p.getLeft());

				int newState = GOTO.get(stateStack.peek()).get(p.left);
				stateStack.push(newState);

			}
			// 接受
			else if (act.equals("acc")) {
				System.out.println(act);
				break;
			}
			// 报错
			else if (act.equals("err")) {
				System.out.println(act);
				break;
			}
		}
	}

}
