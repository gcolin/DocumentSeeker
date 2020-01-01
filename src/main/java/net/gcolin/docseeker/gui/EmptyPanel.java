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

import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * 
 * @author Gael COLIN
 *
 */
public class EmptyPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	public EmptyPanel() {
		super(new FlowLayout(FlowLayout.CENTER));
		setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));
		JLabel label = new JLabel(Messages.getString("result.empty"));
		label.setFont(new Font(label.getFont().getName(), label.getFont().getStyle(), label.getFont().getSize() * 2));
		add(label);
	}
	
}
