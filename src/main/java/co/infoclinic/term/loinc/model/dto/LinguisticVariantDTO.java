package co.infoclinic.term.loinc.model.dto;

import java.util.List;

import javax.persistence.Column;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LingusticVariant DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinguisticVariantDTO {
	
	private String code;
	private String component;
    private String property;
    private String timeAspect;
    private String system;
    private String scaleType;
    private String methodType;
    private String className;
    private String shortName;
    private String longCommonName;
    private String relatedNames2;
	private String isoLang;
	private String isoCountry;
	private String lang;
	
}