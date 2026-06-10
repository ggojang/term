package co.infoclinic.term.common.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.annotations.ApiOperation;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Swagger Configuration
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
	
	/** Logger */
	Logger log = LoggerFactory.getLogger(SwaggerConfig.class);

	@Bean
	public Docket api() throws IOException {
		return new Docket(DocumentationType.SWAGGER_2)
				.apiInfo(apiInfo())
				.select()
				.apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
				.build();
	}

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder().title("InfoClinic's Terminology REST API")
				.termsOfServiceUrl("http://infoclinic.co")
				.contact(new Contact("Admin", "http://infoclinic.co", "info@infoclinic.co"))
				.license("Apache License Version 2.0").version("1.0")
				.build();
	}
}
