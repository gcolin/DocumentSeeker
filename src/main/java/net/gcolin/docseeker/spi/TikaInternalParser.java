package net.gcolin.docseeker.spi;

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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.tika.config.ServiceLoader;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MediaTypeRegistry;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.DefaultParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.WriteOutContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.gcolin.docseeker.FileParser;
import net.gcolin.docseeker.FileRequest;
import net.gcolin.docseeker.Settings;

/**
 * 
 * @author Gael COLIN
 *
 */
public class TikaInternalParser implements FileParser {

	private ParseContext context;
	private Parser parser;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private int tikaLimit = Integer.parseInt(Settings.OPTION_DEFAULT_TIKA_WRITE_LIMIT);
	private LimitedStringWriter writer;

	public TikaInternalParser() {
	}

	public void setSettings(Settings settings) {
		context = new ParseContext();
		PDFParser pdfParser = new PDFParser();
		DefaultParser defaultParser;
		pdfParser.setOcrStrategy("ocr_and_text");
		defaultParser = new DefaultParser(MediaTypeRegistry.getDefaultRegistry(), new ServiceLoader(),
				Arrays.asList(PDFParser.class));

		parser = new AutoDetectParser(defaultParser, pdfParser);
		context.set(Parser.class, parser);

		TesseractOCRConfig config = new TesseractOCRConfig();
		String tesseractPath = settings.getOptions().get(Settings.OPTION_TIKA_TESSERACT);
		if (tesseractPath == null || tesseractPath.isEmpty()) {
			tesseractPath = System.getProperty("docseeker.tesseract");
		}
		if (tesseractPath == null || tesseractPath.isEmpty()) {
			if (SystemUtils.IS_OS_WINDOWS) {
				tesseractPath = Paths.get(System.getProperty("user.dir"), "tesseract").toString();
			} else {
				tesseractPath = "tesseract";
			}
		}
		config.setTesseractPath(tesseractPath);
		if (SystemUtils.IS_OS_WINDOWS) {
			config.setTessdataPath(tesseractPath + "/tessdata");
		}
		context.set(TesseractOCRConfig.class, config);

		String tikaWriteLimit = settings.getOptions().get(Settings.OPTION_TIKA_WRITE_LIMIT);
		if (StringUtils.isNotEmpty(tikaWriteLimit)) {
			tikaLimit = Integer.parseInt(tikaWriteLimit);
		}
		if (tikaLimit > 0) {
			writer = new LimitedStringWriter(tikaLimit);
		}
	}

	@Override
	public FileRequest handle(File file) {
		FileRequest request = new FileRequest();
		request.setPath(file.getPath());
		request.setLength(file.length());
		request.setLastmodified(file.lastModified());

		Metadata metadata = new Metadata();
		metadata.set(Metadata.RESOURCE_NAME_KEY, file.getName());
		WriteOutContentHandler handler = writer == null ? new WriteOutContentHandler(tikaLimit)
				: new WriteOutContentHandler(writer, tikaLimit);
		try (InputStream in = new FileInputStream(file)) {
			parser.parse(in, new BodyContentHandler(handler), metadata, context);
			System.gc();
		} catch (Exception e) {
			logger.debug(e.getMessage(), e);
		}
		request.setContentType(metadata.get(Metadata.CONTENT_TYPE));
		request.setAuthor(metadata.get(TikaCoreProperties.CREATOR));
		request.setContent(handler.toString());
		if (writer != null) {
			writer.clear();
		}
		return request;
	}

}
