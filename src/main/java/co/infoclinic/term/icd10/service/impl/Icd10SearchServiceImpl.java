package co.infoclinic.term.icd10.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import co.infoclinic.term.icd10.model.dto.Icd10SearchResultDTO;
import co.infoclinic.term.icd10.model.entity.Icd10Class;
import co.infoclinic.term.icd10.model.entity.Icd10Rubric;
import co.infoclinic.term.icd10.repository.Icd10ClassRepository;
import co.infoclinic.term.icd10.repository.Icd10RubricRepository;
import co.infoclinic.term.icd10.service.Icd10SearchService;

@Service("ICD10SrchSvc")
public class Icd10SearchServiceImpl implements Icd10SearchService {

    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9.\\-]*$");

    @Inject
    private Icd10RubricRepository rubricRepo;

    @Inject
    private Icd10ClassRepository classRepo;

    @SuppressWarnings("deprecation")
    @Override
    public Page<Icd10SearchResultDTO> searchByWord(String word, int page, int size) {
        if (word == null || word.trim().isEmpty() || page < 1 || size < 1) {
            return new PageImpl<>(new ArrayList<>());
        }

        String q = word.trim();
        int offset = (page - 1) * size;
        boolean looksLikeCode = CODE_PATTERN.matcher(q).matches();

        LinkedHashMap<String, Icd10SearchResultDTO> merged = new LinkedHashMap<>();
        long total = 0;

        // ── 1. Code prefix search ──────────────────────────────────────────
        if (looksLikeCode) {
            List<Icd10Class> codeResults = classRepo.searchByCodePrefix(q.toUpperCase(), offset, size);
            total += classRepo.countByCodePrefix(q.toUpperCase());
            for (Icd10Class c : codeResults) {
                merged.put(c.getCode(), new Icd10SearchResultDTO(
                    c.getCode(), c.getClassKind(), c.getLabel(), c.getKoreanLabel(), c.getIsKcdExt()));
            }
        }

        // ── 2. English label search ────────────────────────────────────────
        List<Icd10Rubric> engResults = rubricRepo.searchByLabel(q, offset, size);
        total += rubricRepo.countByLabel(q);

        List<String> engCodes = engResults.stream().map(Icd10Rubric::getCode).collect(Collectors.toList());
        // key: code → [korean_label, is_kcd_ext]
        Map<String, Object[]> classInfo = new HashMap<>();
        if (!engCodes.isEmpty()) {
            for (Object[] row : rubricRepo.findKoreanLabelsByCodes(engCodes)) {
                if (row[0] != null) classInfo.put(row[0].toString(), row);
            }
        }
        for (Icd10Rubric r : engResults) {
            if (merged.containsKey(r.getCode())) continue;
            Object[] info = classInfo.get(r.getCode());
            String korean = info != null && info[1] != null ? info[1].toString() : null;
            Boolean ext = info != null && info[2] != null ? (Boolean) info[2] : Boolean.FALSE;
            merged.put(r.getCode(), new Icd10SearchResultDTO(
                r.getCode(), r.getKind(), r.getLabel(), korean, ext));
        }

        // ── 3. Korean label search (ICD10_CLASS — covers KCD-9 ext too) ───
        List<Icd10Class> korResults = classRepo.searchByKoreanLabel(q, offset, size);
        total += classRepo.countByKoreanLabel(q);
        for (Icd10Class c : korResults) {
            if (!merged.containsKey(c.getCode())) {
                merged.put(c.getCode(), new Icd10SearchResultDTO(
                    c.getCode(), c.getClassKind(), c.getLabel(), c.getKoreanLabel(), c.getIsKcdExt()));
            }
        }

        List<Icd10SearchResultDTO> dtos = new ArrayList<>(merged.values());
        return new PageImpl<>(dtos, new PageRequest(page - 1, size), total);
    }
}
