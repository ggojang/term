package co.infoclinic.claml;

import java.io.Serializable;

import java.util.ArrayList;


public class ICD10Rubric implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1113612400519556683L;
	
	String code="";
	String version="";
	String id="";
	String kind="";
	String usageKind="";
	String lang="";
	String fragmentType="";
	String paraClass="";
	String superClass="";
	String label= "";
	String ref="";

	ArrayList<String> modifiedBy = new ArrayList<String>();
	ArrayList<String> excludeModifier = new ArrayList<String>();
	
	String endRubric="";

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getUsageKind() {
		return usageKind;
	}

	public void setUsageKind(String usageKind) {
		this.usageKind = usageKind;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getFragmentType() {
		return fragmentType;
	}

	public void setFragmentType(String fragmentType) {
		this.fragmentType = fragmentType;
	}

	public String getParaClass() {
		return paraClass;
	}

	public void setParaClass(String paraClass) {
		this.paraClass = paraClass;
	}

	public String getSuperClass() {
		return superClass;
	}

	public void setSuperClass(String superClass) {
		this.superClass = superClass;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public ArrayList<String> getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(ArrayList<String> modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public ArrayList<String> getExcludeModifier() {
		return excludeModifier;
	}

	public void setExcludeModifier(ArrayList<String> excludeModifier) {
		this.excludeModifier = excludeModifier;
	}

	public String getEndRubric() {
		return endRubric;
	}

	public void setEndRubric(String endRubric) {
		this.endRubric = endRubric;
	}

}
