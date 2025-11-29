package by.bsuir.lab2.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

public class GraphUtils {
    public static void saveTable(String filename, String xLabel, String yLabel, List<Double> x, List<Double> y) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.printf("%-12s %-20s %s%n", "N", yLabel, xLabel);
            for (int i = 0; i < x.size(); i++) {
                pw.printf("%-12d %-20.4f %.4f%n", i + 1, y.get(i), x.get(i));
            }
        }
        System.out.println("Таблица сохранена в " + filename);
    }

    public static void drawGraph(String filename, String title, String xLabel, String yLabel, List<Double> xData, List<Double> yData) throws IOException {
        int w = 800, h = 600;
        int padding = 60;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        // Фон
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Оси
        g.setColor(Color.BLACK);
        g.drawLine(padding, h - padding, w - padding, h - padding); // X
        g.drawLine(padding, h - padding, padding, padding);         // Y

        // Подписи
        g.drawString(title, w / 2 - 100, 30);
        g.drawString(xLabel, w / 2, h - 20);
        g.drawString(yLabel, 10, h / 2);

        // Масштабирование
        double minX = Collections.min(xData), maxX = Collections.max(xData);
        double minY = Collections.min(yData), maxY = Collections.max(yData);

        // Защита от деления на ноль, если все значения одинаковые
        double xRange = (maxX - minX) == 0 ? 1 : (maxX - minX);
        double yRange = (maxY - minY) == 0 ? 1 : (maxY - minY);

        double xScale = (w - 2.0 * padding) / xRange;
        double yScale = (h - 2.0 * padding) / yRange;

        // Рисуем линии и точки
        g.setColor(Color.BLUE);
        for (int i = 0; i < xData.size() - 1; i++) {
            int x1 = padding + (int) ((xData.get(i) - minX) * xScale);
            int y1 = h - padding - (int) ((yData.get(i) - minY) * yScale);
            int x2 = padding + (int) ((xData.get(i + 1) - minX) * xScale);
            int y2 = h - padding - (int) ((yData.get(i + 1) - minY) * yScale);

            g.drawLine(x1, y1, x2, y2);
            g.fillOval(x1 - 3, y1 - 3, 6, 6);
        }
        // Последняя точка
        int xLast = padding + (int) ((xData.getLast() - minX) * xScale);
        int yLast = h - padding - (int) ((yData.getLast() - minY) * yScale);
        g.fillOval(xLast - 3, yLast - 3, 6, 6);

        g.dispose();
        ImageIO.write(img, "png", new File(filename));
        System.out.println("График сохранен в " + filename);
    }
}