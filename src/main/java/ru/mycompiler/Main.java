package ru.mycompiler;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import ru.mycompiler.parser.MyLanguageLexer;
import ru.mycompiler.parser.MyLanguageParser;
import ru.mycompiler.semantic.SemanticAnalyzer;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Использование: java -jar language-processor.jar <путь_к_файлу>");
            System.out.println("Используется тестовый файл по умолчанию...");
        }

        String filePath = (args.length > 0) ? args[0] : "examples/test_semantic_error.txt";
        System.out.println("Анализ файла: " + filePath);

        try {
            // Создаём поток символов из файла
            CharStream input = CharStreams.fromFileName(filePath);

            // Создаём лексер
            MyLanguageLexer lexer = new MyLanguageLexer(input);

            // Создаём поток токенов
            CommonTokenStream tokens = new CommonTokenStream(lexer);

            // Создаём парсер
            MyLanguageParser parser = new MyLanguageParser(tokens);

            // *** КЛЮЧЕВОЙ ШАГ ДЛЯ ЛАБОРАТОРНОЙ 3 ***
            // 1. Удаляем стандартный обработчик ошибок
            parser.removeErrorListeners();
            // 2. Добавляем наш собственный обработчик
            parser.addErrorListener(DescriptiveErrorListener.INSTANCE);

            // Запускаем анализ с правила 'program'
            ParseTree tree = parser.program(); // Нет необходимости сохранять дерево, если мы только ищем ошибки

            // Проверяем, были ли обнаружены ошибки
            if (parser.getNumberOfSyntaxErrors() == 0) {
                System.out.println("Синтаксический анализ прошел успешно.");

                // *** ЗАПУСК СЕМАНТИЧЕСКОГО АНАЛИЗА ***
                ParseTreeWalker walker = new ParseTreeWalker();
                SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
                walker.walk(semanticAnalyzer, tree);

                List<String> semanticErrors = semanticAnalyzer.getErrors();
                if (semanticErrors.isEmpty()) {
                    System.out.println("Семантический анализ прошел успешно ✅. Ошибок не найдено.");
                } else {
                    System.out.println("\nСемантический анализ завершен ❌. Найдены ошибки:");
                    for (String error : semanticErrors) {
                        System.err.println(error);
                    }
                }
            } else {
                System.out.println("\nСинтаксический анализ завершен с ошибками. Семантический анализ не выполнялся.");
            }

        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Произошла критическая ошибка анализа: " + e.getMessage());
            e.printStackTrace();
        }
    }
}