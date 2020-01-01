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

import java.awt.CardLayout;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.gcolin.docseeker.Engine;
import net.gcolin.docseeker.EngineListener;
import net.gcolin.docseeker.EngineState;
import net.gcolin.docseeker.EngineState.Phase;
import net.gcolin.docseeker.SearchResult;

/**
 * 
 * @author Gael COLIN
 *
 */
public class Gui extends JFrame {

	enum State {
		HOME, RESULT, SETTINGS
	}

	private static final String TITLE = "Document seeker";
	private State state = State.HOME;
	private static final long serialVersionUID = 1L;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private HomePanel homePanel = new HomePanel();
	private ResultPanel resultPanel = new ResultPanel(this);
	private SettingsPanel settingsPanel = new SettingsPanel(this);
	private Executor executor = Executors.newSingleThreadExecutor();
	private CardLayout layout;
	private JPanel mainPane;

	public Gui() {
		super(TITLE);
		setIconImages(Arrays.asList(createImage("images/icon3.png").getImage(), createImage("images/icon2.png").getImage(), createImage("images/icon4.png").getImage()));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 600);
		mainPane = new JPanel(new CardLayout());
		setContentPane(mainPane);
		mainPane.add(homePanel, State.HOME.name());
		mainPane.add(resultPanel, State.RESULT.name());
		mainPane.add(settingsPanel, State.SETTINGS.name());
		layout = (CardLayout) mainPane.getLayout();
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				if (state == State.HOME) {
					homePanel.getText().requestFocusInWindow();
				}
			}
		});
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				if (state == State.RESULT) {
					resultPanel.onParentResize();
				}
			}
		});
		ActionListener homeSearch = e -> {
			Engine.SINGLETON.setListener(null);
			setState(State.RESULT);
			resultPanel.getText().setText(homePanel.getText().getText());
			setStart(0);
		};
		homePanel.getText().addActionListener(homeSearch);
		homePanel.getSearch().addActionListener(homeSearch);
		homePanel.getSettings().addActionListener(e -> {
			Engine.SINGLETON.setListener(null);
			setState(State.SETTINGS);
			setTitle(TITLE + " - " + Messages.getString("settings.title"));
			settingsPanel.load(Engine.SINGLETON.getSettings());
		});
		homePanel.getStatus().addActionListener(e -> {
			EngineState state = Engine.SINGLETON.getState();
			if (state == null || state.getPhase() == Phase.END) {
				homePanel.getStatus().setText(Messages.getString("action.stop"));
				Engine.SINGLETON.runAsync();
			} else {
				Engine.SINGLETON.stopAsync();
				displayState(Engine.SINGLETON.getState());
			}
		});
		ActionListener search = e -> {
			homePanel.getText().setText(resultPanel.getText().getText());
			setStart(0);
		};
		resultPanel.getSearch().addActionListener(search);
		resultPanel.getText().addActionListener(search);
		resultPanel.getBack().addActionListener(e -> {
			displayHome();
		});
		settingsPanel.getCancel().addActionListener(e -> {
			displayHome();
		});
		settingsPanel.getSave().addActionListener(e -> {
			Engine.SINGLETON.setSettings(settingsPanel.getSettings());
			displayHome();
		});
		displayHome();
	}

	private void displayHome() {
		setState(State.HOME);
		setTitle(TITLE);
		displayState(Engine.SINGLETON.getState());
		Engine.SINGLETON.setListener(new EngineListener() {


			@Override
			public void onStateChange(EngineState state) {
				SwingUtilities.invokeLater(() -> displayState(state));
			}
		});
	}

	private void displayState(EngineState state) {
		if (state == null || state.getPhase() == Phase.END) {
			homePanel.getStatusProgress().setVisible(false);
			homePanel.getStatusLabel().setText("");
			homePanel.getStatus().setText(Messages.getString("action.start"));
		} else if (state.getPhase() == Phase.BUSY) {
			homePanel.getStatusProgress().setVisible(false);
			homePanel.getStatusLabel().setText(Messages.getString("state." + state.getPhase().name()));
			homePanel.getStatus().setText(Messages.getString("action.start"));
		} else {
			homePanel.getStatusProgress().setVisible(true);
			String newLabel = state.getCurrentPath() == null ? Messages.getString("state." + state.getPhase().name())
					: state.getCurrentPath();
			if (!newLabel.equals(homePanel.getStatusLabel().getText())) {
				homePanel.getStatusLabel().setText(newLabel);
			}
			homePanel.getStatusProgress().setValue((int) (state.getCurrent() * 100 / state.getTotal()));
			homePanel.getStatusProgress().setString(state.getCurrent() + " / " + state.getTotal());
		}
	}

	private void setState(State state) {
		if (this.state != state) {
			this.state = state;
			layout.show(mainPane, state.name());
		}
	}

	public Logger getLogger() {
		return logger;
	}

	public void setStart(int start) {
		resultPanel.loading();
		resultPanel.getSearch().setEnabled(false);
		String text = homePanel.getText().getText();
		executor.execute(() -> {
			SearchResult results = Engine.SINGLETON.getIndexer().search(start, text);
			SwingUtilities.invokeLater(() -> {
				setTitle(TITLE + " - " + results.getTotal() + " "
						+ (results.getTotal() < 2 ? Messages.getString("title.result")
								: Messages.getString("title.results")));
				resultPanel.setResults(results);
				resultPanel.getSearch().setEnabled(true);
				resultPanel.validate();
				resultPanel.repaint();
			});
		});
	}

	public ImageIcon createImage(String path) {
		URL imageURL = Gui.class.getClassLoader().getResource(path);
		if (imageURL == null) {
			logger.error(Messages.getString("error.resource") + path);
			return null;
		} else {
			return new ImageIcon(imageURL, "");
		}
	}

}
