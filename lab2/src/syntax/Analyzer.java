package syntax;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.swing.table.DefaultTableModel;

import lexical.Token;

public class Analyzer {

	private List<Map<String, Integer>> GOTO;
	private List<Map<String, String>> ACTION;
	private Stack<Integer> stateStack = new Stack<Integer>();
	private Stack<String> symbolStack = new Stack<String>();
	private List<String> actions = new ArrayList<String>();

	private List<Token> tokens;
	private int inputIdx = 0;
	private List<Production> productions;
	private DefaultTableModel errorTbMd;

	public Analyzer(List<Map<String, Integer>> Goto, List<Map<String, String>> Action, List<Token> tokens,
			List<Production> productions, DefaultTableModel errorTbMd) {
		GOTO = Goto;
		ACTION = Action;
		this.tokens = tokens;
		int rows = tokens.get(tokens.size() - 1).getRow();
		this.tokens.add(new Token("#", rows));
		this.tokens.add(new Token("\0", rows));
		this.productions = productions;
		this.errorTbMd = errorTbMd;

		symbolStack.push("#");
		stateStack.push(0);
		analyze();
	}

	public List<String> getActions() {
		return actions;
	}

	private void analyze() {
		List<String> errorList = new ArrayList<String>();
		while (true) {
			String symbol = tokens.get(inputIdx).getToken();
			if (symbol.equals("\0")) {
				System.out.println("CAN NOT ACCEPT!");
				break;
			}
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
				errorTbMd.addRow(new String[] {
						String.format("Error at Line %3d: Grammatical errors.", tokens.get(inputIdx).getRow()) });
				while (GOTO.get(stateStack.peek()).size() == 0) {
					stateStack.pop();
				}
				state = stateStack.peek();
				while (true) {
					symbol = tokens.get(inputIdx).getToken();
					if (symbol.equals("\0")) {
						System.out.println("CAN NOT ACCEPT!");
						return;
					}
					if (!ACTION.get(state).keySet().contains(symbol)) {
						inputIdx++;
						continue;
					}

					boolean flag = false;
					for (Map.Entry<String, Integer> entry : GOTO.get(state).entrySet()) {
						String A = entry.getKey();
						int newState = entry.getValue();
						if (!ACTION.get(newState).get(symbol).equals("err")) {
							stateStack.push(newState);
							flag = true;
							break;
						}

					}
					if (flag) {
						break;
					}
					inputIdx++;
				}
				continue;
			}
		}
	}

}
