package co.infoclinic.term.snomedct.service;

import java.util.List;

import co.infoclinic.term.snomedct.model.entity.Scheme;

/**
 * The Scheme Service
 */
public interface SchemeService {

	List<Scheme> getSchemeList();
	
	Scheme getLatestScheme();
	
	String getLatestVersion();
	
	boolean isValid(String version);
	
	String getEffectiveTime(String version);
}
