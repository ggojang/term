// Generated from co/infoclinic/term/snomedct/expression/ECL1Expression.g4 by ANTLR 4.13.1
package co.infoclinic.term.snomedct.expression;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link ECL1ExpressionParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface ECL1ExpressionVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link ECL1ExpressionParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(ECL1ExpressionParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECL1ExpressionParser#refinementExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRefinementExpression(ECL1ExpressionParser.RefinementExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECL1ExpressionParser#conjunctionExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConjunctionExpression(ECL1ExpressionParser.ConjunctionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECL1ExpressionParser#disjunctionExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDisjunctionExpression(ECL1ExpressionParser.DisjunctionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECL1ExpressionParser#exclusionExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExclusionExpression(ECL1ExpressionParser.ExclusionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECL1ExpressionParser#simpleExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleExpression(ECL1ExpressionParser.SimpleExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECL1ExpressionParser#focusConcept}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFocusConcept(ECL1ExpressionParser.FocusConceptContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECL1ExpressionParser#constraintOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraintOperator(ECL1ExpressionParser.ConstraintOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECL1ExpressionParser#conceptReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConceptReference(ECL1ExpressionParser.ConceptReferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECL1ExpressionParser#refinement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRefinement(ECL1ExpressionParser.RefinementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECL1ExpressionParser#attributeGroup}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttributeGroup(ECL1ExpressionParser.AttributeGroupContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECL1ExpressionParser#attributeSet}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttributeSet(ECL1ExpressionParser.AttributeSetContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECL1ExpressionParser#attributeOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttributeOperator(ECL1ExpressionParser.AttributeOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECL1ExpressionParser#attribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttribute(ECL1ExpressionParser.AttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECL1ExpressionParser#attributeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttributeName(ECL1ExpressionParser.AttributeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECL1ExpressionParser#attributeValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttributeValue(ECL1ExpressionParser.AttributeValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECL1ExpressionParser#compoundAttributevalue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompoundAttributevalue(ECL1ExpressionParser.CompoundAttributevalueContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECL1ExpressionParser#exclusionAttributevalue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExclusionAttributevalue(ECL1ExpressionParser.ExclusionAttributevalueContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECL1ExpressionParser#stringOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringOperator(ECL1ExpressionParser.StringOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECL1ExpressionParser#numberOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumberOperator(ECL1ExpressionParser.NumberOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECL1ExpressionParser#expressionOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionOperator(ECL1ExpressionParser.ExpressionOperatorContext ctx);
}