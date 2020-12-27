package co.infoclinic.term.loinc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import co.infoclinic.term.loinc.service.LoadService;

@RestController
@RequestMapping("LOINC/load")
public class LoadCmdController {

	@Autowired
	private LoadService loadSvc;
	
	@RequestMapping(value = "", method = RequestMethod.GET)
	public boolean load() {
		
		//loadSvc.load();
		
		return true;
	}
}
