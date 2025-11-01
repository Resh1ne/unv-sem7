package ru.mycompiler.semantic;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import ru.mycompiler.parser.MyLanguageBaseListener;
import ru.mycompiler.parser.MyLanguageParser;

import java.util.List;
import java.util.Stack;
import java.util.ArrayList;

/**
 * SemanticAnalyzer обходит дерево разбора, построенное ANTLR, и выполняет семантические проверки.
 * - Контроль областей видимости (scope).
 * - Проверка объявлений переменных и функций.
 * - Проверка соответствия типов в выражениях и присваиваниях.
 *
 * Версия 7 (Финальная)
 */
public class SemanticAnalyzer extends MyLanguageBaseListener {

    private static final String ERROR_TYPE = "error_type";
    private final Stack<Scope> scopes = new Stack<>();
    private final ParseTreeProperty<String> nodeTypes = new ParseTreeProperty<>();
    private final List<String> errors = new ArrayList<>();

    public SemanticAnalyzer() {
        Scope globalScope = new Scope(null);
        addPredefinedSymbols(globalScope);
        scopes.push(globalScope);
    }

    private void addPredefinedSymbols(Scope scope) {
        scope.define(new FunctionSymbol("write", "void"));
        scope.define(new FunctionSymbol("load", "image"));
        scope.define(new FunctionSymbol("save", "void"));
        scope.define(new FunctionSymbol("new_image", "image"));
        scope.define(new FunctionSymbol("new_color", "color"));
    }

    public List<String> getErrors() {
        return errors;
    }

    // *** ИСПРАВЛЕНИЕ 1: Сделал метод публичным ***
    public ParseTreeProperty<String> getNodeTypes() {
        return this.nodeTypes;
    }

    private void error(int line, String message) {
        errors.add(String.format("Ошибка в строке %d: %s", line, message));
    }

    // --- Управление областями видимости ---
    @Override public void enterFunctionDefinition(MyLanguageParser.FunctionDefinitionContext ctx) {
        String funcName = ctx.IDENTIFIER().getText();
        String returnType = (ctx.type() != null) ? ctx.type().getText() : "void";
        FunctionSymbol funcSymbol = new FunctionSymbol(funcName, returnType);
        scopes.peek().define(funcSymbol);
        Scope functionScope = new Scope(scopes.peek());
        scopes.push(functionScope);
        if (ctx.parameterList() != null) {
            for (MyLanguageParser.ParameterContext param : ctx.parameterList().parameter()) {
                String paramName = param.IDENTIFIER().getText();
                String paramType = param.type().getText();
                VariableSymbol paramSymbol = new VariableSymbol(paramName, paramType);
                functionScope.define(paramSymbol);
                funcSymbol.addArgument(paramSymbol);
            }
        }
    }
    @Override public void exitFunctionDefinition(MyLanguageParser.FunctionDefinitionContext ctx) { scopes.pop(); }
    @Override public void enterBlock(MyLanguageParser.BlockContext ctx) { if (!(ctx.getParent() instanceof MyLanguageParser.FunctionDefinitionContext)) scopes.push(new Scope(scopes.peek())); }
    @Override public void exitBlock(MyLanguageParser.BlockContext ctx) { if (!(ctx.getParent() instanceof MyLanguageParser.FunctionDefinitionContext)) scopes.pop(); }

    // --- Обработка объявлений и присваиваний ---
    @Override
    public void exitVariableDeclaration(MyLanguageParser.VariableDeclarationContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        String varType = ctx.type().getText();
        Scope currentScope = scopes.peek();

        if (currentScope.resolve(varName) != null) {
            error(ctx.start.getLine(), "переменная '" + varName + "' уже объявлена в этой области видимости.");
        } else {
            currentScope.define(new VariableSymbol(varName, varType));
        }

        if (ctx.expression() != null) {
            String exprType = nodeTypes.get(ctx.expression());
            // *** ИСПРАВЛЕНИЕ 3а: isTypeCompatible должен возвращать true для совместимых типов ***
            if (exprType != null && isTypeCompatible(varType, exprType)) {
                error(ctx.start.getLine(), "несоответствие типов. Нельзя присвоить выражение типа '" + exprType + "' переменной типа '" + varType + "'.");
            }
        }
    }

    @Override
    public void exitAssignment(MyLanguageParser.AssignmentContext ctx) {
        String varName = ctx.lvalue().IDENTIFIER().getText();
        Symbol symbol = scopes.peek().lookup(varName);

        if (symbol == null) {
            error(ctx.start.getLine(), "переменная '" + varName + "' не была объявлена.");
            return;
        }

        String varType = symbol.getType();
        String exprType = nodeTypes.get(ctx.expression());
        // *** ИСПРАВЛЕНИЕ 3б: isTypeCompatible должен возвращать true для совместимых типов ***
        if (exprType != null && isTypeCompatible(varType, exprType)) {
            error(ctx.start.getLine(), "несоответствие типов при присваивании. Нельзя присвоить '" + exprType + "' переменной '" + varName + "' типа '" + varType + "'.");
        }
    }

    // --- Логика вывода типов для выражений ---
    @Override
    public void exitAddSubExpr(MyLanguageParser.AddSubExprContext ctx) {
        String leftType = nodeTypes.get(ctx.expression(0));
        String rightType = nodeTypes.get(ctx.expression(1));
        String resultType = ERROR_TYPE;

        if (leftType != null && rightType != null && !leftType.equals(ERROR_TYPE) && !rightType.equals(ERROR_TYPE)) {
            if ("int".equals(leftType) && "int".equals(rightType)) {
                resultType = "int";
            } else if ("+".equals(ctx.op.getText()) && "string".equals(leftType) && "string".equals(rightType)) {
                resultType = "string";
            } else {
                error(ctx.start.getLine(), "операция '" + ctx.op.getText() + "' не применима к типам '" + leftType + "' и '" + rightType + "'.");
            }
        }
        nodeTypes.put(ctx, resultType);
    }
    // Выражения-обертки
    @Override public void exitAtomExpr(MyLanguageParser.AtomExprContext ctx) { propagateTypeFromChild(ctx, ctx.atom()); }
    @Override public void exitParenExpr(MyLanguageParser.ParenExprContext ctx) { propagateTypeFromChild(ctx, ctx.expression()); }
    // "Листья" дерева выражений
    @Override public void exitLiteralExpr(MyLanguageParser.LiteralExprContext ctx) {
        String type = null;
        if (ctx.literal().INTEGER() != null) type = "int";
        else if (ctx.literal().STRING() != null) type = "string";
        else if (ctx.literal().getText().equals("null")) type = "null";
        nodeTypes.put(ctx.getParent(), type);
    }
    @Override public void exitLValueExpr(MyLanguageParser.LValueExprContext ctx) {
        String varName = ctx.lvalue().IDENTIFIER().getText();
        Symbol symbol = scopes.peek().lookup(varName);
        String type;
        if (symbol == null) {
            error(ctx.start.getLine(), "переменная '" + varName + "' не была объявлена.");
            type = ERROR_TYPE;
        } else {
            type = symbol.getType();
        }
        nodeTypes.put(ctx.getParent(), type);
    }
    @Override public void exitFuncCallExpr(MyLanguageParser.FuncCallExprContext ctx) {
        String funcName = ctx.functionCall().IDENTIFIER().getText();
        Symbol symbol = scopes.peek().lookup(funcName);
        String type;
        if (symbol == null) {
            error(ctx.start.getLine(), "функция '" + funcName + "' не была объявлена.");
            type = ERROR_TYPE;
        } else if (!(symbol instanceof FunctionSymbol)) {
            error(ctx.start.getLine(), "'" + funcName + "' не является функцией.");
            type = ERROR_TYPE;
        } else {
            type = symbol.getType();
        }
        nodeTypes.put(ctx.getParent(), type);
    }

    // --- Вспомогательные методы ---
    private void propagateTypeFromChild(ParseTree parent, ParseTree child) {
        String childType = nodeTypes.get(child);
        if (childType != null) {
            nodeTypes.put(parent, childType);
        }
    }

    // *** ИСПРАВЛЕНИЕ 2: Исправлена логика совместимости типов ***
    private boolean isTypeCompatible(String varType, String exprType) {
        if (ERROR_TYPE.equals(exprType)) {
            return true; // Выражение с ошибкой несовместимо ни с чем.
        }
        if ("null".equals(exprType)) {
            // null совместим с "объектными" типами (image, color, pixel, string), но не с int
            return !"image".equals(varType) && !"color".equals(varType) && !"pixel".equals(varType) && !"string".equals(varType);
        }
        // В остальных случаях требуется строгое совпадение типов.
        return !varType.equals(exprType);
    }
}