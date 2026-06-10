package co.infoclinic.term.icd10.service;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import co.infoclinic.term.icd10.model.dto.Icd10SiblingsDTO;

public interface Icd10SiblingService {

  Icd10SiblingsDTO getSiblingsDTOByClassCode(String code);
}
