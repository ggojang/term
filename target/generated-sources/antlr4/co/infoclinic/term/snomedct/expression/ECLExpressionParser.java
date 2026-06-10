// Generated from co/infoclinic/term/snomedct/expression/ECLExpression.g4 by ANTLR 4.13.1
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
public class ECLExpressionParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		DESCENDANTOF=1, DESCENDANTORSELFOF=2, CHILDOF=3, ANCESTOF=4, ANCESTORSELFOF=5, 
		PARENTOF=6, MEMBEROF=7, COLON=8, PLUS=9, COMMA=10, LPARAN=11, RPARAN=12, 
		LCBRACKET=13, RCBRACKET=14, CONJUNCTION=15, DISJUNCTION=16, EXCLUSION=17, 
		EQUAL=18, ANY=19, TERM=20, SCTID=21, NUMBER=22, STRING=23, WS=24;
	public static final int
		RULE_statments = 0, RULE_statement = 1, RULE_expression = 2, RULE_subExpression = 3, 
		RULE_nestedExpression = 4, RULE_focusConcept = 5, RULE_conceptReference = 6, 
		RULE_constraintOperator = 7, RULE_refinementOnly = 8, RULE_refinement = 9, 
		RULE_attributeOperator = 10, RULE_attributeGroup = 11, RULE_attributeNonGroup = 12, 
		RULE_attributeSet = 13, RULE_attribute = 14, RULE_attributeValue = 15;
	private static String[] makeRuleNames() {
		return new String[] {
			"statments", "statement", "expression", "subExpression", "nestedExpression", 
			"focusConcept", "conceptReference", "constraintOperator", "refinementOnly", 
			"refinement", "attributeOperator", "attributeGroup", "attributeNonGroup", 
			"attributeSet", "attribute", "attributeValue"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'<'", "'<<'", "'<!'", "'>'", "'>>'", "'>!'", "'^'", "':'", "'+'", 
			"','", "'('", "')'", "'{'", "'}'", "'AND'", "'OR'", "'MINUS'", "'='", 
			"'*'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "DESCENDANTOF", "DESCENDANTORSELFOF", "CHILDOF", "ANCESTOF", "ANCESTORSELFOF", 
			"PARENTOF", "MEMBEROF", "COLON", "PLUS", "COMMA", "LPARAN", "RPARAN", 
			"LCBRACKET", "RCBRACKET", "CONJUNCTION", "DISJUNCTION", "EXCLUSION", 
			"EQUAL", "ANY", "TERM", "SCTID", "NUMBER", "STRING", "WS"
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
	public String getGrammarFileName() { return "ECLExpression.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public ECLExpressionParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StatmentsContext extends ParserRuleContext {
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public StatmentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statments; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).enterStatments(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).exitStatments(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECLExpressionVisitor ) return ((ECLExpressionVisitor<? extends T>)visitor).visitStatments(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatmentsContext statments() throws RecognitionException {
		StatmentsContext _localctx = new StatmentsContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_statments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(32);
			statement();
			setState(36);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==LPARAN) {
				{
				{
				setState(33);
				statement();
				}
				}
				setState(38);
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
		public List<TerminalNode> LPARAN() { return getTokens(ECLExpressionParser.LPARAN); }
		public TerminalNode LPARAN(int i) {
			return getToken(ECLExpressionParser.LPARAN, i);
		}
		public List<SubExpressionContext> subExpression() {
			return getRuleContexts(SubExpressionContext.class);
		}
		public SubExpressionContext subExpression(int i) {
			return getRuleContext(SubExpressionContext.class,i);
		}
		public List<TerminalNode> RPARAN() { return getTokens(ECLExpressionParser.RPARAN); }
		public TerminalNode RPARAN(int i) {
			return getToken(ECLExpressionParser.RPARAN, i);
		}
		public ConstraintOperatorContext constraintOperator() {
			return getRuleContext(ConstraintOperatorContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).exitStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECLExpressionVisitor ) return ((ECLExpressionVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(39);
			match(LPARAN);
			setState(40);
			subExpression();
			setState(41);
			match(RPARAN);
			setState(42);
			constraintOperator();
			setState(43);
			match(LPARAN);
			setState(44);
			subExpression();
			setState(45);
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
		public ConstraintOperatorContext constraintOperator() {
			return getRuleContext(ConstraintOperatorContext.class,0);
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
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECLExpressionVisitor ) return ((ECLExpressionVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_expression);
		try {
			setState(51);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DESCENDANTOF:
			case DESCENDANTORSELFOF:
			case CHILDOF:
			case ANCESTOF:
			case ANCESTORSELFOF:
			case PARENTOF:
				enterOuterAlt(_localctx, 1);
				{
				setState(47);
				constraintOperator();
				setState(48);
				subExpression();
				}
				break;
			case SCTID:
				enterOuterAlt(_localctx, 2);
				{
				setState(50);
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
	public static class SubExpressionContext extends ParserRuleContext {
		public FocusConceptContext focusConcept() {
			return getRuleContext(FocusConceptContext.class,0);
		}
		public TerminalNode COLON() { return getToken(ECLExpressionParser.COLON, 0); }
		public RefinementContext refinement() {
			return getRuleContext(RefinementContext.class,0);
		}
		public SubExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).enterSubExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).exitSubExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECLExpressionVisitor ) return ((ECLExpressionVisitor<? extends T>)visitor).visitSubExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubExpressionContext subExpression() throws RecognitionException {
		SubExpressionContext _localctx = new SubExpressionContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_subExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(53);
			focusConcept();
			setState(56);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(54);
				match(COLON);
				setState(55);
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
	public static class NestedExpressionContext extends ParserRuleContext {
		public TerminalNode LPARAN() { return getToken(ECLExpressionParser.LPARAN, 0); }
		public SubExpressionContext subExpression() {
			return getRuleContext(SubExpressionContext.class,0);
		}
		public TerminalNode RPARAN() { return getToken(ECLExpressionParser.RPARAN, 0); }
		public NestedExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nestedExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).enterNestedExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).exitNestedExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECLExpressionVisitor ) return ((ECLExpressionVisitor<? extends T>)visitor).visitNestedExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NestedExpressionContext nestedExpression() throws RecognitionException {
		NestedExpressionContext _localctx = new NestedExpressionContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_nestedExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(58);
			match(LPARAN);
			setState(59);
			subExpression();
			setState(60);
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
	public static class FocusConceptContext extends ParserRuleContext {
		public List<ConceptReferenceContext> conceptReference() {
			return getRuleContexts(ConceptReferenceContext.class);
		}
		public ConceptReferenceContext conceptReference(int i) {
			return getRuleContext(ConceptReferenceContext.class,i);
		}
		public List<TerminalNode> PLUS() { return getTokens(ECLExpressionParser.PLUS); }
		public TerminalNode PLUS(int i) {
			return getToken(ECLExpressionParser.PLUS, i);
		}
		public FocusConceptContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_focusConcept; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).enterFocusConcept(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).exitFocusConcept(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECLExpressionVisitor ) return ((ECLExpressionVisitor<? extends T>)visitor).visitFocusConcept(this);
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
			setState(62);
			conceptReference();
			setState(67);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PLUS) {
				{
				{
				setState(63);
				match(PLUS);
				setState(64);
				conceptReference();
				}
				}
				setState(69);
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
		public TerminalNode SCTID() { return getToken(ECLExpressionParser.SCTID, 0); }
		public TerminalNode TERM() { return getToken(ECLExpressionParser.TERM, 0); }
		public ConceptReferenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conceptReference; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).enterConceptReference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).exitConceptReference(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECLExpressionVisitor ) return ((ECLExpressionVisitor<? extends T>)visitor).visitConceptReference(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConceptReferenceContext conceptReference() throws RecognitionException {
		ConceptReferenceContext _localctx = new ConceptReferenceContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_conceptReference);
		try {
			setState(73);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(70);
				match(SCTID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(71);
				match(SCTID);
				setState(72);
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
	public static class ConstraintOperatorContext extends ParserRuleContext {
		public TerminalNode DESCENDANTOF() { return getToken(ECLExpressionParser.DESCENDANTOF, 0); }
		public TerminalNode DESCENDANTORSELFOF() { return getToken(ECLExpressionParser.DESCENDANTORSELFOF, 0); }
		public TerminalNode CHILDOF() { return getToken(ECLExpressionParser.CHILDOF, 0); }
		public TerminalNode ANCESTOF() { return getToken(ECLExpressionParser.ANCESTOF, 0); }
		public TerminalNode ANCESTORSELFOF() { return getToken(ECLExpressionParser.ANCESTORSELFOF, 0); }
		public TerminalNode PARENTOF() { return getToken(ECLExpressionParser.PARENTOF, 0); }
		public ConstraintOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constraintOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).enterConstraintOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).exitConstraintOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECLExpressionVisitor ) return ((ECLExpressionVisitor<? extends T>)visitor).visitConstraintOperator(this);
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
			setState(75);
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
	public static class RefinementOnlyContext extends ParserRuleContext {
		public TerminalNode ANY() { return getToken(ECLExpressionParser.ANY, 0); }
		public TerminalNode COLON() { return getToken(ECLExpressionParser.COLON, 0); }
		public RefinementContext refinement() {
			return getRuleContext(RefinementContext.class,0);
		}
		public RefinementOnlyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_refinementOnly; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).enterRefinementOnly(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).exitRefinementOnly(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECLExpressionVisitor ) return ((ECLExpressionVisitor<? extends T>)visitor).visitRefinementOnly(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RefinementOnlyContext refinementOnly() throws RecognitionException {
		RefinementOnlyContext _localctx = new RefinementOnlyContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_refinementOnly);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(77);
			match(ANY);
			setState(80);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(78);
				match(COLON);
				setState(79);
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
	public static class RefinementContext extends ParserRuleContext {
		public AttributeNonGroupContext attributeNonGroup() {
			return getRuleContext(AttributeNonGroupContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(ECLExpressionParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ECLExpressionParser.COMMA, i);
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
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).enterRefinement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).exitRefinement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECLExpressionVisitor ) return ((ECLExpressionVisitor<? extends T>)visitor).visitRefinement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RefinementContext refinement() throws RecognitionException {
		RefinementContext _localctx = new RefinementContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_refinement);
		int _la;
		try {
			setState(98);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SCTID:
				enterOuterAlt(_localctx, 1);
				{
				setState(82);
				attributeNonGroup();
				setState(87);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(83);
					match(COMMA);
					setState(84);
					attributeGroup();
					}
					}
					setState(89);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case LCBRACKET:
				enterOuterAlt(_localctx, 2);
				{
				setState(90);
				attributeGroup();
				setState(95);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(91);
					match(COMMA);
					setState(92);
					attributeGroup();
					}
					}
					setState(97);
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
	public static class AttributeOperatorContext extends ParserRuleContext {
		public TerminalNode DESCENDANTOF() { return getToken(ECLExpressionParser.DESCENDANTOF, 0); }
		public TerminalNode DESCENDANTORSELFOF() { return getToken(ECLExpressionParser.DESCENDANTORSELFOF, 0); }
		public AttributeOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attributeOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).enterAttributeOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).exitAttributeOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECLExpressionVisitor ) return ((ECLExpressionVisitor<? extends T>)visitor).visitAttributeOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttributeOperatorContext attributeOperator() throws RecognitionException {
		AttributeOperatorContext _localctx = new AttributeOperatorContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_attributeOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(100);
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
	public static class AttributeGroupContext extends ParserRuleContext {
		public TerminalNode LCBRACKET() { return getToken(ECLExpressionParser.LCBRACKET, 0); }
		public AttributeSetContext attributeSet() {
			return getRuleContext(AttributeSetContext.class,0);
		}
		public TerminalNode RCBRACKET() { return getToken(ECLExpressionParser.RCBRACKET, 0); }
		public AttributeGroupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attributeGroup; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).enterAttributeGroup(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).exitAttributeGroup(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECLExpressionVisitor ) return ((ECLExpressionVisitor<? extends T>)visitor).visitAttributeGroup(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttributeGroupContext attributeGroup() throws RecognitionException {
		AttributeGroupContext _localctx = new AttributeGroupContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_attributeGroup);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(102);
			match(LCBRACKET);
			setState(103);
			attributeSet();
			setState(104);
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
	public static class AttributeNonGroupContext extends ParserRuleContext {
		public List<AttributeContext> attribute() {
			return getRuleContexts(AttributeContext.class);
		}
		public AttributeContext attribute(int i) {
			return getRuleContext(AttributeContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ECLExpressionParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ECLExpressionParser.COMMA, i);
		}
		public AttributeNonGroupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attributeNonGroup; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).enterAttributeNonGroup(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).exitAttributeNonGroup(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECLExpressionVisitor ) return ((ECLExpressionVisitor<? extends T>)visitor).visitAttributeNonGroup(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttributeNonGroupContext attributeNonGroup() throws RecognitionException {
		AttributeNonGroupContext _localctx = new AttributeNonGroupContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_attributeNonGroup);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(106);
			attribute();
			setState(111);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,9,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(107);
					match(COMMA);
					setState(108);
					attribute();
					}
					} 
				}
				setState(113);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,9,_ctx);
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
		public List<TerminalNode> COMMA() { return getTokens(ECLExpressionParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ECLExpressionParser.COMMA, i);
		}
		public AttributeSetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attributeSet; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).enterAttributeSet(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).exitAttributeSet(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECLExpressionVisitor ) return ((ECLExpressionVisitor<? extends T>)visitor).visitAttributeSet(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttributeSetContext attributeSet() throws RecognitionException {
		AttributeSetContext _localctx = new AttributeSetContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_attributeSet);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(114);
			attribute();
			setState(119);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(115);
				match(COMMA);
				setState(116);
				attribute();
				}
				}
				setState(121);
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
		public TerminalNode EQUAL() { return getToken(ECLExpressionParser.EQUAL, 0); }
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
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).enterAttribute(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).exitAttribute(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECLExpressionVisitor ) return ((ECLExpressionVisitor<? extends T>)visitor).visitAttribute(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttributeContext attribute() throws RecognitionException {
		AttributeContext _localctx = new AttributeContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_attribute);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(122);
			((AttributeContext)_localctx).attributeType = conceptReference();
			setState(123);
			match(EQUAL);
			setState(124);
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
		public TerminalNode STRING() { return getToken(ECLExpressionParser.STRING, 0); }
		public TerminalNode NUMBER() { return getToken(ECLExpressionParser.NUMBER, 0); }
		public AttributeValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attributeValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).enterAttributeValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLExpressionListener ) ((ECLExpressionListener)listener).exitAttributeValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ECLExpressionVisitor ) return ((ECLExpressionVisitor<? extends T>)visitor).visitAttributeValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttributeValueContext attributeValue() throws RecognitionException {
		AttributeValueContext _localctx = new AttributeValueContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_attributeValue);
		try {
			setState(130);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SCTID:
				enterOuterAlt(_localctx, 1);
				{
				setState(126);
				conceptReference();
				}
				break;
			case LPARAN:
				enterOuterAlt(_localctx, 2);
				{
				setState(127);
				nestedExpression();
				}
				break;
			case STRING:
				enterOuterAlt(_localctx, 3);
				{
				setState(128);
				match(STRING);
				}
				break;
			case NUMBER:
				enterOuterAlt(_localctx, 4);
				{
				setState(129);
				match(NUMBER);
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

	public static final String _serializedATN =
		"\u0004\u0001\u0018\u0085\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
		"\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004"+
		"\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007"+
		"\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b"+
		"\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007"+
		"\u000f\u0001\u0000\u0001\u0000\u0005\u0000#\b\u0000\n\u0000\f\u0000&\t"+
		"\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0003\u00024\b\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0003"+
		"\u00039\b\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0005\u0005B\b\u0005\n\u0005\f\u0005E\t"+
		"\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0003\u0006J\b\u0006\u0001"+
		"\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0003\bQ\b\b\u0001\t\u0001"+
		"\t\u0001\t\u0005\tV\b\t\n\t\f\tY\t\t\u0001\t\u0001\t\u0001\t\u0005\t^"+
		"\b\t\n\t\f\ta\t\t\u0003\tc\b\t\u0001\n\u0001\n\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0005\fn\b\f\n\f\f\f"+
		"q\t\f\u0001\r\u0001\r\u0001\r\u0005\rv\b\r\n\r\f\ry\t\r\u0001\u000e\u0001"+
		"\u000e\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0003\u000f\u0083\b\u000f\u0001\u000f\u0000\u0000\u0010\u0000\u0002"+
		"\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e"+
		"\u0000\u0002\u0001\u0000\u0001\u0006\u0001\u0000\u0001\u0002\u0082\u0000"+
		" \u0001\u0000\u0000\u0000\u0002\'\u0001\u0000\u0000\u0000\u00043\u0001"+
		"\u0000\u0000\u0000\u00065\u0001\u0000\u0000\u0000\b:\u0001\u0000\u0000"+
		"\u0000\n>\u0001\u0000\u0000\u0000\fI\u0001\u0000\u0000\u0000\u000eK\u0001"+
		"\u0000\u0000\u0000\u0010M\u0001\u0000\u0000\u0000\u0012b\u0001\u0000\u0000"+
		"\u0000\u0014d\u0001\u0000\u0000\u0000\u0016f\u0001\u0000\u0000\u0000\u0018"+
		"j\u0001\u0000\u0000\u0000\u001ar\u0001\u0000\u0000\u0000\u001cz\u0001"+
		"\u0000\u0000\u0000\u001e\u0082\u0001\u0000\u0000\u0000 $\u0003\u0002\u0001"+
		"\u0000!#\u0003\u0002\u0001\u0000\"!\u0001\u0000\u0000\u0000#&\u0001\u0000"+
		"\u0000\u0000$\"\u0001\u0000\u0000\u0000$%\u0001\u0000\u0000\u0000%\u0001"+
		"\u0001\u0000\u0000\u0000&$\u0001\u0000\u0000\u0000\'(\u0005\u000b\u0000"+
		"\u0000()\u0003\u0006\u0003\u0000)*\u0005\f\u0000\u0000*+\u0003\u000e\u0007"+
		"\u0000+,\u0005\u000b\u0000\u0000,-\u0003\u0006\u0003\u0000-.\u0005\f\u0000"+
		"\u0000.\u0003\u0001\u0000\u0000\u0000/0\u0003\u000e\u0007\u000001\u0003"+
		"\u0006\u0003\u000014\u0001\u0000\u0000\u000024\u0003\u0006\u0003\u0000"+
		"3/\u0001\u0000\u0000\u000032\u0001\u0000\u0000\u00004\u0005\u0001\u0000"+
		"\u0000\u000058\u0003\n\u0005\u000067\u0005\b\u0000\u000079\u0003\u0012"+
		"\t\u000086\u0001\u0000\u0000\u000089\u0001\u0000\u0000\u00009\u0007\u0001"+
		"\u0000\u0000\u0000:;\u0005\u000b\u0000\u0000;<\u0003\u0006\u0003\u0000"+
		"<=\u0005\f\u0000\u0000=\t\u0001\u0000\u0000\u0000>C\u0003\f\u0006\u0000"+
		"?@\u0005\t\u0000\u0000@B\u0003\f\u0006\u0000A?\u0001\u0000\u0000\u0000"+
		"BE\u0001\u0000\u0000\u0000CA\u0001\u0000\u0000\u0000CD\u0001\u0000\u0000"+
		"\u0000D\u000b\u0001\u0000\u0000\u0000EC\u0001\u0000\u0000\u0000FJ\u0005"+
		"\u0015\u0000\u0000GH\u0005\u0015\u0000\u0000HJ\u0005\u0014\u0000\u0000"+
		"IF\u0001\u0000\u0000\u0000IG\u0001\u0000\u0000\u0000J\r\u0001\u0000\u0000"+
		"\u0000KL\u0007\u0000\u0000\u0000L\u000f\u0001\u0000\u0000\u0000MP\u0005"+
		"\u0013\u0000\u0000NO\u0005\b\u0000\u0000OQ\u0003\u0012\t\u0000PN\u0001"+
		"\u0000\u0000\u0000PQ\u0001\u0000\u0000\u0000Q\u0011\u0001\u0000\u0000"+
		"\u0000RW\u0003\u0018\f\u0000ST\u0005\n\u0000\u0000TV\u0003\u0016\u000b"+
		"\u0000US\u0001\u0000\u0000\u0000VY\u0001\u0000\u0000\u0000WU\u0001\u0000"+
		"\u0000\u0000WX\u0001\u0000\u0000\u0000Xc\u0001\u0000\u0000\u0000YW\u0001"+
		"\u0000\u0000\u0000Z_\u0003\u0016\u000b\u0000[\\\u0005\n\u0000\u0000\\"+
		"^\u0003\u0016\u000b\u0000][\u0001\u0000\u0000\u0000^a\u0001\u0000\u0000"+
		"\u0000_]\u0001\u0000\u0000\u0000_`\u0001\u0000\u0000\u0000`c\u0001\u0000"+
		"\u0000\u0000a_\u0001\u0000\u0000\u0000bR\u0001\u0000\u0000\u0000bZ\u0001"+
		"\u0000\u0000\u0000c\u0013\u0001\u0000\u0000\u0000de\u0007\u0001\u0000"+
		"\u0000e\u0015\u0001\u0000\u0000\u0000fg\u0005\r\u0000\u0000gh\u0003\u001a"+
		"\r\u0000hi\u0005\u000e\u0000\u0000i\u0017\u0001\u0000\u0000\u0000jo\u0003"+
		"\u001c\u000e\u0000kl\u0005\n\u0000\u0000ln\u0003\u001c\u000e\u0000mk\u0001"+
		"\u0000\u0000\u0000nq\u0001\u0000\u0000\u0000om\u0001\u0000\u0000\u0000"+
		"op\u0001\u0000\u0000\u0000p\u0019\u0001\u0000\u0000\u0000qo\u0001\u0000"+
		"\u0000\u0000rw\u0003\u001c\u000e\u0000st\u0005\n\u0000\u0000tv\u0003\u001c"+
		"\u000e\u0000us\u0001\u0000\u0000\u0000vy\u0001\u0000\u0000\u0000wu\u0001"+
		"\u0000\u0000\u0000wx\u0001\u0000\u0000\u0000x\u001b\u0001\u0000\u0000"+
		"\u0000yw\u0001\u0000\u0000\u0000z{\u0003\f\u0006\u0000{|\u0005\u0012\u0000"+
		"\u0000|}\u0003\u001e\u000f\u0000}\u001d\u0001\u0000\u0000\u0000~\u0083"+
		"\u0003\f\u0006\u0000\u007f\u0083\u0003\b\u0004\u0000\u0080\u0083\u0005"+
		"\u0017\u0000\u0000\u0081\u0083\u0005\u0016\u0000\u0000\u0082~\u0001\u0000"+
		"\u0000\u0000\u0082\u007f\u0001\u0000\u0000\u0000\u0082\u0080\u0001\u0000"+
		"\u0000\u0000\u0082\u0081\u0001\u0000\u0000\u0000\u0083\u001f\u0001\u0000"+
		"\u0000\u0000\f$38CIPW_bow\u0082";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}