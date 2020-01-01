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
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.apache.commons.lang3.SystemUtils;

import net.gcolin.docseeker.ResultUtil;
import net.gcolin.docseeker.SearchResult;
import net.gcolin.docseeker.SearchResultItem;
import net.gcolin.docseeker.Settings;

/**
 * 
 * @author Gael COLIN
 *
 */
public class ResultPanel extends JPanel {

	enum State {
		RESULT, NO_RESULT
	}

	private static final long serialVersionUID = 1L;
	private final JButton search = new JButton(Messages.getString("action.search"));
	private final JButton back = new JButton("X");
	private final JPanel content = new JPanel();
	private final JPanel bottom = new JPanel(new FlowLayout());
	private final JTextField text = new JTextField();
	private SearchResult results;
	private final Gui gui;
	private JScrollPane scroll;
	private CardLayout centerLayout;
	private JPanel resultPane;

	public ResultPanel(Gui gui) {
		this.gui = gui;
		setLayout(new BorderLayout());

		JPanel textPane = new JPanel(new BorderLayout());
		textPane.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));
		text.setFont(new Font(text.getFont().getName(), text.getFont().getStyle(), text.getFont().getSize() * 2));
		textPane.add(text, BorderLayout.CENTER);
		search.setFont(text.getFont());
		textPane.add(search, BorderLayout.EAST);

		back.setFont(text.getFont());
		textPane.add(back, BorderLayout.WEST);
		add(textPane, BorderLayout.NORTH);

		resultPane = new JPanel(new CardLayout());
		centerLayout = (CardLayout) resultPane.getLayout();
		add(resultPane, BorderLayout.CENTER);

		JPanel resultsPane = new JPanel(new BorderLayout());
		resultsPane.add(bottom, BorderLayout.SOUTH);

		content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
		scroll = new JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		resultsPane.add(scroll, BorderLayout.CENTER);
		resultPane.add(resultsPane, State.RESULT.name());

		JPanel noresultPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		noresultPane.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));
		JLabel label = new JLabel(Messages.getString("result.empty"));
		label.setFont(new Font(label.getFont().getName(), label.getFont().getStyle(), label.getFont().getSize() * 2));
		noresultPane.add(label);
		resultPane.add(noresultPane, State.NO_RESULT.name());
	}

	public void loading() {
		content.removeAll();
		bottom.removeAll();
		content.add(new JLabel("..."));
	}

	public void onParentResize() {
		if (results != null) {
			setResults(results);
		}
	}

	public synchronized void setResults(SearchResult results) {
		this.results = results;
		if (results == null) {
			return;
		}
		content.removeAll();
		bottom.removeAll();

		if (results.getTotal() == 0) {
			centerLayout.show(resultPane, State.NO_RESULT.name());
		} else {
			centerLayout.show(resultPane, State.RESULT.name());
		}

		double contentWidth = gui.getWidth() - 50;

		for (int i = 0; i < results.getItems().size(); i++) {
			SearchResultItem item = results.getItems().get(i);
			JPanel result = new JPanel(new BorderLayout());
			result.setBorder(BorderFactory.createEmptyBorder(10, 4, 10, 4));
			JLabel label = new JLabel(String.valueOf(results.getStart() + 1 + i) + ". " + item.getPath());
			label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			label.setFont(new Font(label.getFont().getName(), Font.BOLD, (int) (label.getFont().getSize() * 1.5)));

			FontMetrics metrics = label.getFontMetrics(label.getFont());
			int width = metrics.stringWidth(label.getText());
			if (width > contentWidth * 0.8) {
				int nbCharToHide = (int) (label.getText().length() * 0.3);
				int maxSplit = item.getPath().lastIndexOf(File.separatorChar);
				int split = Math.min(nbCharToHide, maxSplit);
				label.setText(String.valueOf(results.getStart() + 1 + i) + ". ..." + item.getPath().substring(split));
			}

			JButton openFolder = new JButton(Messages.getString("action.openFolder"));
			JPanel title = new JPanel(new FlowLayout(FlowLayout.LEFT));
			title.add(label);
			title.add(openFolder);
			result.add(title, BorderLayout.NORTH);
			String resultText = ResultUtil.sumerize(item.getContent(), text.getText());
			JLabel area = new JLabel();
			Font resultFont = new Font(Font.MONOSPACED, Font.PLAIN, area.getFont().getSize());
			area.setFont(resultFont);
			metrics = area.getFontMetrics(resultFont);
			width = metrics.charWidth('a');
			int numberCharPerLine = (int) contentWidth / width - 1;
			JPanel resultTextPane = new JPanel();
			resultTextPane.setLayout(new BoxLayout(resultTextPane, BoxLayout.Y_AXIS));
			for (int j = 0; j < resultText.length(); j += numberCharPerLine) {
				String part = resultText.substring(j, Math.min(resultText.length(), j + numberCharPerLine));
				JLabel t = new JLabel(ResultUtil.htmlBold(part, text.getText()));
				t.setFont(resultFont);
				resultTextPane.add(t);
			}
			result.add(resultTextPane, BorderLayout.CENTER);
			content.add(result);

			openFolder.addActionListener(new OpenInFolder(item.getPath()));
			label.addMouseListener(new OpenListener(item.getPath()));
		}
		int nbPage = results.getTotal() / results.getPageSize();
		if (results.getTotal() % results.getPageSize() != 0) {
			nbPage++;
		}

		for (int i = 0; i < nbPage; i++) {
			JButton b = new JButton(String.valueOf(i + 1));
			if (i * results.getPageSize() == results.getStart()) {
				b.setEnabled(false);
			} else {
				b.addActionListener(new PageActionListener(i * results.getPageSize()));
			}
			bottom.add(b);
		}
		bottom.invalidate();
		bottom.validate();
		javax.swing.SwingUtilities.invokeLater(() -> {
			scroll.getVerticalScrollBar().setValue(0);
			getText().requestFocusInWindow();
		});
	}

	private class OpenListener extends MouseAdapter {
		private String path;

		public OpenListener(String path) {
			this.path = path;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			try {
				Desktop.getDesktop().open(new File(path));
			} catch (IOException e1) {
				gui.getLogger().error("cannot open file: " + path, e1);
			}
		}
	}

	private class PageActionListener implements ActionListener {

		private final int start;

		public PageActionListener(int start) {
			this.start = start;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			gui.setStart(start);
		}

	}

	private class OpenInFolder implements ActionListener {

		private final String path;

		private OpenInFolder(String path) {
			this.path = path;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (SystemUtils.IS_OS_WINDOWS_10) {
				try {
					Runtime.getRuntime().exec(MessageFormat.format(Settings.OPTION_DEFAULT_OPEN_DIR_COMMAND,
							new File(path).getAbsolutePath()));
				} catch (IOException e1) {
					gui.getLogger().error("cannot open file in folder: " + path, e1);
				}
			}
			try {
				Desktop.getDesktop().open(new File(path).getParentFile());
			} catch (IOException e1) {
				gui.getLogger().error("cannot open file in folder: " + path, e1);
			}
		}

	}

	public JButton getSearch() {
		return search;
	}

	public JButton getBack() {
		return back;
	}

	public JTextField getText() {
		return text;
	}

}
