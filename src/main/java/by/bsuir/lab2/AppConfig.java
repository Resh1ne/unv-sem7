// Индивидуальная лабораторная работа 2 по дисциплине МРЗвИС вариант 13
// Выполнена студентом группы 221702 БГУИР Потоцким Даниилом Александровичем
// Файл получения входных аргументов программы
// Последние изменения: 03.11.2025, версия: 1
//
// Использованные источники:
// Формальные модели обработки информации и параллельные модели решения задач. Практикум: учебно-методическое пособие / В. П. Ивашенко. – Минск: БГУИР, 2020.
//
package by.bsuir.lab2;

public class AppConfig {
    public String mode = "test-all";
    public char singleLetter = 'A';
    public int maxIterations = 1_000_000;
    public float learningRate = 0.8f;      // eta
    public float recallTolerance = 1e-3f;  // tolerance
    public float trainingTolerance = 1e-8f;// tolerance_learning
    public float noiseLevel = 0.2f;        // noisy
    public int patternReplications = 0;    // inclusions
    public boolean invertNoise = false;

    public static AppConfig parse(String[] args) {
        AppConfig cfg = new AppConfig();
        if (args.length > 0) {
            cfg.mode = args[0];
        }

        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            String next = (i + 1 < args.length) ? args[i + 1] : "";

            switch (arg) {
                case "-l":
                case "--letter":
                    if (!next.isEmpty()) {
                        cfg.singleLetter = next.charAt(0);
                        i++;
                    }
                    break;
                case "-i":
                case "--max-iters":
                    if (!next.isEmpty()) {
                        cfg.maxIterations = Integer.parseInt(next);
                        i++;
                    }
                    break;
                case "-e":
                case "--eta":
                    if (!next.isEmpty()) {
                        cfg.learningRate = Float.parseFloat(next);
                        i++;
                    }
                    break;
                case "-t":
                case "--tolerance":
                    if (!next.isEmpty()) {
                        cfg.recallTolerance = Float.parseFloat(next);
                        i++;
                    }
                    break;
                case "--tol-learn":
                    if (!next.isEmpty()) {
                        cfg.trainingTolerance = Float.parseFloat(next);
                        i++;
                    }
                    break;
                case "-n":
                case "--noisy":
                    if (!next.isEmpty()) {
                        cfg.noiseLevel = Float.parseFloat(next);
                        i++;
                    }
                    break;
                case "-p":
                case "--inclusions":
                    if (!next.isEmpty()) {
                        cfg.patternReplications = Integer.parseInt(next);
                        i++;
                    }
                    break;
                case "-v":
                case "--invert":
                    cfg.invertNoise = true;
                    break;
            }
        }
        return cfg;
    }
}