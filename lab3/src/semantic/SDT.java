package semantic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.swing.table.DefaultTableModel;

import lexical.Symbol;

public class SDT {
	private int offset = 0;
	private int addrCount = 0;
	private SymbolTable symbolTable = new SymbolTable();
	private Stack<Symbol> stack;
	private List<String> output = new ArrayList<String>();
	private DefaultTableModel errorTbMd;

	public SDT(Stack<Symbol> stack, DefaultTableModel errorTbMd) {
		this.stack = stack;
		this.errorTbMd = errorTbMd;
	}

	public void getOutput(DefaultTableModel threeAddrTbMd) {
		for (int i = 0; i < output.size(); i++) {
			String s = Integer.toString(i + 100) + " : \t" + output.get(i);
			threeAddrTbMd.addRow(new String[] { s });
		}
	}

	public void statute(int production_idx) {
		switch (production_idx) {
		case 0:
			rule0();
			break;
		case 1:
			rule1();
			break;
		case 2:
			rule2();
			break;
		case 3:
			rule3();
			break;
		case 4:
			rule4();
			break;
		case 5:
			rule5();
			break;
		case 6:
			rule6();
			break;
		case 7:
			rule7();
			break;
		case 8:
			rule8();
			break;
		case 9:
			rule9();
			break;
		case 10:
			rule10();
			break;
		case 11:
			rule11();
			break;
		case 12:
			rule12();
			break;
		case 13:
			rule13();
			break;
		case 14:
			rule14();
			break;
		case 15:
			rule15();
			break;
		case 16:
			rule16();
			break;
		case 17:
			rule17();
			break;
		case 18:
			rule18();
			break;
		case 19:
			rule19();
			break;
		case 20:
			rule20();
			break;
		case 21:
			rule21();
			break;
		case 22:
			rule22();
			break;
		case 23:
			rule23();
			break;
		case 24:
			rule24();
			break;
		case 25:
			rule25();
			break;
		case 26:
			rule26();
			break;
		case 27:
			rule27();
			break;
		case 28:
			rule28();
			break;
		case 29:
			rule29();
			break;
		case 30:
			rule30();
			break;
		case 31:
			rule31();
			break;
		case 32:
			rule32();
			break;
		case 33:
			rule33();
			break;
		case 34:
			rule34();
			break;
		case 35:
			rule35();
			break;
		case 36:
			rule36();
			break;
		case 37:
			rule37();
			break;
		case 38:
			rule38();
			break;
		}
	}

	// 0 P' -> P
	public void rule0() {

//		Symbol P = stack.pop();
//		backpatch(P.nextlist, nextquad());
//		stack.push(new Symbol("P'"));
//		gencode("end");
		stack.pop();
		stack.push(new Symbol("P'"));
	}

	// 1 P -> L
	public void rule1() {
		Symbol L = stack.pop();
		Symbol P = new Symbol("P");
		P.nextlist = L.nextlist;
		stack.push(P);
		backpatch(P.nextlist, nextquad());
		gencode("end");
//		stack.pop();
//		stack.push(new Symbol("P"));
	}

	// 2 L -> L M S
	/// {backpatch(L1.nextlist,M.quad); L.nextlist=S.nextlist}
	public void rule2() {
		Symbol S = stack.pop();
		Symbol M = stack.pop();
		Symbol L1 = stack.pop();
		Symbol L = new Symbol("L");
		backpatch(L1.nextlist, M.quad);
		L.nextlist = S.nextlist;
		stack.push(L);
	}

	// 3 L -> S
	// {L.nextlist=S.nextlist}
	public void rule3() {
		Symbol S = stack.pop();
		Symbol L = new Symbol("L");
		L.nextlist = S.nextlist;
		stack.push(L);
	}

	// 4 S -> D
	public void rule4() {
		stack.pop();
		stack.push(new Symbol("S"));
	}

	/**************************** 声明语句 ****************************/
	// D -> T id ;
	// {enter(id.name, T.type, offset); offset=offset+T.width}
	public void rule5() {
		stack.pop();
		Symbol id = stack.pop();
		Symbol T = stack.pop();
		symbolTable.enter(id.val, T.type, offset);
		offset += T.width;
		stack.push(new Symbol("D"));
	}

	// T -> int
	// {T.type=int;T.width=4}
	public void rule6() {
		stack.pop();
		Symbol T = new Symbol("T");
		T.type = "int";
		T.width = 4;
		stack.push(T);
	}

	// T -> char
	// {T.type=char;T.width=1}
	public void rule7() {
		stack.pop();
		Symbol T = new Symbol("T");
		T.type = "char";
		T.width = 1;
		stack.push(T);
	}

	/**************************** 赋值语句 ****************************/
	// S -> id = E ;
	// {p=lookup(id.name); if(p!=nil) then gencode(p,"=",E.addr) else error}
	public void rule8() {
		stack.pop();
		Symbol E = stack.pop();
		stack.pop();
		Symbol id = stack.pop();
		String idName = symbolTable.lookup(id.val);
		if (idName != null) {
			gencode(idName + " = " + E.addr);
			stack.push(new Symbol("S"));
		} else {// 未定义的标识符
			errorTbMd.addRow(new String[] {
					String.format("Error at Line %3d: Undefined identifier \"%s\".", id.getRow(), idName) });
		}
	}

	// E -> E + item
	// {E.addr=newtemp; gencode(E.addr,"=",E1.addr,"+",item.addr)}
	public void rule9() {
		Symbol item = stack.pop();
		stack.pop();
		Symbol E1 = stack.pop();
		Symbol E = new Symbol("E");
		E.addr = newtemp();
		gencode(E.addr + " = " + E1.addr + " + " + item.addr);
		stack.push(E);
	}

	// E -> E - item
	// {E.addr=newtemp; gencode(E.addr,"=",E1.addr,"-",item.addr)}
	public void rule10() {
		Symbol item = stack.pop();
		stack.pop();
		Symbol E1 = stack.pop();
		Symbol E = new Symbol("E");
		E.addr = newtemp();
		gencode(E.addr + " = " + E1.addr + " - " + item.addr);
		stack.push(E);
	}

	// E -> item
	// {E.addr=item.addr}
	public void rule11() {
		Symbol item = stack.pop();
		Symbol E = new Symbol("E");
		E.addr = item.addr;
		stack.push(E);
	}

	// item -> item * factor
	// {item.addr=newtemp; gencode(item.addr,"=",item1.addr,"*",factor.addr)}
	public void rule12() {
		Symbol factor = stack.pop();
		stack.pop();
		Symbol item1 = stack.pop();
		Symbol item = new Symbol("item");
		item.addr = newtemp();
		gencode(item.addr + " = " + item1.addr + " * " + factor.addr);
		stack.push(item);
	}

	// item -> item / factor
	// {item.addr=newtemp; gencode(item.addr,"=",item1.addr,"/",factor.addr)}
	public void rule13() {
		Symbol factor = stack.pop();
		stack.pop();
		Symbol item1 = stack.pop();
		Symbol item = new Symbol("item");
		item.addr = newtemp();
		gencode(item.addr + " = " + item1.addr + " / " + factor.addr);
		stack.push(item);
	}

	// item -> factor
	// {item.addr=factor.addr}
	public void rule14() {
		Symbol factor = stack.pop();
		Symbol item = new Symbol("item");
		item.addr = factor.addr;
		stack.push(item);
	}

	// factor -> ( E )
	// {factor.addr = E.addr}
	public void rule15() {
		stack.pop();
		Symbol E = stack.pop();
		stack.pop();
		Symbol factor = new Symbol("factor");
		factor.addr = E.addr;
		stack.push(factor);
	}

	// factor -> id
	// {p=lookup(id.name); if(p!=nil) then factor.addr=p else error}
	public void rule16() {
		Symbol id = stack.pop();
		String idName = symbolTable.lookup(id.val);
		Symbol factor = new Symbol("factor");
		if (idName != null) {
			factor.addr = idName;
		} else {
			errorTbMd.addRow(new String[] {
					String.format("Error at Line %3d: Undefined identifier \"%s\".", id.getRow(), idName) });
			factor.addr = "0";
		}
		stack.push(factor);
	}

	// factor -> const
	// {factor.addr=const}
	public void rule17() {
		Symbol _const = stack.pop();
		Symbol factor = new Symbol("factor");
		factor.addr = _const.val;
		stack.push(factor);
	}

	/**************************** 布尔表达式 ****************************/

	// 18 B -> B || M Ba
	// {backpatch(B1.falselist,M.quad); B.truelist=merge(B1.truelist,Ba.truelist);
	// B.falselist=Ba.falselist}
	public void rule18() {
		Symbol Ba = stack.pop();
		Symbol M = stack.pop();
		stack.pop();
		Symbol B1 = stack.pop();
		Symbol B = new Symbol("B");

		backpatch(B1.falselist, M.quad);
		B.truelist = merge(B1.truelist, Ba.truelist);
		B.falselist = Ba.falselist;
		stack.push(B);
	}

	// 19 B -> Ba
	// {B.truelist=Ba.truelist; B.falselist=Ba.falselist}
	public void rule19() {
		Symbol Ba = stack.pop();
		Symbol B = new Symbol("B");

		B.truelist = Ba.truelist;
		B.falselist = Ba.falselist;
		stack.push(B);
	}

	// 20 Ba -> Ba && M Bb
	// {backpatch(Ba1.truelist,M.quad); Ba.truelist=Bb.truelist;
	// Ba.falselist=merge(Ba1.falselist,Bb.falselist)}
	public void rule20() {
		Symbol Bb = stack.pop();
		Symbol M = stack.pop();
		stack.pop();
		Symbol Ba1 = stack.pop();
		Symbol Ba = new Symbol("Ba");

		backpatch(Ba1.truelist, M.quad);
		Ba.truelist = Bb.truelist;
		Ba.falselist = merge(Ba1.falselist, Bb.falselist);
		stack.push(Ba);
	}

	// 21 Ba -> Bb
	// {Ba.truelist=Bb.truelist; Ba.falselist=Bb.falselist}
	public void rule21() {
		Symbol Bb = stack.pop();
		Symbol Ba = new Symbol("Ba");

		Ba.truelist = Bb.truelist;
		Ba.falselist = Bb.falselist;
		stack.push(Ba);
	}

	// 22 Bb -> ! Bb
	// {Bb.truelist=Bb1.falselist; Bb.falselist=Bb1.truelist}
	public void rule22() {
		Symbol Bb1 = stack.pop();
		Symbol Bb = new Symbol("Bb");

		Bb.truelist = Bb1.falselist;
		Bb.falselist = Bb1.truelist;
		stack.push(Bb);
	}

	// 23 Bb -> Bc
	// {Bb.truelist=Bc.truelist; Bb.falselist=Bc.falselist}
	public void rule23() {
		Symbol Bc = stack.pop();
		Symbol Bb = new Symbol("Bb");

		Bb.truelist = Bc.truelist;
		Bb.falselist = Bc.falselist;
		stack.push(Bb);
	}

	// 24 Bc -> E relop E
	// {Bc.truelist=makelist(nextquad); Bc.falselist=makelist(nextquad+1);
	// gencode("if" E1.addr relop.op E2.addr "goto "); gencode("goto ")}
	public void rule24() {
		Symbol E2 = stack.pop();
		Symbol relop = stack.pop();
		Symbol E1 = stack.pop();
		Symbol Bc = new Symbol("Bc");

		Bc.truelist = makelist(nextquad());
		Bc.falselist = makelist(nextquad() + 1);
		gencode("if " + E1.addr + relop.op + E2.addr + " goto ");
		gencode("goto ");
		stack.push(Bc);
	}

	// 25 Bc -> ( B )
	// {Bc.truelist=B.truelist; Bc.falselist=B.falselist}
	public void rule25() {
		stack.pop();
		Symbol B = stack.pop();
		stack.pop();
		Symbol Bc = new Symbol("Bc");

		Bc.truelist = B.truelist;
		Bc.falselist = B.falselist;
		stack.push(Bc);
	}

	// 26 Bc -> true
	// {Bc.truelist=makelist(nextquad); gencode("goto ")}
	public void rule26() {
		stack.pop();
		Symbol Bc = new Symbol("Bc");

		Bc.truelist = makelist(nextquad());
		gencode("goto ");
		stack.push(Bc);
	}

	// 27 Bc -> false
	// {Bc.falselist=makelist(nextquad); gencode("goto ")}
	public void rule27() {
		stack.pop();
		Symbol Bc = new Symbol("Bc");

		Bc.falselist = makelist(nextquad());
		gencode("goto ");
		stack.push(Bc);
	}

	// 28 relop -> ==
	// {relop.op = ==}
	public void rule28() {
		stack.pop();
		Symbol relop = new Symbol("relop");

		relop.op = "==";
		stack.push(relop);
	}

	// 29 relop -> !=
	// {relop.op = !=}
	public void rule29() {
		stack.pop();
		Symbol relop = new Symbol("relop");

		relop.op = "!=";
		stack.push(relop);
	}

	// 30 relop -> >
	// {relop.op = >}
	public void rule30() {
		stack.pop();
		Symbol relop = new Symbol("relop");

		relop.op = ">";
		stack.push(relop);
	}

	// 31 relop -> >=
	// {relop.op = >=}
	public void rule31() {
		stack.pop();
		Symbol relop = new Symbol("relop");

		relop.op = ">=";
		stack.push(relop);
	}

	// 32 relop -> <
	// {relop.op = <}
	public void rule32() {
		stack.pop();
		Symbol relop = new Symbol("relop");

		relop.op = "<";
		stack.push(relop);
	}

	// 33 relop -> <=
	// {relop.op = <=}
	public void rule33() {
		stack.pop();
		Symbol relop = new Symbol("relop");

		relop.op = "<=";
		stack.push(relop);
	}

	/**************************** 控制语句 ****************************/
	// 34 M -> ε
	// {M.quad=nextquad}
	public void rule34() {
		Symbol M = new Symbol("M");
		M.quad = nextquad();
		stack.push(M);
	}

	// 35 N -> ε
	// {N.nextlist=makelist(nextquad); gencode("goto ")}
	public void rule35() {
		Symbol N = new Symbol("N");
		N.nextlist = makelist(nextquad());
		gencode("goto ");
		stack.push(N);
	}

	// 36 S -> if ( B ) { M L }
	// {backpatch(B.truelist,M.quad); S.nextlist=merge(B.falselist,L.nextlist)}
	public void rule36() {
		stack.pop();
		Symbol L = stack.pop();
		Symbol M = stack.pop();
		stack.pop();
		stack.pop();
		Symbol B = stack.pop();
		stack.pop();
		stack.pop();
		Symbol S = new Symbol("S");

		backpatch(B.truelist, M.quad);
		S.nextlist = merge(B.falselist, L.nextlist);
		stack.push(S);
	}

	// 37 S -> if ( B ) { M L } N else { M L }
	// {backpatch(B.truelist,M1.quad); backpatch(B.falselist,M2.quad);
	// S.nextlist=merge(L1.nextlist,merge(N.nextlist,L2.nextlist))}
	public void rule37() {
		stack.pop();
		Symbol L2 = stack.pop();
		Symbol M2 = stack.pop();
		stack.pop();
		stack.pop();
		Symbol N = stack.pop();
		stack.pop();
		Symbol L1 = stack.pop();
		Symbol M1 = stack.pop();
		stack.pop();
		stack.pop();
		Symbol B = stack.pop();
		stack.pop();
		stack.pop();
		Symbol S = new Symbol("S");

		backpatch(B.truelist, M1.quad);
		backpatch(B.falselist, M2.quad);
		S.nextlist = merge(L1.nextlist, merge(N.nextlist, L2.nextlist));
		stack.push(S);
	}

	// 38 S -> while ( M B ) { M L }
	// {backpatch(L.nextlist,M1.quad); backpatch(B.truelist,M2.quad);
	// S.nextlist=B.falselist; gencode("goto" M1.quad)}
	public void rule38() {
		stack.pop();
		Symbol L = stack.pop();
		Symbol M2 = stack.pop();
		stack.pop();
		stack.pop();
		Symbol B = stack.pop();
		Symbol M1 = stack.pop();
		stack.pop();
		stack.pop();
		Symbol S = new Symbol("S");

		backpatch(L.nextlist, M1.quad);
		backpatch(B.truelist, M2.quad);
		S.nextlist = B.falselist;
		gencode("goto " + Integer.toString(M1.quad + 100));
		stack.push(S);
	}

	private String newtemp() {
		return "t" + Integer.toString(addrCount++);
	}

	private void gencode(String s) {
		output.add(s);
	}

	private int nextquad() {
		return output.size();
	}

	// 回填 将 i 作为目标标号插入到 p 所指列表中的各指令中
	private void backpatch(Set<Integer> p, int go) {
		for (int i : p) {
			output.set(i, output.get(i) + Integer.toString(100 + go));
		}
	}

	// 合并
	private Set<Integer> merge(Set<Integer> p1, Set<Integer> p2) {
		Set<Integer> set = new HashSet<Integer>();
		for (int i : p1) {
			set.add(i);
		}
		for (int i : p2) {
			set.add(i);
		}
		return set;
	}

	// 创建一个只包含i 的集合
	private Set<Integer> makelist(int i) {
		Set<Integer> set = new HashSet<Integer>();
		set.add(i);
		return set;
	}

}
