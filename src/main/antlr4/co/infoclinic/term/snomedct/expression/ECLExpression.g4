grammar ECLExpression;

options {
	language = Java;
}

CHILDORSELFOF : '<<!' ;
PARENTORSELFOF : '>>!' ;
DESCENDANTORSELFOF : '<<' ;
DESCENDANTOF : '<' ;
CHILDOF : '<!' ;
ANCESTORSELFOF : '>>' ;
ANCESTOF : '>' ;
PARENTOF : '>!' ;
MEMBEROF : '^' ;
COLON : ':' ;
PLUS : '+' ;
COMMA : ',' ;
LPARAN : '(' ;
RPARAN : ')' ;
LCBRACKET : '{' ;
RCBRACKET : '}' ;
CONJUNCTION : 'AND';
DISJUNCTION : 'OR';
EXCLUSION : 'MINUS';
EQUAL : '=' ;
ANY : '*' ;

statments : statement ( statement )* ;

statement : LPARAN subExpression RPARAN constraintOperator LPARAN subExpression RPARAN ;

expression : constraintOperator subExpression | subExpression ;

subExpression : focusConcept ( COLON refinement )? ;

nestedExpression : LPARAN subExpression RPARAN ;

focusConcept : conceptReference ( PLUS conceptReference )* ;

conceptReference : SCTID | SCTID TERM ;

constraintOperator : CHILDORSELFOF | PARENTORSELFOF | DESCENDANTOF | DESCENDANTORSELFOF | CHILDOF | ANCESTOF | ANCESTORSELFOF | PARENTOF ;

refinementOnly : ANY ( COLON refinement )? ;

refinement : attributeNonGroup ( COMMA attributeGroup )* | attributeGroup ( COMMA attributeGroup )* ;

attributeOperator : DESCENDANTOF | DESCENDANTORSELFOF ;

attributeGroup : LCBRACKET attributeSet RCBRACKET ;

attributeNonGroup : attribute ( COMMA attribute )* ;

attributeSet : attribute ( COMMA attribute )* ;

attribute : attributeType = conceptReference EQUAL attributeValue ;

attributeValue : conceptReference | nestedExpression | STRING | NUMBER ;

TERM : '|' ' '* NONWSNONPIPE ( ' ' | NONWSNONPIPE )* '|' ;

SCTID : '-'? DIGITNONZERO (DIGIT|'-')* ;

NUMBER : '#' '-'? DIGITNONZERO DIGIT* ( '.' DIGIT* )? | '#' '-'? '0.' DIGIT+ ;

STRING : '"' ( ESCAPE_CHAR | ~( '"' | '\\' ) )*? '"' ;

fragment ESCAPE_CHAR : '\\"' | '\\\\' ;

fragment NONWSNONPIPE : ~( '|' | '\t' | ' ' | '\r' | '\n' | '\u000C' ) ;

fragment DIGIT : '0' .. '9' ;

fragment DIGITNONZERO : '1' .. '9' ;

WS
:
	(
		'\t'
		| ' '
		| '\r'
		| '\n'
		| '\u000C'
	) -> skip
;