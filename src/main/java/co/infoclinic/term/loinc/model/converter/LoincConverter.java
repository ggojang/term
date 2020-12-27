package co.infoclinic.term.loinc.model.converter;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import co.infoclinic.term.loinc.model.dto.LoincDTO;
import co.infoclinic.term.loinc.model.entity.Loinc;

/**
 * Loinc DTO와 Entity간 객체 변환기
 */
@Component(value = "LNCEntityCvt")
public final class LoincConverter {

	static final Logger log = LoggerFactory.getLogger(LoincConverter.class);
 
	
	/**
	 * Loinc Entity List를 DTO List로 변환
	 * @param srcs
	 * @return
	 */
	public static List<LoincDTO> toDTOList(List<Loinc> entities) {
		List<LoincDTO> dtos = new ArrayList<LoincDTO>();
		
		int srcsSize = entities.size();
		for (int i = 0; i < srcsSize; i++) {
			dtos.add(toDTO(entities.get(i)));
		}
		
		return dtos;
	}
	
	
	/**
	 * Loinc Entity를 DTO로 변환
	 * @param src
	 * @return
	 */
	public static LoincDTO toDTO(Loinc entity) {
		LoincDTO dto = new LoincDTO();
		
		dto.setCode(entity.getCode());
		dto.setComponent(entity.getComponent());
		dto.setProperty(entity.getProperty());
		dto.setTime(entity.getTimeAspect());
		dto.setSystem(entity.getSystem());
		dto.setScale(entity.getScaleType());
		dto.setMethod(entity.getMethodType());
		dto.setLastChngdVer(entity.getLastChangedVersion());
		dto.setClsName(entity.getClassName());
		dto.setChngType(entity.getChangeType());
		dto.setDefDesc(entity.getDefinitionDescription());
		dto.setStatus(entity.getStatus());
		dto.setConsName(entity.getConsumerName());
		dto.setClsType(entity.getClassType());
		dto.setFormula(entity.getFormula());
		//dto.setSpecies(entity.getSpecies());
		dto.setExAnswers(entity.getExampleAnswers());
		dto.setSurvQstTxt(entity.getSurveyQuestText());
		dto.setSurvQstSrc(entity.getSurveyQuestSrc());
		dto.setUnitsRqd(entity.getUnitsRequired());
		dto.setSubmittedUnits(entity.getSubmittedUnits());
		dto.setRelatedNames(entity.getRelatedNames2());
		dto.setShortName(entity.getShortName());
		dto.setOrderObs(entity.getOrderObs());
		dto.setCdiscCmnTests(entity.getCdiscCommonTests());
		dto.setHl7FldSubfldId(entity.getHl7FieldSubfieldId());
		dto.setExtCoprNotice(entity.getExternalCopyrightNotice());
		dto.setExUnits(entity.getExampleUnits());
		dto.setLongName(entity.getLongCommonName());
		dto.setUnitsAndRange(entity.getUnitsAndRange());
		//dto.setDocSection(entity.getDocumentSection());
		dto.setExUcumUnits(entity.getExampleUcumUnits());
		dto.setExSiUcumUnits(entity.getExampleSiUcumUnits());
		dto.setStatusReason(entity.getStatusReason());
		dto.setStatusTxt(entity.getStatusText());
		dto.setChngReasonPublic(entity.getChangeReasonPublic());
		dto.setCmnTestRnk(entity.getCommonTestRank());
		dto.setCmnOrderRnk(entity.getCommonOrderRank());
		dto.setCmnSiTestRnk(entity.getCommonSiTestRank());
		dto.setHl7AttStru(entity.getHl7AttachmentStructure());
		dto.setExtCoprLnk(entity.getExternalCopyrightLink());
		dto.setPanelType(entity.getPanelType());
		dto.setAskAtOrderEntry(entity.getAskAtOrderEntry());
		dto.setAscObs(entity.getAssociatedObservations());
		dto.setFirstRelVer(entity.getFirstReleasedVersion());
		dto.setValHL7AttReq(entity.getValidHL7AttachmentRequest());
		dto.setDispName(entity.getDisplayName());
		
		return dto;
	}
}
