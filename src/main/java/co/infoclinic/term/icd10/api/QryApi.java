package co.infoclinic.term.icd10.api;

public class QryApi {

	// Code System
	public static final String CS = "ICD10";
	
	public static final String API_TAGS_ENTITY = "III-01. " + CS;
	//public static final String API_TAGS_PANEL = "II-2. " + CS;
	public static final String API_TAGS_SEARCH = "III-02. " + CS;
	
	// ------------------------------
	// Param
	// ------------------------------
	
	public static final String PARAM_VER = "version";
	public static final String PARAM_CD = "code";
	//public static final String PARAM_LP = "LP";

	// Path
	public static final String PARAM_PATH = "path";
	// Language
	public static final String PARAM_LANG = "lang";
	public static final String PARAM_PAGE = "page";
	public static final String PARAM_SIZE = "size";
	//public static final String PARAM_CRITERIA_CD = "criteria";
	
	
	// ------------------------------
	// Partial API
	// ------------------------------
	
	// CodeSystem: /ICD10
	private static final String S_CS = "/" + CS;
	// Version: /version
	private static final String S_VER = "/" + PARAM_VER;
	// Code: /{code}
	private static final String S_CD = "/{" + PARAM_CD + ":.+}";
    // CodeSystem and Code: /ICD10/{code}
	private static final String S_CS_CD = S_CS + S_CD;
 	
	
	// ------------------------------
	// Entity API
	// ------------------------------
	
	public static final String PARAM_VAL = "val";

	// getVersion: /version/ICD10
	public static final String API_GET_VERSION = S_VER + S_CS;
	// getEntityByCode: /entity/ICD10/{code}
	public static final String API_GET_ENTITY_BY_CODE = "/entity" + S_CS_CD;
	// getEntityByLP: /LP/LOINC/{LP}
	//public static final String API_GET_ENTITY_BY_LP = "/LP" + S_CS_LP;
	// getChildrenByCode: /children/LOINC/{code}
	public static final String API_GET_CHILDREN_BY_CODE = "/children" + S_CS_CD;
	public static final String API_GET_ANCESTOR_BY_CODE = "/ancestor" + S_CS_CD;
	// getDescendantListByCode: /descendants/LOINC/{code}
	//public static final String API_GET_DESCENDANTS_BY_CODE = "/descendants" + S_CS_CD;
	// getDescendantOrSelfListByCode: /descendants/LOINC/{code}
	//public static final String API_GET_DESCENDANTS_OR_SELF_BY_CODE = "/descendantsOrSelf" + S_CS_CD;
	// getParentListByCode: /parents/LOINC/{code}
	//public static final String API_GET_PARENTS_BY_CODE = "/parents" + S_CS_CD;
	// getAncestorListByCode: /ancestors/LOINC/{code}
	//public static final String API_GET_ANCESTORS_BY_CODE = "/ancestors" + S_CS_CD;
	// getAncestorListByCode: /ancestors/LOINC/{code}
	//public static final String API_GET_ANCESTORS_OR_SELF_BY_CODE = "/ancestorsOrSelf" + S_CS_CD;
	// getPathListByCode; /paths/LOINC/{code}
	//public static final String API_GET_PATHS_BY_CODE = "/paths" + S_CS_CD;
	// getEntityList; /entities/LOINC
	//public static final String API_GET_ENTITIES = "/entities" + S_CS;

	
	// ------------------------------
	// Search API
	// ------------------------------

	public static final String PARAM_Q = "q";
	//public static final String PARAM_PART = "part";
	
	// search: /search/LOINC
	public static final String API_GET_SRCH = "/search" + S_CS;
	
	
	// ------------------------------
	// Panel API
	// ------------------------------
	
	// getPanel: /panel/LOINC
	//public static final String API_GET_PANEL_BY_CODE = "/panel" + S_CS_CD;
	
}
