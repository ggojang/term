// Generated from co/infoclinic/term/snomedct/expression/CGExpression.g4 by ANTLR 4.13.1
package co.infoclinic.term.snomedct.expression;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link CGExpressionParser}.
 */
public interface CGExpressionListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link CGExpressionParser#statements}.
	 * @param ctx the parse tree
	 */
	void enterStatements(CGExpressionParser.StatementsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CGExpressionParser#statements}.
	 * @param ctx the parse tree
	 */
	void exitStatements(CGExpressionParser.StatementsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CGExpressionParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(CGExpressionParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link CGExpressionParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(CGExpressionParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link CGExpressionParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(CGExpressionParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CGExpressionParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(CGExpressionParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CGExpressionParser#definitionStatus}.
	 * @param ctx the parse tree
	 */
	void enterDefinitionStatus(CGExpressionParser.DefinitionStatusContext ctx);
	/**
	 * Exit a parse tree produced by {@link CGExpressionParser#definitionStatus}.
	 * @param ctx the parse tree
	 */
	void exitDefinitionStatus(CGExpressionParser.DefinitionStatusContext ctx);
	/**
	 * Enter a parse tree produced by {@link CGExpressionParser#subExpression}.
	 * @param ctx the parse tree
	 */
	void enterSubExpression(CGExpressionParser.SubExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CGExpressionParser#subExpression}.
	 * @param ctx the parse tree
	 */
	void exitSubExpression(CGExpressionParser.SubExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CGExpressionParser#focusConcept}.
	 * @param ctx the parse tree
	 */
	void enterFocusConcept(CGExpressionParser.FocusConceptContext ctx);
	/**
	 * Exit a parse tree produced by {@link CGExpressionParser#focusConcept}.
	 * @param ctx the parse tree
	 */
	void exitFocusConcept(CGExpressionParser.FocusConceptContext ctx);
	/**
	 * Enter a parse tree produced by {@link CGExpressionParser#conceptReference}.
	 * @param ctx the parse tree
	 */
	void enterConceptReference(CGExpressionParser.ConceptReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link CGExpressionParser#conceptReference}.
	 * @param ctx the parse tree
	 */
	void exitConceptReference(CGExpressionParser.ConceptReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link CGExpressionParser#refinement}.
	 * @param ctx the parse tree
	 */
	void enterRefinement(CGExpressionParser.RefinementContext ctx);
	/**
	 * Exit a parse tree produced by {@link CGExpressionParser#refinement}.
	 * @param ctx the parse tree
	 */
	void exitRefinement(CGExpressionParser.RefinementContext ctx);
	/**
	 * Enter a parse tree produced by {@link CGExpressionParser#attributeGroup}.
	 * @param ctx the parse tree
	 */
	void enterAttributeGroup(CGExpressionParser.AttributeGroupContext ctx);
	/**
	 * Exit a parse tree produced by {@link CGExpressionParser#attributeGroup}.
	 * @param ctx the parse tree
	 */
	void exitAttributeGroup(CGExpressionParser.AttributeGroupContext ctx);
	/**
	 * Enter a parse tree produced by {@link CGExpressionParser#nonGroupedAttributeSet}.
	 * @param ctx the parse tree
	 */
	void enterNonGroupedAttributeSet(CGExpressionParser.NonGroupedAttributeSetContext ctx);
	/**
	 * Exit a parse tree produced by {@link CGExpressionParser#nonGroupedAttributeSet}.
	 * @param ctx the parse tree
	 */
	void exitNonGroupedAttributeSet(CGExpressionParser.NonGroupedAttributeSetContext ctx);
	/**
	 * Enter a parse tree produced by {@link CGExpressionParser#attributeSet}.
	 * @param ctx the parse tree
	 */
	void enterAttributeSet(CGExpressionParser.AttributeSetContext ctx);
	/**
	 * Exit a parse tree produced by {@link CGExpressionParser#attributeSet}.
	 * @param ctx the parse tree
	 */
	void exitAttributeSet(CGExpressionParser.AttributeSetContext ctx);
	/**
	 * Enter a parse tree produced by {@link CGExpressionParser#attribute}.
	 * @param ctx the parse tree
	 */
	void enterAttribute(CGExpressionParser.AttributeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CGExpressionParser#attribute}.
	 * @param ctx the parse tree
	 */
	void exitAttribute(CGExpressionParser.AttributeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CGExpressionParser#attributeValue}.
	 * @param ctx the parse tree
	 */
	void enterAttributeValue(CGExpressionParser.AttributeValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CGExpressionParser#attributeValue}.
	 * @param ctx the parse tree
	 */
	void exitAttributeValue(CGExpressionParser.AttributeValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link CGExpressionParser#nestedExpression}.
	 * @param ctx the parse tree
	 */
	void enterNestedExpression(CGExpressionParser.NestedExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CGExpressionParser#nestedExpression}.
	 * @param ctx the parse tree
	 */
	void exitNestedExpression(CGExpressionParser.NestedExpressionContext ctx);
}