package ru.mycompiler.codegen;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import ru.mycompiler.parser.MyLanguageBaseVisitor;
import ru.mycompiler.parser.MyLanguageParser;

import java.util.HashMap;
import java.util.Map;

public class CodeGenerator extends MyLanguageBaseVisitor<Void> {

    private final StringBuilder cilCodeBody = new StringBuilder();
    private final StringBuilder cilLocals = new StringBuilder();

    private final Map<String, Integer> localVariableMap = new HashMap<>();
    private int localVariableIndex = 0;

    // Хранилище типов, полученное из семантического анализатора
    private final ParseTreeProperty<String> nodeTypes;

    /**
     * Конструктор, принимающий результаты семантического анализа.
     * @param nodeTypes Карта, связывающая узлы дерева с их типами.
     */
    public CodeGenerator(ParseTreeProperty<String> nodeTypes) {
        this.nodeTypes = nodeTypes;
    }

    /**
     * Главный метод, запускающий генерацию и собирающий финальный CIL-файл.
     */
    public String generate(ParseTree tree) {
        visit(tree); // Запускаем обход дерева

        StringBuilder finalCil = new StringBuilder();
        finalCil.append(".assembly extern mscorlib {}\n");
        finalCil.append(".assembly output {}\n");
        finalCil.append(".module output.exe\n\n");
        finalCil.append(".class private auto ansi beforefieldinit Program\n");
        finalCil.append("    extends [mscorlib]System.Object\n{\n");
        finalCil.append("    .method public hidebysig static void Main(string[] args) cil managed\n");
        finalCil.append("    {\n");
        finalCil.append("        .entrypoint\n");
        finalCil.append("        .locals init (\n");

        String localsBlock = cilLocals.toString();
        if (localsBlock.endsWith(",\n")) {
            localsBlock = localsBlock.substring(0, localsBlock.length() - 2) + "\n";
        }
        finalCil.append(localsBlock);

        finalCil.append("        )\n");
        finalCil.append("        // --- Start of code body ---\n");
        finalCil.append(cilCodeBody.toString());
        finalCil.append("        // ---  End of code body  ---\n");
        finalCil.append("        ret\n");
        finalCil.append("    }\n");
        finalCil.append("}\n");

        return finalCil.toString();
    }

    /**
     * Обрабатывает вызовы функций. Пока реализована только 'write'.
     */
    @Override
    public Void visitFunctionCall(MyLanguageParser.FunctionCallContext ctx) {
        String funcName = ctx.IDENTIFIER().getText();

        if ("write".equals(funcName) && ctx.arguments() != null) {
            MyLanguageParser.ExpressionContext argument = ctx.arguments().expression(0);

            // Получаем тип аргумента, вычисленный семантическим анализатором
            String argType = this.nodeTypes.get(argument);

            // Генерируем код для вычисления аргумента (результат окажется на стеке)
            visit(argument);

            // Вызываем нужную перегруженную версию System.Console.WriteLine
            if (argType != null) {
                cilCodeBody.append(String.format("        call void [mscorlib]System.Console::WriteLine(%s)\n", mapTypeToCil(argType)));
            }
        }
        return null;
    }

    // --- Остальные visit-методы ---

    @Override
    public Void visitVariableDeclaration(MyLanguageParser.VariableDeclarationContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        String varType = mapTypeToCil(ctx.type().getText());
        cilLocals.append(String.format("            [%d] %s %s,\n", localVariableIndex, varType, varName));
        localVariableMap.put(varName, localVariableIndex);
        localVariableIndex++;
        if (ctx.expression() != null) {
            visit(ctx.expression());
            cilCodeBody.append(String.format("        stloc.%d // %s\n", localVariableMap.get(varName), varName));
        }
        return null;
    }
    @Override
    public Void visitAssignment(MyLanguageParser.AssignmentContext ctx) {
        String varName = ctx.lvalue().IDENTIFIER().getText();
        Integer varIndex = localVariableMap.get(varName);
        if (varIndex != null) {
            visit(ctx.expression());
            cilCodeBody.append(String.format("        stloc.%d // %s\n", varIndex, varName));
        }
        return null;
    }
    @Override
    public Void visitAddSubExpr(MyLanguageParser.AddSubExprContext ctx) {
        visit(ctx.expression(0));
        visit(ctx.expression(1));
        if ("+".equals(ctx.op.getText())) { cilCodeBody.append("        add\n"); }
        else { cilCodeBody.append("        sub\n"); }
        return null;
    }
    @Override
    public Void visitLiteralExpr(MyLanguageParser.LiteralExprContext ctx) {
        if (ctx.literal().INTEGER() != null) {
            cilCodeBody.append(String.format("        ldc.i4 %s\n", ctx.literal().INTEGER().getText()));
        } else if (ctx.literal().STRING() != null) {
            cilCodeBody.append(String.format("        ldstr %s\n", ctx.literal().STRING().getText()));
        }
        return null;
    }
    @Override
    public Void visitLValueExpr(MyLanguageParser.LValueExprContext ctx) {
        String varName = ctx.lvalue().IDENTIFIER().getText();
        Integer varIndex = localVariableMap.get(varName);
        if (varIndex != null) {
            cilCodeBody.append(String.format("        ldloc.%d // %s\n", varIndex, varName));
        }
        return null;
    }
    private String mapTypeToCil(String langType) {
        switch (langType) {
            case "int": return "int32";
            case "string": return "string";
            default: return "object"; // Тип по умолчанию
        }
    }
}