// Generated from co/infoclinic/term/snomedct/expression/ECLExpression.g4 by ANTLR 4.13.1
package co.infoclinic.term.snomedct.expression;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link ECLExpressionParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface ECLExpressionVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link ECLExpressionParser#statments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatments(ECLExpressionParser.StatmentsContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECLExpressionParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(ECLExpressionParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECLExpressionParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(ECLExpressionParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECLExpressionParser#subExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubExpression(ECLExpressionParser.SubExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECLExpressionParser#nestedExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNestedExpression(ECLExpressionParser.NestedExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECLExpressionParser#focusConcept}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFocusConcept(ECLExpressionParser.FocusConceptContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECLExpressionParser#conceptReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConceptReference(ECLExpressionParser.ConceptReferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECLExpressionParser#constraintOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraintOperator(ECLExpressionParser.ConstraintOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECLExpressionParser#refinementOnly}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRefinementOnly(ECLExpressionParser.RefinementOnlyContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECLExpressionParser#refinement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRefinement(ECLExpressionParser.RefinementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECLExpressionParser#attributeOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttributeOperator(ECLExpressionParser.AttributeOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECLExpressionParser#attributeGroup}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttributeGroup(ECLExpressionParser.AttributeGroupContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECLExpressionParser#attributeNonGroup}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttributeNonGroup(ECLExpressionParser.AttributeNonGroupContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECLExpressionParser#attributeSet}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttributeSet(ECLExpressionParser.AttributeSetContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECLExpressionParser#attribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttribute(ECLExpressionParser.AttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link ECLExpressionParser#attributeValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttributeValue(ECLExpressionParser.AttributeValueContext ctx);
}