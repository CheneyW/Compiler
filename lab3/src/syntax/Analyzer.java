package syntax;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.swing.table.DefaultTableModel;

import lexical.Symbol;
import semantic.SDT;

public class Analyzer {

	private List<Map<String, Integer>> GOTO;
	private List<Map<String, String>> ACTION;
	private Stack<Integer> stateStack = new Stack<Integer>();// 状态栈
	private Stack<Symbol> symbolStack = new Stack<Symbol>();// 符号栈
	private List<String> actions = new ArrayList<String>();// 规约动作
	private SDT sdt;

	private List<Symbol> tokens;
	private int inputIdx = 0;
	private List<Production> productions;
	private DefaultTableModel errorTbMd;

	public Analyzer(List<Map<String, Integer>> Goto, List<Map<String, String>> Action, List<Symbol> tokens,
			List<Production> productions, DefaultTableModel errorTbMd, DefaultTableModel threeAddrTbMd) {
		GOTO = Goto;
		ACTION = Action;
		this.tokens = tokens;
		int rows = tokens.get(tokens.size() - 1).getRow();
		this.tokens.add(new Symbol("#", rows));
		this.tokens.add(new Symbol("\0", rows));
		this.productions = productions;
		this.errorTbMd = errorTbMd;

		for (Symbol t : tokens) {
			System.out.print(t.getSymbol() + " ");
		}
		System.out.println();

		symbolStack.push(new Symbol("#"));
		stateStack.push(0);
		sdt = new SDT(symbolStack, errorTbMd);
		analyze();
		sdt.getOutput(threeAddrTbMd);
	}

	public List<String> getActions() {
		return actions;
	}

	private void analyze() {
		while (true) {
			Symbol thisToken = tokens.get(inputIdx);
			if (thisToken.getSymbol().equals("\0")) {
				System.out.println("CAN NOT ACCEPT!");
				break;
			}
			int state = stateStack.peek();
			String act = ACTION.get(state).get(thisToken.getSymbol());
			// 移进
			if (act.startsWith("s")) {
				stateStack.push(Integer.parseInt(act.substring(1)));
				symbolStack.push(thisToken);
				inputIdx++;
			}
			// 规约
			else if (act.startsWith("r")) {
				int production_idx = Integer.parseInt(act.substring(1));
				Production p = productions.get(production_idx);
				actions.add(p.toString());
				for (int i = 0; i < p.getRight().length; i++) {
					stateStack.pop();
				}
				sdt.statute(production_idx);

				int newState = GOTO.get(stateStack.peek()).get(p.left);
				stateStack.push(newState);

			}
			// 接受
			else if (act.equals("acc")) {
				sdt.statute(0);
				System.out.println(act);
				break;
			}
			// 报错
			else if (act.equals("err")) {
				for (Symbol s : symbolStack) {
					System.out.print(s.symbol + " ");
				}
				System.out.println();
				errorTbMd.addRow(new String[] {
						String.format("Error at Line %3d: Grammatical errors.", tokens.get(inputIdx).getRow()) });
				while (GOTO.get(stateStack.peek()).size() == 0) {
					stateStack.pop();
				}
				state = stateStack.peek();
				while (true) {
					thisToken = tokens.get(inputIdx);
					if (thisToken.getSymbol().equals("\0")) {
						System.out.println("CAN NOT ACCEPT!");
						return;
					}
					if (!ACTION.get(state).keySet().contains(thisToken.getSymbol())) {
						inputIdx++;
						continue;
					}

					boolean flag = false;
					for (Map.Entry<String, Integer> entry : GOTO.get(state).entrySet()) {
						int newState = entry.getValue();
						if (!ACTION.get(newState).get(thisToken.getSymbol()).equals("err")) {
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
