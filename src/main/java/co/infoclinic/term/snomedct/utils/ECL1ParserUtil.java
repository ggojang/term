package co.infoclinic.term.snomedct.utils;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;

import co.infoclinic.term.snomedct.expression.ECL1ExpressionLexer;
import co.infoclinic.term.snomedct.expression.ECL1ExpressionParser;
import co.infoclinic.term.snomedct.utils.ECLParserUtil.ThrowErrorListener;

public class ECL1ParserUtil {
	
	private static String checkExpression(String expression) {
		
		ANTLRInputStream input = new ANTLRInputStream(expression);
		ECL1ExpressionLexer lexer = new ECL1ExpressionLexer(input);
		
		lexer.removeErrorListeners();
		lexer.addErrorListener(ThrowErrorListener.TEL);
		
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ECL1ExpressionParser parser = new ECL1ExpressionParser(tokens);
		
		parser.removeErrorListeners();
		parser.addErrorListener(ThrowErrorListener.TEL);
		
		ParseTree tree = parser.expression();
		
		int child = tree.getChild(0).getChildCount();
		String str = "";
		if(child == 3) {
			String type = tree.getChild(0).getChild(1).getText();
			String colon = ECL1ExpressionParser.VOCABULARY.getLiteralName(ECL1ExpressionParser.COLON).replace("'", "");
			String con = ECL1ExpressionParser.VOCABULARY.getLiteralName(ECL1ExpressionParser.CONJUNCTION).replace("'", "");
			String dis = ECL1ExpressionParser.VOCABULARY.getLiteralName(ECL1ExpressionParser.DISJUNCTION).replace("'", "");
			String exc = ECL1ExpressionParser.VOCABULARY.getLiteralName(ECL1ExpressionParser.EXCLUSION).replace("'", "");
			if(type.equalsIgnoreCase(colon)) str = ECL1ExpressionParser.VOCABULARY.getSymbolicName(ECL1ExpressionParser.COLON);
			else if(type.equalsIgnoreCase(con)) str = ECL1ExpressionParser.VOCABULARY.getSymbolicName(ECL1ExpressionParser.CONJUNCTION);
			else if(type.equalsIgnoreCase(dis)) str = ECL1ExpressionParser.VOCABULARY.getSymbolicName(ECL1ExpressionParser.DISJUNCTION);
			else if(type.equalsIgnoreCase(exc)) str = ECL1ExpressionParser.VOCABULARY.getSymbolicName(ECL1ExpressionParser.EXCLUSION);
		} else {
			str = "SIMPLE";
		}
		
		return str;
	}
	
	public static ParseTree parseExpression(String expression) throws ParseCancellationException, ECLSyntaxError {
		
		String type = checkExpression(expression);
		//System.out.println(type);
		ParseTree tree = null;
		ANTLRInputStream input = new ANTLRInputStream(expression);
		ECL1ExpressionLexer lexer = new ECL1ExpressionLexer(input);
		
		lexer.removeErrorListeners();
		lexer.addErrorListener(ThrowErrorListener.TEL);
		
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ECL1ExpressionParser parser = new ECL1ExpressionParser(tokens);
		
		parser.removeErrorListeners();
		parser.addErrorListener(ThrowErrorListener.TEL);

		try {
			
			if(type.equals("COLON")) tree = parser.refinementExpression();
			else if(type.equalsIgnoreCase("CONJUNCTION")) tree = parser.conjunctionExpression();
			else if(type.equalsIgnoreCase("DISJUNCTION")) tree = parser.disjunctionExpression();
			else if(type.equalsIgnoreCase("EXCLUSION")) tree = parser.exclusionExpression();
			else tree = parser.simpleExpression();
			
		}  catch (Exception e) {
			throw new ECLSyntaxError(e);
		}
		
		if(tree == null) throw new ECLSyntaxError("Parse result is null.");
		
		return tree;
	}
	
}
