package com.dingtao.shp;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import com.dingtao.shp.model.RMLine;
import com.dingtao.shp.model.RMPoi;
import com.dingtao.shp.model.RMPoiList;
import com.dingtao.shp.util.Util;
import com.google.gson.Gson;

public class Test extends JFrame implements ActionListener {
	GridBagLayout g = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	private static final String UTF_8 = "UTF-8";
	JLabel userName;
	JLabel password;
	JTextField dataPath;
	JTextField shpPath;
	JButton importMcBtn;
	JButton exportBtn;
	JButton importPoiBtn;
	JTextArea mResult;

	Test(String str) {
		super(str);
		setSize(300, 500);
		setDefaultCloseOperation(3);
		setLayout(this.g);

		addComponent();
		this.importMcBtn.addActionListener(this);
		this.importPoiBtn.addActionListener(new ImportPoiListener());
		this.exportBtn.addActionListener(new ExportFile());
		setVisible(true);
		setLocationRelativeTo(null);
	}

	public void addComponent() {
		this.userName = new JLabel("数据文件路径：");
		add(this.g, this.c, this.userName, 0, 0, 1, 1);

		this.dataPath = new JTextField(10);
		add(this.g, this.c, this.dataPath, 1, 0, 3, 1);

		this.password = new JLabel("SHP文件路径：");
		add(this.g, this.c, this.password, 0, 1, 1, 1);

		this.shpPath = new JTextField(10);
		add(this.g, this.c, this.shpPath, 1, 1, 3, 1);

		this.importMcBtn = new JButton("导入.mc");
		this.c.insets = new Insets(7, 0, 4, 0);
		add(this.g, this.c, this.importMcBtn, 1, 5, 1, 1);

		this.importPoiBtn = new JButton("导入.poi.door");
		this.c.insets = new Insets(7, 0, 4, 0);
		add(this.g, this.c, this.importPoiBtn, 0, 5, 1, 1);

		this.exportBtn = new JButton("导出");
		this.c.insets = new Insets(7, 0, 4, 0);
		add(this.g, this.c, this.exportBtn, 2, 5, 1, 1);

		this.mResult = new JTextArea(20, 20);
		this.mResult.setLineWrap(true);
		this.mResult.setWrapStyleWord(true);
		JScrollPane scroll = new JScrollPane(this.mResult);

		scroll.setHorizontalScrollBarPolicy(30);
		scroll.setVerticalScrollBarPolicy(20);
		add(this.g, this.c, scroll, 0, 6, 4, 4);
	}

	public void add(GridBagLayout g, GridBagConstraints c, JComponent jc,
			int x, int y, int gw, int gh) {
		c.gridx = x;
		c.gridy = y;
		c.anchor = 17;
		c.gridwidth = gw;
		c.gridheight = gh;
		g.setConstraints(jc, c);
		add(jc);
	}

	public void actionPerformed(ActionEvent arg0) {
		showFileDialog(1);
	}

	private void showFileDialog(int exprand) {
		String str = this.dataPath.getText();
		String path = getClass().getProtectionDomain().getCodeSource()
				.getLocation().getPath();
		if ((str != null) && (!"".equals(str)))
			path = str.split(";")[0];
		System.out.println("path : " + path);
		JFileChooser jfc = new JFileChooser(new File(path));
		this.mResult.setText("");

		jfc.setMultiSelectionEnabled(true);
		jfc.setFileSelectionMode(0);
		jfc.setFileFilter(new FileFilterTest(exprand));
		jfc.showDialog(new JLabel(), "选择");
		File[] file = jfc.getSelectedFiles();
		String result = "";
		for (int i = 0; i < file.length; i++) {
			result = result + file[i].getAbsolutePath() + ";";
		}
		System.out.println(result);
		this.mResult.setText(result.replaceAll(";", "\n"));
		if ((result != null) && (!"".equals(result)))
			this.dataPath.setText(result);
	}

	private void showExportFileDialog() {
		String str = this.shpPath.getText();
		String path = getClass().getProtectionDomain().getCodeSource()
				.getLocation().getPath();
		if ((str != null) && (!"".equals(str)))
			path = str.split(";")[0];
		System.out.println("path : " + path);
		JFileChooser jfc = new JFileChooser(new File(path));

		jfc.setFileSelectionMode(1);
		jfc.showDialog(new JLabel(), "选择文件夹");
		File file = jfc.getSelectedFile();
		String result = file.getAbsolutePath();
		System.out.println(result);
		if ((result != null) && (!"".equals(result)))
			this.shpPath.setText(result);
		expandFile(result + File.separator);
	}

	private void expandFile(String expand) {
		String result = this.dataPath.getText();
		if ((result == null) || ("".equals(result)))
			return;
		String[] arg = result.split(";");
		Iterator<String> it;
		String key;
		if (arg[0].endsWith(".mc")) {
			HashMap<String, ArrayList<RMLine>> map = new HashMap<String, ArrayList<RMLine>>();
			for (String path : arg) {
				RMLine line = getPoiRouteList(path);
				String filename = path.substring(path
						.lastIndexOf(File.separator) + 1);
				String mapname = filename.substring(0, filename.indexOf("_"));
				key = expand + mapname + File.separator + mapname;
				System.out.println("filename: " + filename + "    mapName : "
						+ mapname + "    key : " + key);
				if (map.containsKey(key)) {
					((ArrayList<RMLine>) map.get(key)).add(line);
				} else {
					File file = new File(expand + mapname);
					file.mkdirs();
					map.put(key, new ArrayList<RMLine>());
					((ArrayList<RMLine>) map.get(key)).add(line);
				}
			}
			Set<String> keys = map.keySet();
			it = keys.iterator();
			while (it.hasNext()) {
				key = (String) it.next();
				Util.line2Shp(key, (ArrayList<RMLine>) map.get(key));
			}
		} else {
			HashMap<String, ArrayList<RMPoi>> map = new HashMap<String, ArrayList<RMPoi>>();
			for (String path : arg) {
				ArrayList<RMPoi> list = getPoiPointList(path);
				if ((list != null) && (list.size() > 0)) {
					String filename = path.substring(path
							.lastIndexOf(File.separator) + 1);
					String mapname = filename.substring(0,
							filename.lastIndexOf("."));
					key = expand + mapname + "_poi" + File.separator + mapname;
					System.out.println("filename: " + filename
							+ "    mapName : " + mapname + "    key :" + key);
					if (map.containsKey(key)) {
						((ArrayList<RMPoi>) map.get(key)).addAll(list);
					} else {
						File file = new File(expand + mapname + "_poi");
						System.out.println(file.mkdirs());
						map.put(key, list);
					}
				}
			}
			Set<String> keys = map.keySet();
			it = keys.iterator();
			while (it.hasNext()) {
				key = (String) it.next();
				Util.point2Shp(key, (ArrayList<RMPoi>) map.get(key));
			}
		}
	}

	private ArrayList<RMPoi> getPoiPointList(String path) {
		File file = new File(path);
		if (!file.exists())
			return null;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));
			String result = "";
			String line;
			while ((line = br.readLine()) != null) {
				result = result + line;
			}
			br.close();
			if ((result != null) && (!"".equals(result))) {
				Gson gson = new Gson();
				RMPoiList list = (RMPoiList) gson.fromJson(result,
						RMPoiList.class);
				return list.getPoiList();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private RMLine getPoiRouteList(String path) {
		File file = new File(path);
		if (!file.exists())
			return null;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));
			String result = "";
			String line;
			while ((line = br.readLine()) != null) {
				result = result + line;
			}
			br.close();
			if ((result != null) && (!"".equals(result))) {
				Gson gson = new Gson();
				RMLine list = (RMLine) gson.fromJson(result, RMLine.class);
				return list;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	class ExportFile implements ActionListener {
		ExportFile() {
		}

		public void actionPerformed(ActionEvent arg0) {
			Test.this.showExportFileDialog();
		}
	}

	class FileFilterTest extends FileFilter {
		private int type;

		public FileFilterTest(int type) {
			this.type = type;
		}

		public boolean accept(File f) {
			if (f.isDirectory())
				return true;
			if (this.type == 0)
				return (f.getName().endsWith(".poi"))
						|| (f.getName().endsWith(".door"));
			if (this.type == 1)
				return f.getName().endsWith(".mc");
			return false;
		}

		public String getDescription() {
			if (this.type == 1)
				return ".mc";
			return ".door;.poi";
		}
	}

	class ImportPoiListener implements ActionListener {
		ImportPoiListener() {
		}

		public void actionPerformed(ActionEvent arg0) {
			Test.this.showFileDialog(0);
		}
	}
}