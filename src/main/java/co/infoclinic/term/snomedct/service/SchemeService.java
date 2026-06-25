package co.infoclinic.term.snomedct.service;

import java.util.List;

import co.infoclinic.term.snomedct.model.entity.Scheme;

/**
 * The Scheme Service
 */
public interface SchemeService {

	/** 모든 릴리즈(International + Extension) 목록 (최신순) */
	List<Scheme> getSchemeList();

	/** International Edition 최신 Scheme */
	Scheme getLatestScheme();

	/** International Edition 최신 version 문자열 (예: "v20241001") */
	String getLatestVersion();

	boolean isValid(String version);

	/** version 문자열 → effectiveTime 8자리 (예: "v20241001" → "20241001") */
	String getEffectiveTime(String version);

	/**
	 * version에 대응하는 TC effectiveTime 반환.
	 * International 릴리즈이면 해당 effectiveTime을 그대로 반환.
	 * Extension 릴리즈이면 TC에 존재하는 가장 가까운 이전 International effectiveTime을 반환.
	 */
	String getTcEffectiveTime(String version);
}
