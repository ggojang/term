// Generated from co/infoclinic/term/snomedct/expression/CGExpression.g4 by ANTLR 4.13.1
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
public class CGExpressionParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		EQUIVALENT_TO=1, SUBTYPE_OF=2, LPARAN=3, RPARAN=4, COLON=5, PLUS=6, COMMA=7, 
		EQUAL=8, LCBRACKET=9, RCBRACKET=10, TERM=11, SCTID=12, NUMBER=13, STRING=14, 
		WS=15;
	public static final int
		RULE_statements = 0, RULE_statement = 1, RULE_expression = 2, RULE_definitionStatus = 3, 
		RULE_subExpression = 4, RULE_focusConcept = 5, RULE_conceptReference = 6, 
		RULE_refinement = 7, RULE_attributeGroup = 8, RULE_nonGroupedAttributeSet = 9, 
		RULE_attributeSet = 10, RULE_attribute = 11, RULE_attributeValue = 12, 
		RULE_nestedExpression = 13;
	private static String[] makeRuleNames() {
		return new String[] {
			"statements", "statement", "expression", "definitionStatus", "subExpression", 
			"focusConcept", "conceptReference", "refinement", "attributeGroup", "nonGroupedAttributeSet", 
			"attributeSet", "attribute", "attributeValue", "nestedExpression"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'==='", "'<<<'", "'('", "')'", "':'", "'+'", "','", "'='", "'{'", 
			"'}'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "EQUIVALENT_TO", "SUBTYPE_OF", "LPARAN", "RPARAN", "COLON", "PLUS", 
			"COMMA", "EQUAL", "LCBRACKET", "RCBRACKET", "TERM", "SCTID", "NUMBER", 
			"STRING", "WS"
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
	public String getGrammarFileName() { return "CGExpression.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public CGExpressionParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StatementsContext extends ParserRuleContext {
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public StatementsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statements; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CGExpressionListener ) ((CGExpressionListener)listener).enterStatements(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CGExpressionListener ) ((CGExpressionListener)listener).exitStatements(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CGExpressionVisitor ) return ((CGExpressionVisitor<? extends T>)visitor).visitStatements(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementsContext statements() throws RecognitionException {
		StatementsContext _localctx = new StatementsContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_statements);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(28);
			statement();
			setState(32);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==LPARAN) {
				{
				{
				setState(29);
				statement();
				}
				}
				setState(34);
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
	public static class StatementContext extends ParserRuleContext {
		public List<TerminalNode> LPARAN() { return getTokens(CGExpressionParser.LPARAN); }
		public TerminalNode LPARAN(int i) {
			return getToken(CGExpressionParser.LPARAN, i);
		}
		public List<SubExpressionContext> subExpression() {
			return getRuleContexts(SubExpressionContext.class);
		}
		public SubExpressionContext subExpression(int i) {
			return getRuleContext(SubExpressionContext.class,i);
		}
		public List<TerminalNode> RPARAN() { return getTokens(CGExpressionParser.RPARAN); }
		public TerminalNode RPARAN(int i) {
			return getToken(CGExpressionParser.RPARAN, i);
		}
		public DefinitionStatusContext definitionStatus() {
			return getRuleContext(DefinitionStatusContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CGExpressionListener ) ((CGExpressionListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CGExpressionListener ) ((CGExpressionListener)listener).exitStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CGExpressionVisitor ) return ((CGExpressionVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(35);
			match(LPARAN);
			setState(36);
			subExpression();
			setState(37);
			match(RPARAN);
			setState(38);
			definitionStatus();
			setState(39);
			match(LPARAN);
			setState(40);
			subExpression();
			setState(41);
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
	public static class ExpressionContext extends ParserRuleContext {
		public DefinitionStatusContext definitionStatus() {
			return getRuleContext(DefinitionStatusContext.class,0);
		}
		public SubExpressionContext subExpression() {
			return getRuleContext(SubExpressionContext.class,0);
		}
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CGExpressionListener ) ((CGExpressionListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CGExpressionListener ) ((CGExpressionListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CGExpressionVisitor ) return ((CGExpressionVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_expression);
		try {
			setState(47);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EQUIVALENT_TO:
			case SUBTYPE_OF:
				enterOuterAlt(_localctx, 1);
				{
				setState(43);
				definitionStatus();
				setState(44);
				subExpression();
				}
				break;
			case SCTID:
				enterOuterAlt(_localctx, 2);
				{
				setState(46);
				subExpression();
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
	public static class DefinitionStatusContext extends ParserRuleContext {
		public TerminalNode EQUIVALENT_TO() { return getToken(CGExpressionParser.EQUIVALENT_TO, 0); }
		public TerminalNode SUBTYPE_OF() { return getToken(CGExpressionParser.SUBTYPE_OF, 0); }
		public DefinitionStatusContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_definitionStatus; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CGExpressionListener ) ((CGExpressionListener)listener).enterDefinitionStatus(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CGExpressionListener ) ((CGExpressionListener)listener).exitDefinitionStatus(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CGExpressionVisitor ) return ((CGExpressionVisitor<? extends T>)visitor).visitDefinitionStatus(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DefinitionStatusContext definitionStatus() throws RecognitionException {
		DefinitionStatusContext _localctx = new DefinitionStatusContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_definitionStatus);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(49);
			_la = _input.LA(1);
			if ( !(_la==EQUIVALENT_TO || _la==SUBTYPE_OF) ) {
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
	public static class SubExpressionContext extends ParserRuleContext {
		public FocusConceptContext focusConcept() {
			return getRuleContext(FocusConceptContext.class,0);
		}
		public TerminalNode COLON() { return getToken(CGExpressionParser.COLON, 0); }
		public RefinementContext refinement() {
			return getRuleContext(RefinementContext.class,0);
		}
		public SubExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CGExpressionListener ) ((CGExpressionListener)listener).enterSubExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CGExpressionListener ) ((CGExpressionListener)listener).exitSubExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CGExpressionVisitor ) return ((CGExpressionVisitor<? extends T>)visitor).visitSubExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubExpressionContext subExpression() throws RecognitionException {
		SubExpressionContext _localctx = new SubExpressionContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_subExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(51);
			focusConcept();
			setState(54);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(52);
				match(COLON);
				setState(53);
				refinement();
				}
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
	public static class FocusConceptContext extends ParserRuleContext {
		public List<ConceptReferenceContext> conceptReference() {
			return getRuleContexts(ConceptReferenceContext.class);
		}
		public ConceptReferenceContext conceptReference(int i) {
			return getRuleContext(ConceptReferenceContext.class,i);
		}
		public List<TerminalNode> PLUS() { return getTokens(CGExpressionParser.PLUS); }
		public TerminalNode PLUS(int i) {
			return getToken(CGExpressionParser.PLUS, i);
		}
		public FocusConceptContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_focusConcept; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CGExpressionListener ) ((CGExpressionListener)listener).enterFocusConcept(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CGExpressionListener ) ((CGExpressionListener)listener).exitFocusConcept(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CGExpressionVisitor ) return ((CGExpressionVisitor<? extends T>)visitor).visitFocusConcept(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FocusConceptContext focusConcept() throws RecognitionException {
		FocusConceptContext _localctx = new FocusConceptContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_focusConcept);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(56);
			conceptReference();
			setState(61);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PLUS) {
				{
				{
				setState(57);
				match(PLUS);
				setState(58);
				conceptReference();
				}
				}
				setState(63);
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
	public static class ConceptReferenceContext extends ParserRuleContext {
		public TerminalNode SCTID() { return getToken(CGExpressionParser.SCTID, 0); }
		public TerminalNode TERM() { return getToken(CGExpressionParser.TERM, 0); }
		public ConceptReferenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conceptReference; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CGExpressionListener ) ((CGExpressionListener)listener).enterConceptReference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CGExpressionListener ) ((CGExpressionListener)listener).exitConceptReference(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CGExpressionVisitor ) return ((CGExpressionVisitor<? extends T>)visitor).visitConceptReference(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConceptReferenceContext conceptReference() throws RecognitionException {
		ConceptReferenceContext _localctx = new ConceptReferenceContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_conceptReference);
		try {
			setState(67);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(64);
				match(SCTID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(65);
				match(SCTID);
				setState(66);
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
		public NonGroupedAttributeSetContext nonGroupedAttributeSet() {
			return getRuleContext(NonGroupedAttributeSetContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(CGExpressionParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(CGExpressionParser.COMMA, i);
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
			if ( listener instanceof CGExpressionListener ) ((CGExpressionListener)listener).enterRefinement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CGExpressionListener ) ((CGExpressionListener)listener).exitRefinement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CGExpressionVisitor ) return ((CGExpressionVisitor<? extends T>)visitor).visitRefinement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RefinementContext refinement() throws RecognitionException {
		RefinementContext _localctx = new RefinementContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_refinement);
		int _la;
		try {
			setState(85);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SCTID:
				enterOuterAlt(_localctx, 1);
				{
				setState(69);
				nonGroupedAttributeSet();
				setState(74);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(70);
					match(COMMA);
					setState(71);
					attributeGroup();
					}
					}
					setState(76);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case LCBRACKET:
				enterOuterAlt(_localctx, 2);
				{
				setState(77);
				attributeGroup();
				setState(82);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(78);
					match(COMMA);
					setState(79);
					attributeGroup();
					}
					}
					setState(84);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
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
	public static class AttributeGroupContext extends ParserRuleContext {
		public TerminalNode LCBRACKET() { return getToken(CGExpressionParser.LCBRACKET, 0); }
		public AttributeSetContext attributeSet() {
			return getRuleContext(AttributeSetContext.class,0);
		}
		public TerminalNode RCBRACKET() { return getToken(CGExpressionParser.RCBRACKET, 0); }
		public AttributeGroupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attributeGroup; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CGExpressionListener ) ((CGExpressionListener)listener).enterAttributeGroup(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CGExpressionListener ) ((CGExpressionListener)listener).exitAttributeGroup(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CGExpressionVisitor ) return ((CGExpressionVisitor<? extends T>)visitor).visitAttributeGroup(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttributeGroupContext attributeGroup() throws RecognitionException {
		AttributeGroupContext _localctx = new AttributeGroupContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_attributeGroup);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(87);
			match(LCBRACKET);
			setState(88);
			attributeSet();
			setState(89);
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
	public static class NonGroupedAttributeSetContext extends ParserRuleContext {
		public List<AttributeContext> attribute() {
			return getRuleContexts(AttributeContext.class);
		}
		public AttributeContext attribute(int i) {
			return getRuleContext(AttributeContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(CGExpressionParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(CGExpressionParser.COMMA, i);
		}
		public NonGroupedAttributeSetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nonGroupedAttributeSet; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CGExpressionListener ) ((CGExpressionListener)listener).enterNonGroupedAttributeSet(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CGExpressionListener ) ((CGExpressionListener)listener).exitNonGroupedAttributeSet(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CGExpressionVisitor ) return ((CGExpressionVisitor<? extends T>)visitor).visitNonGroupedAttributeSet(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NonGroupedAttributeSetContext nonGroupedAttributeSet() throws RecognitionException {
		NonGroupedAttributeSetContext _localctx = new NonGroupedAttributeSetContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_nonGroupedAttributeSet);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(91);
			attribute();
			setState(96);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(92);
					match(COMMA);
					setState(93);
					attribute();
					}
					} 
				}
				setState(98);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
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
	public static class AttributeSetContext extends ParserRuleContext {
		public List<AttributeContext> attribute() {
			return getRuleContexts(AttributeContext.class);
		}
		public AttributeContext attribute(int i) {
			return getRuleContext(AttributeContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(CGExpressionParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(CGExpressionParser.COMMA, i);
		}
		public AttributeSetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attributeSet; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CGExpressionListener ) ((CGExpressionListener)listener).enterAttributeSet(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CGExpressionListener ) ((CGExpressionListener)listener).exitAttributeSet(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CGExpressionVisitor ) return ((CGExpressionVisitor<? extends T>)visitor).visitAttributeSet(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttributeSetContext attributeSet() throws RecognitionException {
		AttributeSetContext _localctx = new AttributeSetContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_attributeSet);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(99);
			attribute();
			setState(104);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(100);
				match(COMMA);
				setState(101);
				attribute();
				}
				}
				setState(106);
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
	public static class AttributeContext extends ParserRuleContext {
		public ConceptReferenceContext attributeType;
		public TerminalNode EQUAL() { return getToken(CGExpressionParser.EQUAL, 0); }
		public AttributeValueContext attributeValue() {
			return getRuleContext(AttributeValueContext.class,0);
		}
		public ConceptReferenceContext conceptReference() {
			return getRuleContext(ConceptReferenceContext.class,0);
		}
		public AttributeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attribute; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CGExpressionListener ) ((CGExpressionListener)listener).enterAttribute(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CGExpressionListener ) ((CGExpressionListener)listener).exitAttribute(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CGExpressionVisitor ) return ((CGExpressionVisitor<? extends T>)visitor).visitAttribute(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttributeContext attribute() throws RecognitionException {
		AttributeContext _localctx = new AttributeContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_attribute);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(107);
			((AttributeContext)_localctx).attributeType = conceptReference();
			setState(108);
			match(EQUAL);
			setState(109);
			attributeValue();
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
		public ConceptReferenceContext conceptReference() {
			return getRuleContext(ConceptReferenceContext.class,0);
		}
		public NestedExpressionContext nestedExpression() {
			return getRuleContext(NestedExpressionContext.class,0);
		}
		public TerminalNode NUMBER() { return getToken(CGExpressionParser.NUMBER, 0); }
		public TerminalNode STRING() { return getToken(CGExpressionParser.STRING, 0); }
		public AttributeValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attributeValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CGExpressionListener ) ((CGExpressionListener)listener).enterAttributeValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CGExpressionListener ) ((CGExpressionListener)listener).exitAttributeValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CGExpressionVisitor ) return ((CGExpressionVisitor<? extends T>)visitor).visitAttributeValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttributeValueContext attributeValue() throws RecognitionException {
		AttributeValueContext _localctx = new AttributeValueContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_attributeValue);
		try {
			setState(115);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SCTID:
				enterOuterAlt(_localctx, 1);
				{
				setState(111);
				conceptReference();
				}
				break;
			case LPARAN:
				enterOuterAlt(_localctx, 2);
				{
				setState(112);
				nestedExpression();
				}
				break;
			case NUMBER:
				enterOuterAlt(_localctx, 3);
				{
				setState(113);
				match(NUMBER);
				}
				break;
			case STRING:
				enterOuterAlt(_localctx, 4);
				{
				setState(114);
				match(STRING);
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
	public static class NestedExpressionContext extends ParserRuleContext {
		public TerminalNode LPARAN() { return getToken(CGExpressionParser.LPARAN, 0); }
		public SubExpressionContext subExpression() {
			return getRuleContext(SubExpressionContext.class,0);
		}
		public TerminalNode RPARAN() { return getToken(CGExpressionParser.RPARAN, 0); }
		public NestedExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nestedExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CGExpressionListener ) ((CGExpressionListener)listener).enterNestedExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CGExpressionListener ) ((CGExpressionListener)listener).exitNestedExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CGExpressionVisitor ) return ((CGExpressionVisitor<? extends T>)visitor).visitNestedExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NestedExpressionContext nestedExpression() throws RecognitionException {
		NestedExpressionContext _localctx = new NestedExpressionContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_nestedExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(117);
			match(LPARAN);
			setState(118);
			subExpression();
			setState(119);
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

	public static final String _serializedATN =
		"\u0004\u0001\u000fz\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0001\u0000\u0001\u0000\u0005\u0000\u001f\b"+
		"\u0000\n\u0000\f\u0000\"\t\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0003\u00020\b\u0002\u0001\u0003\u0001"+
		"\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u00047\b\u0004\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0005\u0005<\b\u0005\n\u0005\f\u0005?\t"+
		"\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0003\u0006D\b\u0006\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0005\u0007I\b\u0007\n\u0007\f\u0007L\t"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0005\u0007Q\b\u0007\n\u0007"+
		"\f\u0007T\t\u0007\u0003\u0007V\b\u0007\u0001\b\u0001\b\u0001\b\u0001\b"+
		"\u0001\t\u0001\t\u0001\t\u0005\t_\b\t\n\t\f\tb\t\t\u0001\n\u0001\n\u0001"+
		"\n\u0005\ng\b\n\n\n\f\nj\t\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0003\ft\b\f\u0001\r\u0001\r\u0001"+
		"\r\u0001\r\u0001\r\u0000\u0000\u000e\u0000\u0002\u0004\u0006\b\n\f\u000e"+
		"\u0010\u0012\u0014\u0016\u0018\u001a\u0000\u0001\u0001\u0000\u0001\u0002"+
		"x\u0000\u001c\u0001\u0000\u0000\u0000\u0002#\u0001\u0000\u0000\u0000\u0004"+
		"/\u0001\u0000\u0000\u0000\u00061\u0001\u0000\u0000\u0000\b3\u0001\u0000"+
		"\u0000\u0000\n8\u0001\u0000\u0000\u0000\fC\u0001\u0000\u0000\u0000\u000e"+
		"U\u0001\u0000\u0000\u0000\u0010W\u0001\u0000\u0000\u0000\u0012[\u0001"+
		"\u0000\u0000\u0000\u0014c\u0001\u0000\u0000\u0000\u0016k\u0001\u0000\u0000"+
		"\u0000\u0018s\u0001\u0000\u0000\u0000\u001au\u0001\u0000\u0000\u0000\u001c"+
		" \u0003\u0002\u0001\u0000\u001d\u001f\u0003\u0002\u0001\u0000\u001e\u001d"+
		"\u0001\u0000\u0000\u0000\u001f\"\u0001\u0000\u0000\u0000 \u001e\u0001"+
		"\u0000\u0000\u0000 !\u0001\u0000\u0000\u0000!\u0001\u0001\u0000\u0000"+
		"\u0000\" \u0001\u0000\u0000\u0000#$\u0005\u0003\u0000\u0000$%\u0003\b"+
		"\u0004\u0000%&\u0005\u0004\u0000\u0000&\'\u0003\u0006\u0003\u0000\'(\u0005"+
		"\u0003\u0000\u0000()\u0003\b\u0004\u0000)*\u0005\u0004\u0000\u0000*\u0003"+
		"\u0001\u0000\u0000\u0000+,\u0003\u0006\u0003\u0000,-\u0003\b\u0004\u0000"+
		"-0\u0001\u0000\u0000\u0000.0\u0003\b\u0004\u0000/+\u0001\u0000\u0000\u0000"+
		"/.\u0001\u0000\u0000\u00000\u0005\u0001\u0000\u0000\u000012\u0007\u0000"+
		"\u0000\u00002\u0007\u0001\u0000\u0000\u000036\u0003\n\u0005\u000045\u0005"+
		"\u0005\u0000\u000057\u0003\u000e\u0007\u000064\u0001\u0000\u0000\u0000"+
		"67\u0001\u0000\u0000\u00007\t\u0001\u0000\u0000\u00008=\u0003\f\u0006"+
		"\u00009:\u0005\u0006\u0000\u0000:<\u0003\f\u0006\u0000;9\u0001\u0000\u0000"+
		"\u0000<?\u0001\u0000\u0000\u0000=;\u0001\u0000\u0000\u0000=>\u0001\u0000"+
		"\u0000\u0000>\u000b\u0001\u0000\u0000\u0000?=\u0001\u0000\u0000\u0000"+
		"@D\u0005\f\u0000\u0000AB\u0005\f\u0000\u0000BD\u0005\u000b\u0000\u0000"+
		"C@\u0001\u0000\u0000\u0000CA\u0001\u0000\u0000\u0000D\r\u0001\u0000\u0000"+
		"\u0000EJ\u0003\u0012\t\u0000FG\u0005\u0007\u0000\u0000GI\u0003\u0010\b"+
		"\u0000HF\u0001\u0000\u0000\u0000IL\u0001\u0000\u0000\u0000JH\u0001\u0000"+
		"\u0000\u0000JK\u0001\u0000\u0000\u0000KV\u0001\u0000\u0000\u0000LJ\u0001"+
		"\u0000\u0000\u0000MR\u0003\u0010\b\u0000NO\u0005\u0007\u0000\u0000OQ\u0003"+
		"\u0010\b\u0000PN\u0001\u0000\u0000\u0000QT\u0001\u0000\u0000\u0000RP\u0001"+
		"\u0000\u0000\u0000RS\u0001\u0000\u0000\u0000SV\u0001\u0000\u0000\u0000"+
		"TR\u0001\u0000\u0000\u0000UE\u0001\u0000\u0000\u0000UM\u0001\u0000\u0000"+
		"\u0000V\u000f\u0001\u0000\u0000\u0000WX\u0005\t\u0000\u0000XY\u0003\u0014"+
		"\n\u0000YZ\u0005\n\u0000\u0000Z\u0011\u0001\u0000\u0000\u0000[`\u0003"+
		"\u0016\u000b\u0000\\]\u0005\u0007\u0000\u0000]_\u0003\u0016\u000b\u0000"+
		"^\\\u0001\u0000\u0000\u0000_b\u0001\u0000\u0000\u0000`^\u0001\u0000\u0000"+
		"\u0000`a\u0001\u0000\u0000\u0000a\u0013\u0001\u0000\u0000\u0000b`\u0001"+
		"\u0000\u0000\u0000ch\u0003\u0016\u000b\u0000de\u0005\u0007\u0000\u0000"+
		"eg\u0003\u0016\u000b\u0000fd\u0001\u0000\u0000\u0000gj\u0001\u0000\u0000"+
		"\u0000hf\u0001\u0000\u0000\u0000hi\u0001\u0000\u0000\u0000i\u0015\u0001"+
		"\u0000\u0000\u0000jh\u0001\u0000\u0000\u0000kl\u0003\f\u0006\u0000lm\u0005"+
		"\b\u0000\u0000mn\u0003\u0018\f\u0000n\u0017\u0001\u0000\u0000\u0000ot"+
		"\u0003\f\u0006\u0000pt\u0003\u001a\r\u0000qt\u0005\r\u0000\u0000rt\u0005"+
		"\u000e\u0000\u0000so\u0001\u0000\u0000\u0000sp\u0001\u0000\u0000\u0000"+
		"sq\u0001\u0000\u0000\u0000sr\u0001\u0000\u0000\u0000t\u0019\u0001\u0000"+
		"\u0000\u0000uv\u0005\u0003\u0000\u0000vw\u0003\b\u0004\u0000wx\u0005\u0004"+
		"\u0000\u0000x\u001b\u0001\u0000\u0000\u0000\u000b /6=CJRU`hs";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}