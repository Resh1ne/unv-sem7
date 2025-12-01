package by.bsuir.lab2;

public class AppConfig {
    public String mode = "test-all";
    public String customImagePath = ""; // Новое поле для пути к файлу
    public char singleLetter = 'A';
    public int maxIterations = 1_000_000;
    public float learningRate = 0.8f;
    public float recallTolerance = 1e-3f;
    public float trainingTolerance = 1e-8f;
    public float noiseLevel = 0.2f;
    public int patternReplications = 0;
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
                case "-f":
                case "--file":
                    if (!next.isEmpty()) { cfg.customImagePath = next; i++; }
                    break;
                case "-l":
                case "--letter":
                    if (!next.isEmpty()) { cfg.singleLetter = next.charAt(0); i++; }
                    break;
                case "-i":
                case "--max-iters":
                    if (!next.isEmpty()) { cfg.maxIterations = Integer.parseInt(next); i++; }
                    break;
                case "-e":
                case "--eta":
                    if (!next.isEmpty()) { cfg.learningRate = Float.parseFloat(next); i++; }
                    break;
                case "-t":
                case "--tolerance":
                    if (!next.isEmpty()) { cfg.recallTolerance = Float.parseFloat(next); i++; }
                    break;
                case "--tol-learn":
                    if (!next.isEmpty()) { cfg.trainingTolerance = Float.parseFloat(next); i++; }
                    break;
                case "-n":
                case "--noisy":
                    if (!next.isEmpty()) { cfg.noiseLevel = Float.parseFloat(next); i++; }
                    break;
                case "-p":
                case "--inclusions":
                    if (!next.isEmpty()) { cfg.patternReplications = Integer.parseInt(next); i++; }
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