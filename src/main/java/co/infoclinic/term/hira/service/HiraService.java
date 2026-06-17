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

    // ─── 행위 장/절 타이틀 ────────────────────────────────────────────────────
    private static final Map<String, String> 장타이틀 = new LinkedHashMap<String, String>() {{
        put("01", "기본진료료");
        put("02", "검사료");
        put("03", "영상진단 및 방사선치료료");
        put("04", "투약 및 조제료");
        put("05", "주사료");
        put("06", "마취료");
        put("07", "이학요법료");
        put("08", "정신요법료");
        put("09", "처치 및 수술료 등");
        put("10", "치과 처치·수술료");
        put("11", "조산료");
        put("12", "보건기관의 진료수가");
        put("13", "한방 검사료");
        put("14", "한방 시술 및 처치료");
        put("15", "약국 약제비");
        put("16", "전혈 및 혈액성분제제료");
        put("17", "입원환자 식대");
        put("18", "치과의 보철료");
        put("19", "응급의료수가");
        put("19-2", "권역외상센터 검사료");
        put("19-3", "권역외상센터 영상진단료");
        put("19-5", "권역외상센터 주사료");
        put("19-6", "권역외상센터 마취료");
        put("19-8", "권역외상센터 정신요법료");
        put("19-9", "권역외상센터 처치 및 수술료");
        put("20", "치과의 교정치료료");
        put("00", "비급여");
    }};

    private static final Map<String, String> 절타이틀 = new LinkedHashMap<String, String>() {{
        put("01|01", "기본진료료");
        put("01|02", "통합관리료");
        put("02|01", "검체 검사료");
        put("02|02", "병리 검사료");
        put("02|03", "기능 검사료");
        put("02|04", "내시경, 천자 및 생검료");
        put("02|05", "초음파 검사료");
        put("03|01", "방사선 일반영상진단료");
        put("03|02", "방사선 특수영상진단료");
        put("03|03", "핵의학영상진단 및 골밀도검사료");
        put("03|04", "방사선 치료료");
        put("05|01", "주사료");
        put("05|02", "채혈 및 수혈료");
        put("06|01", "마취료");
        put("06|02", "치과마취료");
        put("06|03", "신경차단술료");
        put("06|04", "신경파괴술료");
        put("07|01", "기본물리치료료");
        put("07|02", "단순재활치료료");
        put("07|03", "전문재활치료료");
        put("07|04", "기타 이학요법료");
        put("09|01", "처치 및 수술료");
        put("09|02", "캐스트료");
        put("10|01", "치아질환 처치");
        put("10|02", "수술 후 처치·치주조직의 처치 등");
        put("10|03", "구강악안면 수술");
        put("10|04", "치주질환 수술");
        put("10|05", "보철물의 유지관리");
        put("14|01", "시술료");
        put("14|02", "처치료");
        put("14|03", "한방 정신요법료");
        put("19|01", "응급 기본진료료");
        put("19|02", "응급의료행위");
        put("19|03", "권역외상센터 응급의료행위");
        put("19-2|03", "기능 검사료");
        put("19-2|04", "내시경, 천자 및 생검료");
        put("19-3|02", "방사선 특수영상진단료");
        put("19-5|01", "주사료");
        put("19-6|01", "마취료");
        put("19-9|01", "처치 및 수술료");
        put("19-9|02", "캐스트료");
    }};

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
            String title = 장타이틀.get(raw);
            String label;
            if (raw.startsWith("(")) {
                label = raw;
            } else if (title != null) {
                label = raw + "장 " + title;
            } else {
                label = raw + "장";
            }
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
            String title = 절타이틀.get(jang + "|" + raw);
            String label;
            if (raw.startsWith("(")) {
                label = raw;
            } else if (title != null) {
                label = raw + "절 " + title;
            } else {
                label = raw + "절";
            }
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

    // ─── 약제 ATC 트리 ────────────────────────────────────────────────────────
    private int atcNextLen(int len) {
        if (len == 0) return 1;
        if (len == 1) return 3;
        if (len == 3) return 4;
        if (len == 4) return 5;
        if (len == 5) return 7;
        return -1;
    }

    private String buildAtcLabel(String code, String hname, String ename) {
        boolean hasH = hname != null && !hname.isEmpty() && !hname.equals(code);
        boolean hasE = ename != null && !ename.isEmpty() && !ename.equals(code);
        // format: "code\thname\tename" — frontend splits on \t to render ename smaller
        String h = hasH ? hname : "";
        String e = hasE ? ename : "";
        return code + "\t" + h + "\t" + e;
    }

    public List<Map<String, Object>> get약제ATCRoot() {
        String sql = "SELECT g.code, m.atc_hname, m.atc_name, g.cnt"
                   + " FROM ("
                   + "   SELECT SUBSTRING(atc_code, 1, 1) as code, COUNT(DISTINCT 제품코드) as cnt"
                   + "   FROM term.hira_atc_map WHERE LENGTH(atc_code) >= 1"
                   + "   GROUP BY SUBSTRING(atc_code, 1, 1)"
                   + " ) g"
                   + " LEFT JOIN term.hira_atc_master m ON m.atc_code = g.code"
                   + " ORDER BY g.code";
        Query q = em.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            String code = r[0].toString();
            String hname = r[1] != null ? r[1].toString() : null;
            String ename = r[2] != null ? r[2].toString() : null;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", code); m.put("label", buildAtcLabel(code, hname, ename));
            m.put("type", "group"); m.put("childCount", ((Number) r[3]).intValue());
            result.add(m);
        }
        return result;
    }

    public List<Map<String, Object>> get약제ATCChildren(String prefix) {
        int len = prefix.length();
        int nextLen = atcNextLen(len);
        List<Map<String, Object>> result = new ArrayList<>();

        // 하위 ATC 그룹 (nextLen 길이)
        if (nextLen > 0) {
            String sql = "SELECT g.code, m.atc_hname, COALESCE(m.atc_name, g.map_name) as ename, g.cnt"
                       + " FROM ("
                       + "   SELECT SUBSTRING(atc_code, 1, " + nextLen + ") as code,"
                       + "   MAX(CASE WHEN LENGTH(atc_code) = " + nextLen + " THEN atc_name END) as map_name,"
                       + "   COUNT(DISTINCT 제품코드) as cnt"
                       + "   FROM term.hira_atc_map"
                       + "   WHERE atc_code LIKE ?1 AND LENGTH(atc_code) >= " + nextLen
                       + "   GROUP BY SUBSTRING(atc_code, 1, " + nextLen + ")"
                       + " ) g"
                       + " LEFT JOIN term.hira_atc_master m ON m.atc_code = g.code"
                       + " ORDER BY g.code";
            Query q = em.createNativeQuery(sql);
            q.setParameter(1, prefix + "%");
            @SuppressWarnings("unchecked")
            List<Object[]> rows = q.getResultList();
            for (Object[] r : rows) {
                String code = r[0].toString();
                String hname = r[1] != null ? r[1].toString() : null;
                String ename = r[2] != null ? r[2].toString() : null;
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("code", code);
                m.put("label", buildAtcLabel(code, hname, ename));
                m.put("type", "group"); m.put("childCount", ((Number) r[3]).intValue());
                result.add(m);
            }
        }

        // 이 ATC 코드에 직접 매핑된 제품 (leaf)
        String leafSql = "SELECT DISTINCT 제품코드, 제품명 FROM term.hira_atc_map"
                       + " WHERE atc_code = ?1 ORDER BY 제품코드";
        Query lq = em.createNativeQuery(leafSql);
        lq.setParameter(1, prefix);
        @SuppressWarnings("unchecked")
        List<Object[]> leaves = lq.getResultList();
        for (Object[] r : leaves) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", r[0]); m.put("label", r[1]);
            m.put("type", "leaf"); m.put("childCount", 0);
            result.add(m);
        }
        return result;
    }

    public Map<String, Object> get약제ATCDetail(String 제품코드) {
        // ATC 정보 + 최신 약가 정보
        String sql = "SELECT a.제품코드, a.제품명, a.업체명, a.식약분류, a.주성분코드,"
                   + " STRING_AGG(DISTINCT a.atc_code || '|' || COALESCE(a.atc_name,'') || '|' || COALESCE(m.atc_hname,''), ',' ORDER BY a.atc_code || '|' || COALESCE(a.atc_name,'') || '|' || COALESCE(m.atc_hname,'')) as atc_list,"
                   + " MAX(CASE WHEN d.적용시작일자 = latest.mx THEN d.급여기준 END) as 급여기준,"
                   + " MAX(CASE WHEN d.적용시작일자 = latest.mx THEN d.상한가 END) as 상한가,"
                   + " MAX(CASE WHEN d.적용시작일자 = latest.mx THEN d.투여경로 END) as 투여경로,"
                   + " MAX(CASE WHEN d.적용시작일자 = latest.mx THEN d.규격 END) as 규격,"
                   + " MAX(CASE WHEN d.적용시작일자 = latest.mx THEN d.단위 END) as 단위,"
                   + " MAX(CASE WHEN d.적용시작일자 = latest.mx THEN d.전문일반 END) as 전문일반,"
                   + " MAX(CASE WHEN d.적용시작일자 = latest.mx THEN d.적용시작일자 END) as 적용일자"
                   + " FROM term.hira_atc_map a"
                   + " LEFT JOIN term.hira_atc_master m ON a.atc_code = m.atc_code"
                   + " LEFT JOIN term.hira_약제_code d ON a.제품코드 = d.제품코드"
                   + " LEFT JOIN (SELECT 제품코드, MAX(적용시작일자) as mx FROM term.hira_약제_code GROUP BY 제품코드) latest"
                   + "   ON d.제품코드 = latest.제품코드"
                   + " WHERE a.제품코드 = ?1"
                   + " GROUP BY a.제품코드, a.제품명, a.업체명, a.식약분류, a.주성분코드";
        Query q = em.createNativeQuery(sql);
        q.setParameter(1, 제품코드);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        if (rows.isEmpty()) return Collections.emptyMap();
        Object[] r = rows.get(0);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("code", r[0]); m.put("name", r[1]); m.put("company", r[2]);
        m.put("drugClass", r[3]); m.put("ingredient", r[4]);
        List<Map<String, Object>> atcList = new ArrayList<>();
        if (r[5] != null) {
            for (String entry : r[5].toString().split(",")) {
                String[] parts = entry.split("\\|", 3);
                if (parts.length >= 2) {
                    Map<String, Object> a = new LinkedHashMap<>();
                    a.put("code", parts[0]);
                    String hname = parts.length == 3 && !parts[2].isEmpty() ? parts[2] : null;
                    a.put("name", hname != null ? hname : parts[1]);
                    a.put("englishName", parts[1]);
                    atcList.add(a);
                }
            }
        }
        m.put("atcList", atcList);
        m.put("benefit", r[6]); m.put("price", r[7]);
        m.put("route", r[8]); m.put("spec", r[9]); m.put("unit", r[10]);
        m.put("type", r[11]); m.put("applyDate", r[12]);

        // 가격 이력 (약제_code)
        String histSql = "SELECT 적용시작일자, 급여기준, 상한가 FROM term.hira_약제_code"
                       + " WHERE 제품코드 = ?1 ORDER BY 적용시작일자 DESC";
        Query hq = em.createNativeQuery(histSql);
        hq.setParameter(1, 제품코드);
        @SuppressWarnings("unchecked")
        List<Object[]> hist = hq.getResultList();
        List<Map<String, Object>> history = new ArrayList<>();
        for (Object[] h : hist) {
            Map<String, Object> hm = new LinkedHashMap<>();
            hm.put("applyDate", h[0]); hm.put("benefit", h[1]); hm.put("price", h[2]);
            history.add(hm);
        }
        m.put("priceHistory", history);
        return m;
    }

    public Map<String, Object> search약제ATC(String q, int page, int size) {
        int offset = (page - 1) * size;
        String sql = "SELECT DISTINCT 제품코드, 제품명, 업체명, atc_code, atc_name"
                   + " FROM term.hira_atc_map"
                   + " WHERE 제품코드 ILIKE '%' || ?1 || '%'"
                   + "    OR 제품명 ILIKE '%' || ?1 || '%'"
                   + "    OR atc_code ILIKE '%' || ?1 || '%'"
                   + "    OR atc_name ILIKE '%' || ?1 || '%'"
                   + " ORDER BY 제품코드 LIMIT ?2 OFFSET ?3";
        String cntSql = "SELECT COUNT(DISTINCT 제품코드) FROM term.hira_atc_map"
                      + " WHERE 제품코드 ILIKE '%' || ?1 || '%'"
                      + "    OR 제품명 ILIKE '%' || ?1 || '%'"
                      + "    OR atc_code ILIKE '%' || ?1 || '%'"
                      + "    OR atc_name ILIKE '%' || ?1 || '%'";
        Query dq = em.createNativeQuery(sql);
        dq.setParameter(1, q); dq.setParameter(2, size); dq.setParameter(3, offset);
        Query cq = em.createNativeQuery(cntSql);
        cq.setParameter(1, q);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = dq.getResultList();
        long total = ((Number) cq.getSingleResult()).longValue();
        List<Map<String, Object>> items = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("code", r[0]); row.put("name", r[1]); row.put("company", r[2]);
            row.put("atcCode", r[3]); row.put("atcName", r[4]);
            items.add(row);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total); result.put("items", items);
        return result;
    }

    // ─── 약제 ─────────────────────────────────────────────────────────────────
    public List<Map<String, Object>> get약제TreeRoot() {
        String sql = "SELECT 주성분명, COUNT(DISTINCT 제품코드) as cnt"
                   + " FROM term.hira_약제_code GROUP BY 주성분명 ORDER BY 주성분명";
        Query q = em.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", r[0]); m.put("label", r[0] != null ? r[0].toString() : "(성분명없음)");
            m.put("type", "group"); m.put("childCount", ((Number) r[1]).intValue());
            result.add(m);
        }
        return result;
    }

    public List<Map<String, Object>> get약제TreeByDiv(String ingName) {
        String sql = "SELECT 제품명_기본, COUNT(DISTINCT 제품코드) as cnt"
                   + " FROM term.hira_약제_code WHERE 주성분명 = ?1"
                   + " GROUP BY 제품명_기본 ORDER BY 제품명_기본";
        Query q = em.createNativeQuery(sql);
        q.setParameter(1, ingName);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            String productBase = r[0] != null ? r[0].toString() : "(제품명없음)";
            m.put("code", ingName + "|" + productBase);
            m.put("label", productBase);
            m.put("type", "group"); m.put("childCount", ((Number) r[1]).intValue());
            result.add(m);
        }
        return result;
    }

    public List<Map<String, Object>> get약제TreeByProduct(String ingName, String productBase) {
        String sql = "SELECT DISTINCT ON (제품코드) 제품코드, 제품명, 상한가, 적용시작일자"
                   + " FROM term.hira_약제_code WHERE 주성분명 = ?1 AND 제품명_기본 = ?2"
                   + " ORDER BY 제품코드, 적용시작일자 DESC";
        Query q = em.createNativeQuery(sql);
        q.setParameter(1, ingName); q.setParameter(2, productBase);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", r[0]); m.put("label", r[1]);
            m.put("koreanLabel", r[1]); m.put("price", r[2]);
            m.put("type", "leaf"); m.put("childCount", 0);
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
        String sql = "SELECT 시트명, COUNT(*) as cnt"
                   + " FROM term.hira_치료재료_code"
                   + " GROUP BY 시트명 ORDER BY 시트명";
        Query q = em.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", r[0]); m.put("label", r[0].toString());
            m.put("type", "group"); m.put("childCount", ((Number) r[1]).intValue());
            result.add(m);
        }
        return result;
    }

    public List<Map<String, Object>> get치료재료TreeBySheet(String sheet) {
        String sql = "SELECT 중분류코드, MIN(중분류) as 중분류명, COUNT(*) as cnt"
                   + " FROM term.hira_치료재료_code WHERE 시트명 = ?1"
                   + " GROUP BY 중분류코드 ORDER BY 중분류코드";
        Query q = em.createNativeQuery(sql);
        q.setParameter(1, sheet);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", sheet + "|" + r[0]);
            m.put("label", r[0] + " " + r[1]);
            m.put("type", "group"); m.put("childCount", ((Number) r[2]).intValue());
            result.add(m);
        }
        return result;
    }

    public List<Map<String, Object>> get치료재료TreeByMid(String sheet, String midCode) {
        String sql = "SELECT 코드, 품명, 규격, 단위, 상한금액, 급여구분"
                   + " FROM term.hira_치료재료_code WHERE 시트명 = ?1 AND 중분류코드 = ?2 ORDER BY 코드";
        Query q = em.createNativeQuery(sql);
        q.setParameter(1, sheet);
        q.setParameter(2, midCode);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", r[0]); m.put("label", r[1] != null ? r[1].toString() : "");
            m.put("spec", r[2]); m.put("unit", r[3]);
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
        // 중간노드(중분류) + leaf(품목) UNION
        String sql =
            "SELECT 'group' as type, 시트명 || '|' || 중분류코드 as code,"
          + "       중분류코드 || ' ' || MIN(중분류) as name,"
          + "       MIN(시트명) as sheet, 중분류코드 as mid_code, CAST(NULL AS numeric) as price, COUNT(*) as cnt"
          + " FROM term.hira_치료재료_code"
          + " WHERE 중분류코드 ILIKE '%' || ?1 || '%' OR 중분류 ILIKE '%' || ?1 || '%'"
          + " GROUP BY 시트명, 중분류코드"
          + " UNION ALL"
          + " SELECT 'leaf' as type, 코드 as code, 품명 as name,"
          + "        시트명 as sheet, 중분류코드 as mid_code, 상한금액 as price, CAST(NULL AS bigint) as cnt"
          + " FROM term.hira_치료재료_code"
          + " WHERE 코드 ILIKE '%' || ?1 || '%' OR 품명 ILIKE '%' || ?1 || '%'"
          + " ORDER BY type, code"
          + " LIMIT ?2 OFFSET ?3";
        String cntSql =
            "SELECT ("
          + "  SELECT COUNT(DISTINCT 시트명 || '|' || 중분류코드) FROM term.hira_치료재료_code"
          + "  WHERE 중분류코드 ILIKE '%' || ?1 || '%' OR 중분류 ILIKE '%' || ?1 || '%'"
          + ") + ("
          + "  SELECT COUNT(*) FROM term.hira_치료재료_code"
          + "  WHERE 코드 ILIKE '%' || ?1 || '%' OR 품명 ILIKE '%' || ?1 || '%'"
          + ")";
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
            m.put("type", r[0]); m.put("code", r[1]); m.put("name", r[2]);
            m.put("sheet", r[3]); m.put("midCode", r[4]); m.put("price", r[5]);
            if ("group".equals(r[0].toString())) m.put("childCount", ((Number) r[6]).intValue());
            items.add(m);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total); result.put("items", items);
        return result;
    }
}
