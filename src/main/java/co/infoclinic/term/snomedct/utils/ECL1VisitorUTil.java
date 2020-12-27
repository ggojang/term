package co.infoclinic.term.snomedct.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.infoclinic.term.snomedct.expression.ECL1ExpressionBaseVisitor;
import co.infoclinic.term.snomedct.expression.ECL1ExpressionParser;
import co.infoclinic.term.snomedct.expression.ECL1ExpressionParser.AttributeContext;
import co.infoclinic.term.snomedct.expression.ECL1ExpressionParser.AttributeNameContext;
import co.infoclinic.term.snomedct.expression.ECL1ExpressionParser.AttributeSetContext;
import co.infoclinic.term.snomedct.expression.ECL1ExpressionParser.AttributeValueContext;
import co.infoclinic.term.snomedct.expression.ECL1ExpressionParser.ConceptReferenceContext;
import co.infoclinic.term.snomedct.expression.ECL1ExpressionParser.ConjunctionExpressionContext;
import co.infoclinic.term.snomedct.expression.ECL1ExpressionParser.DisjunctionExpressionContext;
import co.infoclinic.term.snomedct.expression.ECL1ExpressionParser.ExclusionExpressionContext;
import co.infoclinic.term.snomedct.expression.ECL1ExpressionParser.FocusConceptContext;
import co.infoclinic.term.snomedct.expression.ECL1ExpressionParser.RefinementExpressionContext;
import co.infoclinic.term.snomedct.expression.ECL1ExpressionParser.SimpleExpressionContext;
import co.infoclinic.term.snomedct.expression.model.ConceptReference;

public class ECL1VisitorUTil extends ECL1ExpressionBaseVisitor<Object> {
	
	@Override
	public ConceptReference visitConceptReference(ConceptReferenceContext ctx) {
		ConceptReference cr = new ConceptReference();
		cr.setSctId(ctx.SCTID().getText());
		if(ctx.TERM() != null && !ctx.TERM().getText().equals("")) cr.setTerm(ctx.TERM().getText());
		return cr;
	}
	
	@Override
	public Map<String, Object> visitFocusConcept(FocusConceptContext ctx) {
		Map<String, Object> map = new HashMap<String, Object>();
		boolean memberOf = (ctx.MEMBEROF() != null && !ctx.MEMBEROF().equals("")) ? true : false;
		ConceptReference cr = null;
		if(ctx.ANY() != null && !ctx.ANY().getText().equals("")) {
			cr = new ConceptReference();
			cr.setSctId("*");
		} else {
			cr = visitConceptReference(ctx.conceptReference());
		}
		
		map.put("memberOf", memberOf);
		map.put("concept", cr);
		
		return map;
	}
	
	@Override
	public Map<String, Object> visitSimpleExpression(SimpleExpressionContext ctx) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		String operator = "SELF";
		if(ctx.constraintOperator() != null) {
			if(ctx.constraintOperator().DESCENDANTOF() != null) operator = ECL1ExpressionParser.VOCABULARY.getSymbolicName(ECL1ExpressionParser.DESCENDANTOF);
			else if(ctx.constraintOperator().DESCENDANTORSELFOF() != null) operator = ECL1ExpressionParser.VOCABULARY.getSymbolicName(ECL1ExpressionParser.DESCENDANTORSELFOF);
			else if(ctx.constraintOperator().CHILDOF() != null) operator = ECL1ExpressionParser.VOCABULARY.getSymbolicName(ECL1ExpressionParser.CHILDOF);
			else if(ctx.constraintOperator().ANCESTOF() != null) operator = ECL1ExpressionParser.VOCABULARY.getSymbolicName(ECL1ExpressionParser.ANCESTOF);
			else if(ctx.constraintOperator().ANCESTORSELFOF() != null) operator = ECL1ExpressionParser.VOCABULARY.getSymbolicName(ECL1ExpressionParser.ANCESTORSELFOF);
			else if(ctx.constraintOperator().PARENTOF() != null) operator = ECL1ExpressionParser.VOCABULARY.getSymbolicName(ECL1ExpressionParser.PARENTOF);
		}
		Map<String, Object> visitMap = visitFocusConcept(ctx.focusConcept());
		
		map.put("operator", operator);
		map.put("type", "FOCUSCONCEPT");
		map.put("obj", visitMap);
		
		return map;
	}
	
	@Override
	public Map<String, Object> visitDisjunctionExpression(DisjunctionExpressionContext ctx) {
		
		Map<String, Object> map = new HashMap<String, Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		
		Map<String, Object> obj = null;
		for(SimpleExpressionContext sec : ctx.simpleExpression()) {
			obj = visitSimpleExpression(sec);
			list.add(obj);
		}
		map.put("expression", ECL1ExpressionParser.VOCABULARY.getSymbolicName(ECL1ExpressionParser.DISJUNCTION));
		map.put("type", "SIMPLEEXPRESSION");
		map.put("obj", list);
		return map;
	}
	
	@Override
	public Map<String, Object> visitConjunctionExpression(ConjunctionExpressionContext ctx) {
		
		Map<String, Object> map = new HashMap<String, Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		
		Map<String, Object> obj = null;
		for(SimpleExpressionContext sec : ctx.simpleExpression()) {
			obj = visitSimpleExpression(sec);
			list.add(obj);
		}
		map.put("expression", ECL1ExpressionParser.VOCABULARY.getSymbolicName(ECL1ExpressionParser.CONJUNCTION));
		map.put("type", "SIMPLEEXPRESSION");
		map.put("obj", list);
		return map;
		
	}
	
	@Override
	public Map<String, Object> visitExclusionExpression(ExclusionExpressionContext ctx) {
		
		Map<String, Object> map = new HashMap<String, Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		
		Map<String, Object> obj = null;
		for(SimpleExpressionContext sec : ctx.simpleExpression()) {
			obj = visitSimpleExpression(sec);
			list.add(obj);
		}
		map.put("expression", ECL1ExpressionParser.VOCABULARY.getSymbolicName(ECL1ExpressionParser.EXCLUSION));
		map.put("type", "SIMPLEEXPRESSION");
		map.put("obj", list);
		return map;
		
	}
	
	@Override
	public ConceptReference visitAttributeName(AttributeNameContext ctx) {
		ConceptReference cr = null;
		if(ctx.ANY() != null) {
			cr = new ConceptReference();
			cr.setSctId("*");
		} else {
			cr = visitConceptReference(ctx.conceptReference());
		}
		return cr;
	}
	
	@Override
	public Map<String, Object> visitAttributeValue(AttributeValueContext ctx) {
		
		Map<String, Object> map = null;
		if(ctx.simpleExpression() != null) map = visitSimpleExpression(ctx.simpleExpression());
		else if(ctx.STRING() != null) {
			map = new HashMap<String, Object>();
			map.put("type", "STRING");
			map.put("operator", ctx.stringOperator().getText());
			map.put("obj", ctx.STRING().getText());
		}
		else if(ctx.NUMBER() != null) {
			map = new HashMap<String, Object>();
			map.put("type", "NUMBER");
			map.put("operator", ctx.numberOperator().getText());
			map.put("obj", ctx.NUMBER().getText());
		}
		else if(ctx.compoundAttributevalue() != null) {
			if(ctx.compoundAttributevalue().conjunctionExpression() != null) {
				map = visitConjunctionExpression(ctx.compoundAttributevalue().conjunctionExpression());
			}
			else if(ctx.compoundAttributevalue().disjunctionExpression() != null) {
				map = visitDisjunctionExpression(ctx.compoundAttributevalue().disjunctionExpression());
			}
		}
		else if(ctx.exclusionAttributevalue() != null) {
			if(ctx.exclusionAttributevalue().compoundAttributevalue().conjunctionExpression() != null){
				map = visitConjunctionExpression(ctx.exclusionAttributevalue().compoundAttributevalue().conjunctionExpression());
			}
			else if(ctx.exclusionAttributevalue().compoundAttributevalue().disjunctionExpression() != null) {
				map = visitDisjunctionExpression(ctx.exclusionAttributevalue().compoundAttributevalue().disjunctionExpression());
			}
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			Map<String, Object> obj = null;
			for(SimpleExpressionContext sec : ctx.exclusionAttributevalue().simpleExpression()) {
				obj = visitSimpleExpression(sec);
				list.add(obj);
			}
			map.put(ECL1ExpressionParser.VOCABULARY.getSymbolicName(ECL1ExpressionParser.EXCLUSION).toLowerCase(), list);
		}
		return map;
	}
	
	@Override
	public Map<String, Object> visitAttribute(AttributeContext ctx) {
		
		Map<String, Object> map = new HashMap<String, Object>();
		String operator = null;
		if(ctx.attributeOperator() != null) {
			if(ctx.attributeOperator().DESCENDANTOF() != null) operator = ECL1ExpressionParser.VOCABULARY.getSymbolicName(ECL1ExpressionParser.DESCENDANTOF);
			else if(ctx.attributeOperator().DESCENDANTORSELFOF() != null) operator = ECL1ExpressionParser.VOCABULARY.getSymbolicName(ECL1ExpressionParser.DESCENDANTORSELFOF);
		}
		map.put("operator", operator);
		map.put("type", "ATTRIBUTE");
		map.put("name", visitAttributeName(ctx.attributeName()));
		map.put("value", visitAttributeValue(ctx.attributeValue()));
		return map;
	}
	
	@Override
	public Map<String, Object> visitAttributeSet(AttributeSetContext ctx) {
		
		Map<String, Object> map = new HashMap<String, Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> obj = null;
		for(AttributeContext ac : ctx.attribute()) {
			obj = visitAttribute(ac);
			list.add(obj);
		}
		map.put("type", "ATTRIBUTESET");
		map.put("obj", list);
		return map;
	}
	
	@Override
	public Map<String, Object> visitRefinementExpression(RefinementExpressionContext ctx) {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("focus", visitSimpleExpression(ctx.simpleExpression()));
		
		if(ctx.refinement().attributeSet() != null) {
			map.put("refinement", visitAttributeSet(ctx.refinement().attributeSet()));
		}
		return map;
	}

}
