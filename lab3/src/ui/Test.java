package ui;

import java.util.ArrayList;
import java.util.List;

public class Test {
	public static void main(String[] args) {
		List<String> list = new ArrayList<String>();
		list.add("1");
		list.add("2");
		list.add("3");
		list.set(1, list.get(1) + "2");

		for (String s : list) {
			System.out.println(s);
		}
	}
}
