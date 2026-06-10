// Generated from co/infoclinic/term/snomedct/expression/CGExpression.g4 by ANTLR 4.13.1
package co.infoclinic.term.snomedct.expression;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class CGExpressionLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		EQUIVALENT_TO=1, SUBTYPE_OF=2, LPARAN=3, RPARAN=4, COLON=5, PLUS=6, COMMA=7, 
		EQUAL=8, LCBRACKET=9, RCBRACKET=10, TERM=11, SCTID=12, NUMBER=13, STRING=14, 
		WS=15;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"EQUIVALENT_TO", "SUBTYPE_OF", "LPARAN", "RPARAN", "COLON", "PLUS", "COMMA", 
			"EQUAL", "LCBRACKET", "RCBRACKET", "TERM", "SCTID", "NUMBER", "STRING", 
			"ESCAPE_CHAR", "NONWSNONPIPE", "DIGIT", "DIGITNONZERO", "WS"
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


	public CGExpressionLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "CGExpression.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\u0004\u0000\u000f\u0097\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002"+
		"\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002"+
		"\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002"+
		"\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002"+
		"\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e"+
		"\u0002\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011"+
		"\u0002\u0012\u0007\u0012\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002"+
		"\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005"+
		"\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001"+
		"\t\u0001\t\u0001\n\u0001\n\u0005\nB\b\n\n\n\f\nE\t\n\u0001\n\u0001\n\u0001"+
		"\n\u0005\nJ\b\n\n\n\f\nM\t\n\u0001\n\u0001\n\u0001\u000b\u0003\u000bR"+
		"\b\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0005\u000bW\b\u000b\n\u000b"+
		"\f\u000bZ\t\u000b\u0001\f\u0001\f\u0003\f^\b\f\u0001\f\u0001\f\u0005\f"+
		"b\b\f\n\f\f\fe\t\f\u0001\f\u0001\f\u0005\fi\b\f\n\f\f\fl\t\f\u0003\fn"+
		"\b\f\u0001\f\u0001\f\u0003\fr\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0004"+
		"\fx\b\f\u000b\f\f\fy\u0003\f|\b\f\u0001\r\u0001\r\u0001\r\u0005\r\u0081"+
		"\b\r\n\r\f\r\u0084\t\r\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001\u000e"+
		"\u0001\u000e\u0003\u000e\u008c\b\u000e\u0001\u000f\u0001\u000f\u0001\u0010"+
		"\u0001\u0010\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0001\u0082\u0000\u0013\u0001\u0001\u0003\u0002\u0005\u0003"+
		"\u0007\u0004\t\u0005\u000b\u0006\r\u0007\u000f\b\u0011\t\u0013\n\u0015"+
		"\u000b\u0017\f\u0019\r\u001b\u000e\u001d\u0000\u001f\u0000!\u0000#\u0000"+
		"%\u000f\u0001\u0000\u0003\u0002\u0000\"\"\\\\\u0004\u0000\t\n\f\r  ||"+
		"\u0003\u0000\t\n\f\r  \u00a2\u0000\u0001\u0001\u0000\u0000\u0000\u0000"+
		"\u0003\u0001\u0000\u0000\u0000\u0000\u0005\u0001\u0000\u0000\u0000\u0000"+
		"\u0007\u0001\u0000\u0000\u0000\u0000\t\u0001\u0000\u0000\u0000\u0000\u000b"+
		"\u0001\u0000\u0000\u0000\u0000\r\u0001\u0000\u0000\u0000\u0000\u000f\u0001"+
		"\u0000\u0000\u0000\u0000\u0011\u0001\u0000\u0000\u0000\u0000\u0013\u0001"+
		"\u0000\u0000\u0000\u0000\u0015\u0001\u0000\u0000\u0000\u0000\u0017\u0001"+
		"\u0000\u0000\u0000\u0000\u0019\u0001\u0000\u0000\u0000\u0000\u001b\u0001"+
		"\u0000\u0000\u0000\u0000%\u0001\u0000\u0000\u0000\u0001\'\u0001\u0000"+
		"\u0000\u0000\u0003+\u0001\u0000\u0000\u0000\u0005/\u0001\u0000\u0000\u0000"+
		"\u00071\u0001\u0000\u0000\u0000\t3\u0001\u0000\u0000\u0000\u000b5\u0001"+
		"\u0000\u0000\u0000\r7\u0001\u0000\u0000\u0000\u000f9\u0001\u0000\u0000"+
		"\u0000\u0011;\u0001\u0000\u0000\u0000\u0013=\u0001\u0000\u0000\u0000\u0015"+
		"?\u0001\u0000\u0000\u0000\u0017Q\u0001\u0000\u0000\u0000\u0019{\u0001"+
		"\u0000\u0000\u0000\u001b}\u0001\u0000\u0000\u0000\u001d\u008b\u0001\u0000"+
		"\u0000\u0000\u001f\u008d\u0001\u0000\u0000\u0000!\u008f\u0001\u0000\u0000"+
		"\u0000#\u0091\u0001\u0000\u0000\u0000%\u0093\u0001\u0000\u0000\u0000\'"+
		"(\u0005=\u0000\u0000()\u0005=\u0000\u0000)*\u0005=\u0000\u0000*\u0002"+
		"\u0001\u0000\u0000\u0000+,\u0005<\u0000\u0000,-\u0005<\u0000\u0000-.\u0005"+
		"<\u0000\u0000.\u0004\u0001\u0000\u0000\u0000/0\u0005(\u0000\u00000\u0006"+
		"\u0001\u0000\u0000\u000012\u0005)\u0000\u00002\b\u0001\u0000\u0000\u0000"+
		"34\u0005:\u0000\u00004\n\u0001\u0000\u0000\u000056\u0005+\u0000\u0000"+
		"6\f\u0001\u0000\u0000\u000078\u0005,\u0000\u00008\u000e\u0001\u0000\u0000"+
		"\u00009:\u0005=\u0000\u0000:\u0010\u0001\u0000\u0000\u0000;<\u0005{\u0000"+
		"\u0000<\u0012\u0001\u0000\u0000\u0000=>\u0005}\u0000\u0000>\u0014\u0001"+
		"\u0000\u0000\u0000?C\u0005|\u0000\u0000@B\u0005 \u0000\u0000A@\u0001\u0000"+
		"\u0000\u0000BE\u0001\u0000\u0000\u0000CA\u0001\u0000\u0000\u0000CD\u0001"+
		"\u0000\u0000\u0000DF\u0001\u0000\u0000\u0000EC\u0001\u0000\u0000\u0000"+
		"FK\u0003\u001f\u000f\u0000GJ\u0005 \u0000\u0000HJ\u0003\u001f\u000f\u0000"+
		"IG\u0001\u0000\u0000\u0000IH\u0001\u0000\u0000\u0000JM\u0001\u0000\u0000"+
		"\u0000KI\u0001\u0000\u0000\u0000KL\u0001\u0000\u0000\u0000LN\u0001\u0000"+
		"\u0000\u0000MK\u0001\u0000\u0000\u0000NO\u0005|\u0000\u0000O\u0016\u0001"+
		"\u0000\u0000\u0000PR\u0005-\u0000\u0000QP\u0001\u0000\u0000\u0000QR\u0001"+
		"\u0000\u0000\u0000RS\u0001\u0000\u0000\u0000SX\u0003#\u0011\u0000TW\u0003"+
		"!\u0010\u0000UW\u0005-\u0000\u0000VT\u0001\u0000\u0000\u0000VU\u0001\u0000"+
		"\u0000\u0000WZ\u0001\u0000\u0000\u0000XV\u0001\u0000\u0000\u0000XY\u0001"+
		"\u0000\u0000\u0000Y\u0018\u0001\u0000\u0000\u0000ZX\u0001\u0000\u0000"+
		"\u0000[]\u0005#\u0000\u0000\\^\u0005-\u0000\u0000]\\\u0001\u0000\u0000"+
		"\u0000]^\u0001\u0000\u0000\u0000^_\u0001\u0000\u0000\u0000_c\u0003#\u0011"+
		"\u0000`b\u0003!\u0010\u0000a`\u0001\u0000\u0000\u0000be\u0001\u0000\u0000"+
		"\u0000ca\u0001\u0000\u0000\u0000cd\u0001\u0000\u0000\u0000dm\u0001\u0000"+
		"\u0000\u0000ec\u0001\u0000\u0000\u0000fj\u0005.\u0000\u0000gi\u0003!\u0010"+
		"\u0000hg\u0001\u0000\u0000\u0000il\u0001\u0000\u0000\u0000jh\u0001\u0000"+
		"\u0000\u0000jk\u0001\u0000\u0000\u0000kn\u0001\u0000\u0000\u0000lj\u0001"+
		"\u0000\u0000\u0000mf\u0001\u0000\u0000\u0000mn\u0001\u0000\u0000\u0000"+
		"n|\u0001\u0000\u0000\u0000oq\u0005#\u0000\u0000pr\u0005-\u0000\u0000q"+
		"p\u0001\u0000\u0000\u0000qr\u0001\u0000\u0000\u0000rs\u0001\u0000\u0000"+
		"\u0000st\u00050\u0000\u0000tu\u0005.\u0000\u0000uw\u0001\u0000\u0000\u0000"+
		"vx\u0003!\u0010\u0000wv\u0001\u0000\u0000\u0000xy\u0001\u0000\u0000\u0000"+
		"yw\u0001\u0000\u0000\u0000yz\u0001\u0000\u0000\u0000z|\u0001\u0000\u0000"+
		"\u0000{[\u0001\u0000\u0000\u0000{o\u0001\u0000\u0000\u0000|\u001a\u0001"+
		"\u0000\u0000\u0000}\u0082\u0005\"\u0000\u0000~\u0081\u0003\u001d\u000e"+
		"\u0000\u007f\u0081\b\u0000\u0000\u0000\u0080~\u0001\u0000\u0000\u0000"+
		"\u0080\u007f\u0001\u0000\u0000\u0000\u0081\u0084\u0001\u0000\u0000\u0000"+
		"\u0082\u0083\u0001\u0000\u0000\u0000\u0082\u0080\u0001\u0000\u0000\u0000"+
		"\u0083\u0085\u0001\u0000\u0000\u0000\u0084\u0082\u0001\u0000\u0000\u0000"+
		"\u0085\u0086\u0005\"\u0000\u0000\u0086\u001c\u0001\u0000\u0000\u0000\u0087"+
		"\u0088\u0005\\\u0000\u0000\u0088\u008c\u0005\"\u0000\u0000\u0089\u008a"+
		"\u0005\\\u0000\u0000\u008a\u008c\u0005\\\u0000\u0000\u008b\u0087\u0001"+
		"\u0000\u0000\u0000\u008b\u0089\u0001\u0000\u0000\u0000\u008c\u001e\u0001"+
		"\u0000\u0000\u0000\u008d\u008e\b\u0001\u0000\u0000\u008e \u0001\u0000"+
		"\u0000\u0000\u008f\u0090\u000209\u0000\u0090\"\u0001\u0000\u0000\u0000"+
		"\u0091\u0092\u000219\u0000\u0092$\u0001\u0000\u0000\u0000\u0093\u0094"+
		"\u0007\u0002\u0000\u0000\u0094\u0095\u0001\u0000\u0000\u0000\u0095\u0096"+
		"\u0006\u0012\u0000\u0000\u0096&\u0001\u0000\u0000\u0000\u0011\u0000CI"+
		"KQVX]cjmqy{\u0080\u0082\u008b\u0001\u0006\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}