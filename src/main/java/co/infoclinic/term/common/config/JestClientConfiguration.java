package co.infoclinic.term.common.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.infoclinic.term.common.utils.PropertiesUtil;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;

@Configuration
public class JestClientConfiguration {
		
	private int maxTotalConnection = 75;
	
	private int connTimeout = 1000;
	
	private int readTimeout = 3000;
	
	@Bean
	public JestClient jestClient() {
		
		PropertiesUtil prop = null;
		try {
			prop = new PropertiesUtil();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String url = prop.getPropValue("es.base.url").toString();
		String user = prop.getPropValue("es.base.username").toString();
		String password = prop.getPropValue("es.base.password").toString();
		
		// Configuration
		HttpClientConfig clientConfig = new HttpClientConfig.Builder(url)
												.defaultCredentials(user, password)
												.multiThreaded(true)
												.maxTotalConnection(maxTotalConnection)
												.defaultMaxTotalConnectionPerRoute(maxTotalConnection)
												.connTimeout(connTimeout)
												.readTimeout(readTimeout)
												.build();
		
		JestClientFactory factory = new JestClientFactory();
		factory.setHttpClientConfig(clientConfig);
		JestClient client = factory.getObject();
		return client;
	}
}
