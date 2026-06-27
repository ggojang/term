package co.infoclinic.term.fhir.repository;

import co.infoclinic.term.fhir.model.entity.FhirResource;
import co.infoclinic.term.fhir.model.entity.FhirResourceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface FhirResourceRepository extends JpaRepository<FhirResource, FhirResourceId> {

    Optional<FhirResource> findByResourceTypeAndId(String resourceType, String id);

    @Query(value = "SELECT content FROM fhir.resource WHERE resource_type = :resourceType AND id = :id", nativeQuery = true)
    Optional<String> findContentByResourceTypeAndId(@org.springframework.data.repository.query.Param("resourceType") String resourceType,
                                                     @org.springframework.data.repository.query.Param("id") String id);

    @Query(value = "SELECT content FROM fhir.resource WHERE resource_type = :resourceType AND url = :url LIMIT 1", nativeQuery = true)
    Optional<String> findContentByResourceTypeAndUrl(@org.springframework.data.repository.query.Param("resourceType") String resourceType,
                                                      @org.springframework.data.repository.query.Param("url") String url);

    @Query(value = "SELECT content FROM fhir.resource WHERE resource_type = :resourceType AND url = :url AND version = :version LIMIT 1", nativeQuery = true)
    Optional<String> findContentByResourceTypeAndUrlAndVersion(@org.springframework.data.repository.query.Param("resourceType") String resourceType,
                                                                @org.springframework.data.repository.query.Param("url") String url,
                                                                @org.springframework.data.repository.query.Param("version") String version);

    @Query(value = "SELECT id, url, name, title, status FROM fhir.resource WHERE resource_type = :resourceType AND ig_id IS NULL ORDER BY name LIMIT 5000", nativeQuery = true)
    List<Object[]> findSummaryByResourceType(@org.springframework.data.repository.query.Param("resourceType") String resourceType);

    @Query(value = "SELECT id, url, name, title, status FROM fhir.resource WHERE resource_type = :resourceType AND ig_id IS NULL AND (name ILIKE :q OR url ILIKE :q OR title ILIKE :q) ORDER BY name LIMIT 200", nativeQuery = true)
    List<Object[]> searchSummaryByResourceType(@org.springframework.data.repository.query.Param("resourceType") String resourceType,
                                                @org.springframework.data.repository.query.Param("q") String q);

    @Query(value = "SELECT id, url, name, title, status FROM fhir.resource WHERE resource_type = :resourceType AND ig_id = :igId ORDER BY name LIMIT 5000", nativeQuery = true)
    List<Object[]> findSummaryByResourceTypeAndIg(@org.springframework.data.repository.query.Param("resourceType") String resourceType,
                                                   @org.springframework.data.repository.query.Param("igId") String igId);

    @Query(value = "SELECT id, url, name, title, status FROM fhir.resource WHERE resource_type = :resourceType AND ig_id = :igId AND (name ILIKE :q OR url ILIKE :q OR title ILIKE :q) ORDER BY name LIMIT 200", nativeQuery = true)
    List<Object[]> searchSummaryByResourceTypeAndIg(@org.springframework.data.repository.query.Param("resourceType") String resourceType,
                                                     @org.springframework.data.repository.query.Param("igId") String igId,
                                                     @org.springframework.data.repository.query.Param("q") String q);

    @Modifying
    @Transactional
    @Query(value =
        "INSERT INTO fhir.resource (resource_type, id, url, version, name, title, status, ig_id, content, created_at, updated_at) " +
        "VALUES (:resourceType, :id, :url, :version, :name, :title, :status, :igId, :content, now(), now()) " +
        "ON CONFLICT (resource_type, id) DO UPDATE SET " +
        "url=EXCLUDED.url, version=EXCLUDED.version, name=EXCLUDED.name, title=EXCLUDED.title, " +
        "status=EXCLUDED.status, ig_id=EXCLUDED.ig_id, content=EXCLUDED.content, updated_at=now()",
        nativeQuery = true)
    void upsertWithIg(@org.springframework.data.repository.query.Param("resourceType") String resourceType,
                      @org.springframework.data.repository.query.Param("id") String id,
                      @org.springframework.data.repository.query.Param("url") String url,
                      @org.springframework.data.repository.query.Param("version") String version,
                      @org.springframework.data.repository.query.Param("name") String name,
                      @org.springframework.data.repository.query.Param("title") String title,
                      @org.springframework.data.repository.query.Param("status") String status,
                      @org.springframework.data.repository.query.Param("igId") String igId,
                      @org.springframework.data.repository.query.Param("content") String content);

    @Query(value = "SELECT content FROM fhir.resource WHERE resource_type = :resourceType AND name ILIKE %:name% LIMIT 100", nativeQuery = true)
    List<String> searchContentByResourceTypeAndName(@org.springframework.data.repository.query.Param("resourceType") String resourceType,
                                                     @org.springframework.data.repository.query.Param("name") String name);

    @Query(value = "SELECT content FROM fhir.resource WHERE resource_type = :resourceType AND url = :url LIMIT 100", nativeQuery = true)
    List<String> searchContentByResourceTypeAndUrl(@org.springframework.data.repository.query.Param("resourceType") String resourceType,
                                                    @org.springframework.data.repository.query.Param("url") String url);

    @Query(value = "SELECT content FROM fhir.resource WHERE resource_type = :resourceType AND status = :status LIMIT 100", nativeQuery = true)
    List<String> findContentByResourceTypeAndStatus(@org.springframework.data.repository.query.Param("resourceType") String resourceType,
                                                     @org.springframework.data.repository.query.Param("status") String status);

    @Modifying
    @Transactional
    @Query(value =
        "INSERT INTO fhir.resource (resource_type, id, url, version, name, title, status, content, created_at, updated_at) " +
        "VALUES (:resourceType, :id, :url, :version, :name, :title, :status, :content, now(), now()) " +
        "ON CONFLICT (resource_type, id) DO UPDATE SET " +
        "url=EXCLUDED.url, version=EXCLUDED.version, name=EXCLUDED.name, title=EXCLUDED.title, " +
        "status=EXCLUDED.status, content=EXCLUDED.content, updated_at=now()",
        nativeQuery = true)
    void upsert(@org.springframework.data.repository.query.Param("resourceType") String resourceType,
                @org.springframework.data.repository.query.Param("id") String id,
                @org.springframework.data.repository.query.Param("url") String url,
                @org.springframework.data.repository.query.Param("version") String version,
                @org.springframework.data.repository.query.Param("name") String name,
                @org.springframework.data.repository.query.Param("title") String title,
                @org.springframework.data.repository.query.Param("status") String status,
                @org.springframework.data.repository.query.Param("content") String content);
}
