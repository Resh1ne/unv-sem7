# Парсер для языка "MyLanguage" на ANTLR v4

Этот проект представляет собой парсер для пользовательского языка программирования, разработанный в рамках лабораторной работы. Грамматика языка описана в файле `MyLanguage.g4` и реализована с использованием инструмента ANTLR v4.

## ⚙️ Необходимые компоненты

Для сборки и запуска парсера вам понадобятся:
*   **Java Development Kit (JDK)** версии 8 или выше.
*   **ANTLR v4 Complete JAR**, который должен находиться в корневой папке проекта (например, `antlr-4.13.2-complete.jar`).

## 🛠️ Сборка парсера

> **Важно:** Все команды необходимо выполнять из корневой директории проекта в вашем терминале или командной строке.

### Шаг 1: Генерация кода парсера

Эта команда использует ANTLR для генерации Java-файлов лексера и парсера (`MyLanguageLexer.java` и `MyLanguageParser.java`) из файла грамматики `MyLanguage.g4`.

```bash
java -jar antlr-4.13.2-complete.jar MyLanguage.g4
```

### Шаг 2: Компиляция Java-файлов

Эта команда компилирует сгенерированный код ANTLR вместе с основной программой (`Main.java`) в байт-код Java (`.class` файлы).

```bash
# Для Windows
javac -cp ".;antlr-4.13.2-complete.jar" *.java

# Для macOS / Linux
javac -cp ".:antlr-4.13.2-complete.jar" *.java
```
---

## 🚀 Запуск парсера

После успешной сборки вы можете запустить парсер для анализа файла с исходным кодом (например, `examples/test1.mylang`). Существует два способа сделать это:

### Вариант А: Вывод дерева в консоль

Этот способ запускает основную программу, которая выводит дерево разбора в текстовом виде прямо в консоль.

```bash
# Для Windows
java -cp ".;antlr-4.13.2-complete.jar" Main examples/test1.mylang

# Для macOS / Linux
java -cp ".:antlr-4.13.2-complete.jar" Main examples/test1.mylang
```

### Вариант Б: Графическое отображение дерева (GUI)

**(Рекомендуется для визуализации и отладки)**

Этот способ запускает встроенную в ANTLR утилиту `TestRig`, которая открывает отдельное окно с интерактивным графическим представлением дерева разбора.

```bash
# Для Windows
java -cp ".;antlr-4.13.2-complete.jar" org.antlr.v4.gui.TestRig MyLanguage program -gui examples/test1.mylang

# Для macOS / Linux
java -cp ".:antlr-4.13.2-complete.jar" org.antlr.v4.gui.TestRig MyLanguage program -gui examples/test1.mylang
```
