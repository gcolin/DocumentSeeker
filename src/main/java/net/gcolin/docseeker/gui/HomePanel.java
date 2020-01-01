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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

/**
 * 
 * @author Gael COLIN
 *
 */
public class HomePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private final JButton search = new JButton(Messages.getString("action.search"));
	private final JButton settings = new JButton(Messages.getString("action.settings"));
	private final JButton status = new JButton(Messages.getString("action.status"));
	private final JLabel statusLabel = new JLabel("sfdf/Sdfsdfsdf/DSfsdfsdf.ferzfr");
	private final JProgressBar statusProgress = new JProgressBar(0, 100);
	private final JTextField text = new JTextField();

	public HomePanel() {
		super(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		{
			JPanel topPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
			topPane.add(settings);
			topPane.add(status);			
			topPane.add(statusProgress);
			topPane.add(statusLabel);
			statusProgress.setStringPainted(true);
			gbc.gridx = gbc.gridy = 0;
			gbc.gridwidth = 3;
			gbc.gridheight = 1;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.weightx = gbc.weighty = 30;
			add(topPane, gbc);
		}
		{
			JPanel bottomWest = new JPanel();
			gbc.gridy = 1;
			gbc.gridx = 0;
			gbc.gridwidth = 1;
			gbc.weighty = 70;
			gbc.weightx = 20;
			add(bottomWest, gbc);
		}
		{
			JPanel bottomCenter = new JPanel(new BorderLayout());
			
			JPanel textPane = new JPanel(new BorderLayout());
			text.setFont(new Font(text.getFont().getName(), text.getFont().getStyle(), text.getFont().getSize() * 2));
			textPane.add(text, BorderLayout.CENTER);
			
			search.setFont(text.getFont());
			textPane.add(search, BorderLayout.EAST);
			
			bottomCenter.add(textPane, BorderLayout.NORTH);

			gbc.gridx = 1;
			gbc.weightx = 60;
			add(bottomCenter, gbc);
		}
		{
			JPanel bottomEast = new JPanel();
			gbc.gridx = 2;
			gbc.weightx = 20;
			add(bottomEast, gbc);
		}
	}
	
	public JLabel getStatusLabel() {
		return statusLabel;
	}
	
	public JProgressBar getStatusProgress() {
		return statusProgress;
	}
	
	public JButton getStatus() {
		return status;
	}
	
	public JTextField getText() {
		return text;
	}
	
	public JButton getSearch() {
		return search;
	}
	
	public JButton getSettings() {
		return settings;
	}
}
