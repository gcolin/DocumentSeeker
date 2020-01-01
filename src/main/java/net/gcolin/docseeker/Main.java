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
package net.gcolin.docseeker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import net.gcolin.docseeker.gui.Gui;

/**
 * 
 * @author Gael COLIN
 *
 */
public class Main {

	public static void main(String[] args) {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		
		UIManager.installLookAndFeel("FlatDarcula", "com.formdev.flatlaf.FlatDarculaLaf");
		UIManager.installLookAndFeel("FlatDark", "com.formdev.flatlaf.FlatDarkLaf");
		UIManager.installLookAndFeel("FlatIntelliJ", "com.formdev.flatlaf.FlatIntelliJLaf");
		UIManager.installLookAndFeel("FlatLight", "com.formdev.flatlaf.FlatLightLaf");

		loadConfigFile();

		String ui = Engine.SINGLETON.getSettings().getOptions().get("ui");
		if (ui != null) {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if (info.getName().equals(ui)) {
					try {
						UIManager.setLookAndFeel(info.getClassName());
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
							| UnsupportedLookAndFeelException e1) {
						e1.printStackTrace();
					}
					break;
				}
			}
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				Engine.SINGLETON.stopAsync();
			}
		});
		new Gui().setVisible(true);
	}

	private static void loadConfigFile() {
		String config = System.getProperty("filecarwler.config");
		if (config != null) {
			Logger log = LoggerFactory.getLogger(Main.class);
			log.debug("load configuration file {}", config);
			Properties props = new Properties();
			try (InputStream in = new FileInputStream(new File(config))) {
				props.load(in);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
			for(Entry<Object, Object> o : props.entrySet()) {
				System.setProperty(o.getKey().toString(), o.getValue().toString());
			}
		}
	}

}
