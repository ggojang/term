package co.infoclinic.term.snomedct.expression.model;

public class AttributeValue {
	
	private String StringValue = null;
	private int IntegerValue = 0;
	private ConceptReference conceptReference = null;
	/**
	 * @return the stringValue
	 */
	public String getStringValue() {
		return StringValue;
	}
	/**
	 * @param stringValue the stringValue to set
	 */
	public void setStringValue(String stringValue) {
		StringValue = stringValue;
	}
	/**
	 * @return the integerValue
	 */
	public int getIntegerValue() {
		return IntegerValue;
	}
	/**
	 * @param integerValue the integerValue to set
	 */
	public void setIntegerValue(int integerValue) {
		IntegerValue = integerValue;
	}
	/**
	 * @return the conceptReference
	 */
	public ConceptReference getConceptReference() {
		return conceptReference;
	}
	/**
	 * @param conceptReference the conceptReference to set
	 */
	public void setConceptReference(ConceptReference conceptReference) {
		this.conceptReference = conceptReference;
	}

}
