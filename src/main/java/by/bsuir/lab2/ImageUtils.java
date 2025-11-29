package by.bsuir.lab2;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ImageUtils {
    private static Dimension lastLoadedDimensions = new Dimension(0, 0);

    public static Dimension getLastLoadedDimensions() {
        return lastLoadedDimensions;
    }

    public static List<float[]> loadPatternsFromDir(String dirPath) throws IOException {
        List<float[]> patterns = new ArrayList<>();
        File dir = new File(dirPath);
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));

        if (files != null) {
            Arrays.sort(files); // Гарантируем порядок A, B, C...
            for (File f : files) {
                BufferedImage img = ImageIO.read(f);
                BufferedImage gray = resize(img, img.getWidth() * 2, img.getHeight() * 2); // Удвоение размера как в Rust
                lastLoadedDimensions = new Dimension(gray.getWidth(), gray.getHeight());
                patterns.add(imageToPattern(gray));
            }
        }
        return patterns;
    }

    public static float[] imageToPattern(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        float[] pattern = new float[w * h];
        int idx = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int p = img.getRGB(x, y) & 0xFF; // берем синий канал (для ч/б одинаково)
                pattern[idx++] = (p > 127) ? 1.0f : -1.0f;
            }
        }
        return pattern;
    }

    public static BufferedImage patternToImage(float[] pattern, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = img.getRaster();
        for (int i = 0; i < pattern.length; i++) {
            int val = (pattern[i] > 0) ? 255 : 0;
            int x = i % w;
            int y = i / w;
            raster.setSample(x, y, 0, val);
        }
        return img;
    }

    public static void savePatternAsImage(float[] pattern, int w, int h, String path) throws IOException {
        ImageIO.write(patternToImage(pattern, w, h), "png", new File(path));
    }

    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        BufferedImage resized = new BufferedImage(newW, newH, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(img, 0, 0, newW, newH, null);
        g.dispose();
        return resized;
    }

    public static BufferedImage combineImagesHorizontal(List<BufferedImage> images) {
        int w = images.stream().mapToInt(BufferedImage::getWidth).sum();
        int h = images.stream().mapToInt(BufferedImage::getHeight).max().orElse(0);
        BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = combined.createGraphics();
        int x = 0;
        for (BufferedImage img : images) {
            g.drawImage(img, x, 0, null);
            x += img.getWidth();
        }
        g.dispose();
        return combined;
    }

    public static float[] addNoise(float[] pattern, float noiseLevel, boolean invert) {
        float[] noisy = new float[pattern.length];
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        for (int i = 0; i < pattern.length; i++) {
            float p = pattern[i];
            if (rng.nextDouble() < noiseLevel) {
                p = -p;
            }
            noisy[i] = invert ? -p : p;
        }
        return noisy;
    }
}