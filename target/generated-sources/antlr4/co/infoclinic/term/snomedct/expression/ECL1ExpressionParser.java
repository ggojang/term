// Generated from co/infoclinic/term/snomedct/expression/ECL1Expression.g4 by ANTLR 4.13.1
package co.infoclinic.term.snomedct.expression;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class ECL1ExpressionParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		DESCENDANTOF=1, DESCENDANTORSELFOF=2, CHILDOF=3, ANCESTOF=4, ANCESTORSELFOF=5, 
		PARENTOF=6, MEMBEROF=7, COLON=8, PLUS=9, COMMA=10, LPARAN=11, RPARAN=12, 
		LCBRACKET=13, RCBRACKET=14, CONJUNCTION=15, DISJUNCTION=16, EXCLUSION=17, 
		EQUAL=18, NOTEQUAL=19, LESSOREQUAL=20, GREATEROREQUAL=21, ANY=22, TERM=23, 
		SCTID=24, NUMBER=25, STRING=26, WS=27;
	public static final int
		RULE_expression = 0, RULE_refinementExpression = 1, RULE_conjunctionExpression = 2, 
		RULE_disjunctionExpression = 3, RULE_exclusionExpression = 4, RULE_simpleExpression = 5, 
		RULE_focusConcept = 6, RULE_constraintOperator = 7, RULE_conceptReference = 8, 
		RULE_refinement = 9, RULE_attributeGroup = 10, RULE_attributeSet = 11, 
		RULE_attributeOperator = 12, RULE_attribute = 13, RULE_attributeName = 14, 
		RULE_attributeValue = 15, RULE_compoundAttributevalue = 16, RULE_exclusionAttributevalue = 17, 
		RULE_stringOperator = 18, RULE_numberOperator = 19, RULE_expressionOperator = 20;
	private static String[] makeRuleNames() {
		return new String[] {
			"expression", "refinementExpression", "conjunctionExpression", "disjunctionExpression", 
			"exclusionExpression", "simpleExpression", "focusConcept", "constraintOperator", 
			"conceptReference", "refinement", "attributeGroup", "attributeSet", "attributeOperator", 
			"attribute", "attributeName", "attributeValue", "compoundAttributevalue", 
			"exclusionAttributevalue", "stringOperator", "numberOperator", "expressionOperator"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'<'", "'<<'", "'<!'", "'>'", "'>>'", "'>!'", "'^'", "':'", "'+'", 
			"','", "'('", "')'", "'{'", "'}'", "'AND'", "'OR'", "'MINUS'", "'='", 
			"'!='", "'<='", "'>='", "'*'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "DESCENDANTOF", "DESCENDANTORSELFOF", "CHILDOF", "ANCESTOF", "ANCESTORSELFOF", 
			"PARENTOF", "MEMBEROF", "COLON", "PLUS", "COMMA", "LPARAN", "RPARAN", 
			"LCBRACKET", "RCBRACKET", "CONJUNCTION", "DISJUNCTION", "EXCLUSION", 
			"EQUAL", "NOTEQUAL", "LESSOREQUAL", "GREATEROREQUAL", "ANY", "TERM", 
			"SCTID", "NUMBER", "STRING", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "ECL1Expression.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public ECL1ExpressionParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionContext extends ParserRuleContext {
		public SimpleExpressionContext simpleExpression() {
			return getRuleContext(SimpleExpressionContext.class,0);
		}
		public ConjunctionExpressionContext conjunctionExpression() {
			return getRuleContext(ConjunctionExpressionContext.class,0);
		}
		public DisjunctionExpressionContext disjunctionExpression() {
			return getRuleContext(DisjunctionExpressionContext.class,0);
		}
		public ExclusionExpressionContext exclusionExpression() {
			return getRuleContext(ExclusionExpressionContext.class,0);
		}
		public RefinementExpressionContext refinementExpression() {
			return getRuleContext(RefinementExpressionContext.class,0);
		}
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECL1ExpressionVisitor ) return ((ECL1ExpressionVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_expression);
		try {
			setState(47);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(42);
				simpleExpression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(43);
				conjunctionExpression();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(44);
				disjunctionExpression();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(45);
				exclusionExpression();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(46);
				refinementExpression();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RefinementExpressionContext extends ParserRuleContext {
		public SimpleExpressionContext simpleExpression() {
			return getRuleContext(SimpleExpressionContext.class,0);
		}
		public TerminalNode COLON() { return getToken(ECL1ExpressionParser.COLON, 0); }
		public RefinementContext refinement() {
			return getRuleContext(RefinementContext.class,0);
		}
		public RefinementExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_refinementExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).enterRefinementExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).exitRefinementExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECL1ExpressionVisitor ) return ((ECL1ExpressionVisitor<? extends T>)visitor).visitRefinementExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RefinementExpressionContext refinementExpression() throws RecognitionException {
		RefinementExpressionContext _localctx = new RefinementExpressionContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_refinementExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(49);
			simpleExpression();
			setState(50);
			match(COLON);
			setState(51);
			refinement();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ConjunctionExpressionContext extends ParserRuleContext {
		public List<SimpleExpressionContext> simpleExpression() {
			return getRuleContexts(SimpleExpressionContext.class);
		}
		public SimpleExpressionContext simpleExpression(int i) {
			return getRuleContext(SimpleExpressionContext.class,i);
		}
		public List<TerminalNode> CONJUNCTION() { return getTokens(ECL1ExpressionParser.CONJUNCTION); }
		public TerminalNode CONJUNCTION(int i) {
			return getToken(ECL1ExpressionParser.CONJUNCTION, i);
		}
		public ConjunctionExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conjunctionExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).enterConjunctionExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).exitConjunctionExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECL1ExpressionVisitor ) return ((ECL1ExpressionVisitor<? extends T>)visitor).visitConjunctionExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConjunctionExpressionContext conjunctionExpression() throws RecognitionException {
		ConjunctionExpressionContext _localctx = new ConjunctionExpressionContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_conjunctionExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(53);
			simpleExpression();
			setState(58);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==CONJUNCTION) {
				{
				{
				setState(54);
				match(CONJUNCTION);
				setState(55);
				simpleExpression();
				}
				}
				setState(60);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DisjunctionExpressionContext extends ParserRuleContext {
		public List<SimpleExpressionContext> simpleExpression() {
			return getRuleContexts(SimpleExpressionContext.class);
		}
		public SimpleExpressionContext simpleExpression(int i) {
			return getRuleContext(SimpleExpressionContext.class,i);
		}
		public List<TerminalNode> DISJUNCTION() { return getTokens(ECL1ExpressionParser.DISJUNCTION); }
		public TerminalNode DISJUNCTION(int i) {
			return getToken(ECL1ExpressionParser.DISJUNCTION, i);
		}
		public DisjunctionExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_disjunctionExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).enterDisjunctionExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).exitDisjunctionExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECL1ExpressionVisitor ) return ((ECL1ExpressionVisitor<? extends T>)visitor).visitDisjunctionExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DisjunctionExpressionContext disjunctionExpression() throws RecognitionException {
		DisjunctionExpressionContext _localctx = new DisjunctionExpressionContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_disjunctionExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(61);
			simpleExpression();
			setState(66);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DISJUNCTION) {
				{
				{
				setState(62);
				match(DISJUNCTION);
				setState(63);
				simpleExpression();
				}
				}
				setState(68);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExclusionExpressionContext extends ParserRuleContext {
		public List<SimpleExpressionContext> simpleExpression() {
			return getRuleContexts(SimpleExpressionContext.class);
		}
		public SimpleExpressionContext simpleExpression(int i) {
			return getRuleContext(SimpleExpressionContext.class,i);
		}
		public List<TerminalNode> EXCLUSION() { return getTokens(ECL1ExpressionParser.EXCLUSION); }
		public TerminalNode EXCLUSION(int i) {
			return getToken(ECL1ExpressionParser.EXCLUSION, i);
		}
		public ExclusionExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exclusionExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).enterExclusionExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).exitExclusionExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECL1ExpressionVisitor ) return ((ECL1ExpressionVisitor<? extends T>)visitor).visitExclusionExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExclusionExpressionContext exclusionExpression() throws RecognitionException {
		ExclusionExpressionContext _localctx = new ExclusionExpressionContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_exclusionExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(69);
			simpleExpression();
			setState(74);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==EXCLUSION) {
				{
				{
				setState(70);
				match(EXCLUSION);
				setState(71);
				simpleExpression();
				}
				}
				setState(76);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SimpleExpressionContext extends ParserRuleContext {
		public FocusConceptContext focusConcept() {
			return getRuleContext(FocusConceptContext.class,0);
		}
		public ConstraintOperatorContext constraintOperator() {
			return getRuleContext(ConstraintOperatorContext.class,0);
		}
		public SimpleExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).enterSimpleExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).exitSimpleExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECL1ExpressionVisitor ) return ((ECL1ExpressionVisitor<? extends T>)visitor).visitSimpleExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SimpleExpressionContext simpleExpression() throws RecognitionException {
		SimpleExpressionContext _localctx = new SimpleExpressionContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_simpleExpression);
		try {
			setState(81);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MEMBEROF:
			case ANY:
			case SCTID:
				enterOuterAlt(_localctx, 1);
				{
				setState(77);
				focusConcept();
				}
				break;
			case DESCENDANTOF:
			case DESCENDANTORSELFOF:
			case CHILDOF:
			case ANCESTOF:
			case ANCESTORSELFOF:
			case PARENTOF:
				enterOuterAlt(_localctx, 2);
				{
				setState(78);
				constraintOperator();
				setState(79);
				focusConcept();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FocusConceptContext extends ParserRuleContext {
		public TerminalNode MEMBEROF() { return getToken(ECL1ExpressionParser.MEMBEROF, 0); }
		public ConceptReferenceContext conceptReference() {
			return getRuleContext(ConceptReferenceContext.class,0);
		}
		public TerminalNode ANY() { return getToken(ECL1ExpressionParser.ANY, 0); }
		public FocusConceptContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_focusConcept; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).enterFocusConcept(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).exitFocusConcept(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECL1ExpressionVisitor ) return ((ECL1ExpressionVisitor<? extends T>)visitor).visitFocusConcept(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FocusConceptContext focusConcept() throws RecognitionException {
		FocusConceptContext _localctx = new FocusConceptContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_focusConcept);
		try {
			setState(87);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MEMBEROF:
				enterOuterAlt(_localctx, 1);
				{
				setState(83);
				match(MEMBEROF);
				setState(84);
				conceptReference();
				}
				break;
			case SCTID:
				enterOuterAlt(_localctx, 2);
				{
				setState(85);
				conceptReference();
				}
				break;
			case ANY:
				enterOuterAlt(_localctx, 3);
				{
				setState(86);
				match(ANY);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ConstraintOperatorContext extends ParserRuleContext {
		public TerminalNode DESCENDANTOF() { return getToken(ECL1ExpressionParser.DESCENDANTOF, 0); }
		public TerminalNode DESCENDANTORSELFOF() { return getToken(ECL1ExpressionParser.DESCENDANTORSELFOF, 0); }
		public TerminalNode CHILDOF() { return getToken(ECL1ExpressionParser.CHILDOF, 0); }
		public TerminalNode ANCESTOF() { return getToken(ECL1ExpressionParser.ANCESTOF, 0); }
		public TerminalNode ANCESTORSELFOF() { return getToken(ECL1ExpressionParser.ANCESTORSELFOF, 0); }
		public TerminalNode PARENTOF() { return getToken(ECL1ExpressionParser.PARENTOF, 0); }
		public ConstraintOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constraintOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).enterConstraintOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).exitConstraintOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECL1ExpressionVisitor ) return ((ECL1ExpressionVisitor<? extends T>)visitor).visitConstraintOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstraintOperatorContext constraintOperator() throws RecognitionException {
		ConstraintOperatorContext _localctx = new ConstraintOperatorContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_constraintOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(89);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 126L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ConceptReferenceContext extends ParserRuleContext {
		public TerminalNode SCTID() { return getToken(ECL1ExpressionParser.SCTID, 0); }
		public TerminalNode TERM() { return getToken(ECL1ExpressionParser.TERM, 0); }
		public ConceptReferenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conceptReference; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).enterConceptReference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).exitConceptReference(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECL1ExpressionVisitor ) return ((ECL1ExpressionVisitor<? extends T>)visitor).visitConceptReference(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConceptReferenceContext conceptReference() throws RecognitionException {
		ConceptReferenceContext _localctx = new ConceptReferenceContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_conceptReference);
		try {
			setState(94);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(91);
				match(SCTID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(92);
				match(SCTID);
				setState(93);
				match(TERM);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RefinementContext extends ParserRuleContext {
		public AttributeSetContext attributeSet() {
			return getRuleContext(AttributeSetContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(ECL1ExpressionParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ECL1ExpressionParser.COMMA, i);
		}
		public List<AttributeGroupContext> attributeGroup() {
			return getRuleContexts(AttributeGroupContext.class);
		}
		public AttributeGroupContext attributeGroup(int i) {
			return getRuleContext(AttributeGroupContext.class,i);
		}
		public RefinementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_refinement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).enterRefinement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).exitRefinement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECL1ExpressionVisitor ) return ((ECL1ExpressionVisitor<? extends T>)visitor).visitRefinement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RefinementContext refinement() throws RecognitionException {
		RefinementContext _localctx = new RefinementContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_refinement);
		int _la;
		try {
			setState(113);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(96);
				attributeSet();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(97);
				attributeSet();
				setState(102);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(98);
					match(COMMA);
					setState(99);
					attributeGroup();
					}
					}
					setState(104);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(105);
				attributeGroup();
				setState(110);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(106);
					match(COMMA);
					setState(107);
					attributeGroup();
					}
					}
					setState(112);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AttributeGroupContext extends ParserRuleContext {
		public TerminalNode LCBRACKET() { return getToken(ECL1ExpressionParser.LCBRACKET, 0); }
		public AttributeSetContext attributeSet() {
			return getRuleContext(AttributeSetContext.class,0);
		}
		public TerminalNode RCBRACKET() { return getToken(ECL1ExpressionParser.RCBRACKET, 0); }
		public AttributeGroupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attributeGroup; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).enterAttributeGroup(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).exitAttributeGroup(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECL1ExpressionVisitor ) return ((ECL1ExpressionVisitor<? extends T>)visitor).visitAttributeGroup(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttributeGroupContext attributeGroup() throws RecognitionException {
		AttributeGroupContext _localctx = new AttributeGroupContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_attributeGroup);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(115);
			match(LCBRACKET);
			setState(116);
			attributeSet();
			setState(117);
			match(RCBRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AttributeSetContext extends ParserRuleContext {
		public List<AttributeContext> attribute() {
			return getRuleContexts(AttributeContext.class);
		}
		public AttributeContext attribute(int i) {
			return getRuleContext(AttributeContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ECL1ExpressionParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ECL1ExpressionParser.COMMA, i);
		}
		public AttributeSetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attributeSet; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).enterAttributeSet(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).exitAttributeSet(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECL1ExpressionVisitor ) return ((ECL1ExpressionVisitor<? extends T>)visitor).visitAttributeSet(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttributeSetContext attributeSet() throws RecognitionException {
		AttributeSetContext _localctx = new AttributeSetContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_attributeSet);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(119);
			attribute();
			setState(124);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(120);
					match(COMMA);
					setState(121);
					attribute();
					}
					} 
				}
				setState(126);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AttributeOperatorContext extends ParserRuleContext {
		public TerminalNode DESCENDANTOF() { return getToken(ECL1ExpressionParser.DESCENDANTOF, 0); }
		public TerminalNode DESCENDANTORSELFOF() { return getToken(ECL1ExpressionParser.DESCENDANTORSELFOF, 0); }
		public AttributeOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attributeOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).enterAttributeOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).exitAttributeOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECL1ExpressionVisitor ) return ((ECL1ExpressionVisitor<? extends T>)visitor).visitAttributeOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttributeOperatorContext attributeOperator() throws RecognitionException {
		AttributeOperatorContext _localctx = new AttributeOperatorContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_attributeOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(127);
			_la = _input.LA(1);
			if ( !(_la==DESCENDANTOF || _la==DESCENDANTORSELFOF) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AttributeContext extends ParserRuleContext {
		public AttributeNameContext attributeType;
		public TerminalNode EQUAL() { return getToken(ECL1ExpressionParser.EQUAL, 0); }
		public AttributeValueContext attributeValue() {
			return getRuleContext(AttributeValueContext.class,0);
		}
		public AttributeNameContext attributeName() {
			return getRuleContext(AttributeNameContext.class,0);
		}
		public AttributeOperatorContext attributeOperator() {
			return getRuleContext(AttributeOperatorContext.class,0);
		}
		public AttributeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attribute; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).enterAttribute(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).exitAttribute(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECL1ExpressionVisitor ) return ((ECL1ExpressionVisitor<? extends T>)visitor).visitAttribute(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttributeContext attribute() throws RecognitionException {
		AttributeContext _localctx = new AttributeContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_attribute);
		try {
			setState(138);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ANY:
			case SCTID:
				enterOuterAlt(_localctx, 1);
				{
				setState(129);
				((AttributeContext)_localctx).attributeType = attributeName();
				setState(130);
				match(EQUAL);
				setState(131);
				attributeValue();
				}
				break;
			case DESCENDANTOF:
			case DESCENDANTORSELFOF:
				enterOuterAlt(_localctx, 2);
				{
				setState(133);
				attributeOperator();
				setState(134);
				attributeName();
				setState(135);
				match(EQUAL);
				setState(136);
				attributeValue();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AttributeNameContext extends ParserRuleContext {
		public ConceptReferenceContext conceptReference() {
			return getRuleContext(ConceptReferenceContext.class,0);
		}
		public TerminalNode ANY() { return getToken(ECL1ExpressionParser.ANY, 0); }
		public AttributeNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attributeName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).enterAttributeName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).exitAttributeName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECL1ExpressionVisitor ) return ((ECL1ExpressionVisitor<? extends T>)visitor).visitAttributeName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttributeNameContext attributeName() throws RecognitionException {
		AttributeNameContext _localctx = new AttributeNameContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_attributeName);
		try {
			setState(142);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SCTID:
				enterOuterAlt(_localctx, 1);
				{
				setState(140);
				conceptReference();
				}
				break;
			case ANY:
				enterOuterAlt(_localctx, 2);
				{
				setState(141);
				match(ANY);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AttributeValueContext extends ParserRuleContext {
		public SimpleExpressionContext simpleExpression() {
			return getRuleContext(SimpleExpressionContext.class,0);
		}
		public CompoundAttributevalueContext compoundAttributevalue() {
			return getRuleContext(CompoundAttributevalueContext.class,0);
		}
		public ExclusionAttributevalueContext exclusionAttributevalue() {
			return getRuleContext(ExclusionAttributevalueContext.class,0);
		}
		public StringOperatorContext stringOperator() {
			return getRuleContext(StringOperatorContext.class,0);
		}
		public TerminalNode STRING() { return getToken(ECL1ExpressionParser.STRING, 0); }
		public NumberOperatorContext numberOperator() {
			return getRuleContext(NumberOperatorContext.class,0);
		}
		public TerminalNode NUMBER() { return getToken(ECL1ExpressionParser.NUMBER, 0); }
		public AttributeValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attributeValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).enterAttributeValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).exitAttributeValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECL1ExpressionVisitor ) return ((ECL1ExpressionVisitor<? extends T>)visitor).visitAttributeValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttributeValueContext attributeValue() throws RecognitionException {
		AttributeValueContext _localctx = new AttributeValueContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_attributeValue);
		try {
			setState(153);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(144);
				simpleExpression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(145);
				compoundAttributevalue();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(146);
				exclusionAttributevalue();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				{
				setState(147);
				stringOperator();
				setState(148);
				match(STRING);
				}
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				{
				setState(150);
				numberOperator();
				setState(151);
				match(NUMBER);
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CompoundAttributevalueContext extends ParserRuleContext {
		public TerminalNode LPARAN() { return getToken(ECL1ExpressionParser.LPARAN, 0); }
		public TerminalNode RPARAN() { return getToken(ECL1ExpressionParser.RPARAN, 0); }
		public ConjunctionExpressionContext conjunctionExpression() {
			return getRuleContext(ConjunctionExpressionContext.class,0);
		}
		public DisjunctionExpressionContext disjunctionExpression() {
			return getRuleContext(DisjunctionExpressionContext.class,0);
		}
		public CompoundAttributevalueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_compoundAttributevalue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).enterCompoundAttributevalue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).exitCompoundAttributevalue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECL1ExpressionVisitor ) return ((ECL1ExpressionVisitor<? extends T>)visitor).visitCompoundAttributevalue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CompoundAttributevalueContext compoundAttributevalue() throws RecognitionException {
		CompoundAttributevalueContext _localctx = new CompoundAttributevalueContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_compoundAttributevalue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(155);
			match(LPARAN);
			setState(158);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				{
				setState(156);
				conjunctionExpression();
				}
				break;
			case 2:
				{
				setState(157);
				disjunctionExpression();
				}
				break;
			}
			setState(160);
			match(RPARAN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExclusionAttributevalueContext extends ParserRuleContext {
		public TerminalNode LPARAN() { return getToken(ECL1ExpressionParser.LPARAN, 0); }
		public CompoundAttributevalueContext compoundAttributevalue() {
			return getRuleContext(CompoundAttributevalueContext.class,0);
		}
		public TerminalNode RPARAN() { return getToken(ECL1ExpressionParser.RPARAN, 0); }
		public List<TerminalNode> EXCLUSION() { return getTokens(ECL1ExpressionParser.EXCLUSION); }
		public TerminalNode EXCLUSION(int i) {
			return getToken(ECL1ExpressionParser.EXCLUSION, i);
		}
		public List<SimpleExpressionContext> simpleExpression() {
			return getRuleContexts(SimpleExpressionContext.class);
		}
		public SimpleExpressionContext simpleExpression(int i) {
			return getRuleContext(SimpleExpressionContext.class,i);
		}
		public ExclusionAttributevalueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exclusionAttributevalue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).enterExclusionAttributevalue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).exitExclusionAttributevalue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECL1ExpressionVisitor ) return ((ECL1ExpressionVisitor<? extends T>)visitor).visitExclusionAttributevalue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExclusionAttributevalueContext exclusionAttributevalue() throws RecognitionException {
		ExclusionAttributevalueContext _localctx = new ExclusionAttributevalueContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_exclusionAttributevalue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(162);
			match(LPARAN);
			setState(163);
			compoundAttributevalue();
			setState(168);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==EXCLUSION) {
				{
				{
				setState(164);
				match(EXCLUSION);
				setState(165);
				simpleExpression();
				}
				}
				setState(170);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(171);
			match(RPARAN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StringOperatorContext extends ParserRuleContext {
		public TerminalNode EQUAL() { return getToken(ECL1ExpressionParser.EQUAL, 0); }
		public TerminalNode NOTEQUAL() { return getToken(ECL1ExpressionParser.NOTEQUAL, 0); }
		public StringOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stringOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).enterStringOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).exitStringOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECL1ExpressionVisitor ) return ((ECL1ExpressionVisitor<? extends T>)visitor).visitStringOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StringOperatorContext stringOperator() throws RecognitionException {
		StringOperatorContext _localctx = new StringOperatorContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_stringOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(173);
			_la = _input.LA(1);
			if ( !(_la==EQUAL || _la==NOTEQUAL) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NumberOperatorContext extends ParserRuleContext {
		public TerminalNode EQUAL() { return getToken(ECL1ExpressionParser.EQUAL, 0); }
		public TerminalNode NOTEQUAL() { return getToken(ECL1ExpressionParser.NOTEQUAL, 0); }
		public TerminalNode LESSOREQUAL() { return getToken(ECL1ExpressionParser.LESSOREQUAL, 0); }
		public TerminalNode GREATEROREQUAL() { return getToken(ECL1ExpressionParser.GREATEROREQUAL, 0); }
		public TerminalNode DESCENDANTOF() { return getToken(ECL1ExpressionParser.DESCENDANTOF, 0); }
		public TerminalNode ANCESTOF() { return getToken(ECL1ExpressionParser.ANCESTOF, 0); }
		public NumberOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numberOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).enterNumberOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).exitNumberOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECL1ExpressionVisitor ) return ((ECL1ExpressionVisitor<? extends T>)visitor).visitNumberOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumberOperatorContext numberOperator() throws RecognitionException {
		NumberOperatorContext _localctx = new NumberOperatorContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_numberOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(175);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 3932178L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionOperatorContext extends ParserRuleContext {
		public TerminalNode EQUAL() { return getToken(ECL1ExpressionParser.EQUAL, 0); }
		public TerminalNode NOTEQUAL() { return getToken(ECL1ExpressionParser.NOTEQUAL, 0); }
		public ExpressionOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expressionOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).enterExpressionOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECL1ExpressionListener ) ((ECL1ExpressionListener)listener).exitExpressionOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECL1ExpressionVisitor ) return ((ECL1ExpressionVisitor<? extends T>)visitor).visitExpressionOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionOperatorContext expressionOperator() throws RecognitionException {
		ExpressionOperatorContext _localctx = new ExpressionOperatorContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_expressionOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(177);
			_la = _input.LA(1);
			if ( !(_la==EQUAL || _la==NOTEQUAL) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u001b\u00b4\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
		"\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004"+
		"\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007"+
		"\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b"+
		"\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007"+
		"\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007"+
		"\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0001\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0003\u00000\b\u0000\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0005\u00029\b\u0002\n\u0002\f\u0002<\t\u0002\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0005\u0003A\b\u0003\n\u0003\f\u0003D\t\u0003\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0005\u0004I\b\u0004\n\u0004\f\u0004L\t"+
		"\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0003\u0005R\b"+
		"\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0003\u0006X\b"+
		"\u0006\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0003\b_\b\b\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0005\te\b\t\n\t\f\th\t\t\u0001\t\u0001\t\u0001"+
		"\t\u0005\tm\b\t\n\t\f\tp\t\t\u0003\tr\b\t\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\u000b\u0001\u000b\u0001\u000b\u0005\u000b{\b\u000b\n\u000b\f"+
		"\u000b~\t\u000b\u0001\f\u0001\f\u0001\r\u0001\r\u0001\r\u0001\r\u0001"+
		"\r\u0001\r\u0001\r\u0001\r\u0001\r\u0003\r\u008b\b\r\u0001\u000e\u0001"+
		"\u000e\u0003\u000e\u008f\b\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0003"+
		"\u000f\u009a\b\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0003\u0010\u009f"+
		"\b\u0010\u0001\u0010\u0001\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0001"+
		"\u0011\u0005\u0011\u00a7\b\u0011\n\u0011\f\u0011\u00aa\t\u0011\u0001\u0011"+
		"\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0001\u0014"+
		"\u0001\u0014\u0001\u0014\u0000\u0000\u0015\u0000\u0002\u0004\u0006\b\n"+
		"\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(\u0000\u0004"+
		"\u0001\u0000\u0001\u0006\u0001\u0000\u0001\u0002\u0001\u0000\u0012\u0013"+
		"\u0003\u0000\u0001\u0001\u0004\u0004\u0012\u0015\u00b6\u0000/\u0001\u0000"+
		"\u0000\u0000\u00021\u0001\u0000\u0000\u0000\u00045\u0001\u0000\u0000\u0000"+
		"\u0006=\u0001\u0000\u0000\u0000\bE\u0001\u0000\u0000\u0000\nQ\u0001\u0000"+
		"\u0000\u0000\fW\u0001\u0000\u0000\u0000\u000eY\u0001\u0000\u0000\u0000"+
		"\u0010^\u0001\u0000\u0000\u0000\u0012q\u0001\u0000\u0000\u0000\u0014s"+
		"\u0001\u0000\u0000\u0000\u0016w\u0001\u0000\u0000\u0000\u0018\u007f\u0001"+
		"\u0000\u0000\u0000\u001a\u008a\u0001\u0000\u0000\u0000\u001c\u008e\u0001"+
		"\u0000\u0000\u0000\u001e\u0099\u0001\u0000\u0000\u0000 \u009b\u0001\u0000"+
		"\u0000\u0000\"\u00a2\u0001\u0000\u0000\u0000$\u00ad\u0001\u0000\u0000"+
		"\u0000&\u00af\u0001\u0000\u0000\u0000(\u00b1\u0001\u0000\u0000\u0000*"+
		"0\u0003\n\u0005\u0000+0\u0003\u0004\u0002\u0000,0\u0003\u0006\u0003\u0000"+
		"-0\u0003\b\u0004\u0000.0\u0003\u0002\u0001\u0000/*\u0001\u0000\u0000\u0000"+
		"/+\u0001\u0000\u0000\u0000/,\u0001\u0000\u0000\u0000/-\u0001\u0000\u0000"+
		"\u0000/.\u0001\u0000\u0000\u00000\u0001\u0001\u0000\u0000\u000012\u0003"+
		"\n\u0005\u000023\u0005\b\u0000\u000034\u0003\u0012\t\u00004\u0003\u0001"+
		"\u0000\u0000\u00005:\u0003\n\u0005\u000067\u0005\u000f\u0000\u000079\u0003"+
		"\n\u0005\u000086\u0001\u0000\u0000\u00009<\u0001\u0000\u0000\u0000:8\u0001"+
		"\u0000\u0000\u0000:;\u0001\u0000\u0000\u0000;\u0005\u0001\u0000\u0000"+
		"\u0000<:\u0001\u0000\u0000\u0000=B\u0003\n\u0005\u0000>?\u0005\u0010\u0000"+
		"\u0000?A\u0003\n\u0005\u0000@>\u0001\u0000\u0000\u0000AD\u0001\u0000\u0000"+
		"\u0000B@\u0001\u0000\u0000\u0000BC\u0001\u0000\u0000\u0000C\u0007\u0001"+
		"\u0000\u0000\u0000DB\u0001\u0000\u0000\u0000EJ\u0003\n\u0005\u0000FG\u0005"+
		"\u0011\u0000\u0000GI\u0003\n\u0005\u0000HF\u0001\u0000\u0000\u0000IL\u0001"+
		"\u0000\u0000\u0000JH\u0001\u0000\u0000\u0000JK\u0001\u0000\u0000\u0000"+
		"K\t\u0001\u0000\u0000\u0000LJ\u0001\u0000\u0000\u0000MR\u0003\f\u0006"+
		"\u0000NO\u0003\u000e\u0007\u0000OP\u0003\f\u0006\u0000PR\u0001\u0000\u0000"+
		"\u0000QM\u0001\u0000\u0000\u0000QN\u0001\u0000\u0000\u0000R\u000b\u0001"+
		"\u0000\u0000\u0000ST\u0005\u0007\u0000\u0000TX\u0003\u0010\b\u0000UX\u0003"+
		"\u0010\b\u0000VX\u0005\u0016\u0000\u0000WS\u0001\u0000\u0000\u0000WU\u0001"+
		"\u0000\u0000\u0000WV\u0001\u0000\u0000\u0000X\r\u0001\u0000\u0000\u0000"+
		"YZ\u0007\u0000\u0000\u0000Z\u000f\u0001\u0000\u0000\u0000[_\u0005\u0018"+
		"\u0000\u0000\\]\u0005\u0018\u0000\u0000]_\u0005\u0017\u0000\u0000^[\u0001"+
		"\u0000\u0000\u0000^\\\u0001\u0000\u0000\u0000_\u0011\u0001\u0000\u0000"+
		"\u0000`r\u0003\u0016\u000b\u0000af\u0003\u0016\u000b\u0000bc\u0005\n\u0000"+
		"\u0000ce\u0003\u0014\n\u0000db\u0001\u0000\u0000\u0000eh\u0001\u0000\u0000"+
		"\u0000fd\u0001\u0000\u0000\u0000fg\u0001\u0000\u0000\u0000gr\u0001\u0000"+
		"\u0000\u0000hf\u0001\u0000\u0000\u0000in\u0003\u0014\n\u0000jk\u0005\n"+
		"\u0000\u0000km\u0003\u0014\n\u0000lj\u0001\u0000\u0000\u0000mp\u0001\u0000"+
		"\u0000\u0000nl\u0001\u0000\u0000\u0000no\u0001\u0000\u0000\u0000or\u0001"+
		"\u0000\u0000\u0000pn\u0001\u0000\u0000\u0000q`\u0001\u0000\u0000\u0000"+
		"qa\u0001\u0000\u0000\u0000qi\u0001\u0000\u0000\u0000r\u0013\u0001\u0000"+
		"\u0000\u0000st\u0005\r\u0000\u0000tu\u0003\u0016\u000b\u0000uv\u0005\u000e"+
		"\u0000\u0000v\u0015\u0001\u0000\u0000\u0000w|\u0003\u001a\r\u0000xy\u0005"+
		"\n\u0000\u0000y{\u0003\u001a\r\u0000zx\u0001\u0000\u0000\u0000{~\u0001"+
		"\u0000\u0000\u0000|z\u0001\u0000\u0000\u0000|}\u0001\u0000\u0000\u0000"+
		"}\u0017\u0001\u0000\u0000\u0000~|\u0001\u0000\u0000\u0000\u007f\u0080"+
		"\u0007\u0001\u0000\u0000\u0080\u0019\u0001\u0000\u0000\u0000\u0081\u0082"+
		"\u0003\u001c\u000e\u0000\u0082\u0083\u0005\u0012\u0000\u0000\u0083\u0084"+
		"\u0003\u001e\u000f\u0000\u0084\u008b\u0001\u0000\u0000\u0000\u0085\u0086"+
		"\u0003\u0018\f\u0000\u0086\u0087\u0003\u001c\u000e\u0000\u0087\u0088\u0005"+
		"\u0012\u0000\u0000\u0088\u0089\u0003\u001e\u000f\u0000\u0089\u008b\u0001"+
		"\u0000\u0000\u0000\u008a\u0081\u0001\u0000\u0000\u0000\u008a\u0085\u0001"+
		"\u0000\u0000\u0000\u008b\u001b\u0001\u0000\u0000\u0000\u008c\u008f\u0003"+
		"\u0010\b\u0000\u008d\u008f\u0005\u0016\u0000\u0000\u008e\u008c\u0001\u0000"+
		"\u0000\u0000\u008e\u008d\u0001\u0000\u0000\u0000\u008f\u001d\u0001\u0000"+
		"\u0000\u0000\u0090\u009a\u0003\n\u0005\u0000\u0091\u009a\u0003 \u0010"+
		"\u0000\u0092\u009a\u0003\"\u0011\u0000\u0093\u0094\u0003$\u0012\u0000"+
		"\u0094\u0095\u0005\u001a\u0000\u0000\u0095\u009a\u0001\u0000\u0000\u0000"+
		"\u0096\u0097\u0003&\u0013\u0000\u0097\u0098\u0005\u0019\u0000\u0000\u0098"+
		"\u009a\u0001\u0000\u0000\u0000\u0099\u0090\u0001\u0000\u0000\u0000\u0099"+
		"\u0091\u0001\u0000\u0000\u0000\u0099\u0092\u0001\u0000\u0000\u0000\u0099"+
		"\u0093\u0001\u0000\u0000\u0000\u0099\u0096\u0001\u0000\u0000\u0000\u009a"+
		"\u001f\u0001\u0000\u0000\u0000\u009b\u009e\u0005\u000b\u0000\u0000\u009c"+
		"\u009f\u0003\u0004\u0002\u0000\u009d\u009f\u0003\u0006\u0003\u0000\u009e"+
		"\u009c\u0001\u0000\u0000\u0000\u009e\u009d\u0001\u0000\u0000\u0000\u009f"+
		"\u00a0\u0001\u0000\u0000\u0000\u00a0\u00a1\u0005\f\u0000\u0000\u00a1!"+
		"\u0001\u0000\u0000\u0000\u00a2\u00a3\u0005\u000b\u0000\u0000\u00a3\u00a8"+
		"\u0003 \u0010\u0000\u00a4\u00a5\u0005\u0011\u0000\u0000\u00a5\u00a7\u0003"+
		"\n\u0005\u0000\u00a6\u00a4\u0001\u0000\u0000\u0000\u00a7\u00aa\u0001\u0000"+
		"\u0000\u0000\u00a8\u00a6\u0001\u0000\u0000\u0000\u00a8\u00a9\u0001\u0000"+
		"\u0000\u0000\u00a9\u00ab\u0001\u0000\u0000\u0000\u00aa\u00a8\u0001\u0000"+
		"\u0000\u0000\u00ab\u00ac\u0005\f\u0000\u0000\u00ac#\u0001\u0000\u0000"+
		"\u0000\u00ad\u00ae\u0007\u0002\u0000\u0000\u00ae%\u0001\u0000\u0000\u0000"+
		"\u00af\u00b0\u0007\u0003\u0000\u0000\u00b0\'\u0001\u0000\u0000\u0000\u00b1"+
		"\u00b2\u0007\u0002\u0000\u0000\u00b2)\u0001\u0000\u0000\u0000\u0010/:"+
		"BJQW^fnq|\u008a\u008e\u0099\u009e\u00a8";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}