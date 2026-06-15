package co.infoclinic.claml;

import java.io.Serializable;

import java.util.ArrayList;

public class ICD10Class implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9143794369491232301L;
	
	String code="";
	String version="";
	String classKind="";
	String usageKind="";
	String superClass="";
	String label="";
	String ref="";
	int childrenCount=0;
	int descendantCount=0;
	String path="";
	
	ArrayList<String> modifiedBy = new ArrayList<String>();
	ArrayList<String> excludeModifier = new ArrayList<String>();
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
	public String getClassKind() {
		return classKind;
	}
	public void setClassKind(String classKind) {
		this.classKind = classKind;
	}
	public String getUsageKind() {
		return usageKind;
	}
	public void setUsageKind(String usageKind) {
		this.usageKind = usageKind;
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
	public int getChildrenCount() {
		return childrenCount;
	}
	public void setChildrenCount(int childrenCount) {
		this.childrenCount = childrenCount;
	}
	public int getDescendantCount() {
		return descendantCount;
	}
	public void setDescendantCount(int descendantCount) {
		this.descendantCount = descendantCount;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
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
				
}