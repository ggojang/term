package co.infoclinic.term.common.utils;

import javax.annotation.PostConstruct;

import org.apache.commons.validator.routines.checkdigit.CheckDigitException;
import org.apache.commons.validator.routines.checkdigit.VerhoeffCheckDigit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import co.infoclinic.term.snomedct.service.SCTIDService;

@Component
public final class SNOMEDCTIdentifierGenerator {
	
	Logger log = LoggerFactory.getLogger(SNOMEDCTIdentifierGenerator.class);
	
	private static SCTIDService sctidService;
	
	@Autowired
	private SCTIDService tmpSctidService;
	
	
	@PostConstruct
	public void init() {
		SNOMEDCTIdentifierGenerator.sctidService = tmpSctidService;
	}
	
	public static String create(SNOMEDCTComponentTypeEnum componentType, String namespaceId) {
		String sctId = null;
		String extensionItemId = String.valueOf(sctidService.getId(componentType).getId());
		String partitionId = componentType.getLongType();
		String checkDigit = null;
		
		//TODO NamespaceId
		if (namespaceId == null) namespaceId = "1000300"; //throw new Exception("Namespace Id is Null");
		
		// SCTID = Extension Item Identifier + Namespace Identifier + Partition Identifier + Check-digit
		sctId = new StringBuilder().append(extensionItemId).append(namespaceId).append(partitionId).toString();
		try {
			checkDigit = VerhoeffCheckDigit.VERHOEFF_CHECK_DIGIT.calculate(sctId);
		} catch (CheckDigitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sctId = new StringBuilder().append(sctId).append(checkDigit).toString();
		
		return sctId;
	}
}
