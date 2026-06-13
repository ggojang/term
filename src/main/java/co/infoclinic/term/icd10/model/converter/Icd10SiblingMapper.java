package co.infoclinic.term.icd10.model.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import co.infoclinic.term.icd10.model.dto.Icd10RubricDTO;
import co.infoclinic.term.icd10.model.dto.Icd10RubricKindDTO;
import co.infoclinic.term.icd10.model.dto.Icd10SiblingsDTO;
import co.infoclinic.term.icd10.model.entity.Icd10Sibling;

@Component
public class Icd10SiblingMapper {

    static Logger log = LoggerFactory.getLogger(Icd10SiblingMapper.class);

    public static Icd10SiblingsDTO mapSiblingIntoDTOMap(ArrayList<Icd10Sibling> entities) {
        return mapSiblingIntoDTOMap(entities, Collections.<String, String>emptyMap(), Collections.<String, Boolean>emptyMap());
    }

    public static Icd10SiblingsDTO mapSiblingIntoDTOMap(ArrayList<Icd10Sibling> entities, Map<String, String> koreanLabels) {
        return mapSiblingIntoDTOMap(entities, koreanLabels, Collections.<String, Boolean>emptyMap());
    }

    public static Icd10SiblingsDTO mapSiblingIntoDTOMap(ArrayList<Icd10Sibling> entities, Map<String, String> koreanLabels, Map<String, Boolean> extMap) {
        if (entities == null || entities.isEmpty()) {
            return null;
        }

        String codeSystem = "ICD10";
        String version = entities.get(0).getVersion();

        ArrayList<Icd10RubricDTO> dtos = new ArrayList<Icd10RubricDTO>();
        Icd10RubricDTO dto = new Icd10RubricDTO();
        List<Icd10RubricKindDTO> kindList = new ArrayList<Icd10RubricKindDTO>();

        for (int i = 0; i < entities.size(); i++) {
            // New code group: flush previous DTO
            if (i != 0 && !entities.get(i - 1).getCode().equals(entities.get(i).getCode())) {
                dto.setKinds(kindList);
                setPreferredLabel(dto, koreanLabels, extMap);
                dtos.add(dto);
                dto = new Icd10RubricDTO();
                kindList = new ArrayList<Icd10RubricKindDTO>();
            }

            dto.setCodeSystem(codeSystem);
            dto.setVersion(version);
            dto.setCode(entities.get(i).getCode());

            String usageKind = entities.get(i).getUsageKind();
            if (usageKind != null && !usageKind.isEmpty()) {
                dto.setUsageKind(usageKind);
            }

            String kind = entities.get(i).getKind();
            if (kind != null && !kind.isEmpty()) {
                Icd10RubricKindDTO k = new Icd10RubricKindDTO();
                k.setKind(kind);
                k.setModCode(entities.get(i).getModifierCode());
                k.setId(entities.get(i).getId());
                k.setFtype(entities.get(i).getFragmentType());
                k.setParaType(entities.get(i).getParaType());
                k.setLang(entities.get(i).getLang());
                k.setLabel(entities.get(i).getLabel());
                k.setRef(entities.get(i).getRef());
                kindList.add(k);
            }
        }
        // Flush last DTO
        dto.setKinds(kindList);
        setPreferredLabel(dto, koreanLabels, extMap);
        dtos.add(dto);

        Icd10SiblingsDTO result = new Icd10SiblingsDTO();
        result.setSiblings(dtos);
        return result;
    }

    private static void setPreferredLabel(Icd10RubricDTO dto, Map<String, String> koreanLabels, Map<String, Boolean> extMap) {
        if (dto.getKinds() != null) {
            for (Icd10RubricKindDTO k : dto.getKinds()) {
                if ("preferred".equals(k.getKind()) && k.getLabel() != null) {
                    dto.setLabel(k.getLabel());
                    break;
                }
            }
        }
        if (dto.getCode() != null) {
            if (koreanLabels.containsKey(dto.getCode())) dto.setKoreanLabel(koreanLabels.get(dto.getCode()));
            if (extMap.containsKey(dto.getCode())) dto.setIsKcdExt(extMap.get(dto.getCode()));
        }
    }
}
