package co.infoclinic.term.common.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Swagger Configuration
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

	Logger log = LoggerFactory.getLogger(SwaggerConfig.class);

	@Bean
	public Docket api() throws IOException {
		return new Docket(DocumentationType.SWAGGER_2)
				.apiInfo(apiInfo())
				.tags(
					new Tag("I-01 SNOMEDCT", "SNOMED CT 개념 조회"),
					new Tag("I-02 SNOMEDCT", "SNOMED CT 검색"),
					new Tag("I-03 SNOMEDCT", "SNOMED CT 계층구조"),
					new Tag("I-04 SNOMEDCT", "SNOMED CT 설명"),
					new Tag("I-05 SNOMEDCT", "SNOMED CT 관계"),
					new Tag("I-06 SNOMEDCT", "SNOMED CT RefSet"),
					new Tag("I-07 SNOMEDCT", "SNOMED CT MRCM"),
					new Tag("I-08 SNOMEDCT", "SNOMED CT Mapping"),
					new Tag("I-09 SNOMEDCT", "SNOMED CT 편집"),
					new Tag("I-10 SNOMEDCT", "SNOMED CT 기타"),
					new Tag("II-01. LOINC", "LOINC 개념 조회"),
					new Tag("II-02. LOINC", "LOINC 검색"),
					new Tag("II-03. LOINC", "LOINC 계층구조"),
					new Tag("II-04. LOINC", "LOINC Panel"),
					new Tag("II-05. LOINC", "LOINC LP"),
					new Tag("II-06. LOINC", "LOINC LG"),
					new Tag("II-07. LOINC", "LOINC LA"),
					new Tag("III-1. ICD10", "ICD-10 / KCD 조회"),
					new Tag("III-02. ICD10", "ICD-10 검색"),
					new Tag("IV-01. HIRA", "HIRA 행위/약제 조회"),
					new Tag("V-01. FHIR", "FHIR R4 CapabilityStatement"),
					new Tag("V-02. FHIR CodeSystem", "FHIR R4 CodeSystem CRUD + 연산"),
					new Tag("V-03. FHIR ValueSet", "FHIR R4 ValueSet CRUD + $expand"),
					new Tag("V-04. FHIR ConceptMap", "FHIR R4 ConceptMap CRUD + $translate"),
					new Tag("V-05. FHIR NamingSystem", "FHIR R4 NamingSystem CRUD"),
					new Tag("V-06. FHIR Package", "FHIR IG 패키지 설치"),
					new Tag("VI-01. Map", "SNOMED CT Mapping 검색")
				)
				.select()
				.apis(RequestHandlerSelectors.basePackage("co.infoclinic.term"))
				.build();
	}

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder()
				.title("InfoClinic Terminology REST API")
				.description("SNOMED CT · LOINC · ICD-10/KCD · HIRA · FHIR R4 통합 용어 서비스 API")
				.contact(new Contact("InfoClinic", "http://infoclinic.co", "info@infoclinic.co"))
				.license("Apache License Version 2.0")
				.version("2.0")
				.build();
	}
}
