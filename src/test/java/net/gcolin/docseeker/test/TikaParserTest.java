package net.gcolin.docseeker.test;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Ignore;
import org.junit.Test;

import net.gcolin.docseeker.spi.TikaInternalParser;

@Ignore
public class TikaParserTest {

	@Test
	public void testPdf() throws URISyntaxException {
		System.out.println(new TikaInternalParser().handle(new File("D:\\gcolin\\Downloads\\test\\Arabrom.zip")).getContent());
	}
	
}
