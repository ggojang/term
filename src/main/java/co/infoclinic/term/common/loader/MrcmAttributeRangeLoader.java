package co.infoclinic.term.common.loader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MRCM Attribute Range Snapshot → MRCM_CONSTRAINTS 테이블 적재기
 *
 * 입력 파일 (RF2 탭 구분):
 *   der2_ssccRefset_MRCMAttributeRangeSnapshot_INT_*.txt
 *
 * RF2 컬럼 구조 (0-based):
 *   [5] referencedComponentId → attribute_id
 *   [6] rangeConstraint       → value_id, value_name (ECL 첫 번째 SCTID)
 *   [7] attributeRule         → source_id, source_name, attribute_name, cardinality
 *
 * attributeRule은 세 가지 구조를 가진다:
 *
 *   [단일 소스]
 *     << S |N|: [card] { [c] A |N| = range }
 *     << S |N|: [card] A |N| = range           (중괄호 없는 단순형)
 *
 *   [Type 1 - 다중 소스, 공통 규칙]
 *     (<< S1 |N1| OR << S2 |N2|): [card] { rule }
 *     → S1, S2 각각 별도 행으로 분리
 *
 *   [Type 2 - OR 연결 독립 규칙]
 *     (<< S1 |N1|: [card1] { rule1 }) OR (<< S2 |N2|: [card2] { rule2 })
 *     → 각 OR 블록을 별도 행으로 분리
 *
 * 이름(FSN)은 ECL에 |인라인|으로 포함되어 DB 조회 없이 파싱 가능.
 */
public class MrcmAttributeRangeLoader {

    private static final String JDBC_URL      = "jdbc:postgresql://localhost:5432/term";
    private static final String JDBC_USER     = "postgres";
    private static final String JDBC_PASSWORD = "julab123!";

    private static final int BATCH_SIZE = 500;

    /** propagateIsaInheritance()에서 사용할 MRCM Domain 파일 경로 (main에서 설정) */
    private static Path domainFilePath = null;

    /** ECL 내 <<(또는 <) SCTID |Name| 패턴 */
    private static final Pattern SCTID_NAME = Pattern.compile(
            "<?<\\s*(\\d+)\\s*\\|([^|]+)\\|");

    /** attributeRule 내 도메인 추출: ':' 바로 앞까지의 첫 번째 << SCTID |Name| */
    private static final Pattern SOURCE_BEFORE_COLON = Pattern.compile(
            "<?<\\s*(\\d+)\\s*\\|([^|]+)\\|\\s*:");

    /** ':' 바로 뒤 카디널리티 [n..m] */
    private static final Pattern CARDINALITY_AFTER_COLON = Pattern.compile(
            ":\\s*\\[(\\d+\\.\\.[\\d*]+)\\]");

    public static void main(String[] args) throws Exception {
        String defaultPath =
            "release_files/SnomedCT_InternationalRF2_PRODUCTION_20260601T120000Z"
            + "/Snapshot/Refset/Metadata"
            + "/der2_ssccRefset_MRCMAttributeRangeSnapshot_INT_20260601.txt";

        Path filePath = args.length > 0
                ? Paths.get(args[0]).toAbsolutePath().normalize()
                : Paths.get("").toAbsolutePath().resolve(defaultPath);

        System.out.println("[INFO] Range 파일: " + filePath);

        // MRCM Domain Snapshot 파일 자동 탐색 (Range 파일과 같은 디렉토리)
        // 파일명 패턴: der2_sssssssRefset_MRCMDomainSnapshot_*.txt
        domainFilePath = findDomainFile(filePath.getParent());
        if (domainFilePath != null) {
            System.out.println("[INFO] Domain 파일: " + domainFilePath);
        } else {
            System.out.println("[WARN] Domain 파일을 찾지 못함 → parentDomain 상속 생략");
        }

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            conn.setAutoCommit(false);

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SET search_path TO term");
                stmt.execute("TRUNCATE TABLE term.mrcm_constraints RESTART IDENTITY");
            }

            ensureCardinalityColumn(conn);

            long count = loadFile(conn, filePath);
            conn.commit();
            System.out.println("[INFO] MRCM_CONSTRAINTS 기본 적재 완료: " + count + "건");

            // MRCM Domain parentDomain 기반 상속 전파
            long inherited = propagateIsaInheritance(conn);
            conn.commit();
            System.out.println("[INFO] parentDomain 상속 적재 완료: " + inherited + "건 추가");
            System.out.println("[INFO] 최종 MRCM_CONSTRAINTS 총 건수: " + (count + inherited) + "건");
        }
    }

    /**
     * 지정 디렉토리에서 MRCMDomainSnapshot 파일을 찾아 반환한다.
     * 파일명에 "MRCMDomainSnapshot"이 포함된 첫 번째 .txt 파일을 선택.
     */
    private static Path findDomainFile(Path metadataDir) {
        if (metadataDir == null) return null;
        File[] files = metadataDir.toFile().listFiles(
            f -> f.isFile() && f.getName().contains("MRCMDomainSnapshot") && f.getName().endsWith(".txt"));
        if (files != null && files.length > 0) {
            return files[0].toPath();
        }
        return null;
    }

    /** CARDINALITY 컬럼 없으면 추가 (단독 실행 시 대비) */
    private static void ensureCardinalityColumn(Connection conn) throws Exception {
        String check = "SELECT 1 FROM information_schema.columns "
                     + "WHERE table_schema='term' AND table_name='mrcm_constraints' "
                     + "AND column_name='cardinality'";
        try (Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(check)) {
            if (!rs.next()) {
                stmt.execute("ALTER TABLE term.mrcm_constraints "
                           + "ADD COLUMN cardinality VARCHAR(20) DEFAULT NULL");
                conn.commit();
                System.out.println("[INFO] cardinality 컬럼 추가 완료.");
            }
        }
    }

    private static long loadFile(Connection conn, Path filePath) throws Exception {
        String sql = "INSERT INTO term.mrcm_constraints "
                   + "(attribute_id, attribute_name, source_id, source_name, "
                   + " value_id, value_name, cardinality) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        long inserted = 0;
        long skippedInactive = 0;
        long skippedParseFail = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath.toFile()), StandardCharsets.UTF_8));
             PreparedStatement ps = conn.prepareStatement(sql)) {

            reader.readLine(); // 헤더 스킵

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                String[] cols = line.split("\t", -1);
                if (cols.length < 8) continue;

                // active=0 비활성 행 제외
                if ("0".equals(cols[2].trim())) {
                    skippedInactive++;
                    continue;
                }

                String attributeId     = cols[5].trim();
                String rangeConstraint = cols[6].trim();
                String attributeRule   = cols[7].trim();

                // attributeRule을 단순 규칙 목록으로 전개 (다중 소스 처리)
                List<String> simpleRules = expandRules(attributeRule);

                // value_id/name: rangeConstraint 첫 번째 SCTID|Name| (모든 규칙 공통)
                String valueId   = null;
                String valueName = null;
                Matcher vm = SCTID_NAME.matcher(rangeConstraint);
                if (vm.find()) {
                    valueId   = vm.group(1).trim();
                    valueName = vm.group(2).trim();
                }

                for (String rule : simpleRules) {
                    ParsedMrcm parsed = parseSingleRule(attributeId, rule);

                    if (parsed.sourceId == null) {
                        skippedParseFail++;
                        System.out.printf("[WARN] source_id 파싱 실패 (attribute=%s): %s%n",
                                attributeId, rule.length() > 120 ? rule.substring(0, 120) + "..." : rule);
                        continue;
                    }

                    ps.setString(1, parsed.attributeId);
                    ps.setString(2, parsed.attributeName);
                    ps.setString(3, parsed.sourceId);
                    ps.setString(4, parsed.sourceName);
                    ps.setString(5, valueId);
                    ps.setString(6, valueName);
                    ps.setString(7, parsed.cardinality);

                    ps.addBatch();
                    inserted++;

                    if (inserted % BATCH_SIZE == 0) {
                        ps.executeBatch();
                        conn.commit();
                        System.out.printf("[INFO]   %d건 처리 중...%n", inserted);
                    }
                }
            }

            if (inserted % BATCH_SIZE != 0) {
                ps.executeBatch();
            }
        }

        System.out.printf("[INFO] 결과: 적재=%d건, 비활성스킵=%d건, 파싱실패=%d건%n",
                inserted, skippedInactive, skippedParseFail);
        return inserted;
    }

    // -------------------------------------------------------------------------
    // Rule 전개: 다중 소스 attributeRule → 단순 규칙 목록
    // -------------------------------------------------------------------------

    /**
     * attributeRule을 단순 규칙 문자열 목록으로 전개한다.
     *
     * 전개 규칙:
     *   [단일 소스]  << S |N|: ...          → [원본 그대로]
     *   [Type 1]     (<< S1 OR << S2): ...  → ["<< S1 |N1|: ...", "<< S2 |N2|: ..."]
     *   [Type 2]     (rule1) OR (rule2)     → ["rule1", "rule2"] (각 블록 분리)
     */
    private static List<String> expandRules(String attributeRule) {
        List<String> result = new ArrayList<>();

        // 단일 소스: << 로 시작
        if (attributeRule.startsWith("<<") || attributeRule.startsWith("<")) {
            result.add(attributeRule);
            return result;
        }

        if (!attributeRule.startsWith("(")) {
            result.add(attributeRule);
            return result;
        }

        // Type 2 감지: ") OR (" 가 최상위에 존재 → 독립된 규칙 블록이 OR로 연결된 것
        // "|...(...)...|" 내부의 괄호는 | 로 감싸져 있어 "} OR (" 로만 나타남
        if (attributeRule.contains(") OR (")) {
            // ") OR (" 기준으로 분리, 각 블록의 바깥 괄호 제거
            String[] parts = attributeRule.split("\\) OR \\(");
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i].trim();
                if (i == 0 && part.startsWith("("))           part = part.substring(1).trim();
                if (i == parts.length - 1 && part.endsWith(")")) part = part.substring(0, part.length() - 1).trim();
                result.add(part);
            }
            return result;
        }

        // Type 1 감지: ( << S1 OR << S2 ): [card] { rule }
        // "): " 또는 "):" 를 찾아 소스 목록과 공통 규칙 분리
        int parenClose = attributeRule.indexOf("):");
        if (parenClose > 0) {
            String sourcePart    = attributeRule.substring(1, parenClose).trim();
            String ruleRemainder = attributeRule.substring(parenClose + 1).trim(); // ": [card] { ... }"

            // 소스 목록에서 모든 SCTID|Name| 추출 후 각각 단순 규칙으로 조합
            Matcher m = SCTID_NAME.matcher(sourcePart);
            while (m.find()) {
                result.add("<< " + m.group(1) + " |" + m.group(2) + "|" + ruleRemainder);
            }

            if (!result.isEmpty()) return result;
        }

        // 위 규칙에 모두 해당하지 않으면 원본 반환 (파싱 실패로 기록됨)
        result.add(attributeRule);
        return result;
    }

    // -------------------------------------------------------------------------
    // 단순 규칙 파싱: << S |N|: [card] { ... }
    // -------------------------------------------------------------------------

    /**
     * 단순 규칙 문자열에서 MRCM 필드를 파싱한다.
     * 입력은 반드시 "<< SCTID |Name|: ..." 형식이어야 한다.
     */
    private static ParsedMrcm parseSingleRule(String attributeId, String rule) {
        ParsedMrcm r = new ParsedMrcm();
        r.attributeId = attributeId;

        // source_id / source_name: ':' 앞 첫 번째 << SCTID |Name|
        Matcher sourceMatcher = SOURCE_BEFORE_COLON.matcher(rule);
        if (sourceMatcher.find()) {
            r.sourceId   = sourceMatcher.group(1).trim();
            r.sourceName = sourceMatcher.group(2).trim();
        }

        // attribute_name: attributeId |Name| 인라인 텍스트
        Matcher attrNameMatcher = Pattern.compile(
                Pattern.quote(attributeId) + "\\s*\\|([^|]+)\\|").matcher(rule);
        if (attrNameMatcher.find()) {
            r.attributeName = attrNameMatcher.group(1).trim();
        }

        // cardinality: ':' 뒤 첫 번째 [n..m]
        Matcher cardMatcher = CARDINALITY_AFTER_COLON.matcher(rule);
        if (cardMatcher.find()) {
            r.cardinality = cardMatcher.group(1).trim();
        }

        return r;
    }

    private static class ParsedMrcm {
        String attributeId   = null;
        String attributeName = null;
        String sourceId      = null;
        String sourceName    = null;
        String cardinality   = null;
    }

    // =========================================================================
    // MRCM Domain parentDomain 기반 상속 전파
    // =========================================================================

    /**
     * MRCM Domain Snapshot 파일의 parentDomain 컬럼으로 정의된 계층을 이용해
     * 부모 도메인의 attribute를 자식 도메인에 전파한다.
     *
     * MRCM 표준에서 상속 계층은 SNOMED CT IS-A 관계가 아닌
     * der2_sssssssRefset_MRCMDomainSnapshot 의 parentDomain 필드로 정의된다.
     *
     * 처리 순서:
     *   1. MRCM Domain Snapshot 파일 파싱 → (domainId, domainName, parentDomainId) 목록
     *   2. mrcm_constraints 현재 데이터 로드 (source 별 constraint 목록)
     *   3. parentDomain 계층으로 BFS: 각 도메인의 모든 조상 도메인 탐색
     *   4. 조상 도메인의 attribute를 자식 도메인에 삽입 (중복 제외)
     *      - Range 파일에 없던 도메인(예: 723264001)도 자동 추가됨
     *
     * 중복 판단: (attribute_id, source_id, value_id) 조합이 이미 존재하면 스킵.
     */
    private static long propagateIsaInheritance(Connection conn) throws Exception {
        System.out.println("[INFO] MRCM Domain parentDomain 기반 상속 전파 시작...");

        // --- 1. mrcm_constraints 현재 데이터 로드 ---
        Map<String, List<MrcmConstraintRow>> bySource = new HashMap<>();
        Set<String> existingKeys = new HashSet<>();
        Map<String, String> knownNames = new HashMap<>(); // sourceId → sourceName

        String selectSql = "SELECT attribute_id, attribute_name, source_id, source_name, "
                         + "value_id, value_name, cardinality FROM term.mrcm_constraints";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSql)) {
            while (rs.next()) {
                MrcmConstraintRow row = new MrcmConstraintRow(
                    rs.getString("attribute_id"),
                    rs.getString("attribute_name"),
                    rs.getString("source_id"),
                    rs.getString("source_name"),
                    rs.getString("value_id"),
                    rs.getString("value_name"),
                    rs.getString("cardinality")
                );
                bySource.computeIfAbsent(row.sourceId, k -> new ArrayList<>()).add(row);
                existingKeys.add(dupKey(row.attributeId, row.sourceId, row.valueId));
                if (row.sourceName != null && !row.sourceName.isEmpty()) {
                    knownNames.put(row.sourceId, row.sourceName);
                }
            }
        }
        System.out.println("[INFO]   Range 파일 기반 source 수: " + bySource.size());

        // --- 2. MRCM Domain Snapshot 파일 파싱 (parentDomain 계층 구축) ---
        // domainChildToParent: domainId → parentDomainId  (parentDomain 컬럼 기반)
        // domainNames: domainId → name (domainConstraint 또는 parentDomain 에서 추출)
        Map<String, String> domainChildToParent = new HashMap<>();
        Map<String, String> domainNames = new HashMap<>(knownNames);

        loadMrcmDomainHierarchy(domainFilePath, domainChildToParent, domainNames);

        System.out.println("[INFO]   Domain 파일 내 도메인 수: " + domainNames.size());
        System.out.println("[INFO]   parentDomain 관계 수: " + domainChildToParent.size());

        for (Map.Entry<String, String> e : domainChildToParent.entrySet()) {
            System.out.printf("[INFO]     %s (%s) → 부모: %s (%s)%n",
                e.getKey(), domainNames.getOrDefault(e.getKey(), "?"),
                e.getValue(), domainNames.getOrDefault(e.getValue(), "?"));
        }

        if (domainChildToParent.isEmpty()) {
            System.out.println("[INFO]   parentDomain 관계 없음. 상속 전파 생략.");
            return 0;
        }

        // --- 3. BFS: 각 자식 도메인의 모든 조상 도메인 탐색 ---
        String insertSql = "INSERT INTO term.mrcm_constraints "
                         + "(attribute_id, attribute_name, source_id, source_name, "
                         + " value_id, value_name, cardinality) "
                         + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        long inserted = 0;
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            for (String childId : domainChildToParent.keySet()) {
                String childName = domainNames.getOrDefault(childId, childId);

                // BFS로 전이적 조상 탐색
                Set<String> ancestors = findAllAncestors(childId, domainChildToParent);
                System.out.printf("[INFO]   %s (%s): 조상 도메인 %d개%n",
                        childId, childName, ancestors.size());

                for (String ancestorId : ancestors) {
                    List<MrcmConstraintRow> ancestorRows = bySource.get(ancestorId);
                    if (ancestorRows == null) continue; // 조상에 정의된 attribute 없으면 스킵

                    for (MrcmConstraintRow row : ancestorRows) {
                        String key = dupKey(row.attributeId, childId, row.valueId);
                        if (existingKeys.contains(key)) continue;

                        ps.setString(1, row.attributeId);
                        ps.setString(2, row.attributeName);
                        ps.setString(3, childId);
                        ps.setString(4, childName);
                        ps.setString(5, row.valueId);
                        ps.setString(6, row.valueName);
                        ps.setString(7, row.cardinality);
                        ps.addBatch();

                        existingKeys.add(key);
                        inserted++;

                        if (inserted % BATCH_SIZE == 0) {
                            ps.executeBatch();
                            conn.commit();
                        }
                    }
                }
            }
            if (inserted % BATCH_SIZE != 0) ps.executeBatch();
        }

        return inserted;
    }

    /**
     * MRCM Domain Snapshot 파일을 파싱하여 parentDomain 계층을 구축한다.
     *
     * RF2 컬럼 (0-based):
     *   [2]  active
     *   [5]  referencedComponentId → domainId
     *   [6]  domainConstraint      → domainName (첫 번째 SCTID|Name| 추출)
     *   [7]  parentDomain          → parentDomainId, parentDomainName (SCTID |Name| 형식)
     */
    private static void loadMrcmDomainHierarchy(Path domainFile,
                                                  Map<String, String> domainChildToParent,
                                                  Map<String, String> domainNames) throws Exception {
        if (domainFile == null || !domainFile.toFile().exists()) {
            System.out.println("[WARN] MRCM Domain 파일을 찾을 수 없음. parentDomain 상속 생략.");
            return;
        }
        System.out.println("[INFO]   Domain 파일: " + domainFile);

        // parentDomain 컬럼: "SCTID |Name|" 형식 (ECL 없이 ID와 이름만)
        Pattern parentPattern = Pattern.compile("(\\d+)\\s*\\|([^|]+)\\|");
        // domainConstraint: "<< SCTID |Name|" 또는 "^ SCTID |Name|" 형식
        Pattern domainConstraintPattern = Pattern.compile("[<^]+\\s*(\\d+)\\s*\\|([^|]+)\\|");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(domainFile.toFile()), StandardCharsets.UTF_8))) {

            reader.readLine(); // 헤더 스킵
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] cols = line.split("\t", -1);
                if (cols.length < 8) continue;
                if ("0".equals(cols[2].trim())) continue; // 비활성 제외

                String domainId = cols[5].trim();

                // domainName: domainConstraint 컬럼에서 추출
                Matcher dm = domainConstraintPattern.matcher(cols[6].trim());
                if (dm.find()) {
                    domainNames.putIfAbsent(dm.group(1).trim(), dm.group(2).trim());
                    // referencedComponentId와 domainConstraint의 SCTID가 다를 수 있으므로 두 곳 등록
                    domainNames.putIfAbsent(domainId, dm.group(2).trim());
                }

                // parentDomain: "SCTID |Name|" 형식, 비어있으면 최상위 도메인
                String parentDomainRaw = cols[7].trim();
                if (!parentDomainRaw.isEmpty()) {
                    Matcher pm = parentPattern.matcher(parentDomainRaw);
                    if (pm.find()) {
                        String parentId = pm.group(1).trim();
                        domainNames.putIfAbsent(parentId, pm.group(2).trim());
                        domainChildToParent.put(domainId, parentId);
                    }
                }
            }
        }
    }

    /**
     * BFS로 주어진 개념의 모든 전이적 조상을 반환한다.
     * childToParent 맵: child → 직접 부모 (1:1, MRCM parentDomain 기반).
     */
    private static Set<String> findAllAncestors(String startId,
                                                  Map<String, String> childToParent) {
        Set<String> ancestors = new LinkedHashSet<>();
        String current = childToParent.get(startId);
        while (current != null && ancestors.add(current)) {
            current = childToParent.get(current);
        }
        return ancestors;
    }

    /** 중복 체크 키 생성 */
    private static String dupKey(String attributeId, String sourceId, String valueId) {
        return attributeId + "|" + sourceId + "|" + (valueId == null ? "" : valueId);
    }

    /** IN 절용 콤마 구분 문자열 생성 (SQL 인젝션 방어: SCTID는 숫자 전용) */
    private static String buildInClause(Set<String> ids) {
        StringBuilder sb = new StringBuilder();
        for (String id : ids) {
            if (!id.matches("\\d+")) continue;
            if (sb.length() > 0) sb.append(',');
            sb.append('\'').append(id).append('\'');
        }
        return sb.toString();
    }

    /** mrcm_constraints 행 VO */
    private static class MrcmConstraintRow {
        final String attributeId;
        final String attributeName;
        final String sourceId;
        final String sourceName;
        final String valueId;
        final String valueName;
        final String cardinality;

        MrcmConstraintRow(String attributeId, String attributeName,
                          String sourceId, String sourceName,
                          String valueId, String valueName,
                          String cardinality) {
            this.attributeId   = attributeId;
            this.attributeName = attributeName;
            this.sourceId      = sourceId;
            this.sourceName    = sourceName;
            this.valueId       = valueId;
            this.valueName     = valueName;
            this.cardinality   = cardinality;
        }
    }
}
