package net.gcolin.docseeker.test;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import net.gcolin.docseeker.ResultUtil;

public class SumerizeTest {
	
	@Test
	public void testOneLine() throws IOException {
		Assert.assertEquals("hello I'm iris. ", ResultUtil.sumerize("hello I'm iris.", "iris"));
	}
	
	@Test
	public void testMultipleLines() throws IOException {
		Assert.assertEquals(" hello I'm iris you won ", ResultUtil.sumerize("\n\nhello\nI'm iris\n\nyou won", "iris"));
	}
	
	@Test
	public void testMultipleWords() throws IOException {
		Assert.assertEquals("hello I'm iris. hello I'm iris. hello I'm iris. ", ResultUtil.sumerize("hello I'm iris. hello I'm iris. hello I'm iris.", "iris"));
	}
	
	@Test
	public void testMultipleWordsSplit() throws IOException {
		Assert.assertEquals("hello I'm iris. Lorem ipsum dolor sit amet,  ... mi in fermentum volutpat. hello I'm iris. ", ResultUtil.sumerize("hello I'm iris. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum luctus neque sed felis lacinia fermentum sed ut erat. Integer at eros elementum, consectetur odio quis, consequat ipsum. Sed lacinia justo vel ipsum varius venenatis. Donec sed tincidunt orci. Nunc rhoncus, mi in fermentum volutpat. hello I'm iris.", "iris"));
	}
	
	@Test
	public void testMultipleWordsSplitLines() throws IOException {
		Assert.assertEquals("hello I'm iris. Lorem ipsum dolor sit amet,  ... mi in fermentum volutpat. hello I'm iris. ", ResultUtil.sumerize("hello I'm iris.\n\n\n\n\n\n Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n Vestibulum luctus neque sed felis lacinia fermentum sed ut erat. Integer at eros elementum, consectetur odio quis, consequat ipsum. Sed lacinia justo vel ipsum varius venenatis. Donec sed tincidunt orci. Nunc rhoncus, mi in fermentum volutpat. hello I'm iris.", "iris"));
	}
	
}
