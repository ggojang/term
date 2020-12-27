package co.infoclinic.term.snomedct.expression.model;

public class SubExpression {
	
	private FocusConcept focusConcept = null;
	private Refinement refinement = null;
	/**
	 * @return the focusConcept
	 */
	public FocusConcept getFocusConcept() {
		return focusConcept;
	}
	/**
	 * @param focusConcept the focusConcept to set
	 */
	public void setFocusConcept(FocusConcept focusConcept) {
		this.focusConcept = focusConcept;
	}
	/**
	 * @return the refinement
	 */
	public Refinement getRefinement() {
		return refinement;
	}
	/**
	 * @param refinement the refinement to set
	 */
	public void setRefinement(Refinement refinement) {
		this.refinement = refinement;
	}

}
