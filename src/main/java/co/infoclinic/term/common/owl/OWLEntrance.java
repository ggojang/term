package co.infoclinic.term.common.owl;

import java.io.IOException;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class OWLEntrance {
	
	Logger log = LoggerFactory.getLogger(OWLEntrance.class);
	
	//private String userKey = null;
	
	public OWLEntrance(String userKey) {
		super();
		//this.userKey = userKey;
		MDC.put("userKey", userKey);
	}
	
	public String editor(Map<String, Object> map) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException {
		//MDC.put("userKey", userKey);
		RDF2ConceptEditor rdf2 = new RDF2ConceptEditor();
		return rdf2.editor(map);
	}
	
	public Map<String, Object> loadInference(Map<String, Object> map) throws Exception {
		//MDC.put("userKey", userKey);
		ELKDLQuery elk = new ELKDLQuery();
		return elk.loadInference(map);
	}

}
