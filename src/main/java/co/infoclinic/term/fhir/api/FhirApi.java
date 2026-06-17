package co.infoclinic.term.fhir.api;

public class FhirApi {
    public static final String BASE = "/fhir";
    public static final String METADATA = BASE + "/metadata";

    public static final String CODE_SYSTEM         = BASE + "/CodeSystem";
    public static final String CODE_SYSTEM_ID      = BASE + "/CodeSystem/{id}";
    public static final String CODE_SYSTEM_LOOKUP  = BASE + "/CodeSystem/$lookup";
    public static final String CODE_SYSTEM_VALIDATE= BASE + "/CodeSystem/$validate-code";
    public static final String CODE_SYSTEM_SUBSUMES= BASE + "/CodeSystem/$subsumes";

    public static final String VALUE_SET           = BASE + "/ValueSet";
    public static final String VALUE_SET_ID        = BASE + "/ValueSet/{id}";
    public static final String VALUE_SET_EXPAND    = BASE + "/ValueSet/$expand";
    public static final String VALUE_SET_EXPAND_ID = BASE + "/ValueSet/{id}/$expand";
    public static final String VALUE_SET_VALIDATE  = BASE + "/ValueSet/$validate-code";

    public static final String CONCEPT_MAP         = BASE + "/ConceptMap";
    public static final String CONCEPT_MAP_ID      = BASE + "/ConceptMap/{id}";
    public static final String CONCEPT_MAP_TRANSLATE= BASE + "/ConceptMap/$translate";

    public static final String NAMING_SYSTEM            = BASE + "/NamingSystem";
    public static final String NAMING_SYSTEM_ID         = BASE + "/NamingSystem/{id}";
    public static final String NAMING_SYSTEM_PREFERRED  = BASE + "/NamingSystem/$preferred-id";

    public static final String INSTALL_PACKAGE     = BASE + "/$install-package";
}
