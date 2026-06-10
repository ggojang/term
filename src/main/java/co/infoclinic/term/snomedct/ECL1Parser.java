package co.infoclinic.term.snomedct;

import java.util.Map;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;

import co.infoclinic.term.snomedct.utils.ECL1ParserUtil;
import co.infoclinic.term.snomedct.utils.ECL1VisitorUTil;
import co.infoclinic.term.snomedct.utils.ECLSyntaxError;

public class ECL1Parser {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//String expression = "< 19829001 |disorder of lung| MINUS < 301867009 |edema of trunk|";
		String expression = "^ 404684003 |clinical finding|";
		//String expression = "<< 404684003 |clinical finding|: << 47429007 |associated with| = << 267038008 |edema|";
		//String expression = "< 404684003 |clinical finding|: 116676008 |associated morphology| = ((<< 56208002 |ulcer| AND << 50960005 |hemorrhage|) MINUS << 26036001 |obstruction|)";
		try {
			//ECL1ParserUtil.parseExpression(expression);
			ParseTree tree = ECL1ParserUtil.parseExpression(expression);
			
			ECL1VisitorUTil visitor = new ECL1VisitorUTil();
			@SuppressWarnings("unchecked")
			Map<String, Object> exMap = (Map<String, Object>) visitor.visit(tree);
			System.out.println(exMap);
			
			 
		} catch (ParseCancellationException | ECLSyntaxError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

}
