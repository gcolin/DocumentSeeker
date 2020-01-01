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
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.gcolin.docseeker.EngineState.Phase;
import net.gcolin.docseeker.spi.LuceneIndexer;
import net.gcolin.docseeker.spi.TikaInternalParser;

/**
 * 
 * @author Gael COLIN
 *
 */
public class Engine implements Runnable {

	private Settings settings;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private SearchIndexer indexer;
	private SettingsLoader settingsLoader = new SettingsLoader();
	public static final Engine SINGLETON = new Engine();
	private AtomicBoolean running = new AtomicBoolean();
	private AtomicBoolean requestReset = new AtomicBoolean();
	private EngineListener listener;
	private EngineState state;
	private FileVisitResult visitState = FileVisitResult.CONTINUE;
	private ExecutorService executor;
	private Semaphore semaphore;

	private Engine() {
		running.set(false);
		requestReset.set(false);
		settings = settingsLoader.loadSettings();
	}

	public Settings getSettings() {
		return settings;
	}

	public SearchIndexer getIndexer() {
		if (indexer == null) {
			indexer = new LuceneIndexer();
			indexer.setSettings(settings);
		}
		return indexer;
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
		settingsLoader.saveSettings(settings);
		if (!running.get()) {
			indexer = null;
		} else {
			requestReset.set(true);
		}
	}

	private void notifyVisitFile(Path file) {
		state.setCurrentPath(file.toFile().getPath());
		state.increment();
		notifyStateChange();
	}

	class RunnerVisitor implements Runnable {
		private FileParser parser;
		private File f;
		private AtomicBoolean available = new AtomicBoolean();
		private Semaphore semaphore;

		public RunnerVisitor() {
			parser = new TikaInternalParser();
			parser.setSettings(settings);
			available.set(true);
		}

		public void init(File f) {
			this.f = f;
		}

		@Override
		public void run() {
			FileInfo info = indexer.info(f.getPath());
			logger.info("visit {}", f.getPath());
			if (info == null || info.getLastmodified() != f.lastModified() || f.length() != info.getLength()) {
				FileRequest req = parser.handle(f);
				if (req != null) {
					if (info != null) {
						req.setIndex(info.getIndex());
					}
					if (running.get()) {
						synchronized (indexer) {
							indexer.index(req);
						}
					}
				}
			}
			
			if(semaphore != null) {
				available.set(true);
				semaphore.release();
			}
		}
	}

	class Visitor extends SimpleFileVisitor<Path> {

		private RunnerVisitor runner;

		public Visitor() {
			runner = new RunnerVisitor();
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			notifyVisitFile(file);
			runner.init(file.toFile());
			runner.run();
			return visitState;
		}

	}

	class MultiThreadVisitor extends SimpleFileVisitor<Path> {

		private RunnerVisitor[] runners;

		public MultiThreadVisitor(int nbThreads) {
			runners = new RunnerVisitor[nbThreads];
			semaphore = new Semaphore(nbThreads);
			for (int i = 0; i < nbThreads; i++) {
				runners[i] = new RunnerVisitor();
				runners[i].semaphore = semaphore;
			}
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			notifyVisitFile(file);
			try {
				semaphore.acquire();
				if (executor != null && !executor.isShutdown()) {
					for (int i = 0; i < runners.length; i++) {
						if (runners[i].available.get()) {
							runners[i].available.set(false);
							runners[i].init(file.toFile());
							executor.submit(runners[i]);
							break;
						}
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			return visitState;
		}
	}

	static class CountVisitor extends SimpleFileVisitor<Path> {

		long count = 0;

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			count++;
			return super.visitFile(file, attrs);
		}

	}

	public static void main(String[] args) {
		new Engine().run();
	}

	private void notifyStateChange() {
		if (listener != null) {
			listener.onStateChange(state);
		}
	}

	public void stop() {
		visitState = FileVisitResult.TERMINATE;
	}

	public void stopAsync() {
		if (!running.get()) {
			return;
		}
		stop();
		try {
			executor.shutdown();
			executor.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error("cannot wait terminaison", e);
		}
		if (state.getPhase() == Phase.CRAWL) {
			state = null;
			notifyStateChange();
		}
		executor = null;
	}

	public void runAsync() {
		if (executor == null) {
			executor = Executors.newCachedThreadPool();
		}
		executor.execute(this);
	}

	public void run() {
		if (running.get()) {
			return;
		}
		running.set(true);
		state = new EngineState();
		notifyStateChange();
		visitState = FileVisitResult.CONTINUE;
		try {
			getIndexer();
			indexer.setSettings(settings);
			if (!indexer.isWritable()) {
				running.set(false);
				state.setPhase(Phase.BUSY);
				notifyStateChange();
				return;
			}
			state.setPhase(Phase.PRUNE);
			notifyStateChange();
			logger.info("start pruning");
			indexer.prune();

			if (state.getCurrentPath() == null) {
				state.setPhase(Phase.PREPARE);
				notifyStateChange();
				logger.info("prepare crawling");
				CountVisitor countVisitor = new CountVisitor();
				try {
					for (String path : settings.getPaths()) {
						Path p = Paths.get(path);
						if (Files.exists(p) && Files.isDirectory(p)) {
							Files.walkFileTree(p, countVisitor);
						}
					}
				} catch (IOException e) {
					logger.error("cannot count files", e);
				}
				if (countVisitor.count == 0) {
					countVisitor.count = 1;
				}
				state.setTotal(countVisitor.count);
			}

			state.setPhase(Phase.CRAWL);

			int tikaCore = Runtime.getRuntime().availableProcessors() / 2 - 1;
			logger.info("start crawling nb Tika Core {}", tikaCore);

			SimpleFileVisitor<Path> fileVisitor = null;
			if (tikaCore < 2) {
				fileVisitor = new Visitor();
			} else {
				fileVisitor = new MultiThreadVisitor(tikaCore);
			}

			for (String path : settings.getPaths()) {
				logger.info("path: {}", path);
				Path p = Paths.get(path);
				if (Files.exists(p) && Files.isDirectory(p)) {
					try {
						Files.walkFileTree(p, fileVisitor);
					} catch (IOException e) {
						logger.error("error while indexing " + path, e);
					}
				} else {
					logger.warn("{} is not a valid directory", path);
				}
			}
			
			if(semaphore != null) {
				try {
					semaphore.acquire(tikaCore);
					semaphore.release(tikaCore);
					semaphore = null;
				} catch (InterruptedException e) {
					logger.error("cannot wait terminaison", e);
				}
			}
			if (visitState == FileVisitResult.CONTINUE) {
				state.setPhase(Phase.COMMIT);
				notifyStateChange();
			}
			indexer.end();
			logger.info("crawling done");
			if (visitState == FileVisitResult.CONTINUE) {
				state.setPhase(Phase.END);
				notifyStateChange();
			}
		} finally {
			running.set(false);
			if (requestReset.get()) {
				indexer = null;
				requestReset.set(false);
			}
		}
	}

	public EngineListener getListener() {
		return listener;
	}

	public void setListener(EngineListener listener) {
		this.listener = listener;
	}

	public EngineState getState() {
		return state;
	}

}
