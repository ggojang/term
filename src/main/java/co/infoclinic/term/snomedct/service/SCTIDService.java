package co.infoclinic.term.snomedct.service;

import co.infoclinic.term.common.utils.SNOMEDCTComponentTypeEnum;
import co.infoclinic.term.snomedct.model.entity.SCTID;

public interface SCTIDService {

	SCTID getId(SNOMEDCTComponentTypeEnum componentType);
}
