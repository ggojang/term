package co.infoclinic.term.snomedct.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.infoclinic.term.snomedct.expression.CGExpressionBaseVisitor;
import co.infoclinic.term.snomedct.expression.CGExpressionParser.AttributeContext;
import co.infoclinic.term.snomedct.expression.CGExpressionParser.AttributeGroupContext;
import co.infoclinic.term.snomedct.expression.CGExpressionParser.AttributeSetContext;
import co.infoclinic.term.snomedct.expression.CGExpressionParser.AttributeValueContext;
import co.infoclinic.term.snomedct.expression.CGExpressionParser.ConceptReferenceContext;
import co.infoclinic.term.snomedct.expression.CGExpressionParser.ExpressionContext;
import co.infoclinic.term.snomedct.expression.CGExpressionParser.FocusConceptContext;
import co.infoclinic.term.snomedct.expression.CGExpressionParser.NonGroupedAttributeSetContext;
import co.infoclinic.term.snomedct.expression.CGExpressionParser.RefinementContext;
import co.infoclinic.term.snomedct.expression.CGExpressionParser.SubExpressionContext;
import co.infoclinic.term.snomedct.expression.model.Attribute;
import co.infoclinic.term.snomedct.expression.model.AttributeValue;
import co.infoclinic.term.snomedct.expression.model.ConceptReference;
import co.infoclinic.term.snomedct.expression.model.FocusConcept;
import co.infoclinic.term.snomedct.expression.model.Refinement;
import co.infoclinic.term.snomedct.expression.model.SubExpression;

public class CGVisitorUtil extends CGExpressionBaseVisitor<Object> {
	
	@Override
	public AttributeValue visitAttributeValue(AttributeValueContext ctx) {
		AttributeValue attributeValue = new AttributeValue();
		if(!ctx.conceptReference().isEmpty()) {
			attributeValue.setConceptReference(visitConceptReference(ctx.conceptReference()));
		}
		else if(ctx.STRING() != null && !ctx.STRING().getText().equals("")) attributeValue.setStringValue(ctx.STRING().getText());
		else if(ctx.NUMBER() != null && !ctx.NUMBER().getText().equals("")) attributeValue.setIntegerValue(Integer.parseInt(ctx.NUMBER().getText()));
		return attributeValue;
	}
	
	@Override
	public Attribute visitAttribute(AttributeContext ctx) {
		Attribute attr = new Attribute();
		attr.setAttributeName(visitConceptReference(ctx.conceptReference()));
		attr.setAttributeValue(visitAttributeValue(ctx.attributeValue()));
		return attr;
	}
	
	@Override
	public List<Attribute> visitAttributeSet(AttributeSetContext ctx) {
		List<Attribute> attrs = new ArrayList<Attribute>();
		if(ctx.getChildCount() > 1) {
			for(AttributeContext attr : ctx.attribute()) {
				attrs.add(visitAttribute(attr));
			}
		} else {
			attrs.add(visitAttribute(ctx.attribute(0)));
		}
		return attrs;
	}
	
	public List<Attribute> visitnonGroupedAttributeSet(NonGroupedAttributeSetContext ctx) {
		List<Attribute> attrs = new ArrayList<Attribute>();
		if(ctx.getChildCount() > 1) {
			for(AttributeContext attr : ctx.attribute()) {
				attrs.add(visitAttribute(attr));
			}
		} else {
			attrs.add(visitAttribute(ctx.attribute(0)));
		}
		return attrs;
	}
	
	public List<Attribute> visitAttributeGroup(AttributeGroupContext ctx) {
		return visitAttributeSet(ctx.attributeSet());
	}
	
	@Override
	public ConceptReference visitConceptReference(ConceptReferenceContext ctx) {
		ConceptReference cr = new ConceptReference();
		cr.setSctId(ctx.SCTID().getText());
		cr.setTerm(ctx.TERM().getText());
		return cr;
	}
	
	@Override
	public FocusConcept visitFocusConcept(FocusConceptContext ctx) {
		FocusConcept fc = new FocusConcept();
		List<ConceptReference> crList = new ArrayList<ConceptReference>();
		if(ctx.getChildCount() > 1) {
			for(ConceptReferenceContext crCtx : ctx.conceptReference()) {
				crList.add(visitConceptReference(crCtx));
			}
		} else {
			crList.add(visitConceptReference(ctx.conceptReference(0)));
		}
		fc.setFocusConcept(crList);
		return fc;
	}
	
	@Override
	public Refinement visitRefinement(RefinementContext ctx) {
		Refinement refinement = new Refinement();
		if(ctx.nonGroupedAttributeSet() != null) {
			refinement.setNonGroupedAttributeSet(visitnonGroupedAttributeSet(ctx.nonGroupedAttributeSet()));
		}
		if(!ctx.attributeGroup().isEmpty()) {
			List<List<Attribute>> attrGrp = new ArrayList<List<Attribute>>();
			for(AttributeGroupContext ctxGrop : ctx.attributeGroup()) {
				attrGrp.add(visitAttributeGroup(ctxGrop));
			}
			refinement.setAttributeGroup(attrGrp);
		}
		return refinement;
	}
	
	@Override
	public SubExpression visitSubExpression(SubExpressionContext ctx) {
		SubExpression se = new SubExpression();
		if(ctx.refinement() != null) {
			se.setRefinement(visitRefinement(ctx.refinement()));
		}
		se.setFocusConcept(visitFocusConcept(ctx.focusConcept()));
		return se;
	}
	
	@Override
	public Map<String, Object> visitExpression(ExpressionContext ctx) {
		Map<String, Object> expression = new HashMap<String, Object>();
		if(ctx.definitionStatus() != null) {
			expression.put("definitionStatus", ctx.definitionStatus().getText());
		}
		expression.put("subExpression", visitSubExpression(ctx.subExpression()));
		return expression;
	}

}
