package co.infoclinic.term.snomedct.api;

/**
 * Query API를 정의하는 클래스
 * 
 * 규칙. 파라메터 이름은 소문자를 사용할 것.
 * 규칙. 파라메터 값으로 Boolean을 사용하지말고 1|0으로 사용할 것.
 *
 */
public final class QryApi {

	public static final String CS = "SNOMEDCT";
	
	public static final String API_TAGS_ENTITY = "I-01 " + CS;
	public static final String API_TAGS_CONSTRAINT = "I-02 " + CS;
	public static final String API_TAGS_EXPRESSION = "I-03 " + CS;
	public static final String API_TAGS_DESCRIPTION = "I-04 " + CS;
	public static final String API_TAGS_ASSOCIATION = "I-05 " + CS;
	public static final String API_TAGS_REFERENCESET = "I-06 " + CS;
	public static final String API_TAGS_MEMBER = "I-07 " + CS;
	public static final String API_TAGS_MRCM = "I-08 " + CS;
	public static final String API_TAGS_HISTORY = "I-09 " + CS;
	public static final String API_TAGS_SEARCH = "I-10 " + CS;
	
	// ------------------------------
	// Param
	// ------------------------------
	
	public static final String PARAM_VER = "version";
	public static final String PARAM_CD = "code";
	public static final String PARAM_ACT = "active";
	public static final String PARAM_TYPEID = "typeId";
	public static final String PARAM_PAGE = "page";
	public static final String PARAM_SIZE = "size";
	public static final String PARAM_CRITERIA = "criteria";
	public static final String PARAM_TYPE = "type";
	public static final String PARAM_CG = "cg";
	
	
	// ------------------------------
	// Param Comment
	// ------------------------------
	
	public static final String PARAM_VER_CMNT = "Version (eg. v20160731)"; 
	public static final String PARAM_CD_CMNT = "Code (6~18 digit)";
	public static final String PARAM_ACT_CMNT = "Active State";
	public static final String PARAM_CG_CMNT = "Compositional Grammar";
	public static final String PARAM_SIZE_CMNT = "Size (default 20, per 1)";
	
	// ------------------------------
	// Partial API
	// ------------------------------
	
	// CodeSystem: /SNOMEDCT
	private static final String S_CS = "/" + CS;
	// /{code}
	private static final String S_CD = "/{" + PARAM_CD + "}";
    // /SNOMEDCT/{code}
	private static final String S_CS_CD = S_CS + S_CD;
	
	
	// ------------------------------
	// Concept API
	// ------------------------------

	// getVersion: /version/SNOMEDCT
	public static final String API_GET_VERSION = "/version" + S_CS;
	// getConceptByCode: /entity/SNOMEDCT/{code}
	public static final String API_GET_ENTITY_BY_CODE = "/entity" + S_CS_CD;
	// getChildrenByCode: /children/SNOMEDCT/{code}
	public static final String API_GET_CHILDREN_BY_CODE = "/children" + S_CS_CD;
	// getDescendantListByCode: /descendants/SNOMEDCT/{code}
	public static final String API_GET_DESCENDANTS_BY_CODE = "/descendants" + S_CS_CD;
	// getDescendantListOrSelfByCode: /descendantsOrSelf/SNOMEDCT/{code}
	public static final String API_GET_DESCENDANTS_OR_SELF_BY_CODE = "/descendantsOrSelf" + S_CS_CD;
	// getDescendantTreeByCode: /descendantsTree/SNOMEDCT/{code}
	public static final String API_GET_DESCENDANTS_TREE_BY_CODE = "/descendantsTree" + S_CS_CD;
	// getParentListByCode: /parents/SNOMEDCT/{code}
	public static final String API_GET_PARENTS_BY_CODE = "/parents" + S_CS_CD;
	// getAncestorListByCode: /ancestors/SNOMEDCT/{code}
	public static final String API_GET_ANCESTORS_BY_CODE = "/ancestors" + S_CS_CD;
	// getAncestorListByOrSelfCode: /ancestors/SNOMEDCT/{code}
	public static final String API_GET_ANCESTORS_OR_SELF_BY_CODE = "/ancestorsOrSelf" + S_CS_CD;
	
	//ecl query...
	public static final String API_GET_ENTITIES = "/entities" + S_CS;
	
	// subsumptionTest: /subsumption/SNOMEDCT
	public static final String API_GET_SUBSUMPTION = "/subsumption" + S_CS;

	// getPostcoordinatedExpression: /postexpr/SNOMEDCT/{version}/{code}
	public static final String API_GET_POST_EXPR = "/postexpr" + S_CS_CD;
	
	
	// ------------------------------
	// Description API
	// ------------------------------
	
	public static final String PARAM_LANGGR = "langgroup";
	
	public static final String API_DESCRIPTION_LIST = "/descriptions" + S_CS;
	// getDescriptionListByCode: /description/SNOMEDCT/{code}
	public static final String API_DESCRIPTION_LIST_BY_CODE = "/descriptions" + S_CS_CD;

	
	// ------------------------------
	// Relationship API
	// ------------------------------
	
	public static final String PARAM_STATED_CMNT = "is stated relationship?";
	
	public static final String PARAM_STATED = "stated";
	
	// getRelationshipListByCode: /relationships/SNOMEDCT/{code}
	public static final String API_GET_ASSOCIATION_LIST_BY_CODE = "/associations" + S_CS_CD;
	
	
	// ------------------------------
	// Refset API
	// ------------------------------
	
	public static final String PARAM_REFSET_ID = "refsetid";
	public static final String PARAM_REFCPNT_ID = "refcpntid";
	
	public static final String API_GET_REFSET_MBR_LIST = "/members" + S_CS;
	
	
	// ------------------------------
	// MRCM API
	// ------------------------------
	
	public static final String PARAM_ATTR_ID_CMNT = "Attribute ID";
	
	public static final String PARAM_ATTR_ID = "attributeid";
	
	public static final String S_ATTR_ID = "/{" + PARAM_ATTR_ID + "}";
	
	// getAllowDefiningAttributes: /mrcmAllowDefiningAttrs/SNOMEDCT/{code}
	//public static final String API_GET_MRCM_ALLOW_DEF_ATTR_LIST = "/mrcmAllowDefAttrs" + S_CS_CD;
	public static final String API_GET_MRCM_ALLOW_ATTR_LIST_BY_CODE = "/allow/attributes" + S_CS_CD;
	// getMrcmValues: /mrcmValues/SNOMEDCT; code is attributeId
	//public static final String API_GET_MRCM_VALUE_LIST = "/mrcmValues" + S_CS;
	public static final String API_GET_MRCM_ALLOW_VALUE_LIST_BY_CODE = "/allow/invalues" + S_CS + S_ATTR_ID;
	
	
	// ------------------------------
	// History API
	// ------------------------------
	
	public static final String API_GET_HISTORY_LIST_BY_CODE = "/histories" + S_CS_CD;
	
	
	
	// ------------------------------
	// Search API
	// ------------------------------
	
	public static final String PARAM_MATCH_CMNT = "Match Type (PARTIAL, REGEX, FULLTEXT)";
	public static final String PARAM_STATE_CMNT = "Entity Status Type (ACTIVE, INACTIVE, BOTH)";
	public static final String PARAM_Q_CMNT = "Search Word (SNOMEDCT ID, String ...)";
	public static final String PARAM_SEMFILTER_CMNT = "SemanticTag Filter (eg. disorder,procedure)";
	public static final String PARAM_RANGE_ID_CMNT = "범위 아이디";
	
	
	public static final String PARAM_MATCH = "match";
	public static final String PARAM_STATE = "state";
	public static final String PARAM_Q = "q";
	public static final String PARAM_SEMFILTER = "semanticfilter";
	public static final String PARAM_RANGE_ID = "rangeid";
	
	// getSearchTerm: /search/SNOMEDCT
	public static final String API_GET_SRCH_TRM = "/search" + S_CS;
	// getSuggestSearchTerm: /search/SNOMEDCT
	public static final String API_GET_SRCH_SGST_TRM = "/search/suggest" + S_CS;
	// getPathList: /paths/SNOMEDCT
	public static final String API_GET_PATH_LIST = "/paths" + S_CS_CD;

	
}
