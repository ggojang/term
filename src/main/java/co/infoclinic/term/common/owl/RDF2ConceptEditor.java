package co.infoclinic.term.common.owl;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.infoclinic.term.common.utils.PropertiesUtil;
import uk.ac.manchester.cs.owl.owlapi.OWLDatatypeImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImpl;

public class RDF2ConceptEditor {
	
	Logger log = LoggerFactory.getLogger(RDF2ConceptEditor.class);
	
	/** The rolegroupsctid. */
	private long ROLEGROUPSCTID = 609096000l;
	
	/** The primitive. */
	private String PRIMITIVE="900000000000074008";
	
	private String SNOMEDCT_OWL_DIR;
	private String SNOMEDCT_OWL_FILE;
	
	private String IRI_PREFIX = "http://snomed.info/id/";
	
	/** The manager. */
	private OWLOntologyManager manager = null;
	
	/** The ont. */
	private OWLOntology ontology = null;
	
	/** The factory. */
	private OWLDataFactory factory ;
	
	private Map<String, Object> newConceptMap = null;
	
	private void load() throws OWLOntologyCreationException, IOException {
		
		PropertiesUtil prop = new PropertiesUtil();
		SNOMEDCT_OWL_DIR = prop.getPropValue("owl.base.dir").toString();
		SNOMEDCT_OWL_FILE = prop.getPropValue("owl.load.owlf").toString();
		
		log.info("SNOMED CT OWL Loading Start");
		File f = new File(SNOMEDCT_OWL_DIR + File.separatorChar + SNOMEDCT_OWL_FILE);
		manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument(f);
		factory = manager.getOWLDataFactory();
		log.info("SNOMED CT OWL Loading End");
		
	}
	
	@SuppressWarnings("unchecked")
	public String editor(Map<String, Object> map) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException {
		
		newConceptMap = map;
		String conceptId = newConceptMap.get("conceptId").toString();
		log.info("New Concept => "+conceptId);
		load();
		log.info("Create Stated Relationship...");
		IRI cptIri = IRI.create(IRI_PREFIX + conceptId);
		//Create Concept declaration
		log.info("OWL Inference...");
		OWLClass conceptClass = factory.getOWLClass(cptIri);
		OWLDeclarationAxiom declarationAxiom = factory.getOWLDeclarationAxiom(conceptClass);
		manager.addAxiom(ontology, declarationAxiom);
		
		boolean isPrimitive = (newConceptMap.get("definitionStatusId").toString().equals(PRIMITIVE));
		
		//Create Descriptions
		List<Map<String, Object>> des = (List<Map<String, Object>>) newConceptMap.get("des");
		List<Map<String, Object>> inList = null;
		int i=0;
		for(Map<String, Object> desSet : des) {
			String country = desSet.get("lang").toString()+"-"+desSet.get("langCountry").toString();
			inList = (List<Map<String, Object>>) desSet.get("descriptions");
			int a=0;
			for(Map<String, Object> description : inList) {
				OWLDatatypeImpl dtt=new OWLDatatypeImpl(OWL2Datatype.RDF_PLAIN_LITERAL.getIRI());
				OWLAnnotationProperty propA;
				if(a == 0) {
					propA = factory.getOWLAnnotationProperty(IRI.create("sctf:Description.term."+country+".fsn"));
				} else if(a == 1) {
					propA = factory.getOWLAnnotationProperty(IRI.create("sctf:Description.term."+country+".preferred"));
				} else {
					propA = factory.getOWLAnnotationProperty(IRI.create("sctf:Description.term."+country+".synonym"));
				}
				if(i == 0 && a == 0) {
					OWLAnnotationProperty propB = factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
					OWLAnnotation annotationB = factory.getOWLAnnotation(propB,new OWLLiteralImpl(description.get("term").toString(),null,dtt));
					OWLAnnotationAssertionAxiom axiomB = factory.getOWLAnnotationAssertionAxiom(cptIri, annotationB);
					manager.addAxiom(ontology, axiomB);
				}
				OWLAnnotation annotation = factory.getOWLAnnotation(propA,new OWLLiteralImpl(description.get("term").toString(),description.get("languageCode").toString(),dtt));
				OWLAnnotationAssertionAxiom axiom = factory.getOWLAnnotationAssertionAxiom(cptIri, annotation);
				manager.addAxiom(ontology, axiom);
				a++;
			}
			i++;
		}
		
		//Attributes
		OWLObjectProperty roleGroupProp = factory.getOWLObjectProperty(IRI.create(IRI_PREFIX + ROLEGROUPSCTID));
		List<Map<String, Object>> attributes = (List<Map<String, Object>>) newConceptMap.get("attributes");
		List<Map<String, Object>> grpList = new ArrayList<Map<String, Object>>();;
		List<Map<String, Object>> attrList;
		Map<String, Object> hashMap;
		String grp = "-1";
		for(Map<String, Object> attr : attributes) {
			if(grp.equals(attr.get("group").toString())) {
				for(Map<String, Object> groupAttr : grpList){
					if(attr.get("group").toString().equals(groupAttr.get("grp").toString())) {
						List<Map<String, Object>> in = (List<Map<String, Object>>) groupAttr.get("attrs");
						in.add(attr);
						groupAttr.replace("attrs", in);
						break;
					}
				}
			} else {
				attrList = new ArrayList<Map<String, Object>>();
				attrList.add(attr);
				hashMap = new HashMap<String, Object>();
				hashMap.put("grp", attr.get("group"));
				hashMap.put("attrs", attrList);
				grpList.add(hashMap);
				grp = attr.get("group").toString();
			}
		}
		
		List<Map<String, Object>> parents = (List<Map<String, Object>>) newConceptMap.get("parents");
		Map<String, Object> parent = parents.get(0);
		OWLClass tc = factory.getOWLClass(IRI.create(IRI_PREFIX + parent.get("conceptId").toString()));
		if(attributes.size() == 0) {
			manager.addAxiom(ontology, factory.getOWLSubClassOfAxiom(conceptClass, tc));
		} else {
			Set<OWLClassExpression> set = new HashSet<OWLClassExpression>();
			set.add(tc);
			
			for(Map<String, Object> group : grpList) {
				List<Map<String, Object>> attrs = (List<Map<String, Object>>) group.get("attrs");
				if("0".equals(group.get("grp").toString())) {
					int i1=0;
					for(Map<String, Object> attr : attrs) {
						OWLClass targetClass = factory.getOWLClass(IRI.create(IRI_PREFIX + attr.get("value").toString()));
						OWLObjectProperty property = factory.getOWLObjectProperty(IRI.create(IRI_PREFIX + attr.get("name").toString()));
						OWLClassExpression role = factory.getOWLObjectSomeValuesFrom(property, targetClass);
						if(i1 == 0) {
							OWLClassExpression singlerolegroup=factory.getOWLObjectSomeValuesFrom(roleGroupProp, role);
							set.add(singlerolegroup);
						} else {
							set.add(role);
						}
						i1++;	
					}
				} else {
					HashSet<OWLClassExpression> setRoles = new HashSet<OWLClassExpression>();
					for(Map<String, Object> attr : attrs) {
						OWLClass targetClass = factory.getOWLClass(IRI.create(IRI_PREFIX + attr.get("value").toString()));
						OWLObjectProperty property = factory.getOWLObjectProperty(IRI.create(IRI_PREFIX + attr.get("name").toString()));
						OWLClassExpression role=factory.getOWLObjectSomeValuesFrom(property, targetClass);
						setRoles.add(role);
					}
					OWLObjectIntersectionOf intersection=factory.getOWLObjectIntersectionOf(setRoles);
					OWLClassExpression roleGroup=factory.getOWLObjectSomeValuesFrom(roleGroupProp, intersection);
					set.add(roleGroup);
				}
			}
			
			OWLObjectIntersectionOf intersection=factory.getOWLObjectIntersectionOf(set);
			if(isPrimitive) {
				manager.addAxiom(ontology,factory.getOWLSubClassOfAxiom(conceptClass, intersection));
			} else {
				manager.addAxiom(ontology,factory.getOWLEquivalentClassesAxiom(conceptClass,intersection));
			}
		}
		log.info("Success Stated Relationship");
		String tmpOwlFile = "^snomedct_owlf_\\d{8}-\\d{6}\\.owl$";
		File dir = new File(SNOMEDCT_OWL_DIR);
		File[] fileList = dir.listFiles();
		for(File file : fileList) {
			if(file.isFile()) {
				if(file.getName().matches(tmpOwlFile)) {
					file.delete();
				}
			}
		}
		
		Date today = new Date();
		SimpleDateFormat transFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
		String to = transFormat.format(today);
		
		String statedFile = SNOMEDCT_OWL_DIR + File.separatorChar + "snomedct_owlf_"+to+".owl";
		File f = new File(statedFile);
		IRI documentIRI = IRI.create(f);
		//manager.saveOntology(ontology, new RDFXMLOntologyFormat(), documentIRI);
		manager.saveOntology(ontology, documentIRI);
		log.info("SNOMED CT OWL Stated Relationship file !! => ["+statedFile+"]");
		manager.removeOntology(ontology);
		log.info("Success create new concept");
		
		return statedFile;
	}
	
}
