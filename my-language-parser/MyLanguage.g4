grammar MyLanguage;

// --- ПРАВИЛА ПАРСЕРА ---

program: (statement | functionDefinition)* EOF;

functionDefinition:
    'func' IDENTIFIER '(' parameterList? ')' ('->' type)? block;

parameterList:
    parameter (',' parameter)*;

parameter:
    type IDENTIFIER;

block:
    '{' statement* '}';

statement:
    variableDeclaration ';'
    | assignment ';'
    | functionCall ';'
    | ifStatement
    | switchStatement
    | forLoop
    | whileLoop
    | returnStatement ';'
    | breakStatement ';'
    | block;

variableDeclaration:
    type IDENTIFIER ('=' expression)?;

assignment:
    lvalue '=' expression;

lvalue:
    IDENTIFIER
    | IDENTIFIER '[' expression ',' expression ']';

ifStatement:
    'if' '(' expression ')' statement ('else' statement)?;

switchStatement:
    'switch' '(' expression ')' '{' caseBlock* defaultBlock? '}';

caseBlock:
    'case' expression ':' '{' statement* '}';

defaultBlock:
    'default' ':' '{' statement* '}';

forLoop:
    'for' IDENTIFIER '=' expression 'to' expression block;

whileLoop:
    'while' '(' expression ')' block;

returnStatement:
    'return' expression;

breakStatement:
    'break';

expression
    : expression '||' expression                                  # LogicalOrExpr
    | expression op=('==' | '!=') expression                      # EqualityExpr
    | expression op=('<'|'>'|'<='|'>=') expression                 # RelationalExpr
    | expression op=('+'|'-') expression                          # AddSubExpr
    | expression op=('*'|'/') expression                          # MulDivExpr
    | expression '.' IDENTIFIER                                   # PropertyAccessExpr
    | atom                                                        # AtomExpr
    ;

atom
    : 'new_' IDENTIFIER '(' arguments? ')'                        # NewObjectExpr
    | lvalue                                                      # LValueExpr
    | functionCall                                                # FuncCallExpr
    | literal                                                     # LiteralExpr
    | '(' expression ')'                                          # ParenExpr
    ;

functionCall:
    IDENTIFIER '(' arguments? ')';

arguments:
    expression (',' expression)*;

type:
    'image' | 'color' | 'int' | 'string' | 'pixel';

literal:
    INTEGER
    | STRING
    | 'null'
    ;

// --- ПРАВИЛА ЛЕКСЕРА ---

IDENTIFIER: [a-zA-Z_] [a-zA-Z_0-9]*;
INTEGER: [0-9]+;

STRING: '"' ~["]* '"';

WS: [ \t\r\n]+ -> skip;
LINE_COMMENT: '//' ~[\r\n]* -> skip;
