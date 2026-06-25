package co.infoclinic.term.snomedct.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.infoclinic.term.common.utils.PropertiesUtil;
import co.infoclinic.term.common.utils.SNOMEDCTComponentTypeEnum;
import co.infoclinic.term.snomedct.model.dto.RefsetMemberCmdDTO;
import co.infoclinic.term.snomedct.model.dto.SimpleMemberCmdDTO;
import co.infoclinic.term.snomedct.model.entity.Description;
import co.infoclinic.term.snomedct.service.DescriptionService;
import co.infoclinic.term.snomedct.service.RefsetMemberCommandService;
import co.infoclinic.term.snomedct.service.TransitiveClosureService;

/**
 * 레퍼런스세트 멤버 C.U.D를 처리하는 서비스
 * 
 * @author dongwon
 *
 */
@Service(value="RefsetMemberCommandService")
@Transactional
public class RefsetMemberCommandServiceImpl implements RefsetMemberCommandService {

	/** Logger */
	Logger log = LoggerFactory.getLogger(RefsetMemberCommandServiceImpl.class);
	
	private static final String STATE_ACTIVE = "ACTIVE";
	private static final String STATE_HISTORY = "HISTORY";
	private static final String STATE_INACTIVE = "INACTIVE";
	private static final String STATE_DELETED = "DELETED";
	
	
	/** DI: Description service */
	@Autowired
	private DescriptionService descSvc;
	
	/** DI: Transitive closure service */
	@Autowired
	private TransitiveClosureService tcSvc;
	
	
	
	
	// ----------------------------------------
	// Public methods
	// ----------------------------------------
	
	
	/*
	 * 멤버 목록 추가
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.RefsetMemberCommandService#addMemberList(java.lang.String, co.infoclinic.term.snomedct.model.dto.RefsetMemberCmdDTO)
	 */
	@Override
	public boolean addMemberList(String refsetId, RefsetMemberCmdDTO dto) {
		
		// FIXME: from user
		// effective time
		String effectiveTime = "20161231";
		
		// 예비 신규활성 멤버 목록
		List<SimpleMemberCmdDTO> actMbrs = dto.getActiveMbrs();
		// 예비 비활성멤버 목록
		List<SimpleMemberCmdDTO> inactMbrs = dto.getInactiveMbrs();
		// 예비 삭제멤버 목록
		List<SimpleMemberCmdDTO> delMbrs = dto.getDeletedMbrs();
		
		// 추가/비활성화/삭제 대상이 없다면 반환
		if (actMbrs.size() == 0 && inactMbrs.size() == 0 && delMbrs.size() == 0) {
			return true;
		}
		
		// 예비멤버  저장
		if (actMbrs.size() > 0) {
			// 용어 호출 및 하위검사 후 저장할 멤버 모두 호출
			List<String> newMbrs = getMemberList(actMbrs, effectiveTime);
			saveActiveMemberList(refsetId, newMbrs, true);
		}
		
		boolean onlyAct = inactMbrs.size() == 0 && delMbrs.size() == 0 ? true:false;
		if (!onlyAct) {
			// 비활성|삭제 처리
		
			// 비활성화 대상 저장
			if (inactMbrs.size() > 0) {
				saveInactiveMemberList(refsetId, inactMbrs, true);
			}
			
			// 삭제 대상 저장
			if (delMbrs.size() > 0) {
				saveDeleteMemberList(refsetId, delMbrs, true);
			}
			
			// 활성멤버에서 비활성화|삭제 제거 후 활성멤버 재저장
			List<String> excludeList = new ArrayList<String>();
			excludeList.addAll(convertMemberIdList(inactMbrs));
			excludeList.addAll(convertMemberIdList(delMbrs));
			
			List<String> mbrs = loadActiveMemberListWithoutExcludeList(refsetId, excludeList);
			saveActiveMemberList(refsetId, mbrs, false, false);
		}
		
		
		return true;
	}

	
	// ----------------------------------------
	// Private methods
	// ----------------------------------------
	
	/**
	 * 멤버의 자식/자손 포함여부를 확인하여 멤버로 등록될 모든 콤포넌트 목록을 생성 후 저장형태로 반환
	 * 
	 * @return
	 */
	private List<String> getMemberList(List<SimpleMemberCmdDTO> candiMbrs, String effectiveTime) {
		
		// 하위포함여부 확인 후 데이터셋(conceptId그룹, descriptionId그룹, inclSubtypes그룹) 구성
		// conceptId 목록
		List<String> cnptIds = new ArrayList<String>();
		// descriptionId 목록
		List<String> descIds = new ArrayList<String>();
		// expandable conceptId 목록 (inclSubtypes=true)
		List<String> ecnptIds = new ArrayList<String>();
		
		SimpleMemberCmdDTO candiMbr;
		// include subtypes 
		boolean inclSubtypes = false;
		// candidate member id
		String candiMbrId;
		// component type of candidate member(id)
		SNOMEDCTComponentTypeEnum sctType;
		
		// set candidate members size
		int candiMbrsSize = candiMbrs.size();
		// loop candidate members
		for (int i = 0; i < candiMbrsSize; i++) {
			candiMbr = candiMbrs.get(i);
			candiMbrId = candiMbr.getMemberId();
			inclSubtypes = candiMbr.isIncludeSubtypes();
			
			// set SNOMED CT Component Type Enum
			sctType = SNOMEDCTComponentTypeEnum.getById(candiMbrId);
			
			// 예비멤버 아이디 타입이 concept일 경우 conceptId 목록에 추가
			if (SNOMEDCTComponentTypeEnum.CONCEPT.equals(sctType)) {
				cnptIds.add(candiMbrId);
			}
			// 예비멤버 아이디 타입이 description일 경우 descriptionId 목록에 추가
			else if (SNOMEDCTComponentTypeEnum.DESCRIPTION.equals(sctType)) {
				descIds.add(candiMbrId);
			}
			// 예비멤버 아이디 타입이 concept 또는 description이 아니라면 예외발생
			else {
				// FIXME: throw exception
			}
			
			// 하위를 포함한다면, 확장가능한 아이디 목록에 추가
			if (inclSubtypes) {
				ecnptIds.add(candiMbrId);
			}
		}
		
		return expandAll(cnptIds, descIds, ecnptIds, effectiveTime);
	}
	
	
	/**
	 * 
	 * @param conceptIds
	 * @param descriptionIds
	 * @param expandableConceptIds
	 * @param effectiveTime
	 * @return
	 */
	private List<String> expandAll(List<String> conceptIds, List<String> descriptionIds, List<String> expandableConceptIds, String effectiveTime) {
		List<String> mbrs = new ArrayList<String>();
		
		List<String> paths = tcSvc.getPathListByConceptIds(expandableConceptIds, effectiveTime);
		
		List<Description> descs = descSvc.expandAll(conceptIds, descriptionIds, paths, effectiveTime);
		Description desc;
		
		int descsSize = descs.size();
		for (int i = 0; i < descsSize; i++) {
			desc = descs.get(i);
			// exclude \r\n
			mbrs.add(desc.getConceptId() + "\t" + desc.getTerm());
		}
		
		return mbrs;
	}
	
	/**
	 * 활성 멤버 목록을 파일에 저장하는 메소드
	 * 
	 * @param refsetId
	 * @param mbrs
	 * @param append
	 * @param history
	 * @return
	 */
	private boolean saveActiveMemberList(String refsetId, List<String> mbrs, boolean append, boolean history) {
		return save(refsetId, STATE_ACTIVE, mbrs, append, history);
	}
	
	/**
	 * 활성 멤버 목록을 파일에 저장하는 메소드
	 * 
	 * @param refsetId
	 * @param mbrs
	 * @param append
	 * @return
	 */
	private boolean saveActiveMemberList(String refsetId, List<String> mbrs, boolean append) {
		return save(refsetId, STATE_ACTIVE, mbrs, append, true);
	}
	
	/**
	 * 비활성 멤버 목록을 파일에 저장하는 메소드
	 * 
	 * @param refsetId
	 * @param mbrs
	 * @param append
	 * @return
	 */
	private boolean saveInactiveMemberList(String refsetId, List<SimpleMemberCmdDTO> mbrs, boolean append) {
		return save(refsetId, STATE_INACTIVE, convertTsv(mbrs), append, true);
	}
	
	/**
	 * 삭제 멤버 목록을 파일에 저장하는 메소드
	 * 
	 * @param refsetId
	 * @param mbrs
	 * @param append
	 * @return
	 */
	private boolean saveDeleteMemberList(String refsetId, List<SimpleMemberCmdDTO> mbrs, boolean append) {
		return save(refsetId, STATE_DELETED, convertTsv(mbrs), append, true);
	}
	
	/**
	 * 
	 * @param mbrs
	 * @return
	 */
	private List<String> convertTsv(List<SimpleMemberCmdDTO> mbrs) {
		List<String> ms = new ArrayList<String>();
		
		int mbrsSize = mbrs.size();
		for (int i = 0; i < mbrsSize; i++) {
			ms.add(mbrs.get(i).getMemberId() + "\t ");
		}
		
		return ms;
	}
	
	private List<String> convertMemberIdList(List<SimpleMemberCmdDTO> mbrs) {
		List<String> ms = new ArrayList<String>();
		
		int mbrsSize = mbrs.size();
		for (int i = 0; i < mbrsSize; i++) {
			ms.add(mbrs.get(i).getMemberId());
		}
		
		return ms;
	}
	
	/**
	 * 레퍼런스세트 아이디로부터 리소스(파일)의 위치를 반환
	 * 
	 * @param refsetId
	 * @return
	 */
	private String getRefsetFilePath(String refsetId, String state) {
		
		PropertiesUtil prop = null;
		try {
			prop = new PropertiesUtil();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String refsetPath = prop.getPropValue("sct.refset.dir").toString();
		
		// FIXME: implements... 
		String path = "";
		// 레퍼런스세트 타입: simple, ordered, map ...
		String type = getRefsetType(refsetId);
		// 확장자
		String extension = ".tsv";
		
		// 파일명
		String fileName = "SCT-REFSET-" + 
						type.toUpperCase() + 
						"-MEMBER-" + state.toUpperCase() +  
						"-" + refsetId + extension;
		
		// path
		path = refsetPath + "/contents/" + type.toLowerCase() +
			"/" + refsetId + 
			"/" + fileName;
		
		return path;
	}
	
	/**
	 * 레퍼런스세트의 타입을 반환
	 * @param refsetId
	 * @return
	 */
	private String getRefsetType(String refsetId) {
		String type = "simple";
		
		// FIXME: ADMIN폴더 내에있는 fileInfo.json에서 레퍼런스세트 아이디의 타입 가져오기
		// {
		//   "id": {string} refset id
		//   "name": {string} refset name
		//   "type": {string} refset type; simple or ...
		// }
		
		return type;
	}
	
	/**
	 * 파일에 저장
	 * 
	 * @param refsetId
	 * @param state
	 * @param obj
	 */
	private boolean save(String refsetId, String state, List<String> mbrs, boolean append, boolean history) {
		
		// 활성상태;히스토리 저장을 위한 변수
		String act = STATE_ACTIVE.equals(state) ? "1":"0";
		// 삭제상태;히스토리 저장을 위한 변수
		String del = STATE_DELETED.equals(state) ? "1":"0";
		
		String path = getRefsetFilePath(refsetId, state);
		String historyPath = getRefsetFilePath(refsetId, STATE_HISTORY);
		
		// output stream for path
		OutputStream os = null;
		// output stream for history path
		OutputStream hos = null;
		
		// FIXME: 레퍼런스세트의 폴더가 없을 경우 생성
		String refsetPath = path.substring(0, path.lastIndexOf("/"));
		File dirf = new File(refsetPath);
		if (!dirf.exists()) {
			dirf.mkdir();
		}
		
		
		// 파일이 존재하지 않을 경우 생성
		File f = new File(path);
		if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			os = new FileOutputStream(f, append);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// buffered writer for path
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
		
		BufferedWriter hbw = null;
		if (history) {
			
			// 히스토리 파일이 없을 경우 생성
			File hf = new File(historyPath);
			if (!hf.exists()) {
				try {
					hf.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			try {
				// second param is append
				hos = new FileOutputStream(hf, true);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// buffered writer for history path
			hbw = new BufferedWriter(new OutputStreamWriter(hos));
		}
		
		try {
			if (hbw != null) {
				for (String mbr : mbrs) {
					// 활성|비활성|삭제에 저장
					bw.write(mbr + "\r\n");
					// 히스토리에 저장
					hbw.write(mbr + "\t" + act + "\t" + del + "\r\n");
				}
				bw.flush();
				hbw.flush();
			} else {
				for (String mbr : mbrs) {
					// 활성|비활성|삭제에 저장
					bw.write(mbr + "\r\n");
				}
				bw.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (hbw != null) {
				try {
					hbw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		/*
		JsonWriter writer = new JsonWriter(bw);
		
		// Step 2: Start writing JSON contents
		
		// Starts with writing array
		
		
		try {
			// 배열 인코딩 시작
			writer.beginArray();
			
			for (RefsetMemberQueryDTO mbr : mbrs) {
				// 객체 인코딩 시작
				writer.beginObject();
				// name:value 쌍으로 객체 쓰기
				writer.name("id").value(mbr.getId());
				writer.name("term").value((mbr.getTerm()));
				// 객체 인코딩 종료
				writer.endObject();
			}
			writer.
			// 배열 인코딩 종료
			writer.endArray();
			
			writer.flush();
			// writer 닫기
			
			bw.newLine();
			
			writer.close();
			bw.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/
		
		
		/*
		// 파일 쓰기
		try {
			bw = new BufferedWriter(new FileWriter(path));
			gson.toJson(gson.toJsonTree(mbrs), new JsonWriter(bw));
			//bw.write(json);
			//bw.append("");
			//bw.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		*/
		
		// FIXME: failed일 경우 false 반환
		return true;
	}
	
	/**
	 * 파일로부터 멤버목록을 읽어 반환 
	 * 
	 * @param refsetId
	 * @param state
	 * @return
	 */
	private List<String> loadActiveMemberListWithoutExcludeList(String refsetId, List<String> excludeList) {
		List<String> mbrs = new ArrayList<String>();
		
		FileReader fr = null;
		BufferedReader br = null;
		//Gson gson = null;
		String line = null;
		String path = getRefsetFilePath(refsetId, STATE_ACTIVE);
		
		// FIXME: 유효한 경로인지 확인 ? 진행:에러
		
		try {
			fr = new FileReader(path);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		br = new BufferedReader(fr);
		try {
			while ((line = br.readLine()) != null) {
				String[] f = line.split("\t");
				
				String mbrId = f[0];
				if (excludeList.contains(mbrId)) {
					continue;
				}
				
				String term = f[1];
				mbrs.add(mbrId + "\t" + term);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return mbrs;
	}
}
