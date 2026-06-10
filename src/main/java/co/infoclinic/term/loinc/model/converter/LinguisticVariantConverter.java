package co.infoclinic.term.loinc.model.converter;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import co.infoclinic.term.loinc.model.entity.LinguisticVariant;
import co.infoclinic.term.loinc.model.dto.LinguisticVariantDTO;
/**
 * LP DTO와 Entity간 객체 변환기
 */
@Component(value = "LinguisticVariantCvt")
public final class LinguisticVariantConverter {

	static final Logger log = LoggerFactory.getLogger(LinguisticVariantConverter.class);
 
	
	/**
	 * LP Entity List를 DTO List로 변환
	 * @param srcs
	 * @return
	 */
	public static List<LinguisticVariantDTO> toDTOList(List<LinguisticVariant> LinguisticVariants) {
		List<LinguisticVariantDTO> dtos = new ArrayList<LinguisticVariantDTO>();
		
		int LinguisticVariantsSize = LinguisticVariants.size();
		for (int i = 0; i < LinguisticVariantsSize; i++) {
			dtos.add(toDTO(LinguisticVariants.get(i)));
		}
		//System.out.println("Convert - dtos : " + dtos);
		return dtos;
	}
	
	
	
	/**
	 * LP Link Entity를 DTO로 변환
	 * @param src
	 * @return
	 */
	public static LinguisticVariantDTO toDTO(LinguisticVariant LinguisticVariant) {
		LinguisticVariantDTO dto = new LinguisticVariantDTO();
		
		dto.setCode(LinguisticVariant.getCode());
		dto.setComponent(LinguisticVariant.getComponent());
		dto.setProperty(LinguisticVariant.getProperty());
		dto.setTimeAspect(LinguisticVariant.getTimeAspect());
		dto.setSystem(LinguisticVariant.getSystem());
		dto.setScaleType(LinguisticVariant.getScaleType());
		dto.setMethodType(LinguisticVariant.getMethodType());
		dto.setClassName(LinguisticVariant.getClassName());
		dto.setShortName(LinguisticVariant.getShortName());
		dto.setLongCommonName(LinguisticVariant.getLongCommonName());
		dto.setIsoCountry(LinguisticVariant.getIsoCountry());
		dto.setIsoLang(LinguisticVariant.getIsoLang());
		dto.setRelatedNames2(LinguisticVariant.getRelatedNames2());
		dto.setLang(LinguisticVariant.getLang());
	
		return dto;
	}
}
