grammar ECL2Expression;

options {
	language = Java;
}

// ── Compound operators (longest-match first) ────────────────────────────────
CHILDORSELFOF      : '<<!' ;
PARENTORSELFOF     : '>>!' ;
DESCENDANTORSELFOF : '<<' ;
CHILDOF            : '<!' ;
ANCESTORSELFOF     : '>>' ;
PARENTOF           : '>!' ;

// Single-char operators (must follow compound versions)
DESCENDANTOF       : '<' ;
ANCESTOF           : '>' ;

MEMBEROF           : '^' ;
COLON              : ':' ;
COMMA              : ',' ;
DOTDOT             : '..' ;   // MUST be before DOT
DOT                : '.' ;
PLUS               : '+' ;
LPARAN             : '(' ;
RPARAN             : ')' ;
LCBRACKET          : '{' ;
RCBRACKET          : '}' ;
LSQUARE            : '[' ;
RSQUARE            : ']' ;
NOTEQUAL           : '!=' ;
LESSOREQUAL        : '<=' ;
GREATEROREQUAL     : '>=' ;
EQUAL              : '=' ;
ANY                : '*' ;
HASH               : '#' ;

// ECL2: reverse attribute flag (case-insensitive single char)
REVERSEFLAG : [Rr] ;

// Case-insensitive logical operators (ECL2 addition)
CONJUNCTION : [Aa][Nn][Dd] ;
DISJUNCTION : [Oo][Rr] ;
EXCLUSION   : [Mm][Ii][Nn][Uu][Ss] ;

// ── Expression ──────────────────────────────────────────────────────────────

expression
    : dottedExpression
    | compoundFocusExpression
    | conjunctionExpression
    | disjunctionExpression
    | exclusionExpression
    | refinementExpression
    | simpleExpression
    ;

// ECL2: <<X . TYPE_ID [. TYPE_ID2 ...]
dottedExpression : simpleExpression ( DOT attributeName )+ ;

// ECL2: <<X + <<Y [: refinement]  (compound focus = OR-union of foci)
compoundFocusExpression : simpleExpression ( PLUS simpleExpression )+ ( COLON refinement )? ;

refinementExpression : simpleExpression COLON refinement ;

conjunctionExpression : simpleExpression ( CONJUNCTION simpleExpression )+ ;
disjunctionExpression : simpleExpression ( DISJUNCTION simpleExpression )+ ;
exclusionExpression   : simpleExpression EXCLUSION simpleExpression ;

simpleExpression : constraintOperator focusConcept | focusConcept ;

focusConcept : MEMBEROF conceptReference | conceptReference | ANY ;

constraintOperator
    : CHILDORSELFOF | PARENTORSELFOF
    | DESCENDANTORSELFOF | DESCENDANTOF
    | CHILDOF
    | ANCESTORSELFOF | ANCESTOF
    | PARENTOF
    ;

conceptReference : SCTID TERM? ;

// ── Refinement ───────────────────────────────────────────────────────────────

refinement
    : attributeSet ( COMMA attributeGroup )*
    | attributeGroup ( COMMA attributeGroup )+
    ;

attributeGroup : LCBRACKET attributeSet RCBRACKET ;

attributeSet : attribute ( COMMA attribute )* ;

attributeOperator : DESCENDANTOF | DESCENDANTORSELFOF ;

// ECL2: optional [cardinality], optional R (reverse), optional attribute operator
attribute
    : ( LSQUARE cardinality RSQUARE )?
      REVERSEFLAG?
      attributeOperator?
      attributeName EQUAL attributeValue
    ;

// ECL2: [min..max]  e.g. [0..1]  [1..*]
cardinality : cardinalityInt DOTDOT ( cardinalityInt | ANY ) ;

cardinalityInt : ZERO | SCTID ;

ZERO : '0' ;

attributeName : conceptReference | ANY ;

attributeValue
    : simpleExpression
    | compoundAttributeValue
    | exclusionAttributeValue
    | stringOperator STRING
    | numberOperator NUMBER
    ;

compoundAttributeValue  : LPARAN ( conjunctionExpression | disjunctionExpression ) RPARAN ;
exclusionAttributeValue : LPARAN compoundAttributeValue ( EXCLUSION simpleExpression )* RPARAN ;

stringOperator : EQUAL | NOTEQUAL ;
numberOperator : EQUAL | NOTEQUAL | LESSOREQUAL | GREATEROREQUAL | DESCENDANTOF | ANCESTOF ;

// ── Lexer rules ──────────────────────────────────────────────────────────────

TERM   : '|' ' '* NONWSNONPIPE ( ' ' | NONWSNONPIPE )* '|' ;
SCTID  : DIGITNONZERO ( DIGIT | '-' )* ;
NUMBER : HASH '-'? DIGITNONZERO DIGIT* ( '.' DIGIT* )? | HASH '-'? '0.' DIGIT+ ;
STRING : '"' ( ESCAPE_CHAR | ~( '"' | '\\' ) )*? '"' ;

fragment ESCAPE_CHAR  : '\\"' | '\\\\' ;
fragment NONWSNONPIPE : ~( '|' | '\t' | ' ' | '\r' | '\n' | '' ) ;
fragment DIGIT        : '0'..'9' ;
fragment DIGITNONZERO : '1'..'9' ;

WS : ( '\t' | ' ' | '\r' | '\n' | '' ) -> skip ;
