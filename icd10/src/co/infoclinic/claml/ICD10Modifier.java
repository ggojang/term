package co.infoclinic.claml;

import java.io.Serializable;
import java.util.ArrayList;

public class ICD10Modifier implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 228124065014572974L;
	
	//String modifiedBy="";
	String code="";
	ArrayList<String> subClass = new ArrayList<String>();
	String version="";
	String id="";
	String kind="";
	String usageKind="";
	String lang="";
	String fragmentType="";
	String paraClass="";
	String label="";
	String ref="";

	
	public ICD10Modifier() {
		super();
	}


	public String getCode() {
		return code;
	}


	public void setCode(String code) {
		this.code = code;
	}


	public ArrayList<String> getSubClass() {
		return subClass;
	}


	public void setSubClass(ArrayList<String> subClass) {
		this.subClass.addAll(subClass);
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


}
