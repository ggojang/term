package co.infoclinic.term.snomedct.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.infoclinic.term.common.utils.SNOMEDCTUtils;
import co.infoclinic.term.snomedct.model.entity.Scheme;
import co.infoclinic.term.snomedct.repository.SchemeRepository;
import co.infoclinic.term.snomedct.service.SchemeService;

@Service("SCTSchemeSvc")
public class SchemeServiceImpl implements SchemeService {

	/** Logger */
	Logger log = LoggerFactory.getLogger(SchemeServiceImpl.class);
	
	/** DI: TransitiveClosure repository */
	@Autowired
	private SchemeRepository schemeRepo;
	

	
	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.SchemeService#getAllSchemeList()
	 */
	@Override
	public List<Scheme> getSchemeList() {
		List<Scheme> schemes = schemeRepo.findAll();
		return schemes != null ? schemes:new ArrayList<Scheme>();
	}

	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.SchemeService#getLatestScheme()
	 */
	@Override
	public Scheme getLatestScheme() {
		Scheme scheme = schemeRepo.findLatest();
		return scheme != null ? scheme:new Scheme();
	}

	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.SchemeService#getLatestVersion()
	 */
	@Override
	public String getLatestVersion() {
		String version = "";
		Scheme scheme = getLatestScheme();
		
		try {
			version = scheme.getVersion();
		} catch (NullPointerException e) {
			version = "";
		} finally {
			if (version == null) {
				version = "";
			}
		}
		
		return version;
	}

	
	/*
	 * (non-Javadoc)
	 * @see co.infoclinic.term.snomedct.service.SchemeService#isValidVersion(java.lang.String)
	 */
	@Override
	public boolean isValid(String version) {
		if (StringUtils.isEmpty(version) || !version.matches(SNOMEDCTUtils.CODE_PATTERN)) {
			return false;
		}
		
		boolean isValid = false;
		Scheme scheme = schemeRepo.findByVersion(version);
		isValid = scheme != null && version.equals(scheme.getVersion()) ? true:false;
		
		return isValid;
	}


	@Override
	public String getEffectiveTime(String version) {
		if (!isValid(version)) {
			return "";
		}
		
		return version.toLowerCase().replace("v", "");
	}
	
	
}
