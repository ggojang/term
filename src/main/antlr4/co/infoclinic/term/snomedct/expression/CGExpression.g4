grammar CGExpression;

options {
	language = Java;
}

EQUIVALENT_TO : '===';
SUBTYPE_OF : '<<<';
LPARAN : '(';
RPARAN : ')';
COLON : ':';
PLUS : '+';
COMMA : ',';
EQUAL : '=';
LCBRACKET : '{';
RCBRACKET : '}';

statements
:
	statement
	(
		statement
	)*
;

statement
:
	LPARAN subExpression RPARAN definitionStatus LPARAN subExpression RPARAN
;

expression
:
	definitionStatus subExpression
	| subExpression
;

definitionStatus
:
	EQUIVALENT_TO
	| SUBTYPE_OF
;

subExpression
:
	focusConcept
	(
		COLON refinement
	)?
;

focusConcept
:
	conceptReference
	(
		PLUS conceptReference
	)*
;

conceptReference
:
	SCTID
	| SCTID TERM
;

refinement
:
	nonGroupedAttributeSet
	(
		COMMA attributeGroup
	)*
	| attributeGroup
	(
		COMMA attributeGroup
	)*
;

attributeGroup
:
	LCBRACKET attributeSet RCBRACKET
;

nonGroupedAttributeSet
:
	attribute
	(
		COMMA attribute
	)*
;

attributeSet
:
	attribute
	(
		COMMA attribute
	)*
;

attribute : attributeType = conceptReference EQUAL attributeValue ;

attributeValue: conceptReference | nestedExpression | NUMBER | STRING ;

nestedExpression : LPARAN subExpression RPARAN ;

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