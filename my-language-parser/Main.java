import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        // Проверяем, был ли передан путь к файлу
        if (args.length == 0) {
            System.out.println("Пожалуйста, укажите путь к файлу с исходным кодом.");
            return;
        }
        String inputFilePath = args[0];

        // 1. Создаем поток символов из файла
        CharStream input = CharStreams.fromFileName(inputFilePath);

        // 2. Создаем лексер, который обработает поток символов
        MyLanguageLexer lexer = new MyLanguageLexer(input);

        // 3. Создаем поток токенов на основе лексера
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // 4. Создаем парсер, который будет работать с потоком токенов
        MyLanguageParser parser = new MyLanguageParser(tokens);

        // 5. Запускаем разбор с начального правила 'program' и получаем дерево
        ParseTree tree = parser.program();

        // 6. Выводим дерево разбора в консоль в LISP-подобном формате
        System.out.println("Дерево разбора:");
        System.out.println(tree.toStringTree(parser));
    }
}