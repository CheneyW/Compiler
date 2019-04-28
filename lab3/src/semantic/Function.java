package semantic;

import java.util.ArrayList;
import java.util.List;

public class Function {

	String name = "";
	List<String> paras = new ArrayList<String>();

	private int begin, end;// 起始、终止语句序号

	public int getBegin() {
		return begin;
	}

	public List<String> getParas() {
		return paras;
	}

	public void addPara(String para) {
		paras.add(para);
	}

	public void setBegin(int begin) {
		this.begin = begin;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public Function(String id) {
		name = id;
	}

}
