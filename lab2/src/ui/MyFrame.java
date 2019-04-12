package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import lexical.LexicalAnalysis;

public class MyFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private JPanel mainPanel = new JPanel();
	private JTextArea inputText = new JTextArea();

	private JTable tokenTb, errorTb, symbolTb;
	private DefaultTableModel tokenTbMd, errorTbMd, symbolTbMd;

	public MyFrame() {
		setTitle("Lexical Analyzer");
		setSize(700, 800);
		setPanel();
		addActionListener();
	}

	public static void main(String args[]) {
		MyFrame lexframe = new MyFrame();
		lexframe.setResizable(false);
		lexframe.setVisible(true);
	}

	private void setPanel() {
		// 文本编辑区
		JScrollPane textScrollPane = new JScrollPane(inputText);
		textScrollPane.setBounds(20, 10, 500, 260);
		textScrollPane.setRowHeaderView(new LineNumberHeaderView());

		// 符号表
		String[] symbolColName = { "符号表" };
		symbolTbMd = new DefaultTableModel(null, symbolColName);
		symbolTb = new JTable(symbolTbMd);
		symbolTb.setEnabled(false);// 不可修改
		JScrollPane symbolScrollPane = new JScrollPane(symbolTb);
		symbolScrollPane.setBounds(540, 280, 140, 260);

		// token序列
		String[] tokenColName = { "行号", "TOKEN", "宏", "属性值" };
		tokenTbMd = new DefaultTableModel(null, tokenColName);
		tokenTb = new JTable(tokenTbMd);
		tokenTb.setEnabled(false);// 不可修改
		JScrollPane tokenScrollPane = new JScrollPane(tokenTb);
		tokenScrollPane.setBounds(20, 280, 500, 260);

		// 错误分析
		String[] errorColName = { "错误说明" };
		errorTbMd = new DefaultTableModel(null, errorColName);
		errorTb = new JTable(errorTbMd);
		errorTb.setEnabled(false);// 不可修改
		JScrollPane errorScrollPane = new JScrollPane(errorTb);
		errorScrollPane.setBounds(20, 550, 660, 200);

		mainPanel.add(textScrollPane);
		mainPanel.add(tokenScrollPane);
		mainPanel.add(errorScrollPane);
		mainPanel.add(symbolScrollPane);
		mainPanel.setLayout(null);
		this.add(mainPanel);
	}

	private void addActionListener() {
		JButton fileOpen = new JButton("打开文件");
		fileOpen.setBounds(550, 60, 100, 30);

		JButton TestCase = new JButton("测试用例");
		TestCase.setBounds(550, 120, 100, 30);

		JButton LexicalAnalysis = new JButton("词法分析");
		LexicalAnalysis.setBounds(550, 180, 100, 30);

		mainPanel.add(fileOpen);
		mainPanel.add(TestCase);
		mainPanel.add(LexicalAnalysis);

		// fileOpen
		fileOpen.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				inputText.setText("");
				String filePath = "";
				JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new File("./code"));
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
					BufferedReader in = new BufferedReader(new FileReader("./code/testcase.txt"));
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
				clear();
				LexicalAnalysis lex = new LexicalAnalysis(inputText, tokenTbMd, errorTbMd, symbolTbMd);
				lex.run();
			}
		});
	}

	private void clear() {
		int tokenRows = tokenTbMd.getRowCount();
		int errorRows = errorTbMd.getRowCount();
		int symbolRows = symbolTbMd.getRowCount();

		for (int i = 0; i < tokenRows; i++) {
			tokenTbMd.removeRow(0);
			tokenTb.updateUI();
		}
		for (int i = 0; i < errorRows; i++) {
			errorTbMd.removeRow(0);
			errorTb.updateUI();
		}
		for (int i = 0; i < symbolRows; i++) {
			symbolTbMd.removeRow(0);
			symbolTb.updateUI();
		}
	}
}
