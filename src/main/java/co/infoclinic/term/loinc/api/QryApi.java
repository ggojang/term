package co.infoclinic.term.loinc.api;

public class QryApi {

	// Code System
	public static final String CS = "LOINC";
	
	public static final String API_TAGS_ENTITY = "II-01. " + CS;
	public static final String API_TAGS_LP = "II-02. " + CS;
	public static final String API_TAGS_LG = "II-03. " + CS;
	public static final String API_TAGS_LA = "II-04. " + CS;
	public static final String API_TAGS_LV = "II-05. " + CS;
	public static final String API_TAGS_PANEL = "II-06. " + CS;
	public static final String API_TAGS_SEARCH = "II-07. " + CS;
	
	// ------------------------------
	// Param
	// ------------------------------
	
	public static final String PARAM_VER = "version";
	public static final String PARAM_CD = "code";
	
	// Path
	public static final String PARAM_PATH = "path";
	// Language
	public static final String PARAM_LANG = "lang";
	public static final String PARAM_PAGE = "page";
	public static final String PARAM_SIZE = "size";
	public static final String PARAM_CRITERIA_CD = "criteria";
	
	
	// ------------------------------
	// Partial API
	// ------------------------------
	
	// CodeSystem: /LOINC
	private static final String S_CS = "/" + CS;
	// Version: /version
	private static final String S_VER = "/" + PARAM_VER;
	// Code: /{code}
	private static final String S_CD = "/{" + PARAM_CD + ":.+}";
	
    // CodeSystem and Code: /LOINC/{code}
	private static final String S_CS_CD = S_CS + S_CD;
 
	// subsumption: /subsumption/LOINC
	public static final String API_GET_SUBSUMPTION = "/subsumption" + S_CS;
	
	
	// ------------------------------
	// Entity API
	// ------------------------------
	
	public static final String PARAM_VAL = "val";

	// getVersion: /version/LOINC
	public static final String API_GET_VERSION = S_VER + S_CS;
	// getEntityByCode: /entity/LOINC/{code}
	public static final String API_GET_ENTITY_BY_CODE = "/entity" + S_CS_CD;
	// getEntityByLP: /LP/LOINC/{code}
	public static final String API_GET_LP_BY_CODE = "/LP" + S_CS_CD;
	public static final String API_GET_LPLINK_BY_CODE = "/LPLINK" + S_CS_CD;
	public static final String API_GET_LPMAP_BY_CODE = "/LPMAP" + S_CS_CD;
	public static final String API_GET_LG_BY_CODE = "/LG" + S_CS_CD;
	public static final String API_GET_LGATTR_BY_CODE = "/LGATTR" + S_CS_CD;
	public static final String API_GET_LGTERM_BY_CODE = "/LGTERM" + S_CS_CD;
	public static final String API_GET_LGP_BY_CODE = "/LGP" + S_CS_CD;
	public static final String API_GET_LGPATTR_BY_CODE = "/LGPATTR" + S_CS_CD;
	public static final String API_GET_LA_BY_CODE = "/LA" + S_CS_CD;
	public static final String API_GET_LALINK_BY_CODE = "/LALINK" + S_CS_CD;
	public static final String API_GET_LV_BY_CODE = "/LV" + S_CS_CD;
	// getChildrenByCode: /children/LOINC/{code}
	public static final String API_GET_CHILDREN_BY_CODE = "/children" + S_CS_CD;
	// getDescendantListByCode: /descendants/LOINC/{code}
	public static final String API_GET_DESCENDANTS_BY_CODE = "/descendants" + S_CS_CD;
	// getDescendantOrSelfListByCode: /descendants/LOINC/{code}
	public static final String API_GET_DESCENDANTS_OR_SELF_BY_CODE = "/descendantsOrSelf" + S_CS_CD;
	// getParentListByCode: /parents/LOINC/{code}
	public static final String API_GET_PARENTS_BY_CODE = "/parents" + S_CS_CD;
	// getAncestorListByCode: /ancestors/LOINC/{code}
	public static final String API_GET_ANCESTORS_BY_CODE = "/ancestors" + S_CS_CD;
	// getAncestorListByCode: /ancestors/LOINC/{code}
	public static final String API_GET_ANCESTORS_OR_SELF_BY_CODE = "/ancestorsOrSelf" + S_CS_CD;
	// getPathListByCode; /paths/LOINC/{code}
	public static final String API_GET_PATHS_BY_CODE = "/paths" + S_CS_CD;
	// getEntityList; /entities/LOINC
	public static final String API_GET_ENTITIES = "/entities" + S_CS;

	
	// ------------------------------
	// Search API
	// ------------------------------

	public static final String PARAM_Q = "q";
	public static final String PARAM_PART = "part";
	
	// search: /search/LOINC
	public static final String API_GET_SRCH = "/search" + S_CS;
	
	
	// ------------------------------
	// Panel API
	// ------------------------------
	
	// getPanel: /panel/LOINC
	public static final String API_GET_PANEL_BY_CODE = "/panel" + S_CS_CD;
	
}
