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
import co.infoclinic.term.snomedct.service.TransitiveClosureService;

@Service("SCTSchemeSvc")
public class SchemeServiceImpl implements SchemeService {

	Logger log = LoggerFactory.getLogger(SchemeServiceImpl.class);

	@Autowired
	private SchemeRepository schemeRepo;

	@Autowired
	private TransitiveClosureService tcSvc;


	@Override
	public List<Scheme> getSchemeList() {
		List<Scheme> schemes = schemeRepo.findAllOrderByVersionDesc();
		return schemes != null ? schemes : new ArrayList<>();
	}


	@Override
	public Scheme getLatestScheme() {
		Scheme scheme = schemeRepo.findLatest();
		return scheme != null ? scheme : new Scheme();
	}


	@Override
	public String getLatestVersion() {
		String version = "";
		Scheme scheme = getLatestScheme();
		try {
			version = scheme.getVersion();
		} catch (NullPointerException e) {
			version = "";
		} finally {
			if (version == null) version = "";
		}
		return version;
	}


	@Override
	public boolean isValid(String version) {
		if (StringUtils.isEmpty(version) || !version.matches(SNOMEDCTUtils.CODE_PATTERN)) {
			return false;
		}
		Scheme scheme = schemeRepo.findByVersion(version);
		return scheme != null && version.equals(scheme.getVersion());
	}


	@Override
	public String getEffectiveTime(String version) {
		if (!isValid(version)) return "";
		return version.toLowerCase().replace("v", "");
	}


	/**
	 * version에 대응하는 TC effectiveTime 반환.
	 * - International 릴리즈: EXTENSION_NAME IS NULL → effectiveTime 그대로
	 * - Extension 릴리즈: TC에 존재하는 가장 가까운 이전 International effectiveTime 반환
	 */
	@Override
	public String getTcEffectiveTime(String version) {
		if (!isValid(version)) return "";

		Scheme scheme = schemeRepo.findByVersion(version);
		if (scheme == null) return "";

		String et = getEffectiveTime(version);

		// TC에서 해당 날짜 이하의 가장 최신 effectiveTime 선택 (International/Extension 공통)
		List<String> availableTimes = tcSvc.getAvailableEffectiveTimes(); // 캐싱된 값 사용
		for (String t : availableTimes) {
			if (t.compareTo(et) <= 0) {
				return t;
			}
		}

		// fallback: TC의 가장 오래된 버전
		return availableTimes.isEmpty() ? et : availableTimes.get(availableTimes.size() - 1);
	}
}
