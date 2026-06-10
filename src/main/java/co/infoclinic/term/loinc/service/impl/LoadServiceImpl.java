package co.infoclinic.term.loinc.service.impl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.infoclinic.term.loinc.model.entity.Hierarchy;
import co.infoclinic.term.loinc.model.entity.LinguisticVariant;
import co.infoclinic.term.loinc.model.entity.LinguisticVariants;
import co.infoclinic.term.loinc.model.entity.Loinc;
import co.infoclinic.term.loinc.model.entity.MapTo;
import co.infoclinic.term.loinc.model.entity.Panel;
import co.infoclinic.term.loinc.model.entity.SourceOrganization;
import co.infoclinic.term.loinc.repository.HierarchyRepository;
import co.infoclinic.term.loinc.repository.LinguisticVariantRepository;
import co.infoclinic.term.loinc.repository.LinguisticVariantsRepository;
import co.infoclinic.term.loinc.repository.LoincRepository;
import co.infoclinic.term.loinc.repository.MapToRepository;
import co.infoclinic.term.loinc.repository.PanelRepository;
import co.infoclinic.term.loinc.repository.SourceOrganizationRepository;
import co.infoclinic.term.loinc.service.LoadService;

/**
 * LOINC Load Service
 */
@Service("LoincLoadService")
public class LoadServiceImpl implements LoadService {
	
	Logger log = LoggerFactory.getLogger(LoadServiceImpl.class);
	
	// FIXME: Get path from properties
	private static final String ROOT = "/Users/dongwon/Documents/repo/smc/resources/LOINC_2.56";
	
	private static final String PATH_LOINC = ROOT + "/loinc.csv";
	private static final String PATH_HIERARHCY = ROOT + "/AccessoryFiles/LOINC_2.56_MULTI-AXIAL_HIERARCHY.csv";
	private static final String PATH_CLASS = ROOT + "/Extension/loinc_class.csv";
	
	private static final String PARTS = "PARTS";
	private static final String PARTS_WAVE = "PARTS~";
	private static final String DELIMITER_TAB = "	";
	
	// LOINC Code & PreferredName Map
	private Map<String, String> codePreferredNameMap;
	// Class Abbreviation & Path Map
	//private Map<String, String> clsAbbrPathMap;
	// Path & Children Set Map
	private Map<String, Set<String>> pathChildCodesMap;
	// Path & Hierarchy Entity Map
	private Map<String, Hierarchy> pathHierarchyMap;
	
	private Map<String, Set<String>> pathDescendantCodesMap;
	
	// Panel
	Map<String, String> parentIdParentLoincIdMap = new HashMap<String, String>();
	Map<String, Panel> currentIdPanelMap = new HashMap<String, Panel>();
	Map<String, Set<String>> parentIdCurrentIdSetMap = new HashMap<String, Set<String>>();
	
	
	/** DI: Loinc Repository */
	@Autowired
	private LoincRepository lncRepo;
	
	/** DI: Hierarchy Repository */
	@Autowired
	private HierarchyRepository hierRepo;
	
	/** DI: MapTo Repository */
	@Autowired
	private MapToRepository mapToRepo;
	
	/** DI: SourceOrganization Repository */
	@Autowired
	private SourceOrganizationRepository srcOrgRepo;
	
	/** DI: LinguisticVariants Repository */
	@Autowired
	private LinguisticVariantsRepository lingVarsRepo;
	
	/** DI: LinguisticVariant Repository */
	@Autowired
	private LinguisticVariantRepository lingVarRepo;
	
	/** DI: Panel Repository */
	@Autowired
	private PanelRepository panelRepo;
	
	// ----------------------------------------
	// Public
	// ----------------------------------------

	/**
	 * Load LOINC Resources
	 */
	@Override
	@Transactional
	public boolean load() {
		
		// LOINC 읽고 저장, 계층구조 읽고 저장
		// processLncAndHierarchy();
		
		// mapTo 읽고 저장
		//processMapTo();
		
		// sourceOrganization 읽고 저장
		//processSrcOrg();
		
		// Linguistic Variants 읽고 저장
		//processLingVars();
		
		// Linguistic Variant 읽고 저장
		//processLingVar("ko_KR", 13);
		//processLingVar("zh_CN", 5);
		
		// Panels and Forms 읽고 저장
		//processPanel();
		
		return true;
	}
	
	// ----------------------------------------
	// Private
	// ----------------------------------------
	
	
	/**
	 * LOINC 테이블과 계층구조 읽고 저장
	 * 
	 * @return
	 */
	private boolean processLncAndHierarchy() {
		codePreferredNameMap = new HashMap<String, String>();
		//clsAbbrPathMap = new HashMap<String, String>();
		pathChildCodesMap = new HashMap<String, Set<String>>();
		pathHierarchyMap = new HashMap<String, Hierarchy>();
		pathDescendantCodesMap = new HashMap<String, Set<String>>();
		
		// LOINC Entity 목록
		List<Loinc> lncs;
		// LOINC Hierarchy (Root, Class, Parts) 목록
		List<Hierarchy> hiers = new ArrayList<Hierarchy>();
		
		// ------------------------------
		// LOINC 파일 읽고, Entity 목록 구성, codePreferredNameMap 구성
		// ------------------------------
		log.debug("1. LOINC 파일 읽고, Entity 목록 구성 중 .....");
		lncs = loadLoincByFile();
		log.debug("1. 완료");

		// ------------------------------
		// Hierarchy 구성
		// ------------------------------
		
		log.debug("2. 계층구조 구성 중 .....");
		// 1. 최상위 레벨 계층 Entity
		Hierarchy clsTopHier = createHierarchy("CLASS", "", "", 0, 1, "LOINCCLASSTYPES", "Class Types");
		hiers.add(clsTopHier);
		// 자식 수 계산을 위해 자식 추가
		addChild("", "CLASS", clsTopHier);
		
		Hierarchy prtTopHier = createHierarchy("PARTS", "", "", 0, 2, "LOINCPARTS", "Parts");
		hiers.add(prtTopHier);
		// 자식 수 계산을 위해 자식 추가
		addChild("", "PARTS", prtTopHier);
		
		// 2. Class 계층구조 생성
		List<Hierarchy> clsBaseHiers = loadHierarchyByClassFile();
		List<Hierarchy> clsHiers = createHierarchyByClassWithLoincFile(lncs);
		
		// 3. Parts 계층구조 생성
		List<Hierarchy> prtHiers = loadMultiHierarchy();
		
		log.debug("2. 완료");
		
		// ------------------------------
		// 자식, 자손 수 계산 및 적용
		// ------------------------------
		log.debug("3. 계층구조 자식 수 계산 중 .......");
		batchSetSubtypeCount();
		log.debug("3. 완료");
		
		// ------------------------------
		// 저장
		// ------------------------------
		log.debug("4. 저장 중 .......");
		// 1. LOINC Entity 저장
		saveLoincList(lncs);
		
		// 2. Hierarchy Entity 저장
		saveHierarchyList(clsBaseHiers);
		saveHierarchyList(clsHiers);
		saveHierarchyList(prtHiers);
		
		log.debug("4. 완료");
		
		// 5. Flush
		lncRepo.flush();
		hierRepo.flush();
		
		return true;
	}
	
	
	/**
	 * MapTo 파일 읽고 저장
	 * 
	 * @return
	 */
	private boolean processMapTo() {
		String path = ROOT + "/map_to.csv";
		String delimiter = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
		List<MapTo> entities = loadMapTo(path, delimiter);
		
		mapToRepo.save(entities);
		mapToRepo.flush();
		
		return true;
	}
	
	
	/**
	 * SourceOrganization 읽고 저장
	 * 
	 * @return
	 */
	private boolean processSrcOrg() {
		String path = ROOT + "/source_organization.csv";
		String delimiter = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
		List<SourceOrganization> entities = loadSrcOrg(path, delimiter);
		
		srcOrgRepo.save(entities);
		srcOrgRepo.flush();
		
		return true;
	}
	

	/**
	 * LinguisticVariants 읽고 저장
	 * 
	 * @return
	 */
	private boolean processLingVars() {
		String path = ROOT + "/AccessoryFiles/LinguisticVariantsFile/LOINC_2.56_LinguisticVariants.csv";
		String delimiter = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
		List<LinguisticVariants> entities = loadLingVars(path, delimiter);
		
		lingVarsRepo.save(entities);
		lingVarsRepo.flush();
		
		return true;
	}
	
	
	/**
	 * LinguisticVariant 읽고 저장
	 * 
	 * @return
	 */
	private boolean processLingVar(String langCountry, int number) {
		// ko_KR
		// zh_CN
		//String path = ROOT + "/AccessoryFiles/LinguisticVariantsFile/LOINC_2.56_ko_KR_13_LinguisticVariant.csv";
		String path = ROOT + "/AccessoryFiles/LinguisticVariantsFile/LOINC_2.56_" + langCountry + "_" + number + "_LinguisticVariant.csv";
		String delimiter = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
		// FIXME: from path
		String isoLang = "zh";
		String isoCountry = "CN";
		List<LinguisticVariant> entities = loadLingVar(path, delimiter, isoLang, isoCountry);
		
		lingVarRepo.save(entities);
		lingVarRepo.flush();
		
		return true;
	}
	
	
	private boolean processPanel() {
		
		String path = ROOT + "/LOINC_258_Panels.csv"; //ROOT + "/AccessoryFiles/LOINC_258_Panels.csv";
		String delimiter = "	(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
		
		List<Panel> entities = loadPanel("2.58", path, delimiter);
		
		panelRepo.save(entities);
		panelRepo.flush();
		
		return true;
	}
	
	
	/**
	 * Loinc Entity 추가/저장
	 * @param entities
	 * @return
	 */
	private List<Loinc> saveLoincList(List<Loinc> entities) {
		return lncRepo.save(entities);
	}
	
	
	/**
	 * Hierarchy Entity 추가/저장
	 * @param entities
	 * @return
	 */
	private List<Hierarchy> saveHierarchyList(List<Hierarchy> entities) {
		return hierRepo.save(entities);
	}
	
	
	/**
	 * Class 계층구조 Entity 생성
	 * @param lncs
	 * @return
	 */
	private List<Hierarchy> createHierarchyByClassWithLoincFile(List<Loinc> loincs) {
		List<Hierarchy> hierarchies = new ArrayList<Hierarchy>();
		Hierarchy hierarchy;
		
		String code;
		String path;
		String component;
		String preferredName;
		
		// parent = class abbr
		String className; 
		int classType = 0;
		
		Loinc loinc;
		int loincsLen = loincs.size();
		for (int i = 0; i < loincsLen; i++) {
			loinc = loincs.get(i);
			
			code = loinc.getCode();
			className = loinc.getClassName().replaceAll("\\s+", "#");
			classType = loinc.getClassType();
			
			// ClassType
			// 1: Laboratory
			// 2: Clinical
			// 3: Claims Attachment
			// 4: Surveys
			if (classType == 1) {
				path = "CLASS~LABORATORY~" + className;
			} else if (classType == 2) {
				path = "CLASS~CLINICAL~" + className;
			} else if (classType == 3) {
				path = "CLASS~CLAIMS_ATTACHMENT~" + className;
			} else if (classType == 4) {
				path = "CLASS~SURVEYS~" + className;
			} else {
				path = className;
			}

			component = loinc.getComponent();
			preferredName = loinc.getComponent() + ":" + loinc.getProperty() + ":" + loinc.getTimeAspect() + ":" + loinc.getSystem() + ":" + loinc.getScaleType() + (loinc.getMethodType().isEmpty() ? "":(":" + loinc.getMethodType()));
			
			// code, path, parent, type(=Class), sequence, name, preferredName 
			hierarchy = createHierarchy(code, path, className, 1, 1, component, preferredName);
			hierarchies.add(hierarchy);
			
			addChild(path, code, hierarchy);
		}
		
		return hierarchies;
	}
	
	
	/**
	 * LOINC 파일 읽고, Entity 목록 구성, Code&PreferredName Map 구성
	 * @return
	 */
	private List<Loinc> loadLoincByFile() {
		List<Loinc> lncs = new ArrayList<Loinc>();
		Loinc lnc;
		
		FileReader fr = null;
		BufferedReader br = null;
		String line = null;
		
		try {
			fr = new FileReader(PATH_LOINC);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		br = new BufferedReader(fr);
		try {
			
			String prefName = "";
			
			// skip first line (header)
			br.readLine();
			while ((line = br.readLine()) != null) {
				// 필드가 ""로 감싸져있으므로 제거
				//line = line.replace("\"", "");
				// 필드가 ,로 구분 지어짐; 두 번째 매개변수인 limit을 적어주지 않으면 39개까지만 분리가 되어 개수를 적어주었음.
				String[] f = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", 44);
				
				lnc = new Loinc();
				
				// LOINC_NUM","COMPONENT","PROPERTY","TIME_ASPCT","SYSTEM","SCALE_TYP","METHOD_TYP","CLASS",
				
				String lncNum = removeDoubleQuotes(f[0]);
				String cpnt = removeDoubleQuotes(f[1]);
				String prop = removeDoubleQuotes(f[2]);
				String timeAspct = removeDoubleQuotes(f[3]);
				String sys = removeDoubleQuotes(f[4]);
				String scaleTyp = removeDoubleQuotes(f[5]);
				String methodTyp = removeDoubleQuotes(f[6]);
				String clsNm = removeDoubleQuotes(f[7]);
				
				lnc.setCode(lncNum);
				lnc.setComponent(cpnt);
				lnc.setProperty(prop);
				lnc.setTimeAspect(timeAspct);
				lnc.setSystem(sys);
				lnc.setScaleType(scaleTyp);
				lnc.setMethodType(methodTyp);
				lnc.setClassName(clsNm);
				
				//"VersionLastChanged","CHNG_TYPE","DefinitionDescription","STATUS","CONSUMER_NAME","CLASSTYPE",
				
				String lCVer = removeDoubleQuotes(f[8]);
				String chngTyp = removeDoubleQuotes(f[9]);
				String defDesc = removeDoubleQuotes(f[10]);
				String stat = removeDoubleQuotes(f[11]);
				String csmrNm = removeDoubleQuotes(f[12]);
				int clsTyp = 0;
				
				try {
					clsTyp = Integer.parseInt(f[13]);
				} catch (NumberFormatException | NullPointerException e) {
					clsTyp = 0;
				}
				
				lnc.setLastChangedVersion(lCVer);
				lnc.setChangeType(chngTyp);
				lnc.setDefinitionDescription(defDesc);
				lnc.setStatus(stat);
				lnc.setConsumerName(csmrNm);
				lnc.setClassType(clsTyp);
				
				//"FORMULA","SPECIES","EXMPL_ANSWERS","SURVEY_QUEST_TEXT","SURVEY_QUEST_SRC","UNITSREQUIRED",
				
				String fm = removeDoubleQuotes(f[14]);
				//String spec = removeDoubleQuotes(f[15]);
				String examAsr = removeDoubleQuotes(f[15]);
				String sQstTxt = removeDoubleQuotes(f[16]);
				String sQstSrc = removeDoubleQuotes(f[17]);
				String untsReqd = removeDoubleQuotes(f[18]);
				
				lnc.setFormula(fm);
				//lnc.setSpecies(spec);
				lnc.setExampleAnswers(examAsr);
				lnc.setSurveyQuestText(sQstTxt);
				lnc.setSurveyQuestSrc(sQstSrc);
				lnc.setUnitsRequired(untsReqd);
				
				//"SUBMITTED_UNITS","RELATEDNAMES2","SHORTNAME","ORDER_OBS","CDISC_COMMON_TESTS","HL7_FIELD_SUBFIELD_ID",
				
				String sbmtUnits = removeDoubleQuotes(f[19]);
				String relNm2 = removeDoubleQuotes(f[20]);
				String srtNm = removeDoubleQuotes(f[21]);
				String odrObs = removeDoubleQuotes(f[22]);
				String cdiscCmnTst = removeDoubleQuotes(f[23]);
				String hl7FldSubfldId = removeDoubleQuotes(f[24]);
				
				lnc.setSubmittedUnits(sbmtUnits);
				lnc.setRelatedNames2(relNm2);
				lnc.setShortName(srtNm);
				lnc.setOrderObs(odrObs);
				lnc.setCdiscCommonTests(cdiscCmnTst);
				lnc.setHl7FieldSubfieldId(hl7FldSubfldId);
				
				//"EXTERNAL_COPYRIGHT_NOTICE","EXAMPLE_UNITS","LONG_COMMON_NAME","UnitsAndRange","DOCUMENT_SECTION",
				
				String exCopyNtc = removeDoubleQuotes(f[25]);
				String exUnits = removeDoubleQuotes(f[26]);
				String lgCmnNm = removeDoubleQuotes(f[27]);
				String untsNRng = removeDoubleQuotes(f[28]);
				//String docSec = removeDoubleQuotes(f[30]);
				
				lnc.setExternalCopyrightNotice(exCopyNtc);
				lnc.setExampleUnits(exUnits);
				lnc.setLongCommonName(lgCmnNm);
				lnc.setUnitsAndRange(untsNRng);
				//lnc.setDocumentSection(docSec);
				
				//"EXAMPLE_UCUM_UNITS","EXAMPLE_SI_UCUM_UNITS","STATUS_REASON","STATUS_TEXT","CHANGE_REASON_PUBLIC",
				
				String exUcumUnts = removeDoubleQuotes(f[29]);
				String exSiUcumUnts = removeDoubleQuotes(f[30]);
				String statRsn = removeDoubleQuotes(f[31]);
				String statTxt = removeDoubleQuotes(f[32]);
				String chngRsnPblc = removeDoubleQuotes(f[33]);
				
				lnc.setExampleUcumUnits(exUcumUnts);
				lnc.setExampleSiUcumUnits(exSiUcumUnts);
				lnc.setStatusReason(statRsn);
				lnc.setStatusText(statTxt);
				lnc.setChangeReasonPublic(chngRsnPblc);
				
				//"COMMON_TEST_RANK","COMMON_ORDER_RANK","COMMON_SI_TEST_RANK","HL7_ATTACHMENT_STRUCTURE",
				
				int cmnTstRk = 0; // = Integer.parseInt(f[36]);
				int cmnOdrRk = 0; // Integer.parseInt(f[37]);
				int cmnSiTstRk = 0; // = Integer.parseInt(f[38]);
				String hl7AtmtStr = removeDoubleQuotes(f[37]);
				
				try {
					cmnTstRk = Integer.parseInt(f[34]);
				} catch (NumberFormatException | NullPointerException e) {
					cmnTstRk = 0;
				}
				
				try {
					cmnOdrRk = Integer.parseInt(f[35]);
				} catch (NumberFormatException | NullPointerException e) {
					cmnOdrRk = 0;
				}
				
				try {
					cmnSiTstRk = Integer.parseInt(f[36]);
				} catch (NumberFormatException | NullPointerException e) {
					cmnSiTstRk = 0;
				}
				
				lnc.setCommonTestRank(cmnTstRk);
				lnc.setCommonOrderRank(cmnOdrRk);
				lnc.setCommonSiTestRank(cmnSiTstRk);
				lnc.setHl7AttachmentStructure(hl7AtmtStr);
				
				//"EXTERNAL_COPYRIGHT_LINK","PanelType","AskAtOrderEntry","AssociatedObservations"
				
				String exCopyLnk = removeDoubleQuotes(f[38]);
				String panelTyp = removeDoubleQuotes(f[39]);
				String askOdrEty = removeDoubleQuotes(f[40]);
				String asObs = removeDoubleQuotes(f[41]);
				
				lnc.setExternalCopyrightLink(exCopyLnk);
				lnc.setPanelType(panelTyp);
				lnc.setAskAtOrderEntry(askOdrEty);
				lnc.setAssociatedObservations(asObs);
				
				//"VersionFirstReleased","ValidHL7AttachmentRequest","DisplayName"
				
				String firstRelVer = removeDoubleQuotes(f[42]);
				String valHL7AttReq = removeDoubleQuotes(f[43]);
				String dispName = removeDoubleQuotes(f[44]);
				
				lnc.setFirstReleasedVersion(firstRelVer);
				lnc.setValidHL7AttachmentRequest(valHL7AttReq);
				lnc.setDisplayName(dispName);
				
				
				lncs.add(lnc);
				
				prefName = cpnt + ":" + prop + ":" + timeAspct + ":" + sys + ":" + scaleTyp + (methodTyp.isEmpty() ? "":(":" + methodTyp));
				codePreferredNameMap.put(lncNum, prefName);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return lncs;
	}
	
	
	/**
	 * Parts 계층구조 파일 읽고, Entity 목록 구성
	 * @return
	 */
	private List<Hierarchy> loadMultiHierarchy() {
		List<Hierarchy> hierarchies = new ArrayList<Hierarchy>();
		Hierarchy hierarchy;
		
		// 한줄 읽은 후 구분하는 정규식; e.g. https://regex101.com/r/dM3wM7/16
		
		String splitExpr = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
		/*
		String splitExpr = 
				",          " + // Split on comma; Comma로 분리
				"(?=        " + // Followed by expr; 다음을 따름
				"  (?:      " + // Start a non-capture group; 파서가 이 그룹을 잡아내지 않도록 한다.
				"    [^\"]* " + // 0 or more non-quote characters; 0개 이상의 따옴표가 아닌 문자열들
				"    \"     " + // 1 quote; 1개의 따옴표
				"    [^\"]* " + // 0 or more non-quote characters; 0개 이상의 따옴표가 아닌 문자열들
				"    \"     " + // 1 quote; 1개의 따옴표
				"  )*       " + // 0 or more repetition of non-capture group (multiple of 2 quotes will be even);
				"  [^\"]*   " + // Finally 0 or more non-quotes; 마지막으로 0개 이상의 따옴표가 아닌 문자열들 
				"  $        " + // Till the end (This is necessary, else every comma will satisfy the condition); 끝을 알림 (모든 쉼표가 그 조건을 충족하기위해 필요하다)
				")";
		*/
		
		FileReader fr = null;
		BufferedReader br = null;
		String line = null;
		
		try {
			fr = new FileReader(PATH_HIERARHCY);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		br = new BufferedReader(fr);
		try {
			// skip first line (header)
			String parentPath;
			int seq;
			String parentCode;
			String code;
			String term;
			String preferredName;
			br.readLine();
			while ((line = br.readLine()) != null) {
				// Multi-axial Hierarchy csv 파일구조
				// 0: pathToParent
				// 1: sequence
				// 2: immeidateParent
				// 3: code
				// 4: codeText
				// -----------
				
				String[] f = line.split(splitExpr);
				
				// PATH_TO_ROOT
				parentPath = f[0];
				// SEQUENCE
				try {
					seq = Integer.parseInt(f[1]);
				} catch (NumberFormatException | NullPointerException e) {
					seq = 0;
				}
				// IMMEDIATE_PARENT
				parentCode = f[2];
				// CODE
				code = f[3];
				// CODE_TEXT
				term = removeDoubleQuotes(f[4]);
								
				if (!code.contains("LP")) {
					String temp = codePreferredNameMap.get(code);
					preferredName = temp != null ? temp:term;
				} else {
					preferredName = term;
				}
				
				parentPath = parentPath.replace(".", "~");
				parentPath = !"".equals(parentPath) ? PARTS_WAVE + parentPath:PARTS;
				
				if ("".equals(parentCode)) {
					parentCode = PARTS;
				}
				
				hierarchy = createHierarchy(code, parentPath, parentCode, 2, seq, term, preferredName);
				hierarchies.add(hierarchy);
				
				// 자식 수 계산을 위해 자식 추가
				addChild(parentPath, code, hierarchy);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return hierarchies;
	}
	
	
	/**
	 * Class 파일 읽고, 계층구조 Entity 목록 구성, Class의 약어 & Path Map 구성
	 * @param classHiers
	 * @return
	 */
	private List<Hierarchy> loadHierarchyByClassFile() {
		List<Hierarchy> clsHiers = new ArrayList<Hierarchy>();
		Hierarchy hier;
		
		FileReader fr = null;
		BufferedReader br = null;
		String line = null;
		
		try {
			fr = new FileReader(PATH_CLASS);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		br = new BufferedReader(fr);
		try {
			String rootClass = "CLASS";
			String rootCode;
			String rootName;
			
			// create Laboratory Node
					
			rootCode = "LABORATORY";
			rootName = "Laboratory Class";
					
			Hierarchy clsHierLab = createHierarchy(rootCode, rootClass, rootClass, 1, 0, rootName, rootName);
			clsHiers.add(clsHierLab);
			// 자식 수 계산을 위해 자식 추가
			addChild(rootClass, rootCode, clsHierLab);
			
			// create Clinical Node
			
			rootCode = "CLINICAL";
			rootName = "Clinical Class";
			
			Hierarchy clsHierCli = createHierarchy(rootCode, rootClass, rootClass, 1, 0, rootName, rootName);
			clsHiers.add(clsHierCli);
			// 자식 수 계산을 위해 자식 추가
			addChild(rootClass, rootCode, clsHierCli);
			
			// create Claims Attachment Node
			
			rootCode = "CLAIMS_ATTACHMENT";
			rootName = "Claims Attachment";
			Hierarchy clsHierClm = createHierarchy(rootCode, rootClass, rootClass, 1, 0, rootName, rootName);
			clsHiers.add(clsHierClm);
			// 자식 수 계산을 위해 자식 추가
			addChild(rootClass, rootCode, clsHierClm);
			
			// create Surveys Node
			
			rootCode = "SURVEYS";
			rootName = "Surveys";
			
			Hierarchy clsHierSrv = createHierarchy(rootCode, rootClass, rootClass, 1, 0, rootName, rootName);
			clsHiers.add(clsHierSrv);
			// 자식 수 계산을 위해 자식 추가
			addChild(rootClass, rootCode, clsHierSrv);

			// skip first line (header)
			br.readLine();
			while ((line = br.readLine()) != null) {
				
				String[] f = line.split(DELIMITER_TAB);
				// type
				//String type = f[0];
				// name
				String parent = f[1].replaceAll("\\s+","_").toUpperCase();

				// abbreviation
				String abbr = f[2];
				// term
				String name = f[3];
				
				// e.g. CLASS~Laboratory
				String path = "CLASS~" + parent;
				
				hier = createHierarchy(abbr, path, parent, 1, 0, name, name);
				
				// 클래스들의 계층
				clsHiers.add(hier);
				
				// key: 약어, value: 루트까지의 경로
				//clsAbbrPathMap.put(abbr, path);
				
				// 자식 수 계산을 위해 자식 추가
				addChild(path, abbr, hier);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return clsHiers;
	}
	
	
	/**
	 * MapTo 파일 읽고, Entity 만들어 반환
	 * 
	 * @param path mapTo파일 경로
	 * @return
	 */
	private List<MapTo> loadMapTo(String path, String delimiter) {
		List<MapTo> entities = new ArrayList<MapTo>();
		MapTo entity;
		
		FileReader fr = null;
		BufferedReader br = null;
		String line = null;
		
		try {
			fr = new FileReader(path);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		br = new BufferedReader(fr);
		try {
			// skip first line (header)
			br.readLine();
			while ((line = br.readLine()) != null) {
				
				String[] f = line.split(delimiter, 3);
				
				// LOINC Code; orig: LOINC
				String code = removeDoubleQuotes(f[0]);
				// LOINC MapTo; orig: MAP_TO
				String mapTo = removeDoubleQuotes(f[1]);
				// Comment; orig: COMMENT
				String cmt = removeDoubleQuotes(f[2]);
				
				entity = createMapTo(code, mapTo, cmt);
				entities.add(entity);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return entities;
	};
	
	
	/**
	 * Source Organization 파일 읽고, Entity 만들어 반환
	 * 
	 * @param path sourceOrganization 파일 경로
	 * @return
	 */
	private List<SourceOrganization> loadSrcOrg(String path, String delimiter) {
		List<SourceOrganization> entities = new ArrayList<SourceOrganization>();
		SourceOrganization entity;
		
		FileReader fr = null;
		BufferedReader br = null;
		String line = null;
		
		try {
			fr = new FileReader(path);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		br = new BufferedReader(fr);
		try {
			// skip first line (header)
			br.readLine();
			while ((line = br.readLine()) != null) {
				
				String[] f = line.split(delimiter, 5);
				
				// CopyrightId
				String cpyId = removeDoubleQuotes(f[0]);
				// Name
				String name = removeDoubleQuotes(f[1]);
				// Copyright
				String cpy = removeDoubleQuotes(f[2]);
				// TermsOfUse
				String tou =  removeDoubleQuotes(f[3]);
				// Url
				String url =  removeDoubleQuotes(f[4]);
				
				
				entity = createSrcOrg(cpyId, name, cpy, tou, url);
				entities.add(entity);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return entities;
	};
	
	
	/**
	 * Linguistic Variants 파일 읽고, Entity 만들어 반환
	 * 
	 * @param path linguistic variants 파일 경로
	 * @return
	 */
	private List<LinguisticVariants> loadLingVars(String path, String delimiter) {
		List<LinguisticVariants> entities = new ArrayList<LinguisticVariants>();
		LinguisticVariants entity;
		
		FileReader fr = null;
		BufferedReader br = null;
		String line = null;
		
		try {
			fr = new FileReader(path);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		br = new BufferedReader(fr);
		try {
			// skip first line (header)
			br.readLine();
			while ((line = br.readLine()) != null) {
				
				String[] f = line.split(delimiter, 5);
				
				// ISO Language
				String isoLang = removeDoubleQuotes(f[1]);
				// ISO Country
				String isoCty = removeDoubleQuotes(f[2]);
				// Language Name
				String lang= removeDoubleQuotes(f[3]);
				// Producer
				String pd =  removeDoubleQuotes(f[4]);
				
				
				entity = createLingVars(isoLang, isoCty, lang, pd);
				entities.add(entity);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return entities;
	};
	
	
	/**
	 * Linguistic Variants 파일 읽고, Entity 만들어 반환
	 * 
	 * @param path linguistic variants 파일 경로
	 * @return
	 */
	private List<LinguisticVariant> loadLingVar(String path, String delimiter, String isoLang, String isoCountry) {
		List<LinguisticVariant> entities = new ArrayList<LinguisticVariant>();
		LinguisticVariant entity;
		
		FileReader fr = null;
		BufferedReader br = null;
		String line = null;
		
		try {
			fr = new FileReader(path);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		br = new BufferedReader(fr);
		try {
			// skip first line (header)
			br.readLine();
			while ((line = br.readLine()) != null) {
				
				String[] f = line.split(delimiter, 11);
				
				// LOINC Code
				String code = removeDoubleQuotes(f[0]);
				// Component
				String cpnt = removeDoubleQuotes(f[1]);
				// Property
				String prop = removeDoubleQuotes(f[2]);
				// TimeAspect
				String time = removeDoubleQuotes(f[3]);
				// System
				String system = removeDoubleQuotes(f[4]);
				// ScaleType
				String scale = removeDoubleQuotes(f[5]);
				// MethodType
				String method = removeDoubleQuotes(f[6]);
				// ClassName
				String cls = removeDoubleQuotes(f[7]);
				// ShortName
				String sName = removeDoubleQuotes(f[8]);
				// LongCommonName
				String lName = removeDoubleQuotes(f[9]); 
				// RelatedNames2
				String rNames2 = removeDoubleQuotes(f[10]);
			
				entity = createLingVar(code, cpnt, prop, time, system, scale, method, cls, sName, lName, rNames2, isoLang, isoCountry);
				entities.add(entity);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return entities;
	};
	
	
	/**
	 * Panel 파일 읽고, Entity 만들어 반환
	 * 
	 * @param path panel 파일 경로
	 * @param delimiter 구분자
	 * @return
	 */
	private List<Panel> loadPanel(String version, String path, String delimiter) {
		List<Panel> entities = new ArrayList<Panel>();
		Panel entity;
		
		Map<String, String> parentIdParentNameMap = new HashMap<String, String>();
		List<Panel> notFoundRulePanels = new ArrayList<>();
		Set<String> idSet;
		
		FileReader fr = null;
		BufferedReader br = null;
		String line = null;
		
		try {
			fr = new FileReader(path);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		br = new BufferedReader(fr);
		try {
			// skip first line (header)
			br.readLine();
			while ((line = br.readLine()) != null) {
				
				String[] f = line.split(delimiter, 29);
				
				// ParentId
				String parentId = f[0];
				// ParentLoinc
				String parentLoinc = f[1];
				// ParentName
				String parentName = removeDoubleQuotes(f[2]);
				// Id
				String id = f[3];
				// Sequence
				int sequence = Integer.parseInt(f[4]);
				// Loinc
				String loinc = f[5];
				// LoincName
				String loincName = f[6];
				
				String displayNameForForm = removeDoubleQuotes(f[7]);
				
				String observationRequiredInPanel = f[8];
				
				String observationIdInForm = f[9];

				String skipLogicTarget = f[10];
				
				String skipLogicTargetAnswer = f[11];
				
				String skipLogicHelpText = f[12];
				
				String answerRequired = f[13];
				
				String maxNumberOfAnswers = f[14];
				
				String defaultValue = f[15];
				
				String entryType = f[16];
				
				String dataTypeInForm = f[17];
				
				String dataTypeSource = f[18];
				
				String answerSequenceOverride = f[19];
				
				String conditionForInclusion = f[20];
				
				String allowableAlternative = f[21];
				
				String observationCategory = f[22];
				
				String context = f[23];
				
				String consistencyChecks = f[24];
				
				String relevanceEquation = f[25];
				
				String codingInstructions = removeDoubleQuotes(f[26]);
				
				String questionCardinality = f[27];
				
				String answerCardinality = f[28];
				
				
				// 부모아이디 & 부모LOINC아이디
				if (!parentIdParentLoincIdMap.containsKey(parentId)) {
					parentIdParentLoincIdMap.put(parentId, parentLoinc);
				}
				
				if (!parentIdParentNameMap.containsKey(parentId)) {
					parentIdParentNameMap.put(parentId, parentName);
				}
				
				// 현재아이디 & 패널
				if (!currentIdPanelMap.containsKey(id)) {
					entity = new Panel();
					entity.setCode(loinc);
					entity.setName(loincName);
					entity.setVersion(version);
					entity.setSequence(sequence);
					entity.setDisplayNameForForm(displayNameForForm);
					entity.setObservationRequiredInPanel(observationRequiredInPanel);
					entity.setObservationIdInForm(observationIdInForm);
					//entity.setSkipLogicTarget(skipLogicTarget);
					//entity.setSkipLogicTargetAnswer(skipLogicTargetAnswer);
					entity.setSkipLogicHelpText(skipLogicHelpText);
					//entity.setAnswerRequired(answerRequired);
					//entity.setMaxNumberOfAnswers(maxNumberOfAnswers);
					entity.setDefaultValue(defaultValue);
					entity.setEntryType(entryType);
					entity.setDataTypeInForm(dataTypeInForm);
					entity.setDataTypeSource(dataTypeSource);
					entity.setAnswerSequenceOverride(answerSequenceOverride);
					entity.setConditionForInclusion(conditionForInclusion);
					entity.setAllowableAlternative(allowableAlternative);
					entity.setObservationCategory(observationCategory);
					entity.setContext(context);
					entity.setConsistencyChecks(consistencyChecks);
					entity.setRelevanceEquation(relevanceEquation);
					entity.setCodingInstructions(codingInstructions);
					entity.setQuestionCardinality(questionCardinality);
					entity.setAnswerCardinality(answerCardinality);
					
					currentIdPanelMap.put(id, entity);
				}
				
				// 부모아이디 & 현재아이디
				if (!parentIdCurrentIdSetMap.containsKey(parentId)) {
					idSet = new HashSet<String>();
					parentIdCurrentIdSetMap.put(parentId, idSet);
				} else {
					idSet = parentIdCurrentIdSetMap.get(parentId);
				}
				idSet.add(id);
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			Set<String> rootIdSet = new HashSet<String>();
			Iterator<String> itr = parentIdCurrentIdSetMap.keySet().iterator();
			while (itr.hasNext()) {
				String parentId = itr.next();
				idSet = parentIdCurrentIdSetMap.get(parentId);
				if (idSet.contains(parentId)) {
					rootIdSet.add(parentId);
				} else if (!currentIdPanelMap.containsKey(parentId)) {
					rootIdSet.add(parentId);
					
					entity = new Panel();
					String loincCode = parentIdParentLoincIdMap.get(parentId);
					entity.setVersion(version);
					entity.setCode(loincCode);
					entity.setParentCode("");
					entity.setRootCode(loincCode);
					entity.setPath(loincCode);
					entity.setName(parentIdParentNameMap.get(parentId));
					notFoundRulePanels.add(entity);
				}
			}
			
			List<String> rootIds = new ArrayList<String>(rootIdSet);
			Panel p;
			String rootLoincCode;
			String pathToRoot;
			Set<String> currentIdSet;
			String rootId;
			int len = rootIds.size();
			for (int i = 0; i < len; i ++) {
				rootId = rootIds.get(i);
				rootLoincCode = parentIdParentLoincIdMap.get(rootId);
				
				
				currentIdSet = parentIdCurrentIdSetMap.get(rootId);
				for (String currentId : currentIdSet) {
					p = currentIdPanelMap.get(currentId);
					
					if (!rootId.equals(currentId)) {
						pathToRoot = rootLoincCode + "~" + p.getCode();
						p.setParentCode(rootLoincCode);
						p.setPath(pathToRoot);
						p.setRootCode(rootLoincCode);
						
						// 하위가 더 있다면
						if (parentIdParentLoincIdMap.containsKey(currentId)) {
							processSubtypePanel(pathToRoot, rootLoincCode, currentId, p.getCode());
						}
					} else {
						// (rootId == currentId) => Panel Root
						p.setParentCode("");
						p.setPath(rootLoincCode);
						p.setRootCode(rootLoincCode);
					}
				}
				
			}
		}

		entities.addAll(currentIdPanelMap.values());
		entities.addAll(notFoundRulePanels);
		
		return entities;
	}
	
	
	private void processSubtypePanel(String parentPath, String rootLoincCode, String parentId, String parentLoincCode) {
		
		Panel p;
		String path;
		Set<String> currentIdSet = parentIdCurrentIdSetMap.get(parentId);
		for (String currentId : currentIdSet) {
			p = currentIdPanelMap.get(currentId);
			p.setParentCode(parentLoincCode);
			p.setRootCode(rootLoincCode);
			
			path = parentPath + "~" + p.getCode();
			p.setPath(path);
			
			// 하위가 더 있다면
			if (parentIdParentLoincIdMap.containsKey(currentId)) {
				processSubtypePanel(path, rootLoincCode, currentId, p.getCode());
			}
		}
	}
	
	
	/**
	 * 쌍따옴표 제거
	 * @param word
	 * @return
	 */
	private String removeDoubleQuotes(String word) {
		if (word == null) {
			return "";
		}
		
		return word.replaceAll("\"", "");
	}
	
	
	/**
	 * Hierarchy Entity 생성
	 * @param code LOINC code 또는 분류코드
	 * @param path 루트까지의 경로
	 * @param parent 부모 코드
	 * @param type 분류 유형; 0:Top Level, 1: Class, 2: Part
	 * @param sequence 같은 노드상에서의 순서
	 * @param name Component 이름 또는 분류 이름
	 * @param prefName 선호하는 이름; 표시되는 이름; 모든 주요파트를 합친 이름
	 * @return
	 */
	private Hierarchy createHierarchy(String code, String path, String parent, int type, int sequence, String name, String prefName) {
		Hierarchy e = new Hierarchy();
		
		e.setCode(code);
		e.setPath(path);
		e.setParent(parent);
		e.setType(type);
		e.setSequence(sequence);
		e.setName(name);
		e.setPreferredName(prefName);
		
		return e;
	}
	
	
	/**
	 * MapTo Entity 생성
	 * 
	 * @param code
	 * @param mapTo
	 * @param comment
	 * @return
	 */
	private MapTo createMapTo(String code, String mapTo, String comment) {
		MapTo e = new MapTo();
		
		e.setCode(code);
		e.setMapTo(mapTo);
		e.setComment(comment);
		
		return e;
	}
	
	
	/**
	 * SourceOrganization Entity 생성
	 * 
	 * @param copyrightId
	 * @param name
	 * @param copyright
	 * @param termsOfUse
	 * @param url
	 * @return
	 */
	private SourceOrganization createSrcOrg(String copyrightId, String name, String copyright, String termsOfUse, String url) {
		SourceOrganization e = new SourceOrganization();
		
		e.setCopyrightId(copyrightId);
		e.setName(name);
		e.setCopyright(copyright);
		e.setTermsOfUse(termsOfUse);
		e.setUrl(url);
		
		return e;
	}
	
	
	/**
	 * LinguisticVariants Entity 생성
	 * 
	 * @param isoLang
	 * @param isoCountry
	 * @param lang
	 * @param producer
	 * @return
	 */
	private LinguisticVariants createLingVars(String isoLang, String isoCountry, String lang, String producer) {
		LinguisticVariants e = new LinguisticVariants();
		
		e.setIsoLang(isoLang);
		e.setIsoCountry(isoCountry);
		e.setLang(lang);
		e.setProducer(producer);
		
		return e;
	}
	
	
	/**
	 * LinguisticVariant Entity 생성
	 * 
	 * @param isoLang
	 * @param isoCountry
	 * @param lang
	 * @param producer
	 * @return
	 */
	private LinguisticVariant createLingVar(
			String code, 
			String component, 
			String property, 
			String timeAspect, 
			String system, 
			String scaleType, 
			String methodType, 
			String className, 
			String shortName, 
			String longCommonName, 
			String relatedNames2, 
			String isoLang, 
			String isoCountry) {
		LinguisticVariant e = new LinguisticVariant();
		
		e.setCode(code);
		e.setComponent(component);
		e.setProperty(property);
		e.setTimeAspect(timeAspect);
		e.setSystem(system);
		e.setScaleType(scaleType);
		e.setMethodType(methodType);
		e.setClassName(className);
		e.setShortName(shortName);
		e.setLongCommonName(longCommonName);
		e.setRelatedNames2(relatedNames2);
		e.setIsoLang(isoLang);
		e.setIsoCountry(isoCountry);
		
		return e;
	}

	
	/**
	 * 경로의 자손 수
	 * @param path
	 * @return
	 */
	/*
	private int getDescendantCount(String path) {
		int descendantCount = 0;
		Set<String> paths = pathChildCodesMap.keySet();
		for (String p : paths) {
			if (p.startsWith(path)) {
				descendantCount += getChildrenCount(p);
			}
		}
		return descendantCount;
	}
	*/
	
	
	/**
	 * 
	 * @param parentPath
	 * @param code
	 * @param hierarchy
	 */
	private void addChild(String parentPath, String code, Hierarchy hierarchy) {
		addChildCode(parentPath, code);
		addDescendantCode(parentPath, code);
		
		String path = (parentPath != "" ? (parentPath + "~"):"") + code;
		pathHierarchyMap.put(path, hierarchy);
	}
	
	/**
	 * Add child code
	 * 
	 * @param path
	 * @param code
	 */
	private void addChildCode(String path, String childCode) {
		Set<String> childCodes;
		boolean contain = pathChildCodesMap.containsKey(path);
		if (contain) {
			childCodes = pathChildCodesMap.get(path);
		} else {
			childCodes = new HashSet<String>();
			pathChildCodesMap.put(path, childCodes);
		}
		childCodes.add(childCode);
	}
	
	
	/**
	 * Add descendant code
	 * 
	 * @param path
	 * @param childCode
	 */
	private void addDescendantCode(String path, String childCode) {
		Set<String> descendantCodes;
		
		String tempPath = "";
		String[] parentCodes = path.split("~");
		for (String parentCode : parentCodes) {
			tempPath = tempPath != "" ? (tempPath + "~" + parentCode):parentCode;
			
			if (pathDescendantCodesMap.containsKey(tempPath)) {
				descendantCodes = pathDescendantCodesMap.get(tempPath);
			} else {
				descendantCodes = new HashSet<String>();
				pathDescendantCodesMap.put(tempPath, descendantCodes);
			}
			
			descendantCodes.add(childCode);
		}
	}
	
	
	/**
	 * 자식 수 계산 후 계층구조 노드 Entity에 적용
	 */
	private void batchSetSubtypeCount() {
		String path;
		int childrenCount = 0;
		int descendantCount = 0;
		Hierarchy hierarchy;
		
		Iterator<String> itr = pathHierarchyMap.keySet().iterator();
		while (itr.hasNext()) {
			path = itr.next();
			if (path != null) {
				// children count
				if (pathChildCodesMap.containsKey(path)) {
					childrenCount = pathChildCodesMap.get(path).size();
				} else {
					childrenCount = 0;
				}
				
				// descendant count
				if (pathDescendantCodesMap.containsKey(path)) {
					descendantCount = pathDescendantCodesMap.get(path).size();
				} else {
					descendantCount = 0;
				}
				
				hierarchy = pathHierarchyMap.get(path);
				hierarchy.setChildrenCount(childrenCount);
				hierarchy.setDescendantCount(descendantCount);
			}
		}
	}
}
