package co.infoclinic.term.snomedct.expression.model;

import java.util.List;

public class FocusConcept {
	
	private List<ConceptReference> focusConcept = null;

	/**
	 * @return the focusConcept
	 */
	public List<ConceptReference> getFocusConcept() {
		return focusConcept;
	}

	/**
	 * @param focusConcept the focusConcept to set
	 */
	public void setFocusConcept(List<ConceptReference> focusConcept) {
		this.focusConcept = focusConcept;
	}

}
