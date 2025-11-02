//Лабораторная работа 1, вариант 13
//Выполнена студентом группы 221702 БГУИР Потоцким Даниилом Александровичем
//Файл с основными параметрами
//Использованные источники:
//Формальные модели обработки информации и параллельные модели решения задач. Практикум: учебно-методическое пособие / В.П.Ивашенко. – Минск: БГУИР, 2020.
package com.example.imagecompressor;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;

public class CliArguments {

    @Parameters(index = "0", description = "Path to the input image")
    File inputFile;

    @Option(names = {"-o", "--output"}, defaultValue = "pictures/out.bmp", description = "Output image path")
    File outputFile;

    @Option(names = {"-me", "--max-epoch"}, defaultValue = "1200", description = "Number of max epochs")
    int maxEpochs;

    @Option(names = {"-merr", "--max-error"}, defaultValue = "5000.0", description = "The max error")
    double maxError;

    @Option(names = "--lr", defaultValue = "0.0001", description = "Learning rate")
    double learningRate;

    @Option(names = "-m", defaultValue = "8", description = "Width of blocks")
    int blockWidth;

    @Option(names = "-n", defaultValue = "8", description = "Height of blocks")
    int blockHeight;

    @Option(names = {"-sl", "--secret-layer"}, defaultValue = "20", description = "Amount of neurons on secret layer")
    Integer hiddenLayerSize;

    public static CliArguments parse(String[] args) {
        CliArguments cliArgs = new CliArguments();
        new CommandLine(cliArgs).parseArgs(args);
        return cliArgs;
    }
}