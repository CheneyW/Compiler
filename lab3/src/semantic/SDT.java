package semantic;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.swing.table.DefaultTableModel;

import lexical.Symbol;

public class SDT {
	private int addrCount = 0;
	private Stack<Symbol> stack;
	private List<String> output = new ArrayList<String>();
	private DefaultTableModel errorTbMd;

	private Stack<SymbolTable> tblptr = new Stack<SymbolTable>();
	private Stack<Integer> offset = new Stack<Integer>();

	private Map<String, Function> funcs = new HashMap<String, Function>();// 记录函数

	public SDT(Stack<Symbol> stack, DefaultTableModel errorTbMd) {
		this.stack = stack;
		this.errorTbMd = errorTbMd;

		SymbolTable mainTable = new SymbolTable(null);
		mainTable.name = "Main";
		tblptr.push(mainTable);
		offset.push(0);
	}

	public void getOutput(DefaultTableModel threeAddrTbMd) {
		// 符号表
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("./data/symbol.txt"));
			for (String s : tblptr.peek().getSymbols()) {
				out.write(s);
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 三地址码
		for (int i = 0; i < output.size(); i++) {
			String s = Integer.toString(i + 100) + " :  \t" + output.get(i);
			threeAddrTbMd.addRow(new String[] { s });
		}
	}

	/************************** 程序结构 *******************************/
	// 0 P' -> P
	private void rule0() {
		stack.pop();
		stack.push(new Symbol("P'"));
	}

	// 1 P -> L
	// {addwidth(tblptr.top(),offset.top()); tblptr.pop(); offset.pop() }
	private void rule1() {
		Symbol L = stack.pop();
		Symbol P = new Symbol("P");
		P.nextlist = L.nextlist;
		stack.push(P);
		// 回填
		backpatch(P.nextlist, nextquad());
		gencode(" ");
		// 嵌套
		addWidth(tblptr.peek(), offset.peek());
	}

	// 2 L -> L M S
	/// {backpatch(L1.nextlist,M.quad); L.nextlist=S.nextlist}
	private void rule2() {
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
	private void rule3() {
		Symbol S = stack.pop();
		Symbol L = new Symbol("L");
		L.nextlist = S.nextlist;
		stack.push(L);
	}

	// 4 S -> D ;
	private void rule4() {
		stack.pop();
		stack.pop();
		stack.push(new Symbol("S"));
	}

	/**************************** 声明语句 ****************************/
	// D -> T id
	// {enter(tblptr.top(),id.name,T.type,T.width,offset.top());
	// offset.top()+=T.width }
	private void rule5() {
		Symbol id = stack.pop();
		Symbol T = stack.pop();
		id.type = T.type;
		id.width = T.width;
		id.offset = offset.peek();
		enter(tblptr.peek(), id);
		offset.push(offset.pop() + T.width);
		Symbol D = new Symbol("D");
		D.val = id.val;
		stack.push(D);
	}

	// T -> int
	// {T.type=int;T.width=4}
	private void rule6() {
		stack.pop();
		Symbol T = new Symbol("T");
		T.type = "int";
		T.width = 4;
		stack.push(T);
	}

	// T -> String [ const ]
	// {T.type=String;T.width=const}
	private void rule7() {
		stack.pop();
		Symbol _const = stack.pop();
		stack.pop();
		stack.pop();
		Symbol T = new Symbol("T");
		T.type = "String";
		T.width = Integer.parseInt(_const.val);
		stack.push(T);
	}

	/**************************** 赋值语句 ****************************/
	// S -> id = str ;
	// {p=lookup(id.name); if(p!=nil){gencode(p,"=",str)} else{error};
	// if(id.type!="String"){error}; if(id.width<str.len){error} }
	// if(p.type!=String){error} }
	private void rule8() {
		stack.pop();
		Symbol str = stack.pop();
		stack.pop();
		Symbol id = stack.pop();
		Symbol find = tblptr.peek().lookup(id.val);
		if (find != null) {
			// 类型检查
			if (!find.type.equals("String")) {
				errorTbMd.addRow(new String[] { String.format("Error at Line %3d:  Type mismatch.", id.getRow()) });
				return;
			}
			// 字符串长度越界
			if (str.val.length() - 2 > find.width) {
				str.val = str.val.substring(0, find.width+1)+"\"";
				errorTbMd.addRow(
						new String[] { String.format("Error at Line %3d: String length out of bounds.", id.getRow()) });
			}
			gencode(find.val + " = " + str.val);
			stack.push(new Symbol("S"));
		} else {// 未定义的标识符
			errorTbMd.addRow(new String[] {
					String.format("Error at Line %3d: Undefined identifier \"%s\".", id.getRow(), id.val) });
		}
	}

	// S -> id = E ;
	// {p=lookup(id.name); if(p!=nil) then gencode(p,"=",E.addr) else error;
	// if(p.type!=int){error} }
	private void rule9() {
		stack.pop();
		Symbol E = stack.pop();
		stack.pop();
		Symbol id = stack.pop();
		Symbol find = tblptr.peek().lookup(id.val);
		if (find != null) {
			// 类型检查
			if (!find.type.equals("int")) {
				errorTbMd.addRow(new String[] { String.format("Error at Line %3d:  Type mismatch.", id.getRow()) });
				return;
			}
			gencode(find.val + " = " + E.addr);
			stack.push(new Symbol("S"));
		} else {// 未定义的标识符
			errorTbMd.addRow(new String[] {
					String.format("Error at Line %3d: Undefined identifier \"%s\".", id.getRow(), id.val) });
		}
	}

	// E -> E + item
	// {E.addr=newtemp; gencode(E.addr,"=",E1.addr,"+",item.addr)}
	private void rule10() {
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
	private void rule11() {
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
	private void rule12() {
		Symbol item = stack.pop();
		Symbol E = new Symbol("E");
		E.addr = item.addr;
		stack.push(E);
	}

	// item -> item * factor
	// {item.addr=newtemp; gencode(item.addr,"=",item1.addr,"*",factor.addr)}
	private void rule13() {
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
	private void rule14() {
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
	private void rule15() {
		Symbol factor = stack.pop();
		Symbol item = new Symbol("item");
		item.addr = factor.addr;
		stack.push(item);
	}

	// factor -> ( E )
	// {factor.addr = E.addr}
	private void rule16() {
		stack.pop();
		Symbol E = stack.pop();
		stack.pop();
		Symbol factor = new Symbol("factor");
		factor.addr = E.addr;
		stack.push(factor);
	}

	// factor -> id
	// {p=lookup(id.name); if(p!=nil) then factor.addr=p else error}
	private void rule17() {
		Symbol id = stack.pop();
		Symbol find = tblptr.peek().lookup(id.val);
		Symbol factor = new Symbol("factor");
		if (find != null) {
			factor.addr = find.val;
		} else {
			errorTbMd.addRow(new String[] {
					String.format("Error at Line %3d: Undefined identifier \"%s\".", id.getRow(), id.val) });
			factor.addr = "0";
		}
		stack.push(factor);
	}

	// factor -> const
	// {factor.addr=const}
	private void rule18() {
		Symbol _const = stack.pop();
		Symbol factor = new Symbol("factor");
		factor.addr = _const.val;
		stack.push(factor);
	}

	/**************************** 布尔表达式 ****************************/
	// B -> B || M Ba
	// {backpatch(B1.falselist,M.quad); B.truelist=merge(B1.truelist,Ba.truelist);
	// B.falselist=Ba.falselist}
	private void rule19() {
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

	// B -> Ba
	// {B.truelist=Ba.truelist; B.falselist=Ba.falselist}
	private void rule20() {
		Symbol Ba = stack.pop();
		Symbol B = new Symbol("B");

		B.truelist = Ba.truelist;
		B.falselist = Ba.falselist;
		stack.push(B);
	}

	// Ba -> Ba && M Bb
	// {backpatch(Ba1.truelist,M.quad); Ba.truelist=Bb.truelist;
	// Ba.falselist=merge(Ba1.falselist,Bb.falselist)}
	private void rule21() {
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

	// Ba -> Bb
	// {Ba.truelist=Bb.truelist; Ba.falselist=Bb.falselist}
	private void rule22() {
		Symbol Bb = stack.pop();
		Symbol Ba = new Symbol("Ba");

		Ba.truelist = Bb.truelist;
		Ba.falselist = Bb.falselist;
		stack.push(Ba);
	}

	// Bb -> ! Bb
	// {Bb.truelist=Bb1.falselist; Bb.falselist=Bb1.truelist}
	private void rule23() {
		Symbol Bb1 = stack.pop();
		Symbol Bb = new Symbol("Bb");

		Bb.truelist = Bb1.falselist;
		Bb.falselist = Bb1.truelist;
		stack.push(Bb);
	}

	// Bb -> Bc
	// {Bb.truelist=Bc.truelist; Bb.falselist=Bc.falselist}
	private void rule24() {
		Symbol Bc = stack.pop();
		Symbol Bb = new Symbol("Bb");

		Bb.truelist = Bc.truelist;
		Bb.falselist = Bc.falselist;
		stack.push(Bb);
	}

	// Bc -> E relop E
	// {Bc.truelist=makelist(nextquad); Bc.falselist=makelist(nextquad+1);
	// gencode("if" E1.addr relop.op E2.addr "goto "); gencode("goto ")}
	private void rule25() {
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

	// Bc -> ( B )
	// {Bc.truelist=B.truelist; Bc.falselist=B.falselist}
	private void rule26() {
		stack.pop();
		Symbol B = stack.pop();
		stack.pop();
		Symbol Bc = new Symbol("Bc");

		Bc.truelist = B.truelist;
		Bc.falselist = B.falselist;
		stack.push(Bc);
	}

	// Bc -> true
	// {Bc.truelist=makelist(nextquad); gencode("goto ")}
	private void rule27() {
		stack.pop();
		Symbol Bc = new Symbol("Bc");

		Bc.truelist = makelist(nextquad());
		gencode("goto ");
		stack.push(Bc);
	}

	// Bc -> false
	// {Bc.falselist=makelist(nextquad); gencode("goto ")}
	private void rule28() {
		stack.pop();
		Symbol Bc = new Symbol("Bc");

		Bc.falselist = makelist(nextquad());
		gencode("goto ");
		stack.push(Bc);
	}

	// relop -> ==
	// {relop.op = ==}
	private void rule29() {
		stack.pop();
		Symbol relop = new Symbol("relop");

		relop.op = "==";
		stack.push(relop);
	}

	// relop -> !=
	// {relop.op = !=}
	private void rule30() {
		stack.pop();
		Symbol relop = new Symbol("relop");

		relop.op = "!=";
		stack.push(relop);
	}

	// relop -> >
	// {relop.op = >}
	private void rule31() {
		stack.pop();
		Symbol relop = new Symbol("relop");

		relop.op = ">";
		stack.push(relop);
	}

	// relop -> >=
	// {relop.op = >=}
	private void rule32() {
		stack.pop();
		Symbol relop = new Symbol("relop");

		relop.op = ">=";
		stack.push(relop);
	}

	// relop -> <
	// {relop.op = <}
	private void rule33() {
		stack.pop();
		Symbol relop = new Symbol("relop");

		relop.op = "<";
		stack.push(relop);
	}

	// relop -> <=
	// {relop.op = <=}
	private void rule34() {
		stack.pop();
		Symbol relop = new Symbol("relop");

		relop.op = "<=";
		stack.push(relop);
	}

	/**************************** 控制语句 ****************************/
	// M -> ε
	// {M.quad=nextquad}
	private void rule35() {
		Symbol M = new Symbol("M");
		M.quad = nextquad();
		stack.push(M);
	}

	// N -> ε
	// {N.nextlist=makelist(nextquad); gencode("goto ")}
	private void rule36() {
		Symbol N = new Symbol("N");
		N.nextlist = makelist(nextquad());
		gencode("goto ");
		stack.push(N);
	}

	// S -> if ( B ) { M L }
	// {backpatch(B.truelist,M.quad); S.nextlist=merge(B.falselist,L.nextlist)}
	private void rule37() {
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

	// S -> if ( B ) { M L } N else { M L }
	// {backpatch(B.truelist,M1.quad); backpatch(B.falselist,M2.quad);
	// S.nextlist=merge(L1.nextlist,merge(N.nextlist,L2.nextlist))}
	private void rule38() {
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

	// S -> while ( M B ) { M L }
	// {backpatch(L.nextlist,M1.quad); backpatch(B.truelist,M2.quad);
	// S.nextlist=B.falselist; gencode("goto" M1.quad)}
	private void rule39() {
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

	/*************************** 函数定义 **************************/
	// 40 S -> T id ( M' para ) { L }
	// {t=tblptr.top(); addwidth(t,offset.top()); tblptr.pop(); offset.pop();
	// enterproc(tblptr.top(),id.name,t) }
	private void rule40() {
		stack.pop();
		stack.pop();
		stack.pop();
		stack.pop();
		Symbol para = stack.pop();
		Symbol M1 = stack.pop();
		stack.pop();
		Symbol id = stack.pop();
		Symbol T = stack.pop();
		id.type = T.type;
		stack.push(new Symbol("S"));

		Function f = new Function(id.val);
		for (String p : para.paraQueue) {
			f.addPara(p);
		}
		f.setBegin(M1.quad);// 记录函数的第一条语句
		f.setEnd(nextquad());// 记录函数的结束语句
		gencode("goto ");
		funcs.put(id.val, f);
		SymbolTable st = tblptr.pop();
		addWidth(st, offset.pop());
		enterproc(tblptr.peek(), id.val, st);
	}

	// 41 T -> void
	// {T.type=void}
	private void rule41() {
		stack.pop();
		Symbol T = new Symbol("T");
		T.type = "void";
		stack.push(T);
	}

	// 42 M' -> ε
	// {t=mkTable(tblptr.top()); tblptr.push(t); offset.push(0) }
	private void rule42() {
		SymbolTable st = mkTable(tblptr.peek());
		tblptr.push(st);
		offset.push(0);
		Symbol M1 = new Symbol("M'");
		M1.quad = nextquad();// 记录函数的第一条语句
		stack.push(M1);
	}

	// 43 para -> D
	// {para.len=1}
	private void rule43() {
		Symbol D = stack.pop();
		Symbol para = new Symbol("para");
		para.paraQueue.offer(D.val);
		stack.push(para);
	}

	// 44 para -> para , D
	// {para.len+=1 }
	private void rule44() {
		Symbol D = stack.pop();
		stack.pop();
		Symbol para = stack.peek();
		para.paraQueue.offer(D.val);
	}

	// 45 S -> return E ;
	private void rule45() {
		stack.pop();
		Symbol E = stack.pop();
		stack.pop();
		gencode("F = " + E.addr);
		Symbol S = new Symbol("S");
		stack.push(S);
	}

	/*************************** 函数调用 **************************/
	// 46 factor -> call id ( Elist )
	private void rule46() {
		stack.pop();
		Symbol Elist = stack.pop();
		stack.pop();
		Symbol id = stack.pop();
		stack.pop();
		Symbol factor = new Symbol("factor");
		factor.addr = "F";
		stack.push(factor);

		// 函数是否存在
		if (!funcs.containsKey(id.val)) {
			factor.addr = "0";
			errorTbMd.addRow(new String[] { String.format("Error at Line %3d:  Undefined function.", id.getRow()) });
			return;
		}
		// 参数数目
		List<String> paras = funcs.get(id.val).getParas();
		if (Elist.paraQueue.size() != paras.size()) {
			factor.addr = "0";
			errorTbMd.addRow(new String[] { String.format(
					"Error at Line %3d:  The expected number of function arguments is %2d instead of %2d.", id.getRow(),
					paras.size(), Elist.paraQueue.size()) });
			return;
		}
		// 传递参数
		for (int i = 0; i < paras.size(); i++) {
			gencode(paras.get(i) + " = " + Elist.paraQueue.poll());
		}
		gencode("goto " + Integer.toString(funcs.get(id.val).getBegin() + 100));
		int end = funcs.get(id.val).getEnd();
		output.set(end, output.get(end) + Integer.toString(100 + nextquad()));// 回填
	}

	// 47 S -> call id ( Elist ) ;
	// {n=0;for(;queue非空;n++){从 queue队首取出一个实参地址p;gencode("param",p);}
	// gencode("call",id.addr,n) }
	private void rule47() {
		stack.pop();
		stack.pop();
		Symbol Elist = stack.pop();
		stack.pop();
		Symbol id = stack.pop();
		stack.pop();
		Symbol S = new Symbol("S");
		stack.push(S);

		// 函数是否存在
		if (!funcs.containsKey(id.val)) {
			errorTbMd.addRow(new String[] { String.format("Error at Line %3d:  Undefined function.", id.getRow()) });
			return;
		}
		// 参数数目
		List<String> paras = funcs.get(id.val).getParas();
		if (Elist.paraQueue.size() != paras.size()) {
			errorTbMd.addRow(new String[] { String.format(
					"Error at Line %3d:  The expected number of function arguments is %2d instead of %2d.", id.getRow(),
					paras.size(), Elist.paraQueue.size()) });
			return;
		}
		// 传递参数
		for (int i = 0; i < paras.size(); i++) {
			gencode(paras.get(i) + " = " + Elist.paraQueue.poll());
		}
		gencode("goto " + Integer.toString(funcs.get(id.val).getBegin() + 100));
		int end = funcs.get(id.val).getEnd();
		output.set(end, output.get(end) + Integer.toString(100 + nextquad()));// 回填
	}

	// 48 Elist -> Elist , E
	/// {将E.addr添加到queue的队尾}
	private void rule48() {
		Symbol E = stack.pop();
		stack.pop();
		Symbol Elist = stack.peek();
		Elist.paraQueue.offer(E.addr);
	}

	// 49 Elist -> E
	// {初始化queue,然后将E.addr加入到queue的队尾 }
	private void rule49() {
		Symbol E = stack.pop();
		Symbol Elist = new Symbol("Elist");
		Elist.paraQueue.offer(E.addr);
		stack.push(Elist);
	}

	/*************** 辅助函数 ******************/

	// 创建一张新的符号表，previous 为外围过程的符号表
	private SymbolTable mkTable(SymbolTable previous) {
		SymbolTable st = new SymbolTable(previous);
		return st;
	}

	// 在table 指向的符号表中建立一个新表项，
	private void enter(SymbolTable table, Symbol s) {
		table.add(s);
	}

	// 将table 指向的符号表中所有表项的宽度之和记录在符号表的表头
	private void addWidth(SymbolTable table, int width) {
		table.width = width;
	}

	// 在table 指向的符号表中为过程name 建立一个新表项
	// 参数newtable 指向过程name 的符号表
	public void enterproc(SymbolTable table, String funcName, SymbolTable newTable) {
		newTable.name = funcName;
		table.enterproc(newTable);
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

	// 调用入口
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
		case 39:
			rule39();
			break;
		case 40:
			rule40();
			break;
		case 41:
			rule41();
			break;
		case 42:
			rule42();
			break;
		case 43:
			rule43();
			break;
		case 44:
			rule44();
			break;
		case 45:
			rule45();
			break;
		case 46:
			rule46();
			break;
		case 47:
			rule47();
			break;
		case 48:
			rule48();
			break;
		case 49:
			rule49();
			break;
		}
	}

}
