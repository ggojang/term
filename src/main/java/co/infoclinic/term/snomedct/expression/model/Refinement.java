package co.infoclinic.term.snomedct.expression.model;

import java.util.List;

public class Refinement {
	
	private List<Attribute> nonGroupedAttributeSet = null;
	private List<List<Attribute>> attributeGroup = null;
	/**
	 * @return the attributeGroup
	 */
	public List<List<Attribute>> getAttributeGroup() {
		return attributeGroup;
	}
	/**
	 * @param attributeGroup the attributeGroup to set
	 */
	public void setAttributeGroup(List<List<Attribute>> attributeGroup) {
		this.attributeGroup = attributeGroup;
	}
	/**
	 * @return the nonGroupedAttributeSet
	 */
	public List<Attribute> getNonGroupedAttributeSet() {
		return nonGroupedAttributeSet;
	}
	/**
	 * @param nonGroupedAttributeSet the nonGroupedAttributeSet to set
	 */
	public void setNonGroupedAttributeSet(List<Attribute> nonGroupedAttributeSet) {
		this.nonGroupedAttributeSet = nonGroupedAttributeSet;
	}
	
}
