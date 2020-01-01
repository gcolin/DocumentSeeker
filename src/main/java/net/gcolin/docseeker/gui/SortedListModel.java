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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;

/**
 * 
 * @author Gael COLIN
 *
 */
public class SortedListModel<T extends Comparable<? super T>> extends AbstractListModel<T> {

	private static final long serialVersionUID = 1L;
	private List<T> list = new ArrayList<>();

	@Override
	public int getSize() {
		return list.size();
	}

	@Override
	public T getElementAt(int index) {
		return list.get(index);
	}

	public void remove(int index) {
		list.remove(index);
		fireIntervalRemoved(this, index, index);
	}

	public void add(T e) {
		if (!list.contains(e)) {
			list.add(e);
			Collections.sort(list);
			int index = list.indexOf(e);
			fireIntervalAdded(this, index, index);
		}
	}
	
	public void fill(List<T> list) {
		list.addAll(this.list);
	}

	public void clear() {
		int size = list.size();
		if (size > 0) {
			list.clear();
			fireIntervalRemoved(this, 0, size);
		}
	}

}
