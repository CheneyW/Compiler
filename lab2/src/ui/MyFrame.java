package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import lexical.LexicalAnalysis;
import lexical.Token;
import syntax.SyntaxAnalysis;

public class MyFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private JPanel mainPanel = new JPanel();
	private JTextArea inputText = new JTextArea();

	private JTable tokenTb, errorTb, symbolTb, productionTb;
	private DefaultTableModel tokenTbMd, errorTbMd, symbolTbMd, productionTbMd;

	private JScrollPane actionScrollPane = null;
	private JScrollPane gotoScrollPane = null;

	public MyFrame() {
		setTitle("Syntax Analyzer");
		setSize(1600, 800);
		setPanel();
		addButton();
	}

	public static void main(String args[]) {
		MyFrame lexframe = new MyFrame();
		lexframe.setResizable(false);
		lexframe.setVisible(true);
	}

	private void setPanel() {
		// 文本编辑区
		JScrollPane textScrollPane = new JScrollPane(inputText);
		textScrollPane.setBounds(20, 10, 320, 260);
		textScrollPane.setRowHeaderView(new LineNumberHeaderView());

		// 符号表
		String[] symbolColName = { "序号", "符号表" };
		symbolTbMd = new DefaultTableModel(null, symbolColName);
		symbolTb = new JTable(symbolTbMd);
		symbolTb.setEnabled(false);// 不可修改
		JScrollPane symbolScrollPane = new JScrollPane(symbolTb);
		symbolScrollPane.setBounds(360, 280, 110, 260);

		// token序列
		String[] tokenColName = { "行号", "TOKEN", "种别码", "属性值" };
		tokenTbMd = new DefaultTableModel(null, tokenColName);
		tokenTb = new JTable(tokenTbMd);
		tokenTb.setEnabled(false);// 不可修改
		JScrollPane tokenScrollPane = new JScrollPane(tokenTb);
		tokenScrollPane.setBounds(20, 280, 320, 260);

		// 错误分析
		String[] errorColName = { "错误说明" };
		errorTbMd = new DefaultTableModel(null, errorColName);
		errorTb = new JTable(errorTbMd);
		errorTb.setEnabled(false);// 不可修改
		JScrollPane errorScrollPane = new JScrollPane(errorTb);
		errorScrollPane.setBounds(20, 550, 450, 200);

		// 产生式表
		String[] productionColName = { "语法分析结果" };
		productionTbMd = new DefaultTableModel(null, productionColName);
		productionTb = new JTable(productionTbMd);
		productionTb.setEnabled(false);// 不可修改
		JScrollPane productionScrollPane = new JScrollPane(productionTb);
		productionScrollPane.setBounds(480, 50, 180, 700);

		// ACTION
		JLabel actionLabel = new JLabel("ACTION");
		actionLabel.setBounds(670, 10, 100, 40);

		// GOTO
		JLabel gotoLabel = new JLabel("GOTO");
		gotoLabel.setBounds(670, 390, 100, 40);

		mainPanel.add(textScrollPane);
		mainPanel.add(tokenScrollPane);
		mainPanel.add(errorScrollPane);
		mainPanel.add(symbolScrollPane);
		mainPanel.add(productionScrollPane);
		mainPanel.add(actionLabel);
		mainPanel.add(gotoLabel);
		mainPanel.setLayout(null);
		this.add(mainPanel);
	}

	private void addButton() {
		JButton fileOpen = new JButton("打开文件");
		fileOpen.setBounds(360, 50, 100, 30);

		JButton TestCase = new JButton("测试用例");
		TestCase.setBounds(360, 100, 100, 30);

		JButton LexicalAnalysis = new JButton("词法分析");
		LexicalAnalysis.setBounds(360, 150, 100, 30);

		JButton SyntaxAnalysis = new JButton("语法分析");
		SyntaxAnalysis.setBounds(360, 200, 100, 30);

		mainPanel.add(fileOpen);
		mainPanel.add(TestCase);
		mainPanel.add(LexicalAnalysis);
		mainPanel.add(SyntaxAnalysis);

		// fileOpen
		fileOpen.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				inputText.setText("");
				String filePath = "";
				JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new File("./testcase"));
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				if (fc.showOpenDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
					filePath = fc.getSelectedFile().getPath();
					try {
						BufferedReader in = new BufferedReader(new FileReader(filePath));
						String line;
						while ((line = in.readLine()) != null) {
							inputText.append(line + "\n");
						}
						in.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});

		// TestCase
		TestCase.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				inputText.setText("");
				try {
					BufferedReader in = new BufferedReader(new FileReader("./testcase/testcase.txt"));
					String line;
					while ((line = in.readLine()) != null) {
						inputText.append(line + "\n");
					}
					in.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		// LexicalAnalysis
		LexicalAnalysis.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				clearAll();
				LexicalAnalysis lex = new LexicalAnalysis(inputText, tokenTbMd, errorTbMd, symbolTbMd);
				lex.run();
			}
		});

		// SyntaxAnalysis
		SyntaxAnalysis.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				clearAll();
				// 词法分析
				LexicalAnalysis lex = new LexicalAnalysis(inputText, tokenTbMd, errorTbMd, symbolTbMd);
				List<Token> tokens = lex.run();

				// 语法分析
				SyntaxAnalysis sa = new SyntaxAnalysis();
				List<String> actions = sa.analyze(tokens,errorTbMd);
				for (String s : actions) {
					productionTbMd.addRow(new String[] { s });
				}

				// 读取ACTION
				String[] colName = null;
				List<String[]> lines = new ArrayList<String[]>();
				try {
					BufferedReader in = new BufferedReader(new FileReader("./data/ACTION.txt"));
					String str = in.readLine();
					colName = str.split("\t");
					while ((str = in.readLine()) != null) {
						str = str.trim();
						if (str.contentEquals(""))
							continue;
						lines.add(str.split("\t"));
					}
					in.close();
				} catch (IOException e1) {
					System.out.println("ERROR when read ACTION.");
				}
				if (actionScrollPane != null) {
					mainPanel.remove(actionScrollPane);
				}
				DefaultTableModel actionTbMd = new DefaultTableModel(null, colName);
				for (String[] arr : lines) {
					actionTbMd.addRow(arr);
				}
				JTable actionTb = new JTable(actionTbMd);
				actionTb.setEnabled(false);// 不可修改
				JScrollPane newActionScrollPane = new JScrollPane(actionTb);
				newActionScrollPane.setBounds(670, 50, 900, 320);
				actionScrollPane = newActionScrollPane;
				mainPanel.add(newActionScrollPane);

				// 读取GOTO
				colName = null;
				lines = new ArrayList<String[]>();
				try {
					BufferedReader in = new BufferedReader(new FileReader("./data/GOTO.txt"));
					String str = in.readLine();
					colName = str.split("\t");
					while ((str = in.readLine()) != null) {
						if (str.trim().equals(""))
							continue;
						lines.add(str.split("\t"));
					}
					in.close();
				} catch (IOException e1) {
					System.out.println("ERROR when read GOTO.");
				}
				if (gotoScrollPane != null) {
					mainPanel.remove(gotoScrollPane);
				}
				DefaultTableModel gotoTbMd = new DefaultTableModel(null, colName);
				for (String[] arr : lines) {
					gotoTbMd.addRow(arr);
				}
				JTable gotoTb = new JTable(gotoTbMd);
				gotoTb.setEnabled(false);// 不可修改
				JScrollPane newGotoScrollPane = new JScrollPane(gotoTb);
				newGotoScrollPane.setBounds(670, 430, 900, 320);
				gotoScrollPane = newGotoScrollPane;
				mainPanel.add(newGotoScrollPane);
			}
		});
	}

	private void clearAll() {
		clear(tokenTbMd, tokenTb);
		clear(errorTbMd, errorTb);
		clear(symbolTbMd, symbolTb);
		clear(productionTbMd, productionTb);
	}

	private void clear(DefaultTableModel TbMd, JTable Tb) {
		int rows = TbMd.getRowCount();
		for (int i = 0; i < rows; i++) {
			TbMd.removeRow(0);
			Tb.updateUI();
		}
	}
}
