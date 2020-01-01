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
package net.gcolin.docseeker.spi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiBits;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Bits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.gcolin.docseeker.FileInfo;
import net.gcolin.docseeker.FileRequest;
import net.gcolin.docseeker.SearchIndexer;
import net.gcolin.docseeker.SearchResult;
import net.gcolin.docseeker.SearchResultItem;
import net.gcolin.docseeker.Settings;

/**
 * 
 * @author Gael COLIN
 *
 */
public class LuceneIndexer implements SearchIndexer {

	public static final int PAGE_SIZE = 10;
	private static final String[] SEARCH_FIELDS = new String[] { "filename", "path", "content", "author",
			"contentType" };
	private Directory index;
	private IndexWriter writer;
	private IndexReader reader;
	private IndexSearcher searcher;
	private boolean readerFail = false;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private Set<String> fieldsToLoadSearch = new HashSet<String>();
	private Set<String> fieldsToLoadInfo = new HashSet<String>();
	private Set<String> fieldsToLoadPrune = Collections.singleton("path");
	private int maxContentSize = Integer.parseInt(Settings.OPTION_DEFAULT_LUCENE_WRITE_LIMIT);
	private List<String> paths;

	public LuceneIndexer() {
		createDir();

		fieldsToLoadSearch.add("path");
		fieldsToLoadSearch.add("content");
		fieldsToLoadInfo.add("lastmodified");
		fieldsToLoadInfo.add("length");
	}

	public void setSettings(Settings settings) {
		String maxContentSizeParam = settings.getOptions().get(Settings.OPTION_LUCENE_WRITE_LIMIT);
		if (maxContentSizeParam != null && !maxContentSizeParam.isEmpty()) {
			maxContentSize = Integer.parseInt(maxContentSizeParam);
		}
		paths = settings.getPaths();
	}

	private IndexReader getReader() {
		if (reader == null && !readerFail) {
			try {
				reader = DirectoryReader.open(index);
				searcher = new IndexSearcher(reader);
			} catch (IOException e) {
				readerFail = true;
				logger.debug("cannot open reader", e);
			}
		}
		return reader;
	}

	private IndexWriter getWriter() {
		if (writer == null) {
			try {
				writer = new IndexWriter(index, new IndexWriterConfig(new StandardAnalyzer()));
			} catch (IOException e) {
				logger.error("cannot open writer", e);
			}
		}
		return writer;
	}

	@Override
	public void index(FileRequest request) {
		Document doc = new Document();
		doc.add(new StringField("path", request.getPath(), Field.Store.YES));
		doc.add(new TextField("filename", new File(request.getPath()).getName(), Field.Store.NO));

		if (maxContentSize > 0 && request.getContent().length() > maxContentSize) {
			logger.info("skip content of {} size {}", request.getPath(), request.getContent().length());
			doc.add(new TextField("content", request.getContent(), Field.Store.NO));
		} else {
			doc.add(new TextField("content", request.getContent(), Field.Store.YES));
		}
		if (request.getAuthor() != null) {
			doc.add(new TextField("author", request.getAuthor(), Field.Store.YES));
		}
		if (request.getContentType() != null) {
			doc.add(new StringField("contentType", request.getContentType(), Field.Store.YES));
		}
		doc.add(new StoredField("lastmodified", request.getLastmodified()));
		doc.add(new StoredField("length", request.getLength()));
		try {
			if (request.getIndex() != null) {
				getWriter().updateDocument(new Term("path", request.getPath()), doc);
			} else {
				getWriter().addDocument(doc);
			}
		} catch (IOException e) {
			logger.error("cannot add document " + request.getPath(), e);
		}
		end();
	}

	@Override
	public void prune() {
		IndexReader reader = getReader();
		if (reader != null) {
			int count = 0;
			Bits liveDocs = MultiBits.getLiveDocs(reader);
			try {
				for (int i = 0; i < reader.maxDoc(); i++) {
					if (liveDocs != null && !liveDocs.get(i)) {
						continue;
					}

					Document doc = reader.document(i, fieldsToLoadPrune);
					String path = doc.get("path");
					count++;
					if (!new File(path).exists() || paths.isEmpty()
							|| !paths.stream().anyMatch(x -> path.startsWith(x))) {
						logger.info("prune: remove {}", path);
						if (getWriter().tryDeleteDocument(reader, i) == -1) {
							getWriter().deleteDocuments(new Term("path", path));
						}
					}
				}
			} catch (IOException e) {
				logger.error("cannot prune", e);
			}
			logger.info("prune: check {} files", count);
		}
	}

	@Override
	public SearchResult search(int start, String query) {
		SearchResult result = new SearchResult();
		result.setTotal(0);
		result.setStart(start);
		result.setPageSize(PAGE_SIZE);
		MultiFieldQueryParser queryParser = new MultiFieldQueryParser(SEARCH_FIELDS, new StandardAnalyzer());
		try {
			Query luceneQuery = queryParser.parse(query);
			getReader();
			TopDocs docs = searcher.search(luceneQuery, PAGE_SIZE + start);
			result.setTotal((int) docs.totalHits.value);
			int end = Math.min(docs.scoreDocs.length, PAGE_SIZE + start);
			for (int i = start; i < end; i++) {
				Document doc = reader.document(docs.scoreDocs[i].doc, fieldsToLoadSearch);
				SearchResultItem item = new SearchResultItem();
				item.setPath(doc.get("path"));
				item.setContent(doc.get("content"));
				if (item.getContent() == null) {
					item.setContent("");
				}
				item.setScore(docs.scoreDocs[i].score);
				result.getItems().add(item);
			}
		} catch (Exception e) {
			logger.error("cannot search", e);
		}
		return result;
	}

	@Override
	public void end() {
		if (writer != null) {
			try {
				writer.commit();
			} catch (IOException e) {
				logger.error("cannot commit", e);
			}
			closeWriter();
			closeReader();
			readerFail = false;
		}
	}

	private void closeWriter() {
		if (writer != null) {
			try {
				writer.close();
			} catch (IOException e) {
				logger.error("cannot close", e);
			}
			writer = null;
		}
	}

	private void closeReader() {
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				logger.error("cannot close reader", e);
			}
			reader = null;
		}
	}

	@Override
	public FileInfo info(String path) {
		if (getReader() != null) {
			try {
				Term term = new Term("path", path);
				TopDocs docs = searcher.search(new TermQuery(term), 1);
				if (docs.scoreDocs.length > 0) {
					Document doc = reader.document(docs.scoreDocs[0].doc, fieldsToLoadInfo);
					FileInfo info = new FileInfo();
					info.setLastmodified(doc.getField("lastmodified").numericValue().longValue());
					info.setLength(doc.getField("length").numericValue().longValue());
					info.setIndex(Boolean.TRUE);
					return info;
				}
			} catch (IOException e) {
				logger.error("cannot find file", e);
			}
		}
		return null;
	}

	private void createDir() {
		File luceneDir = new File("lucene");
		if (new File("saveToHome").exists()) {
			luceneDir = new File(
					Paths.get(System.getProperty("user.home"), "AppData", "Local", "docseeker", "lucene").toString());
		}
		try {
			luceneDir.mkdir();
			index = FSDirectory.open(luceneDir.toPath());
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public boolean isWritable() {
		return getWriter() != null;
	}

}
