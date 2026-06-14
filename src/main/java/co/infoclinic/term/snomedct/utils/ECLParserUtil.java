package co.infoclinic.term.snomedct.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;

import co.infoclinic.term.snomedct.expression.ECLExpressionLexer;
import co.infoclinic.term.snomedct.expression.ECLExpressionParser;

public class ECLParserUtil {
	
	public static class ThrowErrorListener extends BaseErrorListener {
		
		public static final ThrowErrorListener TEL = new ThrowErrorListener();
		
		@Override
		public void syntaxError(Recognizer<?, ?> recognizer,
				Object offendingSymbol, int line, int charPositionInLine,
				String msg, RecognitionException e)
				throws ParseCancellationException {
			String message = "line " + line + ", pos " + charPositionInLine
					+ ": " + msg;
			throw new ParseCancellationException(message);
		}

	}
	
	private static Map<String, Object> ECL_VALUES = new HashMap<String, Object>();
	private static Map<String, Object> TMP_GROUP_MAP = null;
	
	public static ParseTree parseExpression(String expression, boolean statment) throws ParseCancellationException, ECLSyntaxError {
		
		ParseTree tree = null;
		
		ANTLRInputStream input = new ANTLRInputStream(expression);
		ECLExpressionLexer lexer = new ECLExpressionLexer(input);
		lexer.removeErrorListeners();
		lexer.addErrorListener(ThrowErrorListener.TEL);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ECLExpressionParser parser = new ECLExpressionParser(tokens);
		parser.removeErrorListeners();
		parser.addErrorListener(ThrowErrorListener.TEL);
		
		try {
			if(statment) tree = parser.statement();
			else tree = parser.expression();
		} catch (Exception e) {
			throw new ECLSyntaxError(e);
		}
		
		if(tree == null) throw new ECLSyntaxError("Parse result is null.");
		
		return tree;
		
	}
	
	public static Map<String, Object> getParamExpression(String expression) {

		Map<String, Object> map = null;
		
		ANTLRInputStream input = new ANTLRInputStream(expression);
		ECLExpressionLexer lexer = new ECLExpressionLexer(input);
		lexer.removeErrorListeners();
		lexer.addErrorListener(ThrowErrorListener.TEL);
		
		Token token = lexer.nextToken();
		String firstRuleName = ECLExpressionLexer.ruleNames[token.getType()-1];
		lexer.reset();
		
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ECLExpressionParser parser = new ECLExpressionParser(tokens);
		parser.removeErrorListeners();
		parser.addErrorListener(ThrowErrorListener.TEL);

		ECLVisitorUtil visitUtil = new ECLVisitorUtil();
		ParseTree tree = null;
		
		if(firstRuleName.equals(ECLExpressionLexer.ruleNames[ECLExpressionLexer.ANY-1])) {
			tree = parser.refinementOnly();
			getChildTree(tree.getChild(2).getText(), 1, null);
		} else if(firstRuleName.equals(ECLExpressionLexer.ruleNames[ECLExpressionLexer.SCTID-1])) {
			ECL_VALUES = new HashMap<String, Object>();
			ECL_VALUES.put(firstRuleName, parser.getCurrentToken().getText());
		} else {
			tree = parser.expression();
			//System.out.println(visitUtil.visit(tree.getChild(1)));
			ECL_VALUES = new HashMap<String, Object>();
			ECL_VALUES.put(getNameType(tree.getChild(0).getText()), visitUtil.visit(tree.getChild(1)));
		}
		
		/**
		for( String key : ECL_VALUES.keySet() ){
			System.out.println("key==>"+key +", value===>" + ECL_VALUES.get(key));
		}
		*/
		return ECL_VALUES.isEmpty() ? map : ECL_VALUES;
	}
	
	@SuppressWarnings("unchecked")
	private static void getChildTree(String expression, int depth, String groupType) {
		
		ANTLRInputStream input = new ANTLRInputStream(expression);
		ECLExpressionLexer lexer = new ECLExpressionLexer(input);
		lexer.removeErrorListeners();
		lexer.addErrorListener(ThrowErrorListener.TEL);
		//Token token = lexer.nextToken();
		//String firstRuleName = ECLExpressionLexer.ruleNames[token.getType()-1];
		String firstRuleName = "";
		int x = 0, groupNum = 0;
		Token token;
		while(true) {
			token = lexer.nextToken();
	    	if (token.getType() == Token.EOF) {
	            break;
	        }
	    	if(token.getType() == ECLExpressionLexer.LCBRACKET) {groupNum++;}
	    	if(x == 0) {firstRuleName = ECLExpressionLexer.ruleNames[token.getType()-1];}
	    	x++;
		}
		lexer.reset();
		
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ECLExpressionParser parser = new ECLExpressionParser(tokens);
		ParseTree tree = null;
		ECLVisitorUtil visitUtil = new ECLVisitorUtil();
		//System.out.println(expression);
		
		if(firstRuleName.equals(ECLExpressionLexer.ruleNames[ECLExpressionLexer.LCBRACKET-1])) {
			if(groupNum > 1) tree = parser.refinement();
			else tree = parser.attributeGroup();
			
			if(ECL_VALUES.get("G") == null) ECL_VALUES.put("G", new ArrayList<Map<String,Object>>());
			
			for(int i=0; i<tree.getChildCount(); i++) {
				TMP_GROUP_MAP = new HashMap<String, Object>();
				ParseTree t = tree.getChild(i);
				if(t.getChildCount() > 0) {
					//System.out.println("1>====="+t.getText());
					for(int a=0; a<t.getChildCount(); a++) {
						ParseTree tt = t.getChild(a);
						if(tt.getChildCount() > 0) {
							if(groupNum > 1) {
								for(int b=0; b<tt.getChildCount(); b++) {
									ParseTree ttt = tt.getChild(b);
									if(ttt.getChildCount() > 0) getChildTree(ttt.getText(), depth+1, "G");
								}
							}
							else getChildTree(tt.getText(), depth+1, "G");
						}
					}
				}
				if(!TMP_GROUP_MAP.isEmpty()){
					List<Map<String,Object>> list = (List<Map<String, Object>>) ECL_VALUES.get("G");
					list.add(TMP_GROUP_MAP);
				}
			}
			
		} else {
			tree = parser.refinement();
			Map<String, Object> nonGroup = new HashMap<String, Object>();
			for(int i=0; i<tree.getChildCount(); i++) {
				ParseTree t = tree.getChild(i);
				if(t.getChildCount() > 0) {
					//System.out.println(groupType+","+i+"===>"+t.getText());
					if(groupType != null && groupType.equals("G")) {
						String str = visitUtil.visit(t); 
						String[] sctid = str.split("=");
						TMP_GROUP_MAP.put(sctid[0], sctid[1]);
					} else {
						if(getNameType(t.getText()).equals(ECLExpressionLexer.ruleNames[ECLExpressionLexer.LCBRACKET-1])) {
							getChildTree(t.getText(), depth+1, "G");
						} else {
							if(t.getChildCount() == 1) {
								String str = visitUtil.visit(t); 
								String[] sctid = str.split("=");
								nonGroup.put(sctid[0], sctid[1]);
							} else if(t.getChildCount() > 1) {
								for(int a=0; a<t.getChildCount(); a++) {
									ParseTree tt = t.getChild(a);
									if(tt.getChildCount() > 0){
										String str = visitUtil.visit(tt); 
										String[] sctid = str.split("=");
										nonGroup.put(sctid[0], sctid[1]);
									}	
								}
							}
						}
					}
				}
			}
			
			if(!nonGroup.isEmpty()) {
				ECL_VALUES.put("N", nonGroup);
			}
			
		}
		
	}
	
	private static String getNameType(String expression) {
		ANTLRInputStream input = new ANTLRInputStream(expression);
		ECLExpressionLexer lexer = new ECLExpressionLexer(input);
		lexer.removeErrorListeners();
		lexer.addErrorListener(ThrowErrorListener.TEL);
		return ECLExpressionLexer.ruleNames[lexer.nextToken().getType()-1];
	}

}
