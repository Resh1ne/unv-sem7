grammar MyLanguage;

// Стартовое правило
program: (statement | functionDefinition | functionCall ';')* EOF;

// Определение функции
functionDefinition:
    'func' IDENTIFIER '(' parameterList? ')' ('->' type)? block;

parameterList:
    parameter (',' parameter)*;

parameter:
    type IDENTIFIER;

// Блок кода
block:
    '{' statement* '}';

// Инструкции
statement:
    variableDeclaration ';'
    | assignment ';'
    | functionCall ';'
    | ifStatement
    | switchStatement
    | forLoop
    | whileLoop
    | untilLoop
    | returnStatement ';'
    | breakStatement ';'
    | block
    ;

// Объявление переменной
variableDeclaration:
    type IDENTIFIER ('=' expression)?;

// Присваивание
assignment:
    lvalue '=' expression;

lvalue:
    IDENTIFIER
    | IDENTIFIER '[' expression ',' expression ']';

// Условный оператор if-then-else
ifStatement:
    'if' '(' expression ')' statement ('else' statement)?;

// Оператор switch-case
switchStatement:
    'switch' '(' expression ')' '{' caseBlock* defaultBlock? '}';

caseBlock:
    'case' expression ':' '{' statement* '}';

defaultBlock:
    'default' ':' '{' statement* '}';

// Цикл for
forLoop:
    'for' IDENTIFIER '=' expression 'to' expression block;

// Цикл while
whileLoop:
    'while' '(' expression ')' block;

// Цикл until
untilLoop:
    'until' '(' expression ')' block;

// Оператор return
returnStatement:
    'return' expression;

// Оператор break
breakStatement:
    'break';

// Выражения
expression:
    // ИЗМЕНЕНИЕ 1: Добавлена новая альтернатива для доступа к свойствам (например, base_img.width)
    expression '.' IDENTIFIER                                                                   # PropertyAccessExpr
    // ИЗМЕНЕНИЕ 2: В список операторов добавлен '||'
    | expression op=('+' | '-' | '/' | '*' | '==' | '!=' | '<' | '>=' | '<=' | '>' | '||') expression # InfixExpr
    | 'new_' IDENTIFIER '(' arguments? ')'                                                      # NewObjectExpr
    | lvalue                                                                                    # LValueExpr
    | functionCall                                                                              # FuncCallExpr
    | literal                                                                                   # LiteralExpr
    | '(' expression ')'                                                                        # ParenExpr
    ;

// Вызов функции
functionCall:
    IDENTIFIER '(' arguments? ')';

arguments:
    expression (',' expression)*;

// Типы
type:
    'image' | 'color' | 'int' | 'string' | 'pixel';

// Литералы
literal:
    INTEGER
    | STRING
    | 'null'
    ;

// Лексерные правила
IDENTIFIER: [a-zA-Z_] [a-zA-Z_0-9]*;
INTEGER: [0-9]+;
STRING: '"' (~["\r\n])*? '"';
WS: [ \t\r\n]+ -> skip;
LINE_COMMENT: '//' ~[\r\n]* -> skip;