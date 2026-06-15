package co.infoclinic.term.common.loader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * SNOMED CT RF2 ZIP 릴리즈 파일을 PostgreSQL(term 스키마)로 적재합니다.
 *
 * 적재 대상 테이블:
 *   CONCEPT, DESCRIPTION, DESCRIPTION_TP, INFERRED_RELATIONSHIP,
 *   INFERRED_RELATIONSHIP_SNAP, STATED_RELATIONSHIP, REFERENCESET, SCHEME
 *
 * 사용법:
 *   java -cp .:postgresql-*.jar co.infoclinic.term.common.loader.SnomedRf2Loader \
 *        /path/to/SnomedCT_InternationalRF2_PRODUCTION_20260601T120000Z.zip
 *
 * REFERENCESET:
 *   Snapshot 파일을 우선 적재합니다(UUID당 최신 1건). UNIQUE(REFERENCESET_ID) 충돌 시
 *   ON CONFLICT DO UPDATE로 최신 버전을 유지합니다.
 */
public class SnomedRf2Loader {

    private static final String JDBC_URL      = "jdbc:postgresql://localhost:5432/term";
    private static final String JDBC_USER     = "postgres";
    private static final String JDBC_PASSWORD = "julab123!";

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: SnomedRf2Loader <path-to-snomed-rf2.zip>");
            System.exit(1);
        }

        Path zipPath = Paths.get(args[0]).toAbsolutePath().normalize();
        if (!zipPath.toFile().exists()) {
            throw new IllegalArgumentException("RF2 archive does not exist: " + zipPath);
        }
        System.out.println("Loading SNOMED RF2 archive: " + zipPath);

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            conn.setAutoCommit(false);
            ensureSchema(conn, zipPath);

            int conceptCount = 0, descCount = 0, relCount = 0, refsetCount = 0;

            // --- 1차 패스: Full 파일 (CONCEPT, DESCRIPTION, RELATIONSHIPS) ---
            try (ZipInputStream zis = openZip(zipPath)) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.isDirectory()) continue;
                    String name = entry.getName().toLowerCase(Locale.ROOT);
                    if (!name.endsWith(".txt")) continue;

                    byte[] bytes = readEntry(zis);

                    if (isFull(name) && name.contains("sct2_concept_")) {
                        conceptCount += loadConcepts(conn, new ByteArrayInputStream(bytes));
                    } else if (isFull(name) && name.contains("sct2_description_")) {
                        descCount += loadDescriptions(conn, new ByteArrayInputStream(bytes));
                    } else if (isFull(name) && name.contains("sct2_relationship_") && !name.contains("stated")) {
                        relCount += loadRelationships(conn, new ByteArrayInputStream(bytes), false, false);
                    } else if (isFull(name) && name.contains("sct2_statedrelationship_")) {
                        relCount += loadRelationships(conn, new ByteArrayInputStream(bytes), true, false);
                    } else if (isSnapshot(name) && name.contains("sct2_relationship_") && !name.contains("stated")) {
                        relCount += loadRelationships(conn, new ByteArrayInputStream(bytes), false, true);
                    }
                }
            }
            conn.commit();
            System.out.println("Core tables committed. Concepts=" + conceptCount
                    + ", Descriptions=" + descCount + ", Relationships=" + relCount);

            // --- 2차 패스: Refset Snapshot 파일 ---
            try (ZipInputStream zis = openZip(zipPath)) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.isDirectory()) continue;
                    String name = entry.getName().toLowerCase(Locale.ROOT);
                    if (!name.endsWith(".txt")) continue;
                    if (!isSnapshot(name)) continue;
                    if (!name.contains("refset") && !name.contains("_refset")) continue;
                    // der2_ 로 시작하는 refset 파일만 처리
                    String baseName = Paths.get(name).getFileName().toString();
                    if (!baseName.startsWith("der2_")) continue;

                    byte[] bytes = readEntry(zis);
                    refsetCount += loadRefset(conn, new ByteArrayInputStream(bytes), name);
                }
            }
            conn.commit();
            System.out.println("Refset tables committed. Refset rows=" + refsetCount);

            System.out.println("Import finished successfully.");
        }
    }

    // -------------------------------------------------------------------------
    // 스키마 초기화
    // -------------------------------------------------------------------------

    private static void ensureSchema(Connection conn, Path zipPath) throws SQLException, IOException {
        try (Statement st = conn.createStatement()) {
            st.execute("DROP SCHEMA IF EXISTS term CASCADE");
            st.execute("CREATE SCHEMA term");
            st.execute("SET search_path TO term, public");
            st.execute("CREATE EXTENSION IF NOT EXISTS pg_trgm");

            // CONCEPT
            st.execute("CREATE TABLE concept (" +
                "SEQ BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
                "CONCEPT_ID VARCHAR(18) NOT NULL, " +
                "EFFECTIVE_TIME CHAR(8) NOT NULL, " +
                "ACTIVE SMALLINT NOT NULL, " +
                "MODULE_ID VARCHAR(18) NOT NULL, " +
                "DEFINITION_STATUS_ID VARCHAR(18) NOT NULL, " +
                "DATE_CREATED TIMESTAMP DEFAULT NULL)");
            st.execute("CREATE INDEX idx_concept_concept ON concept(concept_id)");
            st.execute("CREATE INDEX idx_concept_concept_etime ON concept(concept_id, effective_time)");
            st.execute("CREATE INDEX idx_concept_etime ON concept(effective_time)");
            st.execute("CREATE INDEX idx_concept_ds_id ON concept(definition_status_id)");
            st.execute("CREATE INDEX idx_concept_mod_id ON concept(module_id)");
            st.execute("CREATE INDEX idx_concept_act ON concept(active)");

            // DESCRIPTION (Full — all history)
            st.execute("CREATE TABLE description (" +
                "SEQ BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
                "DESCRIPTION_ID VARCHAR(18) NOT NULL, " +
                "EFFECTIVE_TIME CHAR(8) NOT NULL, " +
                "ACTIVE SMALLINT NOT NULL, " +
                "MODULE_ID VARCHAR(18) NOT NULL, " +
                "CONCEPT_ID VARCHAR(18) NOT NULL, " +
                "LANGUAGE_CODE CHAR(2) NOT NULL, " +
                "TYPE_ID VARCHAR(18) NOT NULL, " +
                "TERM TEXT NOT NULL, " +
                "CASE_SIGNIFICANCE_ID VARCHAR(18) NOT NULL, " +
                "DATE_CREATED TIMESTAMP DEFAULT NULL)");
            st.execute("CREATE INDEX idx_description_c_id ON description(concept_id)");
            st.execute("CREATE INDEX idx_description_c_id_etime ON description(concept_id, effective_time)");
            st.execute("CREATE INDEX idx_description_d_id ON description(description_id)");
            st.execute("CREATE INDEX idx_description_etime ON description(effective_time)");
            st.execute("CREATE INDEX idx_description_active ON description(active)");
            st.execute("CREATE INDEX idx_description_type_id ON description(type_id)");

            // DESCRIPTION_TP (same source data, used as main lookup)
            st.execute("CREATE TABLE description_tp (" +
                "SEQ BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
                "DESCRIPTION_ID VARCHAR(18) NOT NULL, " +
                "EFFECTIVE_TIME CHAR(8) NOT NULL, " +
                "ACTIVE SMALLINT NOT NULL, " +
                "MODULE_ID VARCHAR(18) NOT NULL, " +
                "CONCEPT_ID VARCHAR(18) NOT NULL, " +
                "LANGUAGE_CODE CHAR(2) NOT NULL, " +
                "LANGUAGE_CULTURE VARCHAR(5) DEFAULT NULL, " +
                "TYPE_ID VARCHAR(18) NOT NULL, " +
                "TERM TEXT NOT NULL, " +
                "CASE_SIGNIFICANCE_ID VARCHAR(18) NOT NULL, " +
                "DATE_CREATED TIMESTAMP DEFAULT NULL)");
            st.execute("CREATE INDEX idx_desc_tp_c_id ON description_tp(concept_id)");
            st.execute("CREATE INDEX idx_desc_tp_c_id_t_id_etime ON description_tp(concept_id, type_id, effective_time)");
            st.execute("CREATE INDEX idx_desc_tp_etime ON description_tp(effective_time)");
            st.execute("CREATE INDEX idx_desc_tp_active ON description_tp(active)");
            st.execute("CREATE INDEX idx_desc_tp_type_id ON description_tp(type_id)");
            st.execute("CREATE INDEX idx_desc_tp_term_trgm ON description_tp USING gin (term gin_trgm_ops)");

            // INFERRED_RELATIONSHIP (Full)
            st.execute("CREATE TABLE inferred_relationship (" +
                "SEQ BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
                "RELATIONSHIP_ID VARCHAR(18) NOT NULL, " +
                "EFFECTIVE_TIME CHAR(8) NOT NULL, " +
                "ACTIVE SMALLINT NOT NULL, " +
                "MODULE_ID VARCHAR(18) NOT NULL, " +
                "SOURCE_ID VARCHAR(18) NOT NULL, " +
                "DESTINATION_ID VARCHAR(18) NOT NULL, " +
                "RELATIONSHIP_GROUP INTEGER NOT NULL, " +
                "TYPE_ID VARCHAR(18) NOT NULL, " +
                "CHARACTERISTIC_TYPE_ID VARCHAR(18) NOT NULL, " +
                "MODIFIER_ID VARCHAR(18) NOT NULL, " +
                "DATE_CREATED TIMESTAMP DEFAULT NULL)");
            st.execute("CREATE INDEX idx_inferred_s_id ON inferred_relationship(source_id)");
            st.execute("CREATE INDEX idx_inferred_etime_s_id ON inferred_relationship(effective_time, source_id)");
            st.execute("CREATE INDEX idx_inferred_s_id_etime ON inferred_relationship(source_id, effective_time)");

            // INFERRED_RELATIONSHIP_SNAP (Snapshot)
            st.execute("CREATE TABLE inferred_relationship_snap (" +
                "SEQ BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
                "RELATIONSHIP_ID VARCHAR(18) NOT NULL, " +
                "EFFECTIVE_TIME CHAR(8) NOT NULL, " +
                "ACTIVE SMALLINT NOT NULL, " +
                "MODULE_ID VARCHAR(18) NOT NULL, " +
                "SOURCE_ID VARCHAR(18) NOT NULL, " +
                "DESTINATION_ID VARCHAR(18) NOT NULL, " +
                "RELATIONSHIP_GROUP INTEGER NOT NULL, " +
                "TYPE_ID VARCHAR(18) NOT NULL, " +
                "CHARACTERISTIC_TYPE_ID VARCHAR(18) NOT NULL, " +
                "MODIFIER_ID VARCHAR(18) NOT NULL, " +
                "DATE_CREATED TIMESTAMP DEFAULT NULL)");
            st.execute("CREATE INDEX idx_inferred_snap_s_id ON inferred_relationship_snap(source_id)");
            st.execute("CREATE INDEX idx_inferred_snap_etime_s_id ON inferred_relationship_snap(effective_time, source_id)");

            // STATED_RELATIONSHIP
            st.execute("CREATE TABLE stated_relationship (" +
                "SEQ BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
                "RELATIONSHIP_ID VARCHAR(18) NOT NULL, " +
                "EFFECTIVE_TIME CHAR(8) NOT NULL, " +
                "ACTIVE SMALLINT NOT NULL, " +
                "MODULE_ID VARCHAR(18) NOT NULL, " +
                "SOURCE_ID VARCHAR(18) NOT NULL, " +
                "DESTINATION_ID VARCHAR(18) NOT NULL, " +
                "RELATIONSHIP_GROUP INTEGER NOT NULL, " +
                "TYPE_ID VARCHAR(18) NOT NULL, " +
                "CHARACTERISTIC_TYPE_ID VARCHAR(18) NOT NULL, " +
                "MODIFIER_ID VARCHAR(18) NOT NULL, " +
                "DATE_CREATED TIMESTAMP DEFAULT NULL)");
            st.execute("CREATE INDEX idx_stated_s_id ON stated_relationship(source_id)");
            st.execute("CREATE INDEX idx_stated_etime_s_id ON stated_relationship(effective_time, source_id)");

            // REFERENCESET
            st.execute("CREATE TABLE referenceset (" +
                "SEQ BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
                "REFERENCESET_ID CHAR(36) NOT NULL, " +
                "EFFECTIVE_TIME CHAR(8) NOT NULL, " +
                "ACTIVE SMALLINT NOT NULL, " +
                "MODULE_ID VARCHAR(18) NOT NULL, " +
                "REFSET_ID VARCHAR(18) NOT NULL, " +
                "REFERENCED_COMPONENT_ID VARCHAR(18) NOT NULL, " +
                "FIELD1 TEXT DEFAULT NULL, " +
                "FIELD2 TEXT DEFAULT NULL, " +
                "FIELD3 TEXT DEFAULT NULL, " +
                "FIELD4 TEXT DEFAULT NULL, " +
                "FIELD5 TEXT DEFAULT NULL, " +
                "FIELD6 TEXT DEFAULT NULL, " +
                "FIELD7 TEXT DEFAULT NULL, " +
                "DATE_CREATED CHAR(8) DEFAULT NULL, " +
                "UNIQUE (REFERENCESET_ID))");
            st.execute("CREATE INDEX idx_rs_etime_r_id ON referenceset(effective_time, referenceset_id)");
            st.execute("CREATE INDEX idx_rs_refsetid_rc_id ON referenceset(refset_id, referenced_component_id)");
            st.execute("CREATE INDEX idx_rs_refsetid_rc_id_etime ON referenceset(refset_id, referenced_component_id, effective_time)");
            st.execute("CREATE INDEX idx_rs_rc_id_refsetid_etime ON referenceset(referenced_component_id, refset_id, effective_time)");

            // REFERENCESET_ACTIVE (populated separately by refset_active_pg.sh)
            st.execute("CREATE TABLE referenceset_active (" +
                "SEQ BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
                "VERSION VARCHAR(20) NOT NULL, " +
                "UUID CHAR(36) NOT NULL, " +
                "EFFECTIVE_TIME CHAR(8) NOT NULL, " +
                "MODULE_ID VARCHAR(18) NOT NULL, " +
                "MODULE_NAME VARCHAR(255) DEFAULT NULL, " +
                "REFSET_ID VARCHAR(18) NOT NULL, " +
                "REFSET_NAME VARCHAR(255) DEFAULT NULL, " +
                "REFERENCED_COMPONENT_ID VARCHAR(18) NOT NULL, " +
                "REFERENCED_COMPONENT_ACTIVE SMALLINT DEFAULT NULL, " +
                "REFERENCED_COMPONENT_NAME TEXT DEFAULT NULL, " +
                "FIELD1_ID VARCHAR(18) DEFAULT NULL, " +
                "FIELD1_VALUE TEXT DEFAULT NULL, " +
                "FIELD2_ID VARCHAR(18) DEFAULT NULL, " +
                "FIELD2_VALUE TEXT DEFAULT NULL, " +
                "FIELD3_ID VARCHAR(18) DEFAULT NULL, " +
                "FIELD3_VALUE TEXT DEFAULT NULL, " +
                "FIELD4_ID VARCHAR(18) DEFAULT NULL, " +
                "FIELD4_VALUE TEXT DEFAULT NULL, " +
                "FIELD5_ID VARCHAR(18) DEFAULT NULL, " +
                "FIELD5_VALUE TEXT DEFAULT NULL, " +
                "FIELD6_ID VARCHAR(18) DEFAULT NULL, " +
                "FIELD6_VALUE TEXT DEFAULT NULL, " +
                "FIELD7_ID VARCHAR(18) DEFAULT NULL, " +
                "FIELD7_VALUE TEXT DEFAULT NULL)");
            st.execute("CREATE INDEX idx_ra_version_refset ON referenceset_active(version, refset_id)");
            st.execute("CREATE INDEX idx_ra_refset_rc ON referenceset_active(refset_id, referenced_component_id)");
            st.execute("CREATE INDEX idx_ra_rc_refset ON referenceset_active(referenced_component_id, refset_id)");
            st.execute("CREATE INDEX idx_ra_rc_name ON referenceset_active USING gin (to_tsvector('simple', coalesce(referenced_component_name,'')))");

            // USER_REFERENCESET
            st.execute("CREATE TABLE user_referenceset (" +
                "SEQ BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
                "REFERENCESET_ID CHAR(36) NOT NULL, " +
                "EFFECTIVE_TIME CHAR(8) NOT NULL, " +
                "ACTIVE SMALLINT NOT NULL, " +
                "MODULE_ID VARCHAR(18) NOT NULL, " +
                "REFSET_ID VARCHAR(18) NOT NULL, " +
                "REFERENCED_COMPONENT_ID VARCHAR(18) NOT NULL, " +
                "FIELD1 TEXT DEFAULT NULL, " +
                "FIELD2 TEXT DEFAULT NULL, " +
                "FIELD3 TEXT DEFAULT NULL, " +
                "FIELD4 TEXT DEFAULT NULL, " +
                "FIELD5 TEXT DEFAULT NULL, " +
                "FIELD6 TEXT DEFAULT NULL, " +
                "FIELD7 TEXT DEFAULT NULL, " +
                "DATE_CREATED CHAR(8) DEFAULT NULL)");

            // TC (populated by TransitiveClosureGeneratorFromInferred.sh)
            st.execute("CREATE TABLE tc (" +
                "SEQ BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
                "CONCEPT_ID VARCHAR(18) NOT NULL, " +
                "TERM TEXT NOT NULL, " +
                "PARENT_ID VARCHAR(18) NOT NULL, " +
                "CHILDREN_COUNT INTEGER DEFAULT 0, " +
                "DESCENDANT_COUNT INTEGER DEFAULT 0, " +
                "DEPTH INTEGER DEFAULT 0, " +
                "PATH TEXT NOT NULL, " +
                "DATE_CREATED CHAR(8) DEFAULT NULL)");
            st.execute("CREATE INDEX idx_tc_c_id ON tc(concept_id)");
            st.execute("CREATE INDEX idx_tc_p_id ON tc(parent_id)");
            st.execute("CREATE INDEX idx_tc_path_prefix ON tc (path text_pattern_ops)");

            // SCHEME
            st.execute("CREATE TABLE scheme (" +
                "ID VARCHAR(100) PRIMARY KEY, " +
                "NAME VARCHAR(100) DEFAULT NULL, " +
                "EDITION VARCHAR(100) DEFAULT NULL, " +
                "VERSION VARCHAR(100) DEFAULT NULL, " +
                "AUTHORITY VARCHAR(100) DEFAULT NULL, " +
                "DATE CHAR(8) DEFAULT NULL)");

            // SEARCH_INDEX (populated separately)
            st.execute("CREATE TABLE search_index (" +
                "SEQ BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
                "CONCEPT_ID VARCHAR(18) NOT NULL, " +
                "TERM TEXT NOT NULL, " +
                "FSN TEXT NOT NULL, " +
                "SEMANTIC_TAG VARCHAR(100) DEFAULT NULL, " +
                "EFFECTIVE_TIME CHAR(8) NOT NULL, " +
                "ACTIVE SMALLINT NOT NULL, " +
                "MODULE_ID VARCHAR(18) DEFAULT NULL, " +
                "TYPE_ID VARCHAR(18) DEFAULT NULL, " +
                "DEFINITION_STATUS_ID VARCHAR(18) DEFAULT NULL)");
            st.execute("CREATE INDEX idx_search_index_concept ON search_index(concept_id)");
            st.execute("CREATE INDEX idx_search_index_trgm ON search_index USING gin (term gin_trgm_ops)");

            // MRCM_CONSTRAINTS (populated separately)
            st.execute("CREATE TABLE mrcm_constraints (" +
                "SEQ BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
                "ATTRIBUTE_ID VARCHAR(18) DEFAULT NULL, " +
                "ATTRIBUTE_NAME VARCHAR(255) DEFAULT NULL, " +
                "SOURCE_ID VARCHAR(18) DEFAULT NULL, " +
                "SOURCE_NAME VARCHAR(255) DEFAULT NULL, " +
                "VALUE_ID VARCHAR(18) DEFAULT NULL, " +
                "VALUE_NAME VARCHAR(255) DEFAULT NULL)");

            // SCTID
            st.execute("CREATE TABLE sctid (" +
                "SEQ BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
                "SCTID VARCHAR(18) NOT NULL, " +
                "PARTITION_ID CHAR(2) DEFAULT NULL, " +
                "CHECK_DIGIT CHAR(1) DEFAULT NULL)");

            // UMLS_SYNONYM / UMLS_SYNONYM_PHRASE (populated separately)
            st.execute("CREATE TABLE umls_synonym (" +
                "SEQ BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
                "CONCEPT_ID VARCHAR(18) NOT NULL, " +
                "TERM TEXT NOT NULL)");
            st.execute("CREATE TABLE umls_synonym_phrase (" +
                "SEQ BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
                "CONCEPT_ID VARCHAR(18) NOT NULL, " +
                "TERM TEXT NOT NULL)");
        }

        insertScheme(conn, zipPath);
        conn.commit();
        System.out.println("Schema initialized.");
    }

    private static void insertScheme(Connection conn, Path zipPath) throws SQLException {
        String date    = extractReleaseDate(zipPath);
        String id      = "SNOMEDCT-v" + date;
        String edition = "SNOMEDCT v" + date;
        String version = "v" + date;
        String sql = "INSERT INTO term.scheme (ID, NAME, EDITION, VERSION, AUTHORITY, DATE) " +
                     "VALUES (?,?,?,?,?,?) ON CONFLICT (ID) DO UPDATE SET " +
                     "NAME=EXCLUDED.NAME, EDITION=EXCLUDED.EDITION, VERSION=EXCLUDED.VERSION, " +
                     "AUTHORITY=EXCLUDED.AUTHORITY, DATE=EXCLUDED.DATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, "SNOMEDCT-Int");
            ps.setString(3, edition);
            ps.setString(4, version);
            ps.setString(5, "SNOMED International");
            ps.setString(6, date);
            ps.executeUpdate();
        }
        System.out.println("SCHEME: ID=" + id);
    }

    // -------------------------------------------------------------------------
    // 파일 판별
    // -------------------------------------------------------------------------

    private static boolean isFull(String name) {
        return name.contains("_full_") || name.contains("full_int_") || name.contains("full-en_");
    }

    private static boolean isSnapshot(String name) {
        return name.contains("_snapshot_") || name.contains("snapshot_int_") || name.contains("snapshot-en_");
    }

    // -------------------------------------------------------------------------
    // CONCEPT
    // -------------------------------------------------------------------------

    private static int loadConcepts(Connection conn, InputStream in) throws IOException, SQLException {
        String sql = "INSERT INTO term.concept " +
                     "(concept_id, effective_time, active, module_id, definition_status_id) " +
                     "VALUES (?,?,?,?,?)";
        try (BufferedReader r = reader(in);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String line;
            int count = 0;
            while ((line = r.readLine()) != null) {
                if (isHeader(line) || line.trim().isEmpty()) continue;
                String[] c = line.split("\t", -1);
                if (c.length < 5) continue;
                // RF2: id effectiveTime active moduleId definitionStatusId
                ps.setString(1, c[0]); // id → concept_id
                ps.setString(2, c[1]); // effectiveTime
                ps.setInt(3, Integer.parseInt(c[2])); // active
                ps.setString(4, c[3]); // moduleId
                ps.setString(5, c[4]); // definitionStatusId
                ps.addBatch();
                if (++count % 2000 == 0) ps.executeBatch();
            }
            ps.executeBatch();
            System.out.println("CONCEPT loaded: " + count);
            return count;
        }
    }

    // -------------------------------------------------------------------------
    // DESCRIPTION + DESCRIPTION_TP (동일 소스)
    // -------------------------------------------------------------------------

    private static int loadDescriptions(Connection conn, InputStream in) throws IOException, SQLException {
        // RF2 Description: id effectiveTime active moduleId conceptId languageCode typeId term caseSignificanceId
        String sqlD = "INSERT INTO term.description " +
                      "(description_id, effective_time, active, module_id, concept_id, language_code, type_id, term, case_significance_id) " +
                      "VALUES (?,?,?,?,?,?,?,?,?)";
        String sqlTP = "INSERT INTO term.description_tp " +
                       "(description_id, effective_time, active, module_id, concept_id, language_code, language_culture, type_id, term, case_significance_id) " +
                       "VALUES (?,?,?,?,?,?,NULL,?,?,?)";

        byte[] bytes;
        try (ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
            byte[] chunk = new byte[8192];
            int n;
            while ((n = in.read(chunk)) != -1) buf.write(chunk, 0, n);
            bytes = buf.toByteArray();
        }

        int count = 0;
        try (BufferedReader r = reader(new ByteArrayInputStream(bytes));
             PreparedStatement psD  = conn.prepareStatement(sqlD);
             PreparedStatement psTP = conn.prepareStatement(sqlTP)) {
            String line;
            while ((line = r.readLine()) != null) {
                if (isHeader(line) || line.trim().isEmpty()) continue;
                String[] c = line.split("\t", -1);
                if (c.length < 9) continue;
                // 0:id 1:effectiveTime 2:active 3:moduleId 4:conceptId 5:languageCode 6:typeId 7:term 8:caseSignificanceId
                psD.setString(1, c[0]); psD.setString(2, c[1]); psD.setInt(3, parseInt(c[2]));
                psD.setString(4, c[3]); psD.setString(5, c[4]); psD.setString(6, c[5]);
                psD.setString(7, c[6]); psD.setString(8, c[7]); psD.setString(9, c[8]);
                psD.addBatch();

                psTP.setString(1, c[0]); psTP.setString(2, c[1]); psTP.setInt(3, parseInt(c[2]));
                psTP.setString(4, c[3]); psTP.setString(5, c[4]); psTP.setString(6, c[5]);
                psTP.setString(7, c[6]); psTP.setString(8, c[7]); psTP.setString(9, c[8]);
                psTP.addBatch();

                if (++count % 2000 == 0) { psD.executeBatch(); psTP.executeBatch(); }
            }
            psD.executeBatch(); psTP.executeBatch();
        }
        System.out.println("DESCRIPTION / DESCRIPTION_TP loaded: " + count);
        return count;
    }

    // -------------------------------------------------------------------------
    // INFERRED_RELATIONSHIP / INFERRED_RELATIONSHIP_SNAP / STATED_RELATIONSHIP
    // -------------------------------------------------------------------------

    private static int loadRelationships(Connection conn, InputStream in,
                                         boolean stated, boolean snapshot) throws IOException, SQLException {
        String table = stated ? "stated_relationship"
                       : (snapshot ? "inferred_relationship_snap" : "inferred_relationship");
        String sql = "INSERT INTO term." + table +
                     " (relationship_id, effective_time, active, module_id, source_id, destination_id," +
                     " relationship_group, type_id, characteristic_type_id, modifier_id) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?)";
        // RF2: id effectiveTime active moduleId sourceId destinationId relationshipGroup typeId characteristicTypeId modifierId
        try (BufferedReader r = reader(in);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String line;
            int count = 0;
            while ((line = r.readLine()) != null) {
                if (isHeader(line) || line.trim().isEmpty()) continue;
                String[] c = line.split("\t", -1);
                if (c.length < 10) continue;
                ps.setString(1, c[0]); ps.setString(2, c[1]); ps.setInt(3, parseInt(c[2]));
                ps.setString(4, c[3]); ps.setString(5, c[4]); ps.setString(6, c[5]);
                ps.setInt(7, parseInt(c[6]));
                ps.setString(8, c[7]); ps.setString(9, c[8]); ps.setString(10, c[9]);
                ps.addBatch();
                if (++count % 2000 == 0) ps.executeBatch();
            }
            ps.executeBatch();
            System.out.println(table + " loaded: " + count);
            return count;
        }
    }

    // -------------------------------------------------------------------------
    // REFERENCESET (Snapshot — UUID 당 최신 1건)
    // -------------------------------------------------------------------------

    private static int loadRefset(Connection conn, InputStream in, String entryName) throws IOException, SQLException {
        byte[] bytes;
        try (ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
            byte[] chunk = new byte[8192];
            int n;
            while ((n = in.read(chunk)) != -1) buf.write(chunk, 0, n);
            bytes = buf.toByteArray();
        }

        // 헤더에서 컬럼 수 확인
        String headerLine = null;
        try (BufferedReader r = reader(new ByteArrayInputStream(bytes))) {
            headerLine = r.readLine();
        }
        if (headerLine == null) return 0;
        int totalCols = headerLine.split("\t", -1).length;
        int extraFields = totalCols - 6; // id effectiveTime active moduleId refsetId referencedComponentId
        if (extraFields < 0) extraFields = 0;
        if (extraFields > 7)  extraFields = 7; // FIELD1~FIELD7

        // INSERT SQL 동적 생성
        StringBuilder sb = new StringBuilder(
            "INSERT INTO term.referenceset " +
            "(referenceset_id, effective_time, active, module_id, refset_id, referenced_component_id");
        for (int i = 1; i <= extraFields; i++) sb.append(", field").append(i);
        sb.append(") VALUES (?,?,?,?,?,?");
        for (int i = 0; i < extraFields; i++) sb.append(",?");
        sb.append(") ON CONFLICT (referenceset_id) DO UPDATE SET " +
            "effective_time=EXCLUDED.effective_time, " +
            "active=EXCLUDED.active, " +
            "module_id=EXCLUDED.module_id, " +
            "refset_id=EXCLUDED.refset_id, " +
            "referenced_component_id=EXCLUDED.referenced_component_id");
        for (int i = 1; i <= extraFields; i++) {
            sb.append(", field").append(i).append("=EXCLUDED.field").append(i);
        }

        int count = 0;
        try (BufferedReader r = reader(new ByteArrayInputStream(bytes));
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            String line;
            boolean first = true;
            while ((line = r.readLine()) != null) {
                if (first) { first = false; continue; } // skip header
                if (line.trim().isEmpty()) continue;
                String[] c = line.split("\t", -1);
                if (c.length < 6) continue;
                // RF2: 0:id 1:effectiveTime 2:active 3:moduleId 4:refsetId 5:referencedComponentId 6+:fields
                ps.setString(1, c[0].trim()); // UUID → REFERENCESET_ID
                ps.setString(2, c[1]);
                ps.setInt(3, parseInt(c[2]));
                ps.setString(4, c[3]);
                ps.setString(5, c[4]);
                ps.setString(6, c[5]);
                for (int i = 0; i < extraFields; i++) {
                    String val = (i + 6 < c.length) ? c[i + 6] : null;
                    if (val != null && val.isEmpty()) val = null;
                    ps.setString(7 + i, val);
                }
                ps.addBatch();
                if (++count % 2000 == 0) ps.executeBatch();
            }
            ps.executeBatch();
        }
        if (count > 0) {
            System.out.println("REFERENCESET loaded from " +
                Paths.get(entryName).getFileName() + ": " + count + " rows (fields=" + extraFields + ")");
        }
        return count;
    }

    // -------------------------------------------------------------------------
    // 유틸리티
    // -------------------------------------------------------------------------

    private static ZipInputStream openZip(Path zipPath) throws IOException {
        return new ZipInputStream(new BufferedInputStream(new FileInputStream(zipPath.toFile())));
    }

    private static byte[] readEntry(ZipInputStream zis) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] chunk = new byte[65536];
        int n;
        while ((n = zis.read(chunk)) != -1) buf.write(chunk, 0, n);
        return buf.toByteArray();
    }

    private static BufferedReader reader(InputStream in) {
        return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    private static boolean isHeader(String line) {
        return line.startsWith("id\t") || line.startsWith("effectiveTime\t");
    }

    private static int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    private static String extractReleaseDate(Path zipPath) {
        Matcher m = Pattern.compile("(\\d{8})").matcher(zipPath.getFileName().toString());
        return m.find() ? m.group(1) : "20260601";
    }
}
