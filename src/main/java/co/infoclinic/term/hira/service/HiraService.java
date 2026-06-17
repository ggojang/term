package co.infoclinic.term.hira.service;

import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.springframework.stereotype.Service;

@Service
public class HiraService {

    @PersistenceContext
    private EntityManager em;

    // ─── 행위 ─────────────────────────────────────────────────────────────────
    public List<Map<String, Object>> get행위TreeRoot() {
        String sql = "SELECT 시트구분, COUNT(*) as cnt FROM term.hira_행위_code GROUP BY 시트구분 ORDER BY 시트구분";
        Query q = em.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", r[0]); m.put("label", r[0]); m.put("type", "group");
            m.put("childCount", ((Number) r[1]).intValue());
            result.add(m);
        }
        return result;
    }

    public List<Map<String, Object>> get행위TreeBySheet(String sheet) {
        String sql = "SELECT COALESCE(NULLIF(장구분,''), '(기타)') as jang, COUNT(*) as cnt"
                   + " FROM term.hira_행위_code WHERE 시트구분 = ?1"
                   + " GROUP BY COALESCE(NULLIF(장구분,''), '(기타)') ORDER BY jang";
        Query q = em.createNativeQuery(sql);
        q.setParameter(1, sheet);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            String raw = r[0].toString();
            String label = raw.matches("\\d+") ? raw + "장" : raw;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", sheet + "|" + raw); m.put("label", label); m.put("type", "group");
            m.put("childCount", ((Number) r[1]).intValue());
            result.add(m);
        }
        return result;
    }

    public List<Map<String, Object>> get행위TreeByJang(String sheet, String jang) {
        String sql = "SELECT COALESCE(NULLIF(절구분,''), '(직접)') as jeol, COUNT(*) as cnt"
                   + " FROM term.hira_행위_code"
                   + " WHERE 시트구분 = ?1 AND COALESCE(NULLIF(장구분,''),'(기타)') = ?2"
                   + " GROUP BY COALESCE(NULLIF(절구분,''), '(직접)') ORDER BY jeol";
        Query q = em.createNativeQuery(sql);
        q.setParameter(1, sheet); q.setParameter(2, jang);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            String raw = r[0].toString();
            String label = raw.matches("\\d+") ? raw + "절" : raw;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", sheet + "|" + jang + "|" + raw); m.put("label", label); m.put("type", "group");
            m.put("childCount", ((Number) r[1]).intValue());
            result.add(m);
        }
        return result;
    }

    public List<Map<String, Object>> get행위TreeByJeol(String sheet, String jang, String jeol) {
        String sql = "SELECT COALESCE(NULLIF(세분류,''), '(세분류없음)') as sedo, COUNT(*) as cnt"
                   + " FROM term.hira_행위_code"
                   + " WHERE 시트구분 = ?1"
                   + "   AND COALESCE(NULLIF(장구분,''),'(기타)') = ?2"
                   + "   AND COALESCE(NULLIF(절구분,''),'(직접)') = ?3"
                   + " GROUP BY sedo ORDER BY sedo";
        Query q = em.createNativeQuery(sql);
        q.setParameter(1, sheet); q.setParameter(2, jang); q.setParameter(3, jeol);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", sheet + "|" + jang + "|" + jeol + "|" + r[0]);
            m.put("label", r[0]); m.put("type", "group");
            m.put("childCount", ((Number) r[1]).intValue());
            result.add(m);
        }
        return result;
    }

    public List<Map<String, Object>> get행위TreeBySedo(String sheet, String jang, String jeol, String sedo) {
        String sql = "SELECT COALESCE(NULLIF(분류번호,''), '(분류없음)') as classno, COUNT(*) as cnt"
                   + " FROM term.hira_행위_code"
                   + " WHERE 시트구분 = ?1"
                   + "   AND COALESCE(NULLIF(장구분,''),'(기타)') = ?2"
                   + "   AND COALESCE(NULLIF(절구분,''),'(직접)') = ?3"
                   + "   AND COALESCE(NULLIF(세분류,''),'(세분류없음)') = ?4"
                   + " GROUP BY classno ORDER BY classno";
        Query q = em.createNativeQuery(sql);
        q.setParameter(1, sheet); q.setParameter(2, jang); q.setParameter(3, jeol); q.setParameter(4, sedo);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", sheet + "|" + jang + "|" + jeol + "|" + sedo + "|" + r[0]);
            m.put("label", r[0]); m.put("type", "group");
            m.put("childCount", ((Number) r[1]).intValue());
            result.add(m);
        }
        return result;
    }

    public List<Map<String, Object>> get행위TreeByClassNo(String sheet, String jang, String jeol, String sedo, String classNo) {
        String sql = "SELECT 수가코드, 한글명, 영문명, 의원단가"
                   + " FROM term.hira_행위_code"
                   + " WHERE 시트구분 = ?1"
                   + "   AND COALESCE(NULLIF(장구분,''),'(기타)') = ?2"
                   + "   AND COALESCE(NULLIF(절구분,''),'(직접)') = ?3"
                   + "   AND COALESCE(NULLIF(세분류,''),'(세분류없음)') = ?4"
                   + "   AND COALESCE(NULLIF(분류번호,''),'(분류없음)') = ?5"
                   + " ORDER BY 수가코드 LIMIT 500";
        Query q = em.createNativeQuery(sql);
        q.setParameter(1, sheet); q.setParameter(2, jang); q.setParameter(3, jeol);
        q.setParameter(4, sedo); q.setParameter(5, classNo);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", r[0]); m.put("label", r[0] + " " + (r[1] != null ? r[1] : ""));
            m.put("koreanLabel", r[1]); m.put("englishLabel", r[2]);
            m.put("price", r[3]); m.put("type", "leaf"); m.put("childCount", 0);
            result.add(m);
        }
        return result;
    }

    public Map<String, Object> get행위Detail(String code) {
        String sql = "SELECT 수가코드, 적용일자, 분류번호, 한글명, 영문명, 구분, 수술여부,"
                   + " 의원단가, 병원단가, 상대가치점수, 시트구분, 장구분, 절구분, 세분류, 산정명칭"
                   + " FROM term.hira_행위_code WHERE 수가코드 = ?1";
        Query q = em.createNativeQuery(sql);
        q.setParameter(1, code);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        if (rows.isEmpty()) return Collections.emptyMap();
        Object[] r = rows.get(0);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("code", r[0]); m.put("applyDate", r[1]); m.put("classNo", r[2]);
        m.put("koreanLabel", r[3]); m.put("englishLabel", r[4]); m.put("division", r[5]);
        m.put("surgery", r[6]); m.put("clinicPrice", r[7]); m.put("hospitalPrice", r[8]);
        m.put("rvuPoint", r[9]); m.put("sheetType", r[10]); m.put("chapter", r[11]);
        m.put("section", r[12]); m.put("detail", r[13]); m.put("calcName", r[14]);
        return m;
    }

    public Map<String, Object> search행위(String q, int page, int size) {
        int offset = (page - 1) * size;
        String sql = "SELECT 수가코드, 한글명, 영문명, 의원단가, 시트구분, 분류번호"
                   + " FROM term.hira_행위_code"
                   + " WHERE 수가코드 ILIKE '%' || ?1 || '%'"
                   + "    OR 한글명 ILIKE '%' || ?1 || '%'"
                   + "    OR 영문명 ILIKE '%' || ?1 || '%'"
                   + "    OR 분류번호 ILIKE '%' || ?1 || '%'"
                   + " ORDER BY 수가코드 LIMIT ?2 OFFSET ?3";
        String cntSql = "SELECT COUNT(*) FROM term.hira_행위_code"
                      + " WHERE 수가코드 ILIKE '%' || ?1 || '%'"
                      + "    OR 한글명 ILIKE '%' || ?1 || '%'"
                      + "    OR 영문명 ILIKE '%' || ?1 || '%'"
                      + "    OR 분류번호 ILIKE '%' || ?1 || '%'";
        Query dq = em.createNativeQuery(sql);
        dq.setParameter(1, q); dq.setParameter(2, size); dq.setParameter(3, offset);
        Query cq = em.createNativeQuery(cntSql);
        cq.setParameter(1, q);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = dq.getResultList();
        long total = ((Number) cq.getSingleResult()).longValue();
        List<Map<String, Object>> items = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", r[0]); m.put("koreanLabel", r[1]); m.put("englishLabel", r[2]);
            m.put("price", r[3]); m.put("sheetType", r[4]); m.put("classNo", r[5]);
            items.add(m);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total); result.put("items", items);
        return result;
    }

    // ─── 약제 ─────────────────────────────────────────────────────────────────
    public List<Map<String, Object>> get약제TreeRoot() {
        String sql = "SELECT 분류번호, COUNT(DISTINCT 제품코드) as cnt"
                   + " FROM term.hira_약제_code GROUP BY 분류번호 ORDER BY 분류번호";
        Query q = em.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", r[0]); m.put("label", "[" + r[0] + "] 약효분류");
            m.put("type", "group"); m.put("childCount", ((Number) r[1]).intValue());
            result.add(m);
        }
        return result;
    }

    public List<Map<String, Object>> get약제TreeByDiv(String divCode) {
        String sql = "SELECT DISTINCT ON (제품코드) 제품코드, 제품명, 규격, 단위, 상한가, 적용시작일자"
                   + " FROM term.hira_약제_code WHERE 분류번호 = ?1"
                   + " ORDER BY 제품코드, 적용시작일자 DESC";
        Query q = em.createNativeQuery(sql);
        q.setParameter(1, divCode);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", r[0]); m.put("label", r[0] + " " + r[1]);
            m.put("koreanLabel", r[1]); m.put("spec", r[2]); m.put("unit", r[3]);
            m.put("price", r[4]); m.put("type", "leaf"); m.put("childCount", 0);
            result.add(m);
        }
        return result;
    }

    public Map<String, Object> get약제Detail(String code) {
        String sql = "SELECT 제품코드, 적용시작일자, 급여기준, 상한가, 투여경로, 제품명,"
                   + " 규격, 단위, 업체명, 분류번호, 주성분코드, 전문일반"
                   + " FROM term.hira_약제_code WHERE 제품코드 = ?1 ORDER BY 적용시작일자 DESC";
        Query q = em.createNativeQuery(sql);
        q.setParameter(1, code);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        if (rows.isEmpty()) return Collections.emptyMap();
        Object[] r = rows.get(0);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("code", r[0]); m.put("applyDate", r[1]); m.put("benefit", r[2]);
        m.put("price", r[3]); m.put("route", r[4]); m.put("name", r[5]);
        m.put("spec", r[6]); m.put("unit", r[7]); m.put("company", r[8]);
        m.put("classNo", r[9]); m.put("ingredient", r[10]); m.put("type", r[11]);
        List<Map<String, Object>> history = new ArrayList<>();
        for (Object[] hr : rows) {
            Map<String, Object> hm = new LinkedHashMap<>();
            hm.put("applyDate", hr[1]); hm.put("price", hr[3]); hm.put("benefit", hr[2]);
            history.add(hm);
        }
        m.put("priceHistory", history);
        return m;
    }

    public Map<String, Object> search약제(String q, int page, int size) {
        int offset = (page - 1) * size;
        String sql = "SELECT DISTINCT ON (제품코드) 제품코드, 제품명, 규격, 단위, 상한가, 분류번호, 업체명"
                   + " FROM term.hira_약제_code"
                   + " WHERE 제품코드 ILIKE '%' || ?1 || '%'"
                   + "    OR 제품명 ILIKE '%' || ?1 || '%'"
                   + "    OR 주성분코드 ILIKE '%' || ?1 || '%'"
                   + " ORDER BY 제품코드, 적용시작일자 DESC LIMIT ?2 OFFSET ?3";
        String cntSql = "SELECT COUNT(DISTINCT 제품코드) FROM term.hira_약제_code"
                      + " WHERE 제품코드 ILIKE '%' || ?1 || '%'"
                      + "    OR 제품명 ILIKE '%' || ?1 || '%'"
                      + "    OR 주성분코드 ILIKE '%' || ?1 || '%'";
        Query dq = em.createNativeQuery(sql);
        dq.setParameter(1, q); dq.setParameter(2, size); dq.setParameter(3, offset);
        Query cq = em.createNativeQuery(cntSql);
        cq.setParameter(1, q);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = dq.getResultList();
        long total = ((Number) cq.getSingleResult()).longValue();
        List<Map<String, Object>> items = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", r[0]); m.put("name", r[1]); m.put("spec", r[2]);
            m.put("unit", r[3]); m.put("price", r[4]); m.put("classNo", r[5]);
            m.put("company", r[6]);
            items.add(m);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total); result.put("items", items);
        return result;
    }

    // ─── 치료재료 ─────────────────────────────────────────────────────────────
    public List<Map<String, Object>> get치료재료TreeRoot() {
        String sql = "SELECT SUBSTRING(코드, 1, 1) as major, COUNT(*) as cnt"
                   + " FROM term.hira_치료재료_code GROUP BY SUBSTRING(코드, 1, 1) ORDER BY major";
        Query q = em.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", r[0]); m.put("label", r[0] + " 군");
            m.put("type", "group"); m.put("childCount", ((Number) r[1]).intValue());
            result.add(m);
        }
        return result;
    }

    public List<Map<String, Object>> get치료재료TreeByMajor(String major) {
        String sql = "SELECT 중분류코드, MIN(중분류) as 중분류명, COUNT(*) as cnt"
                   + " FROM term.hira_치료재료_code WHERE SUBSTRING(코드, 1, 1) = ?1"
                   + " GROUP BY 중분류코드 ORDER BY 중분류코드";
        Query q = em.createNativeQuery(sql);
        q.setParameter(1, major);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", major + "|" + r[0]);
            m.put("label", "[" + r[0] + "] " + r[1]);
            m.put("type", "group"); m.put("childCount", ((Number) r[2]).intValue());
            result.add(m);
        }
        return result;
    }

    public List<Map<String, Object>> get치료재료TreeByMid(String midCode) {
        String sql = "SELECT 코드, 품명, 규격, 단위, 상한금액, 급여구분"
                   + " FROM term.hira_치료재료_code WHERE 중분류코드 = ?1 ORDER BY 코드 LIMIT 1000";
        Query q = em.createNativeQuery(sql);
        q.setParameter(1, midCode);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", r[0]); m.put("label", r[0] + " " + (r[1] != null ? r[1] : ""));
            m.put("koreanLabel", r[1]); m.put("spec", r[2]); m.put("unit", r[3]);
            m.put("price", r[4]); m.put("benefit", r[5]);
            m.put("type", "leaf"); m.put("childCount", 0);
            result.add(m);
        }
        return result;
    }

    public Map<String, Object> get치료재료Detail(String code) {
        String sql = "SELECT 코드, 최초등재일, 적용일자, 종료일자, 중분류, 중분류코드,"
                   + " 품명, 규격, 단위, 상한금액, 제조회사, 재질, 급여구분"
                   + " FROM term.hira_치료재료_code WHERE 코드 = ?1";
        Query q = em.createNativeQuery(sql);
        q.setParameter(1, code);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        if (rows.isEmpty()) return Collections.emptyMap();
        Object[] r = rows.get(0);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("code", r[0]); m.put("firstDate", r[1]); m.put("applyDate", r[2]);
        m.put("endDate", r[3]); m.put("midClass", r[4]); m.put("midCode", r[5]);
        m.put("name", r[6]); m.put("spec", r[7]); m.put("unit", r[8]);
        m.put("price", r[9]); m.put("manufacturer", r[10]); m.put("material", r[11]);
        m.put("benefit", r[12]);
        return m;
    }

    public Map<String, Object> search치료재료(String q, int page, int size) {
        int offset = (page - 1) * size;
        String sql = "SELECT 코드, 품명, 규격, 단위, 상한금액, 중분류, 급여구분"
                   + " FROM term.hira_치료재료_code"
                   + " WHERE 코드 ILIKE '%' || ?1 || '%'"
                   + "    OR 품명 ILIKE '%' || ?1 || '%'"
                   + "    OR 중분류 ILIKE '%' || ?1 || '%'"
                   + " ORDER BY 코드 LIMIT ?2 OFFSET ?3";
        String cntSql = "SELECT COUNT(*) FROM term.hira_치료재료_code"
                      + " WHERE 코드 ILIKE '%' || ?1 || '%'"
                      + "    OR 품명 ILIKE '%' || ?1 || '%'"
                      + "    OR 중분류 ILIKE '%' || ?1 || '%'";
        Query dq = em.createNativeQuery(sql);
        dq.setParameter(1, q); dq.setParameter(2, size); dq.setParameter(3, offset);
        Query cq = em.createNativeQuery(cntSql);
        cq.setParameter(1, q);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = dq.getResultList();
        long total = ((Number) cq.getSingleResult()).longValue();
        List<Map<String, Object>> items = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", r[0]); m.put("name", r[1]); m.put("spec", r[2]);
            m.put("unit", r[3]); m.put("price", r[4]); m.put("midClass", r[5]);
            m.put("benefit", r[6]);
            items.add(m);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total); result.put("items", items);
        return result;
    }
}
