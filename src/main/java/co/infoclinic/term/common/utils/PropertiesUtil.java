package co.infoclinic.term.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {

	private Properties properties;

	public PropertiesUtil() throws IOException {
		properties = new Properties();
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("use.configure.properties");
		if (is != null) {
			properties.load(is);
			is.close();
		}
	}

	public Object getPropValue(String key) {
		Object obj = null;
		if(getProperties().containsKey(key)) {
			obj = getProperties().getProperty(key);
		}
		return obj;
	}

	private Properties getProperties() {
		return properties;
	}

}
