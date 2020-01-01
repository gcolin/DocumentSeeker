package net.gcolin.docseeker.test;

import org.junit.Ignore;
import org.junit.Test;

import net.gcolin.docseeker.Settings;
import net.gcolin.docseeker.SettingsLoader;

@Ignore
public class SettingsTest {

	@Test
	public void test() {
		SettingsLoader loader = new SettingsLoader();
		Settings settings = loader.loadSettings();
		settings.getOptions().put("ui", "metal");
		System.out.println(settings);
		loader.saveSettings(settings);
		settings = loader.loadSettings();
		System.out.println(settings);
	}
	
}
