package co.infoclinic.term.snomedct.utils;

import java.util.Iterator;
import java.util.TreeSet;

import co.infoclinic.term.snomedct.expression.ECLExpressionBaseVisitor;
import co.infoclinic.term.snomedct.expression.ECLExpressionParser.AttributeContext;
import co.infoclinic.term.snomedct.expression.ECLExpressionParser.AttributeGroupContext;
import co.infoclinic.term.snomedct.expression.ECLExpressionParser.AttributeNonGroupContext;
import co.infoclinic.term.snomedct.expression.ECLExpressionParser.AttributeSetContext;
import co.infoclinic.term.snomedct.expression.ECLExpressionParser.AttributeValueContext;
import co.infoclinic.term.snomedct.expression.ECLExpressionParser.ConceptReferenceContext;
import co.infoclinic.term.snomedct.expression.ECLExpressionParser.ExpressionContext;
import co.infoclinic.term.snomedct.expression.ECLExpressionParser.FocusConceptContext;
import co.infoclinic.term.snomedct.expression.ECLExpressionParser.NestedExpressionContext;
import co.infoclinic.term.snomedct.expression.ECLExpressionParser.RefinementContext;
import co.infoclinic.term.snomedct.expression.ECLExpressionParser.StatementContext;
import co.infoclinic.term.snomedct.expression.ECLExpressionParser.SubExpressionContext;

public class ECLVisitorUtil extends ECLExpressionBaseVisitor<String> {
	
	@Override
	public String visitAttribute(AttributeContext ctx) {
		return ctx.conceptReference().SCTID().getText() + "=" + visitAttributeValue(ctx.attributeValue());
	}
	
	@Override
	public String visitAttributeValue(AttributeValueContext ctx) {
		return super.visitAttributeValue(ctx);
	}
	
	@Override
	public String visitAttributeSet(AttributeSetContext ctx) {
		if(ctx.getChildCount() > 1) {
			TreeSet<String> attributeSet = new TreeSet<String>();
			for(AttributeContext attr : ctx.attribute()) {
				attributeSet.add(visitAttribute(attr));
			}
			StringBuilder result = new StringBuilder();
			for (Iterator<String> i = attributeSet.iterator(); i.hasNext();) {
				result.append(i.next());
				if (i.hasNext())
					result.append(',');
			}
			return result.toString();
		} else {
			return visitAttribute(ctx.attribute(0));
		}	
	}
	
	@Override
	public String visitAttributeGroup(AttributeGroupContext ctx) {
		return "{" + visitAttributeSet(ctx.attributeSet()) + "}";
	}
	
	@Override
	public String visitAttributeNonGroup(AttributeNonGroupContext ctx) {
		if(ctx.getChildCount() > 1) {
			TreeSet<String> attributeSet = new TreeSet<String>();
			for(AttributeContext attr : ctx.attribute()) {
				attributeSet.add(visitAttribute(attr));
			}
			StringBuilder result = new StringBuilder();
			for (Iterator<String> i = attributeSet.iterator(); i.hasNext();) {
				result.append(i.next());
				if (i.hasNext())
					result.append(',');
			}
			return result.toString();
		} else {
			return visitAttribute(ctx.attribute(0));
		}
	}
	
	@Override
	protected String defaultResult() {
		return "";
	}

	@Override
	protected String aggregateResult(String aggregate, String nextResult) {
		return aggregate + nextResult;
	}
	
	@Override
	public String visitConceptReference(ConceptReferenceContext ctx) {
		return ctx.SCTID().getText();
	}
	
	@Override
	public String visitFocusConcept(FocusConceptContext ctx) {
		if(ctx.getChildCount() > 1) {
			TreeSet<Long> conceptIdSet = new TreeSet<Long>();
			for(ConceptReferenceContext crCtx : ctx.conceptReference()) { 
				conceptIdSet.add(Long.valueOf(crCtx.SCTID().getText()));
			}
			StringBuilder result = new StringBuilder();
			for (Iterator<Long> i = conceptIdSet.iterator(); i.hasNext();) {
				result.append(i.next());
				if (i.hasNext())
					result.append('+');
			}
			return result.toString();
		} else {
			return ctx.conceptReference(0).SCTID().getText();
		}
	}
	
	@Override
	public String visitRefinement(RefinementContext ctx) {
		StringBuilder result = new StringBuilder();
		if(ctx.attributeNonGroup() != null) {
			result.append(visitAttributeNonGroup(ctx.attributeNonGroup()));
			if(!ctx.attributeGroup().isEmpty())
				result.append(',');
		}
		
		if(!ctx.attributeGroup().isEmpty()) {
			TreeSet<String> attributeGroupSet = new TreeSet<String>();
			for(AttributeGroupContext attrGroup : ctx.attributeGroup()) {
				attributeGroupSet.add(visitAttributeGroup(attrGroup));
			}
			for (Iterator<String> i = attributeGroupSet.iterator(); i.hasNext();) {
				result.append(i.next());
				if (i.hasNext())
					result.append(',');
			}
		}

		return result.toString();
	}
	
	@Override
	public String visitSubExpression(SubExpressionContext ctx) {
		if(ctx.refinement() != null) {
			return visitFocusConcept(ctx.focusConcept()) + ":" + visitRefinement(ctx.refinement());
		} else {
			return visitFocusConcept(ctx.focusConcept());
		}
	}
	
	@Override
	public String visitNestedExpression(NestedExpressionContext ctx) {
		return "(" + visitSubExpression(ctx.subExpression()) + ")";
	}
	
	@Override
	public String visitExpression(ExpressionContext ctx) {
		if(ctx.constraintOperator() != null) {
			return ctx.constraintOperator().getText() + visitSubExpression(ctx.subExpression());
		} else {
			return visitSubExpression(ctx.subExpression());
		}
	}
	
	@Override
	public String visitStatement(StatementContext ctx) {
		// TODO Auto-generated method stub
		return super.visitStatement(ctx);
	}

}
