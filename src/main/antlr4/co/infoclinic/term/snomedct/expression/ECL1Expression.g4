grammar ECL1Expression;

options {
	language = Java;
}

DESCENDANTOF : '<' ;
DESCENDANTORSELFOF : '<<' ;
CHILDOF : '<!' ;
ANCESTOF : '>' ;
ANCESTORSELFOF : '>>' ;
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
NOTEQUAL : '!=' ;
LESSOREQUAL : '<=' ;
GREATEROREQUAL : '>=' ;
ANY : '*' ;

expression : simpleExpression | conjunctionExpression | disjunctionExpression | exclusionExpression | refinementExpression ;

refinementExpression : simpleExpression COLON refinement;

conjunctionExpression : simpleExpression ( CONJUNCTION simpleExpression)* ;

disjunctionExpression : simpleExpression ( DISJUNCTION simpleExpression)* ;

exclusionExpression : simpleExpression ( EXCLUSION simpleExpression)* ;

simpleExpression : focusConcept | constraintOperator focusConcept ;

focusConcept : MEMBEROF conceptReference | conceptReference | ANY ;

constraintOperator : DESCENDANTOF | DESCENDANTORSELFOF | CHILDOF | ANCESTOF | ANCESTORSELFOF | PARENTOF ;

conceptReference : SCTID | SCTID TERM ;

refinement : attributeSet | attributeSet ( COMMA attributeGroup )* | attributeGroup ( COMMA attributeGroup )* ;

attributeGroup : LCBRACKET attributeSet RCBRACKET ;

attributeSet : attribute ( COMMA attribute )* ;

attributeOperator : DESCENDANTOF | DESCENDANTORSELFOF ;

attribute : attributeType = attributeName EQUAL attributeValue | attributeOperator attributeName EQUAL attributeValue ;

attributeName : conceptReference | ANY ;

attributeValue : simpleExpression | compoundAttributevalue | exclusionAttributevalue | (stringOperator STRING) | (numberOperator NUMBER) ;

compoundAttributevalue : LPARAN (conjunctionExpression | disjunctionExpression) RPARAN ;

exclusionAttributevalue : LPARAN compoundAttributevalue ( EXCLUSION simpleExpression)* RPARAN ;

stringOperator : EQUAL | NOTEQUAL ;

numberOperator : EQUAL | NOTEQUAL | LESSOREQUAL | GREATEROREQUAL | DESCENDANTOF | ANCESTOF ;

expressionOperator : EQUAL | NOTEQUAL ;

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