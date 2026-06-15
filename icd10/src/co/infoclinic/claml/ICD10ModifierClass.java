package co.infoclinic.claml;

import java.io.Serializable;

public class ICD10ModifierClass implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7786520289247583768L;
	
	String modifier="";
	String code="";
	String id="";
	String version="";
	String classKind="";
	String kind="";
	String usageKind="";
	String lang="";
	String fragmentType="";
	String paraClass="";
	String label="";
	String ref="";
	String superClass="";
	String childrenCount="";
	String descendantCount="";
	String path="";
	
	public String getModifier() {
		return modifier;
	}
	public void setModifier(String modifier) {
		this.modifier = modifier;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getClassKind() {
		return classKind;
	}
	public void setClassKind(String classKind) {
		this.classKind = classKind;
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
	public String getSuperClass() {
		return superClass;
	}
	public void setSuperClass(String superClass) {
		this.superClass = superClass;
	}
	public String getChildrenCount() {
		return childrenCount;
	}
	public void setChildrenCount(String childrenCount) {
		this.childrenCount = childrenCount;
	}
	public String getDescendantCount() {
		return descendantCount;
	}
	public void setDescendantCount(String descendantCount) {
		this.descendantCount = descendantCount;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
				
}
