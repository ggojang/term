package co.infoclinic.term.snomedct.expression.model;

public class ConceptReference {
	
	private String sctId = null;
	private String term = null;
	/**
	 * @return the sctId
	 */
	public String getSctId() {
		return sctId;
	}
	/**
	 * @param sctId the sctId to set
	 */
	public void setSctId(String sctId) {
		this.sctId = sctId;
	}
	/**
	 * @return the term
	 */
	public String getTerm() {
		return term;
	}
	/**
	 * @param term the term to set
	 */
	public void setTerm(String term) {
		this.term = term;
	}

}
