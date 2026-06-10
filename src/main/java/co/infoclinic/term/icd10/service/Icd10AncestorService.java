package co.infoclinic.term.icd10.service;

import java.util.List;

import co.infoclinic.term.icd10.model.entity.Icd10Ancestor;
import co.infoclinic.term.icd10.model.dto.Icd10AncestorDTO;

public interface Icd10AncestorService {
	
	List<Icd10Ancestor> getAncestorByClassCode(String code);

	List<Icd10AncestorDTO> getAncestorDTOByClassCode(String code);

}