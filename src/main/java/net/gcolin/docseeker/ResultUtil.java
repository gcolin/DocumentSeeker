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

/**
 * 
 * @author Gael COLIN
 *
 */
public class ResultUtil {

	public static String htmlBold(String input, String query) {
		StringBuilder str = new StringBuilder("<html>");
		String[] partsUp = query.toUpperCase().split("\\s+");
		String inputUp = input.toUpperCase();
		int prec = 0;
		while (str.length() < 250) {
			int min = -1;
			int splitMin = Integer.MAX_VALUE;
			for (int i = 0; i < partsUp.length; i++) {
				int split = inputUp.indexOf(partsUp[i], prec);
				if (split < splitMin && split != -1) {
					min = i;
					splitMin = split;
				}
			}
			if (min != -1) {
				if (splitMin != prec) {
					str.append(input, prec, splitMin);
				}
				str.append("<b>");
				str.append(input, splitMin, splitMin + partsUp[min].length());
				str.append("</b>");
				prec = splitMin + partsUp[min].length();
			} else {
				break;
			}
		}
		if (prec < input.length()) {
			str.append(input, prec, input.length());
		}
		str.append("</html>");
		return str.toString();
	}

	public static String sumerize(String input, String query) {
		int nb = 40;
		StringBuilder str = new StringBuilder();
		String[] partsUp = query.toUpperCase().split("\\s+");
		String inputUp = input.toUpperCase();
		int prec = 0;
		while (str.length() < 250) {
			int min = -1;
			int splitMin = Integer.MAX_VALUE;
			for (int i = 0; i < partsUp.length; i++) {
				int split = inputUp.indexOf(partsUp[i], prec);
				if (split < splitMin && split != -1) {
					min = i;
					splitMin = split;
				}
			}
			if (min != -1) {
				if (splitMin - prec < nb * 2) {
					append0(input, str, prec, splitMin);
				} else {
					{
						int max = Math.min(input.length(), Math.min(splitMin, prec + nb));
						int j = prec;
						int lastSplit = j;
						char ch = ' ';
						for (; j < max; j++) {
							ch = input.charAt(j);
							if (isSplit(ch)) {
								lastSplit = j;
							}
						}
						if (j == input.length()) {
							lastSplit = input.length();
						}
						if (lastSplit != prec) {
							append0(input, str, prec, lastSplit);
						}
						prec = lastSplit;
					}

					str.append(" ... ");

					{
						int j = splitMin - 1;
						char ch = ' ';
						prec = Math.max(prec, splitMin - nb);
						int lastSplit = splitMin;
						for (; j > prec; j--) {
							ch = input.charAt(j);
							if (isSplit(ch)) {
								lastSplit = j;
							}
						}

						if (lastSplit != splitMin) {
							append0(input, str, lastSplit + 1, splitMin);
						}
					}
				}
				str.append(input.substring(splitMin, splitMin + partsUp[min].length()));
				prec = splitMin + partsUp[min].length();
			} else {
				break;
			}
		}
		int max = Math.min(input.length(), prec + nb);
		int j = prec;
		int lastSplit = j;
		char ch = ' ';
		for (; j < max; j++) {
			ch = input.charAt(j);
			if (isSplit(ch)) {
				lastSplit = j;
			}
		}
		if (j == input.length()) {
			lastSplit = input.length();
		}
		if (lastSplit != prec) {
			append0(input, str, prec, lastSplit);
			if (lastSplit != input.length()) {
				str.append("...");
			}
		}
		return str.toString();
	}

	private static void append0(String input, StringBuilder str, int prec, int splitMin) {
		boolean space = false;
		for (int i = prec; i < splitMin; i++) {
			char ch = input.charAt(i);
			if (isSplit(ch)) {
				space = true;
			} else {
				if (space) {
					space = false;
					str.append(' ');
				}
				str.append(ch);
			}
		}
		str.append(' ');
	}

	private static boolean isSplit(char ch) {
		return ch == '\n' || ch == '\t' || ch == '\r' || ch == ' ';
	}

}
