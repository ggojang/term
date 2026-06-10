// Generated from co/infoclinic/term/snomedct/expression/ECL1Expression.g4 by ANTLR 4.13.1
package co.infoclinic.term.snomedct.expression;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ECL1ExpressionParser}.
 */
public interface ECL1ExpressionListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link ECL1ExpressionParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(ECL1ExpressionParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECL1ExpressionParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(ECL1ExpressionParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECL1ExpressionParser#refinementExpression}.
	 * @param ctx the parse tree
	 */
	void enterRefinementExpression(ECL1ExpressionParser.RefinementExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECL1ExpressionParser#refinementExpression}.
	 * @param ctx the parse tree
	 */
	void exitRefinementExpression(ECL1ExpressionParser.RefinementExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECL1ExpressionParser#conjunctionExpression}.
	 * @param ctx the parse tree
	 */
	void enterConjunctionExpression(ECL1ExpressionParser.ConjunctionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECL1ExpressionParser#conjunctionExpression}.
	 * @param ctx the parse tree
	 */
	void exitConjunctionExpression(ECL1ExpressionParser.ConjunctionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECL1ExpressionParser#disjunctionExpression}.
	 * @param ctx the parse tree
	 */
	void enterDisjunctionExpression(ECL1ExpressionParser.DisjunctionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECL1ExpressionParser#disjunctionExpression}.
	 * @param ctx the parse tree
	 */
	void exitDisjunctionExpression(ECL1ExpressionParser.DisjunctionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECL1ExpressionParser#exclusionExpression}.
	 * @param ctx the parse tree
	 */
	void enterExclusionExpression(ECL1ExpressionParser.ExclusionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECL1ExpressionParser#exclusionExpression}.
	 * @param ctx the parse tree
	 */
	void exitExclusionExpression(ECL1ExpressionParser.ExclusionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECL1ExpressionParser#simpleExpression}.
	 * @param ctx the parse tree
	 */
	void enterSimpleExpression(ECL1ExpressionParser.SimpleExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECL1ExpressionParser#simpleExpression}.
	 * @param ctx the parse tree
	 */
	void exitSimpleExpression(ECL1ExpressionParser.SimpleExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECL1ExpressionParser#focusConcept}.
	 * @param ctx the parse tree
	 */
	void enterFocusConcept(ECL1ExpressionParser.FocusConceptContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECL1ExpressionParser#focusConcept}.
	 * @param ctx the parse tree
	 */
	void exitFocusConcept(ECL1ExpressionParser.FocusConceptContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECL1ExpressionParser#constraintOperator}.
	 * @param ctx the parse tree
	 */
	void enterConstraintOperator(ECL1ExpressionParser.ConstraintOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECL1ExpressionParser#constraintOperator}.
	 * @param ctx the parse tree
	 */
	void exitConstraintOperator(ECL1ExpressionParser.ConstraintOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECL1ExpressionParser#conceptReference}.
	 * @param ctx the parse tree
	 */
	void enterConceptReference(ECL1ExpressionParser.ConceptReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECL1ExpressionParser#conceptReference}.
	 * @param ctx the parse tree
	 */
	void exitConceptReference(ECL1ExpressionParser.ConceptReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECL1ExpressionParser#refinement}.
	 * @param ctx the parse tree
	 */
	void enterRefinement(ECL1ExpressionParser.RefinementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECL1ExpressionParser#refinement}.
	 * @param ctx the parse tree
	 */
	void exitRefinement(ECL1ExpressionParser.RefinementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECL1ExpressionParser#attributeGroup}.
	 * @param ctx the parse tree
	 */
	void enterAttributeGroup(ECL1ExpressionParser.AttributeGroupContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECL1ExpressionParser#attributeGroup}.
	 * @param ctx the parse tree
	 */
	void exitAttributeGroup(ECL1ExpressionParser.AttributeGroupContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECL1ExpressionParser#attributeSet}.
	 * @param ctx the parse tree
	 */
	void enterAttributeSet(ECL1ExpressionParser.AttributeSetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECL1ExpressionParser#attributeSet}.
	 * @param ctx the parse tree
	 */
	void exitAttributeSet(ECL1ExpressionParser.AttributeSetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECL1ExpressionParser#attributeOperator}.
	 * @param ctx the parse tree
	 */
	void enterAttributeOperator(ECL1ExpressionParser.AttributeOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECL1ExpressionParser#attributeOperator}.
	 * @param ctx the parse tree
	 */
	void exitAttributeOperator(ECL1ExpressionParser.AttributeOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECL1ExpressionParser#attribute}.
	 * @param ctx the parse tree
	 */
	void enterAttribute(ECL1ExpressionParser.AttributeContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECL1ExpressionParser#attribute}.
	 * @param ctx the parse tree
	 */
	void exitAttribute(ECL1ExpressionParser.AttributeContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECL1ExpressionParser#attributeName}.
	 * @param ctx the parse tree
	 */
	void enterAttributeName(ECL1ExpressionParser.AttributeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECL1ExpressionParser#attributeName}.
	 * @param ctx the parse tree
	 */
	void exitAttributeName(ECL1ExpressionParser.AttributeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECL1ExpressionParser#attributeValue}.
	 * @param ctx the parse tree
	 */
	void enterAttributeValue(ECL1ExpressionParser.AttributeValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECL1ExpressionParser#attributeValue}.
	 * @param ctx the parse tree
	 */
	void exitAttributeValue(ECL1ExpressionParser.AttributeValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECL1ExpressionParser#compoundAttributevalue}.
	 * @param ctx the parse tree
	 */
	void enterCompoundAttributevalue(ECL1ExpressionParser.CompoundAttributevalueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECL1ExpressionParser#compoundAttributevalue}.
	 * @param ctx the parse tree
	 */
	void exitCompoundAttributevalue(ECL1ExpressionParser.CompoundAttributevalueContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECL1ExpressionParser#exclusionAttributevalue}.
	 * @param ctx the parse tree
	 */
	void enterExclusionAttributevalue(ECL1ExpressionParser.ExclusionAttributevalueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECL1ExpressionParser#exclusionAttributevalue}.
	 * @param ctx the parse tree
	 */
	void exitExclusionAttributevalue(ECL1ExpressionParser.ExclusionAttributevalueContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECL1ExpressionParser#stringOperator}.
	 * @param ctx the parse tree
	 */
	void enterStringOperator(ECL1ExpressionParser.StringOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECL1ExpressionParser#stringOperator}.
	 * @param ctx the parse tree
	 */
	void exitStringOperator(ECL1ExpressionParser.StringOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECL1ExpressionParser#numberOperator}.
	 * @param ctx the parse tree
	 */
	void enterNumberOperator(ECL1ExpressionParser.NumberOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECL1ExpressionParser#numberOperator}.
	 * @param ctx the parse tree
	 */
	void exitNumberOperator(ECL1ExpressionParser.NumberOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECL1ExpressionParser#expressionOperator}.
	 * @param ctx the parse tree
	 */
	void enterExpressionOperator(ECL1ExpressionParser.ExpressionOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECL1ExpressionParser#expressionOperator}.
	 * @param ctx the parse tree
	 */
	void exitExpressionOperator(ECL1ExpressionParser.ExpressionOperatorContext ctx);
}