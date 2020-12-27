package co.infoclinic.term.common.owl;

import static org.semanticweb.owlapi.search.EntitySearcher.getAnnotationObjects;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ELKDLQuery {
	
	Logger log = LoggerFactory.getLogger(ELKDLQuery.class);
	
	private OWLOntologyManager manager;
	private OWLOntology ontology;
	private OWLReasonerFactory reasonerFactory;
	private OWLDataFactory dataFactory;
	private OWLReasoner reasoner;
	private ShortFormProvider shortFormProvider;
	private BidirectionalShortFormProvider mapper;
	private Set<OWLOntology> importsClosure;
	
	private void load(String path) throws Exception {
		
		File f = new File(path);
		if(!f.exists()) {
			throw new Exception("File not found!");
		}
		manager = OWLManager.createOWLOntologyManager();
		dataFactory = manager.getOWLDataFactory();
		ontology = manager.loadOntologyFromOntologyDocument(f);
		//factory = manager.getOWLDataFactory();
		log.info("SNOMED CT OWL Loaded => "+ontology.getOntologyID());
		log.info("Inferred reasoner .... OWL ELK Start");
		
		reasonerFactory = new ElkReasonerFactory();
		reasoner = reasonerFactory.createReasoner(ontology);
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		
		shortFormProvider = new SimpleShortFormProvider();
		importsClosure = ontology.getImportsClosure();
		
		mapper = new BidirectionalShortFormProviderAdapter(manager, importsClosure, shortFormProvider);
	}
	
	private OWLClassExpression parseClassExpression(String classExpressionString) {
		
		ManchesterOWLSyntaxParser parser = OWLManager.createManchesterParser();
		parser.setStringToParse(classExpressionString);
		OWLEntityChecker entityChecker = new ShortFormEntityChecker(mapper);

		parser.setDefaultOntology(ontology);
		parser.setOWLEntityChecker(entityChecker);
		
		return parser.parseClassExpression();
	}
	
	private String labelFor(OWLClass clazz) {
		
		//OwlAnnotationSubjectValueVisitor oasv = new OwlAnnotationSubjectValueVisitor();
		String result = null;
		Set<OWLAnnotation> oao = (Set<OWLAnnotation>) getAnnotationObjects(clazz, ontology);
		for(OWLAnnotation node : oao) {
			if(node.getProperty().isLabel()) {
				//node.getValue();
				result = node.getValue().toString();
			}
		}
		if(result != null) return result;
		return clazz.getIRI().toString();
	}
	
	private String labelFor(OWLNamedIndividual clazz) {
		String result = null;
		Set<OWLAnnotation> oao = (Set<OWLAnnotation>) getAnnotationObjects(clazz, ontology);
		for(OWLAnnotation node : oao) {
			if(node.getProperty().isLabel()) {
				result = node.getValue().toString();
			}
		}
		if(result != null) return result;
		return clazz.getIRI().toString();
	}
	
	private List<Map<String, Object>> getSuperClasses(String classExpressionString, boolean direct) {
		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = null;
		
		OWLClassExpression classExpression = parseClassExpression(classExpressionString.trim());
		NodeSet<OWLClass> superClasses = reasoner.getSuperClasses(classExpression, direct);
		Set<OWLClass> results = superClasses.getFlattened();
		for (OWLClass owlClass : results) {
			map = new HashMap<String, Object>();
			map.put("conceptId", shortFormProvider.getShortForm(owlClass));
			map.put("term", labelFor(owlClass));
			list.add(map);
		}
		
		return list;
	}
	
	private List<Map<String, Object>> getInstances(String classExpressionString, boolean direct) {
		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = null;
		
		OWLClassExpression classExpression = parseClassExpression(classExpressionString.trim());
		NodeSet<OWLNamedIndividual> individuals = reasoner.getInstances(classExpression, direct);
		Set<OWLNamedIndividual> results = individuals.getFlattened();
		for (OWLNamedIndividual owlClass : results) {
			map = new HashMap<String, Object>();
			map.put("conceptId", shortFormProvider.getShortForm(owlClass));
			map.put("term", labelFor(owlClass));
			list.add(map);
		}
		return list;
	}
	
	private List<Map<String, Object>> getSubClasses(String classExpressionString, boolean direct) {
		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = null;
		
		OWLClassExpression classExpression = parseClassExpression(classExpressionString.trim());
		NodeSet<OWLClass> subClasses = reasoner.getSubClasses(classExpression, direct);
		Set<OWLClass> results = subClasses.getFlattened();
		for (OWLClass owlClass : results) {
			String conceptId = shortFormProvider.getShortForm(owlClass);//Nothing
			if(!conceptId.toLowerCase().equalsIgnoreCase("nothing")){
				map = new HashMap<String, Object>();
				map.put("conceptId", conceptId);
				map.put("term", labelFor(owlClass));
				list.add(map);
			}
		}
		
		return list;
		
	}
	
	private List<Map<String, Object>> getEquivalentClasses(String classExpressionString) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = null;
		//Set<OWLObjectProperty> property = null;
		OWLClassExpression classExpression = parseClassExpression(classExpressionString.trim());
		Node<OWLClass> equalClasses = reasoner.getEquivalentClasses(classExpression);
		Set<OWLClass> results = equalClasses.getEntities();
		for (OWLClass owlClass : results) {
			if(!owlClass.equals(classExpression)){
				map = new HashMap<String, Object>();
				map.put("conceptId", shortFormProvider.getShortForm(owlClass));
				map.put("term", labelFor(owlClass));
				map.put("attrs", new ArrayList<Map<String, Object>>());
				//owlClass.getIndividualsInSignature()
				//org.semanticweb.owlapi.search.EntitySearcher.getO
				//.owlClass.getObjectPropertiesInSignature().
				//RestrictionVisitor visitor = new RestrictionVisitor(Collections.singleton(ontology));
				//org.semanticweb.owlapi.search.EntitySearcher
				for(OWLObjectPropertyExpression prop : ontology.getObjectPropertiesInSignature()) {
					
					OWLObjectProperty property= prop.getNamedProperty();
					
				}
				/**
				for(OWLObjectPropertyExpression prop : ontology.getObjectPropertiesInSignature()) {
					OWLClassExpression restriction = factory.getOWLObjectSomeValuesFrom(prop, factory.getOWLThing());
					OWLClassExpression intersection = factory.getOWLObjectIntersectionOf(owlClass, factory.getOWLObjectComplementOf(restriction));
					if(!reasoner.isSatisfiable(intersection)) {
						System.out.println(prop.toString());
					}
				}
				*/
				list.add(map);
			}	
		}
		
		return list;
	}
	
	public Map<String, Object> loadInference(Map<String, Object> map) throws Exception {
		
		log.info("Initialization of the SNOMED CT OWL...");
		load(map.get("path").toString());
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		List<Map<String, Object>> superClasses = getSuperClasses(map.get("conceptId").toString(), true);
		List<Map<String, Object>> subClasses = getSubClasses(map.get("conceptId").toString(), true);
		List<Map<String, Object>> instances = getInstances(map.get("conceptId").toString(), true);
		List<Map<String, Object>> equalClasses = getEquivalentClasses(map.get("conceptId").toString());
		resultMap.put("superClasses", superClasses.size() == 0 ? null : superClasses);
		resultMap.put("subClasses", subClasses.size() == 0 ? null : subClasses);
		resultMap.put("instances", instances.size() == 0 ? null : instances);
		resultMap.put("equalClasses", equalClasses.size() == 0 ? null : equalClasses);
		
		reasoner.dispose();
		log.info("Super Classes ("+superClasses.size()+")");
		log.info("Sub Classes ("+subClasses.size()+")");
		log.info("Instances ("+instances.size()+")");
		log.info("Equivalent Classes ("+equalClasses.size()+")");
		log.info("Inferred reasoner .... OWL ELK End");
		return resultMap;
	}

}
