/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.gcolin.docseeker.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.Objects;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;

import net.gcolin.docseeker.Settings;

/**
 * 
 * @author Gael COLIN
 *
 */
public class SettingsPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private JComboBox<String> theme;
	private final JButton cancel = new JButton(Messages.getString("action.close"));
	private final JButton save = new JButton(Messages.getString("action.save"));
	private SortedListModel<String> folderListModel;
	private Settings settings = new Settings();
	private Settings settingsOriginal = new Settings();
	private Logger logger;
	String[][] optionData = { { Messages.getString("settings.options.tika_write_limit"), "" },
			{ Messages.getString("settings.options.tika_tesseract_path"), "" },
			{ Messages.getString("settings.options.lucene_write_limit"), "" } };
	String[] options = { Settings.OPTION_TIKA_WRITE_LIMIT, Settings.OPTION_TIKA_TESSERACT,
			Settings.OPTION_LUCENE_WRITE_LIMIT };

	public SettingsPanel(Gui gui) {
		super(new BorderLayout());
		logger = gui.getLogger();
		JTabbedPane tabbedPane = new JTabbedPane();

		JPanel folders = new JPanel(new BorderLayout());
		folders.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		JList<String> folderList = new JList<String>();
		folderListModel = new SortedListModel<>();
		folderList.setModel(folderListModel);
		JScrollPane scroll = new JScrollPane(folderList);
		folders.add(scroll, BorderLayout.CENTER);
		JPanel folderButtons = new JPanel();
		folderButtons.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		folderButtons.setLayout(new BoxLayout(folderButtons, BoxLayout.Y_AXIS));
		JPanel folderButtonInner = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		JButton add = new JButton(Messages.getString("action.add"));
		folderButtonInner.add(add, gbc);
		gbc.gridy = 1;
		JButton update = new JButton(Messages.getString("action.update"));
		folderButtonInner.add(update, gbc);
		gbc.gridy = 2;
		JButton remove = new JButton(Messages.getString("action.remove"));
		folderButtonInner.add(remove, gbc);
		folderButtons.add(folderButtonInner);
		folders.add(folderButtons, BorderLayout.EAST);
		bindFolders(add, update, remove, folderList);
		tabbedPane.addTab(Messages.getString("settings.folders"), folders);

		JPanel ui = new JPanel();
		tabbedPane.addTab(Messages.getString("settings.ui"), ui);
		ui.setAlignmentX(JScrollPane.LEFT_ALIGNMENT);
		ui.setLayout(new BoxLayout(ui, BoxLayout.Y_AXIS));
		ui.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		JLabel uiselect = new JLabel(Messages.getString("settings.ui.select"));
		ui.add(uiselect);
		Vector<String> themeList = new Vector<String>();
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			themeList.add(info.getName());
		}
		theme = new JComboBox<String>(themeList);
		theme.setMaximumSize(new Dimension(Integer.MAX_VALUE, theme.getPreferredSize().height));
		ui.add(theme);
		bindTheme();

		String[] columnNames = { Messages.getString("settings.options.name"),
				Messages.getString("settings.options.value") };

		@SuppressWarnings("serial")
		AbstractTableModel optionModel = new AbstractTableModel() {
			public String getColumnName(int column) {
				return columnNames[column];
			}

			public int getRowCount() {
				return optionData.length;
			}

			public int getColumnCount() {
				return columnNames.length;
			}

			public Object getValueAt(int row, int col) {
				return optionData[row][col];
			}

			public boolean isCellEditable(int row, int column) {
				return column == 1;
			}

			public void setValueAt(Object value, int row, int col) {
				optionData[row][col] = value.toString();
				settings.getOptions().put(options[row], optionData[row][col]);
				fireTableCellUpdated(row, col);
				check();
			}
		};

		JTable options = new JTable(optionModel);
		scroll = new JScrollPane(options);
		tabbedPane.addTab(Messages.getString("settings.options"), scroll);

		add(tabbedPane, BorderLayout.CENTER);

		JPanel topbuttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		topbuttons.add(cancel);
		topbuttons.add(save);
		add(topbuttons, BorderLayout.NORTH);
		load(new Settings());
	}

	public void load(Settings settings) {
		this.settingsOriginal = settings;
		this.settings = new Settings(settings);
		folderListModel.clear();
		for (String path : settings.getPaths()) {
			folderListModel.add(path);
		}

		String ui = settings.getOptions().get(Settings.OPTION_UI);
		if (ui == null) {
			theme.setSelectedIndex(0);
		} else {
			theme.setSelectedItem(ui);
		}

		String[] defaultOptionData = { Settings.OPTION_DEFAULT_TIKA_WRITE_LIMIT, Settings.OPTION_DEFAULT_TIKA_TESSERACT,
				Settings.OPTION_DEFAULT_LUCENE_WRITE_LIMIT };

		for (int i = 0; i < options.length; i++) {
			String val = settings.getOptions().get(options[i]);
			if (val == null) {
				val = defaultOptionData[i];
				this.settings.getOptions().put(options[i], val);
				settingsOriginal.getOptions().put(options[i], val);
			}
			optionData[i][1] = val;
		}

		check();
	}

	public JButton getCancel() {
		return cancel;
	}

	public Settings getSettings() {
		return settings;
	}

	public JButton getSave() {
		return save;
	}

	private void check() {
		save.setEnabled(!this.settings.equals(this.settingsOriginal));
	}

	private void updateFolders() {
		this.settings.getPaths().clear();
		folderListModel.fill(this.settings.getPaths());
		check();
	}

	private void bindFolders(JButton add, JButton update, JButton remove, JList<String> folderList) {
		update.setEnabled(false);
		remove.setEnabled(false);
		folderList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		folderList.getSelectionModel().addListSelectionListener(e -> {
			if (folderList.getSelectedIndex() != -1) {
				update.setEnabled(true);
				remove.setEnabled(true);
			}
		});
		remove.addActionListener(e -> {
			folderListModel.remove(folderList.getSelectedIndex());
			update.setEnabled(false);
			remove.setEnabled(false);
			updateFolders();
		});
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		add.addActionListener(e -> {
			if (fileChooser.showOpenDialog(folderList) == JFileChooser.APPROVE_OPTION) {
				String newFolder = fileChooser.getSelectedFile().getAbsolutePath();
				folderListModel.add(newFolder);
				folderList.setSelectedValue(newFolder, true);
				updateFolders();
			}
		});
		update.addActionListener(e -> {
			fileChooser.setSelectedFile(new File(folderList.getSelectedValue()));
			if (fileChooser.showOpenDialog(folderList) == JFileChooser.APPROVE_OPTION) {
				folderListModel.remove(folderList.getSelectedIndex());
				String newFolder = fileChooser.getSelectedFile().getAbsolutePath();
				folderListModel.add(newFolder);
				folderList.setSelectedValue(newFolder, true);
				updateFolders();
			}
		});
	}

	private void bindTheme() {
		theme.addItemListener(e -> {
			String value = (String) theme.getSelectedItem();
			String ui = settings.getOptions().get(Settings.OPTION_UI);
			if (!Objects.equals(ui, value)) {
				settings.getOptions().put(Settings.OPTION_UI, value);
				check();
				for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
					if (info.getName().equals(value)) {
						try {
							UIManager.setLookAndFeel(info.getClassName());
							SwingUtilities.updateComponentTreeUI(SwingUtilities.windowForComponent(theme));
						} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
								| UnsupportedLookAndFeelException e1) {
							logger.error("cannot change UI", e1);
						}
						break;
					}
				}
			}
		});
	}
}
