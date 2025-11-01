package ru.mycompiler;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import ru.mycompiler.codegen.CodeGenerator; // Импорт CodeGenerator
import ru.mycompiler.parser.MyLanguageLexer;
import ru.mycompiler.parser.MyLanguageParser;
import ru.mycompiler.semantic.SemanticAnalyzer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Использование: java -jar language-processor.jar <путь_к_файлу>");
            System.out.println("Используется тестовый файл по умолчанию...");
        }

        // Используем путь из аргументов или новый тестовый файл по умолчанию
        String filePath = (args.length > 0) ? args[0] : "examples/test_output.txt";
        System.out.println("Анализ файла: " + filePath);

        CharStream input = CharStreams.fromFileName(filePath);
        MyLanguageLexer lexer = new MyLanguageLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MyLanguageParser parser = new MyLanguageParser(tokens);

        parser.removeErrorListeners();
        parser.addErrorListener(DescriptiveErrorListener.INSTANCE);

        ParseTree tree = parser.program();

        if (parser.getNumberOfSyntaxErrors() == 0) {
            System.out.println("Синтаксический анализ прошел успешно.");

            // Запускаем семантический анализ
            ParseTreeWalker walker = new ParseTreeWalker();
            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
            walker.walk(semanticAnalyzer, tree);

            List<String> semanticErrors = semanticAnalyzer.getErrors();
            if (semanticErrors.isEmpty()) {
                System.out.println("Семантический анализ прошел успешно ✅. Начинается генерация кода...");

                // *** ГЛАВНОЕ ИЗМЕНЕНИЕ ЗДЕСЬ ***
                // Создаем CodeGenerator, передавая ему результаты семантического анализа (карту типов)
                CodeGenerator codeGenerator = new CodeGenerator(semanticAnalyzer.getNodeTypes());
                String cilCode = codeGenerator.generate(tree);

                // Сохраняем сгенерированный код в файл
                try (FileWriter writer = new FileWriter("output.il")) {
                    writer.write(cilCode);
                    System.out.println("Код CIL успешно сгенерирован в файл output.il");
                    System.out.println("\nЧтобы скомпилировать, используйте команду .NET SDK в Developer Command Prompt:");
                    System.out.println("ilasm output.il");
                    System.out.println("\nЧтобы запустить, выполните:");
                    System.out.println("output.exe");
                } catch (IOException e) {
                    System.err.println("Ошибка при записи CIL-файла: " + e.getMessage());
                }

            } else {
                System.out.println("\nСемантический анализ завершен ❌. Найдены ошибки:");
                for (String error : semanticErrors) {
                    System.err.println(error);
                }
            }
        } else {
            System.out.println("\nСинтаксический анализ завершен с ошибками. Семантический и кодогенерация не выполнялись.");
        }
    }
}