package by.bsuir.lab2;

import by.bsuir.lab2.util.ExperimentRunner;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {

    static class NetworkSetup {
        HopfieldNetwork network;
        List<float[]> patterns;
        int width, height;

        public NetworkSetup(HopfieldNetwork n, List<float[]> p, int w, int h) {
            this.network = n; this.patterns = p; this.width = w; this.height = h;
        }
    }

    public static void main(String[] args) {
        AppConfig config = AppConfig.parse(args);
        System.out.println("Запущен режим: " + config.mode);

        try {
            switch (config.mode) {
                case "single":
                    runSingleLetter(config);
                    break;
                case "test-all":
                    runTestAll(config);
                    break;
                case "custom": // НОВЫЙ РЕЖИМ
                    runCustomImage(config);
                    break;
                case "graphs":
                    ExperimentRunner.runAllPlots();
                    break;
                default:
                    System.out.println("Неизвестный режим. Доступно: single, test-all, custom, graphs");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static NetworkSetup setupNetwork(AppConfig config) throws IOException {
        System.out.println("Загрузка обучающей выборки из letters/...");
        List<float[]> patterns = ImageUtils.loadPatternsFromDir("letters/");
        if (patterns.isEmpty()) throw new IOException("Не найдено изображений в папке letters/");

        List<float[]> trainingSet = new ArrayList<>(patterns);
        for (int i = 0; i < config.patternReplications; i++) {
            trainingSet.addAll(patterns);
        }

        HopfieldNetwork network = new HopfieldNetwork(patterns.get(0).length, Math::tanh);
        network.trainProjectiveDelta(trainingSet, config.learningRate, config.maxIterations, config.trainingTolerance);

        Dimension dim = ImageUtils.getLastLoadedDimensions();
        return new NetworkSetup(network, patterns, dim.width, dim.height);
    }

    // --- НОВЫЙ МЕТОД ---
    private static void runCustomImage(AppConfig config) throws IOException {
        if (config.customImagePath.isEmpty()) {
            System.err.println("Ошибка: Укажите путь к файлу через флаг -f (например: -f my_image.png)");
            return;
        }

        File imgFile = new File(config.customImagePath);
        if (!imgFile.exists()) {
            System.err.println("Ошибка: Файл не найден -> " + config.customImagePath);
            return;
        }

        // 1. Обучаем сеть (обязательно, чтобы сформировать веса)
        NetworkSetup setup = setupNetwork(config);
        System.out.println("Сеть обучена. Обработка пользовательского изображения...");

        // 2. Загружаем пользовательское изображение
        BufferedImage rawImg = ImageIO.read(imgFile);

        // 3. Масштабируем его под размер сети (важный момент!)
        // Сеть ожидает массив определенной длины, поэтому ресайзим под setup.width/height
        BufferedImage resizedImg = ImageUtils.resize(rawImg, setup.width, setup.height);

        float[] originalPattern = ImageUtils.imageToPattern(resizedImg);

        // 4. Добавляем шум (если пользователь задал -n)
        float[] noisyPattern = ImageUtils.addNoise(originalPattern, config.noiseLevel, config.invertNoise);

        // 5. Восстанавливаем
        HopfieldNetwork.RecallResult result = setup.network.recall(noisyPattern, Integer.MAX_VALUE, config.recallTolerance);

        // 6. Сохраняем результат
        List<BufferedImage> images = new ArrayList<>();
        images.add(ImageUtils.patternToImage(originalPattern, setup.width, setup.height));
        images.add(ImageUtils.patternToImage(noisyPattern, setup.width, setup.height));
        images.add(ImageUtils.patternToImage(result.state(), setup.width, setup.height));

        BufferedImage combined = ImageUtils.combineImagesHorizontal(images);
        String outName = "custom_out.png";
        ImageIO.write(combined, "png", new File(outName));

        System.out.println("Результат обработки сохранен в файл: " + outName);
        System.out.println("(Слева: Ваше фото (сжатое), Центр: С шумом, Справа: Ответ сети)");
    }

    private static void runSingleLetter(AppConfig config) throws IOException {
        NetworkSetup setup = setupNetwork(config);

        int patternIndex = Character.toUpperCase(config.singleLetter) - 'A';
        if (patternIndex < 0 || patternIndex >= setup.patterns.size()) {
            System.err.println("Индекс буквы выходит за пределы");
            return;
        }

        float[] original = setup.patterns.get(patternIndex);
        float[] noisy = ImageUtils.addNoise(original, config.noiseLevel, config.invertNoise);

        HopfieldNetwork.RecallResult result = setup.network.recall(noisy, Integer.MAX_VALUE, config.recallTolerance);

        List<BufferedImage> images = new ArrayList<>();
        images.add(ImageUtils.patternToImage(original, setup.width, setup.height));
        images.add(ImageUtils.patternToImage(noisy, setup.width, setup.height));
        images.add(ImageUtils.patternToImage(result.state(), setup.width, setup.height));

        BufferedImage combined = ImageUtils.combineImagesHorizontal(images);
        ImageIO.write(combined, "png", new File("out.png"));
        System.out.println("Результат сохранен в out.png");
    }

    private static void runTestAll(AppConfig config) throws IOException {
        NetworkSetup setup = setupNetwork(config);
        String outDir = "recalled";
        Files.createDirectories(Paths.get(outDir));

        int successCount = 0;
        List<Integer> failedIndices = new ArrayList<>();

        for (int i = 0; i < setup.patterns.size(); i++) {
            float[] original = setup.patterns.get(i);
            float[] noisy = ImageUtils.addNoise(original, config.noiseLevel, config.invertNoise);
            HopfieldNetwork.RecallResult result = setup.network.recall(noisy, Integer.MAX_VALUE, config.recallTolerance);

            int w = 40, h = 40;
            ImageUtils.savePatternAsImage(noisy, w, h, outDir + "/pattern_" + i + "_noisy.png");
            ImageUtils.savePatternAsImage(result.state(), w, h, outDir + "/pattern_" + i + "_recalled.png");
            ImageUtils.savePatternAsImage(original, w, h, outDir + "/pattern_" + i + "_original.png");

            if (checkSimilarity(original, result.state(), config.invertNoise)) {
                successCount++;
            } else {
                failedIndices.add(i);
            }
        }
        System.out.printf("Успешно восстановлено %d/%d образов (%.2f%%)%n",
                successCount, setup.patterns.size(), 100.0 * successCount / setup.patterns.size());
    }

    private static boolean checkSimilarity(float[] original, float[] recalled, boolean inverted) {
        float[] normOrg = normalize(original);
        float[] normRec = normalize(recalled);
        for (int i = 0; i < normOrg.length; i++) {
            float target = inverted ? -normOrg[i] : normOrg[i];
            if (Math.abs(normRec[i] - target) > 1e-5) return false;
        }
        return true;
    }

    private static float[] normalize(float[] input) {
        float[] out = new float[input.length];
        for (int i = 0; i < input.length; i++) {
            out[i] = input[i] >= 0 ? 1.0f : -1.0f;
        }
        return out;
    }
}