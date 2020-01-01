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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Gael COLIN
 *
 */
public class Settings {

	public static final String OPTION_UI = "ui";
	public static final String OPTION_TIKA_WRITE_LIMIT = "tika_write_limit";
	public static final String OPTION_TIKA_TESSERACT = "tika_tesseract_path";
	public static final String OPTION_LUCENE_WRITE_LIMIT = "lucene_write_limit";
	public static final String OPTION_DEFAULT_TIKA_WRITE_LIMIT = "10000000";
	public static final String OPTION_DEFAULT_TIKA_TESSERACT = "";
	public static final String OPTION_DEFAULT_LUCENE_WRITE_LIMIT = "100000";
	public static final String OPTION_DEFAULT_OPEN_DIR_COMMAND = "explorer.exe /select,\"{0}\"";
	private List<String> paths = new ArrayList<>();
	private Map<String, String> options = new HashMap<String, String>();

	public Settings() {
	}
	
	public Settings(Settings settings) {
		this.paths.addAll(settings.getPaths());
		this.options.putAll(settings.options);
	}
	
	public List<String> getPaths() {
		return paths;
	}

	public void setPaths(List<String> paths) {
		this.paths = paths;
	}

	public Map<String, String> getOptions() {
		return options;
	}

	public void setOptions(Map<String, String> options) {
		this.options = options;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((options == null) ? 0 : options.hashCode());
		result = prime * result + ((paths == null) ? 0 : paths.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Settings other = (Settings) obj;
		if (options == null) {
			if (other.options != null)
				return false;
		} else if (!options.equals(other.options))
			return false;
		if (paths == null) {
			if (other.paths != null)
				return false;
		} else if (!paths.equals(other.paths))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Settings [paths=" + paths + ", options=" + options + "]";
	}
}
