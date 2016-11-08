package vorgu2.prax1.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class ConfigParser {
	
	private static final File CONFIG_FILE = new File("config.txt");
	private static final Map<String, String> parameters = new HashMap<>();
	
	static {
		try {
			String[] pair;
			for(String line : Files.readAllLines(CONFIG_FILE.toPath())) {
				if(!line.isEmpty() && !line.startsWith(";")) {
					pair = line.split("=");
					parameters.put(pair[0], pair[1]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private ConfigParser() {}
	
	public static String getParameter(String key) {
		return parameters.get(key);
	}
	
	public static int getIntParameter(String key) {
		return Integer.parseInt(parameters.get(key));
	}

}
