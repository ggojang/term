package co.infoclinic.term.loinc.service;

import co.infoclinic.term.loinc.model.dto.PanelDTO;

public interface PanelService {

	PanelDTO getPanel(String code);
}
