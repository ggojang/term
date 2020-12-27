package co.infoclinic.term.snomedct.utils;

import java.util.Map;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;

import co.infoclinic.term.snomedct.expression.CGExpressionLexer;
import co.infoclinic.term.snomedct.expression.CGExpressionParser;
import co.infoclinic.term.snomedct.expression.model.ConceptReference;
import co.infoclinic.term.snomedct.expression.model.FocusConcept;
import co.infoclinic.term.snomedct.expression.model.SubExpression;
import co.infoclinic.term.snomedct.utils.ECLParserUtil.ThrowErrorListener;

public class CGParserUtil {
	
	private static ParseTree parseExpression(String expression) throws ParseCancellationException, CGSyntaxError {
		
		ParseTree tree = null;
		ANTLRInputStream input = new ANTLRInputStream(expression);
		CGExpressionLexer lexer = new CGExpressionLexer(input);
		lexer.removeErrorListeners();
		lexer.addErrorListener(ThrowErrorListener.TEL);
		
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		CGExpressionParser parser = new CGExpressionParser(tokens);
		parser.removeErrorListeners();
		parser.addErrorListener(ThrowErrorListener.TEL);
		
		try {
			tree = parser.expression();
		}  catch (Exception e) {
			throw new CGSyntaxError(e);
		}
		
		if(tree == null) throw new CGSyntaxError("Parse result is null.");
		return tree;
	}
	
	@SuppressWarnings("unchecked")
	public static void getTest(String expression) throws ParseCancellationException, CGSyntaxError {
		ParseTree tree = parseExpression(expression);
		CGVisitorUtil visitor = new CGVisitorUtil();
		Map<String, Object> exMap = (Map<String, Object>) visitor.visit(tree);
		System.out.println(exMap.get("definitionStatus"));
		SubExpression se = (SubExpression) exMap.get("subExpression");
		FocusConcept fc = se.getFocusConcept();
		for(ConceptReference cr : fc.getFocusConcept()) {
			System.out.println(cr.getSctId()+"================="+cr.getTerm());
		}
	}

}
