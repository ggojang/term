package co.infoclinic.term.snomedct.utils;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;

import co.infoclinic.term.snomedct.expression.ECL2ExpressionLexer;
import co.infoclinic.term.snomedct.expression.ECL2ExpressionParser;
import co.infoclinic.term.snomedct.utils.ECLParserUtil.ThrowErrorListener;

/**
 * ECL 2.x 파서 유틸리티
 *
 * ECL2Expression.g4 문법 기반으로 ECL 표현식을 파싱하고 타입을 감지한다.
 * 지원하는 ECL2 기능:
 *   - 대소문자 무관 AND / OR / MINUS
 *   - Dotted 표현식:  <<X . TYPE_ID
 *   - Compound focus: <<X + <<Y [: refinement]
 *   - Reverse 속성:  * : R TYPE_ID = value
 *   - Cardinality:   * : [1..2] TYPE_ID = *
 */
public class ECL2ParserUtil {

    public static final String TYPE_DOTTED        = "DOTTED";
    public static final String TYPE_COMPOUNDFOCUS = "COMPOUNDFOCUS";
    public static final String TYPE_CONJUNCTION   = "CONJUNCTION";
    public static final String TYPE_DISJUNCTION   = "DISJUNCTION";
    public static final String TYPE_EXCLUSION     = "EXCLUSION";
    public static final String TYPE_SIMPLE        = "SIMPLE";

    /**
     * ECL2 문법으로 파싱하여 표현식 타입을 반환한다.
     *
     * @throws ECLSyntaxError 문법 오류 시
     */
    public static String detectType(String expression) throws ECLSyntaxError {
        if (expression == null || expression.trim().isEmpty()) throw new ECLSyntaxError("Empty expression");

        // pipe 표기 제거 후 타입 판별
        String stripped = expression.replaceAll("\\s*\\|[^|]*\\|\\s*", " ").replaceAll("\\s+", " ").trim();

        // Dotted: contains " . " (space-dot-space) and NOT ".." (cardinality)
        if (!stripped.contains("..") && stripped.matches(".*\\s+\\.\\s+.*")) {
            return TYPE_DOTTED;
        }

        // Compound focus: contains " + " between focus expressions
        if (stripped.matches(".*\\s+\\+\\s+.*")) {
            return TYPE_COMPOUNDFOCUS;
        }

        // ECL2 문법으로 파싱 (syntax validation + keyword detection)
        try {
            parseExpression(expression);
        } catch (Exception e) {
            throw new ECLSyntaxError(e);
        }

        String upper = stripped.toUpperCase();
        if (upper.matches(".*\\sAND\\s.*"))   return TYPE_CONJUNCTION;
        if (upper.matches(".*\\sOR\\s.*"))    return TYPE_DISJUNCTION;
        if (upper.matches(".*\\sMINUS\\s.*")) return TYPE_EXCLUSION;

        return TYPE_SIMPLE;
    }

    /**
     * ECL2 문법으로 파싱하여 ParseTree를 반환한다.
     */
    public static ParseTree parseExpression(String expression) throws ParseCancellationException, ECLSyntaxError {
        ANTLRInputStream input = new ANTLRInputStream(expression);
        ECL2ExpressionLexer lexer = new ECL2ExpressionLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(ThrowErrorListener.TEL);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ECL2ExpressionParser parser = new ECL2ExpressionParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(ThrowErrorListener.TEL);

        try {
            ParseTree tree = parser.expression();
            if (tree == null) throw new ECLSyntaxError("Parse result is null.");
            return tree;
        } catch (ParseCancellationException e) {
            throw e;
        } catch (Exception e) {
            throw new ECLSyntaxError(e);
        }
    }
}
