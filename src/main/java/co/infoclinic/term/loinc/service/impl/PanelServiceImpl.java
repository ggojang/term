package co.infoclinic.term.loinc.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.infoclinic.term.common.model.dto.Value;
import co.infoclinic.term.common.utils.LOINCUtils;
import co.infoclinic.term.loinc.model.dto.PanelContentDTO;
import co.infoclinic.term.loinc.model.dto.PanelDTO;
import co.infoclinic.term.loinc.model.entity.Panel;
import co.infoclinic.term.loinc.repository.PanelRepository;
import co.infoclinic.term.loinc.service.PanelService;

/**
 * Loinc Panel Service
 */
@Service("LncPanelSvc")
public class PanelServiceImpl implements PanelService {
	
	/** Logger */
	Logger log = LoggerFactory.getLogger(PanelService.class);
	
	/** DI: Panel repository */
	@Autowired
	private PanelRepository panelRepo;

	
	
	
	// ----------------------------------------
	// Public methods
	// ----------------------------------------
	
	@Override
	public PanelDTO getPanel(String code) {
		if (StringUtils.isEmpty(code) || !code.matches(LOINCUtils.CODE_PATTERN)) {
			return new PanelDTO();
		}
		
		PanelDTO dto = new PanelDTO();
		
		PanelContentDTO contentDTO = getPanelHierarchy(code);
		dto.setPanel(contentDTO);
		
		List<Value> memberOfThesePanels = getRootPanelList(code);
		dto.setMemberOfThesePanels(memberOfThesePanels);
		
		return dto;
	}
	
	
	
	
	private PanelContentDTO getPanelHierarchy(String code) {
		PanelContentDTO rootDto = null;
		
		Map<String, List<String>> parentCodeCurrentCodesMap = new HashMap<String, List<String>>();
		List<String> currentCodes;
		Map<String, PanelContentDTO> codePanelContentMap = new HashMap<String, PanelContentDTO>();
		PanelContentDTO panelContent;
		List<Panel> entities = panelRepo.findByCode(code);
		Panel entity;
		
		// Step1
		int len = entities != null ? entities.size():0;
		for (int i = 0; i < len; i++) {
			entity = entities.get(i);
			
			String cd = entity.getCode();
			String prntCd;
			if (!cd.equals(code)) {
				prntCd = entity.getParentCode();
			} else {
				prntCd = "ROOT";
			}
			
			// ParentCode & CodeList
			if (parentCodeCurrentCodesMap.containsKey(prntCd)) {
				currentCodes = parentCodeCurrentCodesMap.get(prntCd);
			} else {
				currentCodes = new ArrayList<String>();
				parentCodeCurrentCodesMap.put(prntCd, currentCodes);
			}
			currentCodes.add(cd);
			
			
			// Code & PanelContentList
			panelContent = new PanelContentDTO();
			panelContent.setVersion(entity.getVersion());
			panelContent.setCode(cd);
			panelContent.setName(entity.getName());
			panelContent.setSequence(entity.getSequence());
			panelContent.setDisplayNameForForm(entity.getDisplayNameForForm());
			panelContent.setObservationRequiredInPanel(entity.getObservationRequiredInPanel());
			// TODO 나머지
			
			codePanelContentMap.put(cd, panelContent);
  		}
		
		
		// Step2
		// 1. parentCodeCurrentCodesMap에서 parentCode가 ""인 currentCodes를 구하고 사이즈가 1인지확인 및 1값 구함.
		// 2. 1값을 루트로 값을 정한다.
		// 3. 1값을 부모코드로 갖는 currentCodes를 구해서 노드를 구성함.
		// 4. 각 currentCode별로 3번을 반복한다.
		if (parentCodeCurrentCodesMap.containsKey("ROOT")) {
			List<String> codes = parentCodeCurrentCodesMap.get("ROOT");	
			if (codes.size() == 1) {
				rootDto = codePanelContentMap.get(codes.get(0));
				processChildPanel(rootDto, parentCodeCurrentCodesMap, codePanelContentMap);
			}
		}
		
		return rootDto != null ? rootDto:new PanelContentDTO();
	}
	
	
	private void processChildPanel(PanelContentDTO parentPanel, final Map<String, List<String>> parentCodeChildCodesMap, final Map<String, PanelContentDTO> codePanelMap) {
		
		String parentCode = parentPanel.getCode();
		
		// 자식코드 목록이 존재하는 경우만
		if (parentCodeChildCodesMap.containsKey(parentCode)) {
			List<String> childCodes = parentCodeChildCodesMap.get(parentCode);
			List<PanelContentDTO> panels = new ArrayList<PanelContentDTO>();
			parentPanel.setChildren(panels);
			
			PanelContentDTO panel;
			for (String childCode : childCodes) {
				if (codePanelMap.containsKey(childCode)) {
					panel = codePanelMap.get(childCode);
					panels.add(panel);
					
					// 하위 탐색
					processChildPanel(panel, parentCodeChildCodesMap, codePanelMap);
				}
			}
		}
	}
	
	
	private List<Value> getRootPanelList(String code) {
		List<Value> rootPanels = new ArrayList<Value>();
		Value rootPanel;
		
		List<Panel> entities = panelRepo.findRootPanelListByCode(code);
		Panel entity;
		int len = entities != null ? entities.size():0;
		for (int i = 0; i < len; i ++) {
			
			entity = entities.get(i);
							
			rootPanel = new Value();
			rootPanel.setId(entity.getParentCode()); // 2020.12.27 by Yu
			rootPanel.setName(entity.getParentName()); // 2020.12.27 by Yu
		
			rootPanels.add(rootPanel);
		
		}
		
		return rootPanels;
	}

}
