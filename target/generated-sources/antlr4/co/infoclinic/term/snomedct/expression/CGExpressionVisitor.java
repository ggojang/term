// Generated from co/infoclinic/term/snomedct/expression/CGExpression.g4 by ANTLR 4.13.1
package co.infoclinic.term.snomedct.expression;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link CGExpressionParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface CGExpressionVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link CGExpressionParser#statements}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatements(CGExpressionParser.StatementsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CGExpressionParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(CGExpressionParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link CGExpressionParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(CGExpressionParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CGExpressionParser#definitionStatus}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefinitionStatus(CGExpressionParser.DefinitionStatusContext ctx);
	/**
	 * Visit a parse tree produced by {@link CGExpressionParser#subExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubExpression(CGExpressionParser.SubExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CGExpressionParser#focusConcept}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFocusConcept(CGExpressionParser.FocusConceptContext ctx);
	/**
	 * Visit a parse tree produced by {@link CGExpressionParser#conceptReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConceptReference(CGExpressionParser.ConceptReferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link CGExpressionParser#refinement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRefinement(CGExpressionParser.RefinementContext ctx);
	/**
	 * Visit a parse tree produced by {@link CGExpressionParser#attributeGroup}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttributeGroup(CGExpressionParser.AttributeGroupContext ctx);
	/**
	 * Visit a parse tree produced by {@link CGExpressionParser#nonGroupedAttributeSet}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNonGroupedAttributeSet(CGExpressionParser.NonGroupedAttributeSetContext ctx);
	/**
	 * Visit a parse tree produced by {@link CGExpressionParser#attributeSet}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttributeSet(CGExpressionParser.AttributeSetContext ctx);
	/**
	 * Visit a parse tree produced by {@link CGExpressionParser#attribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttribute(CGExpressionParser.AttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CGExpressionParser#attributeValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttributeValue(CGExpressionParser.AttributeValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link CGExpressionParser#nestedExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNestedExpression(CGExpressionParser.NestedExpressionContext ctx);
}