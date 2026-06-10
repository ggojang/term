// Generated from co/infoclinic/term/snomedct/expression/ECLExpression.g4 by ANTLR 4.13.1
package co.infoclinic.term.snomedct.expression;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ECLExpressionParser}.
 */
public interface ECLExpressionListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link ECLExpressionParser#statments}.
	 * @param ctx the parse tree
	 */
	void enterStatments(ECLExpressionParser.StatmentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLExpressionParser#statments}.
	 * @param ctx the parse tree
	 */
	void exitStatments(ECLExpressionParser.StatmentsContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLExpressionParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(ECLExpressionParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLExpressionParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(ECLExpressionParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLExpressionParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(ECLExpressionParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLExpressionParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(ECLExpressionParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLExpressionParser#subExpression}.
	 * @param ctx the parse tree
	 */
	void enterSubExpression(ECLExpressionParser.SubExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLExpressionParser#subExpression}.
	 * @param ctx the parse tree
	 */
	void exitSubExpression(ECLExpressionParser.SubExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLExpressionParser#nestedExpression}.
	 * @param ctx the parse tree
	 */
	void enterNestedExpression(ECLExpressionParser.NestedExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLExpressionParser#nestedExpression}.
	 * @param ctx the parse tree
	 */
	void exitNestedExpression(ECLExpressionParser.NestedExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLExpressionParser#focusConcept}.
	 * @param ctx the parse tree
	 */
	void enterFocusConcept(ECLExpressionParser.FocusConceptContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLExpressionParser#focusConcept}.
	 * @param ctx the parse tree
	 */
	void exitFocusConcept(ECLExpressionParser.FocusConceptContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLExpressionParser#conceptReference}.
	 * @param ctx the parse tree
	 */
	void enterConceptReference(ECLExpressionParser.ConceptReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLExpressionParser#conceptReference}.
	 * @param ctx the parse tree
	 */
	void exitConceptReference(ECLExpressionParser.ConceptReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLExpressionParser#constraintOperator}.
	 * @param ctx the parse tree
	 */
	void enterConstraintOperator(ECLExpressionParser.ConstraintOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLExpressionParser#constraintOperator}.
	 * @param ctx the parse tree
	 */
	void exitConstraintOperator(ECLExpressionParser.ConstraintOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLExpressionParser#refinementOnly}.
	 * @param ctx the parse tree
	 */
	void enterRefinementOnly(ECLExpressionParser.RefinementOnlyContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLExpressionParser#refinementOnly}.
	 * @param ctx the parse tree
	 */
	void exitRefinementOnly(ECLExpressionParser.RefinementOnlyContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLExpressionParser#refinement}.
	 * @param ctx the parse tree
	 */
	void enterRefinement(ECLExpressionParser.RefinementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLExpressionParser#refinement}.
	 * @param ctx the parse tree
	 */
	void exitRefinement(ECLExpressionParser.RefinementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLExpressionParser#attributeOperator}.
	 * @param ctx the parse tree
	 */
	void enterAttributeOperator(ECLExpressionParser.AttributeOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLExpressionParser#attributeOperator}.
	 * @param ctx the parse tree
	 */
	void exitAttributeOperator(ECLExpressionParser.AttributeOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLExpressionParser#attributeGroup}.
	 * @param ctx the parse tree
	 */
	void enterAttributeGroup(ECLExpressionParser.AttributeGroupContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLExpressionParser#attributeGroup}.
	 * @param ctx the parse tree
	 */
	void exitAttributeGroup(ECLExpressionParser.AttributeGroupContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLExpressionParser#attributeNonGroup}.
	 * @param ctx the parse tree
	 */
	void enterAttributeNonGroup(ECLExpressionParser.AttributeNonGroupContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLExpressionParser#attributeNonGroup}.
	 * @param ctx the parse tree
	 */
	void exitAttributeNonGroup(ECLExpressionParser.AttributeNonGroupContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLExpressionParser#attributeSet}.
	 * @param ctx the parse tree
	 */
	void enterAttributeSet(ECLExpressionParser.AttributeSetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLExpressionParser#attributeSet}.
	 * @param ctx the parse tree
	 */
	void exitAttributeSet(ECLExpressionParser.AttributeSetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLExpressionParser#attribute}.
	 * @param ctx the parse tree
	 */
	void enterAttribute(ECLExpressionParser.AttributeContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLExpressionParser#attribute}.
	 * @param ctx the parse tree
	 */
	void exitAttribute(ECLExpressionParser.AttributeContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLExpressionParser#attributeValue}.
	 * @param ctx the parse tree
	 */
	void enterAttributeValue(ECLExpressionParser.AttributeValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLExpressionParser#attributeValue}.
	 * @param ctx the parse tree
	 */
	void exitAttributeValue(ECLExpressionParser.AttributeValueContext ctx);
}