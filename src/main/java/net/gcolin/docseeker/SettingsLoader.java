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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

/**
 * 
 * @author Gael COLIN
 *
 */
public class SettingsLoader {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private File getSettingsFile() {
		String settingsPath = System.getProperty("docseeker.settings");
		if (settingsPath == null) {
			settingsPath = "settings.json";
			if (new File("saveToHome").exists()) {
				File root = new File(
						Paths.get(System.getProperty("user.home"), "AppData", "Local", "docseeker").toString());
				root.mkdirs();
				return new File(root, settingsPath);
			}
		}
		File settingsFile = new File(settingsPath);
		return settingsFile;
	}

	public void saveSettings(Settings settings) {
		File settingsFile = getSettingsFile();
		if (!settingsFile.exists()) {
			settingsFile.getAbsoluteFile().getParentFile().mkdirs();
		}
		try (Writer out = new OutputStreamWriter(new FileOutputStream(settingsFile), StandardCharsets.UTF_8)) {
			JSONObject obj = new JSONObject();
			JSONArray paths = new JSONArray();

			for (String path : settings.getPaths()) {
				paths.put(path);
			}
			obj.put("paths", paths);
			obj.put("options", new JSONObject(settings.getOptions()));

			out.write(obj.toString(4));
		} catch (IOException e) {
			logger.error("cannot save settings", e);
		}
	}

	public Settings loadSettings() {
		Settings settings = new Settings();
		boolean loaded = false;
		File settingsFile = getSettingsFile();
		if (settingsFile.exists() && settingsFile.isFile()) {
			try (Reader in = new InputStreamReader(new FileInputStream(settingsFile), StandardCharsets.UTF_8)) {
				JSONObject obj = new JSONObject(IOUtils.toString(in));
				if(obj.has("paths")) {
					JSONArray paths = obj.getJSONArray("paths");
					for(int i=0;i<paths.length();i++) {
						settings.getPaths().add(paths.getString(i));
					}
				}
				if(obj.has("options")) {
					JSONObject options = obj.getJSONObject("options");
					for(String key: options.keySet()) {
						settings.getOptions().put(key, options.getString(key));
					}
				}
				loaded = true;
			} catch (IOException e) {
				logger.error("cannot load settings", e);
			}
		}
		if (!loaded) {
			logger.info("cannot find settings.");
			settings = new Settings();
			settings.getOptions().put("ui", "FlatIntelliJ");
		}
		return settings;
	}

}
