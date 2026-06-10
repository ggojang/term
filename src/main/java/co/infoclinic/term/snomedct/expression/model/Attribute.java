package co.infoclinic.term.snomedct.expression.model;

public class Attribute {
	
	private ConceptReference attributeName = null;
	private AttributeValue attributeValue = null;
	/**
	 * @return the attributeName
	 */
	public ConceptReference getAttributeName() {
		return attributeName;
	}
	/**
	 * @param attributeName the attributeName to set
	 */
	public void setAttributeName(ConceptReference attributeName) {
		this.attributeName = attributeName;
	}
	/**
	 * @return the attributeValue
	 */
	public AttributeValue getAttributeValue() {
		return attributeValue;
	}
	/**
	 * @param attributeValue the attributeValue to set
	 */
	public void setAttributeValue(AttributeValue attributeValue) {
		this.attributeValue = attributeValue;
	}
	
}
