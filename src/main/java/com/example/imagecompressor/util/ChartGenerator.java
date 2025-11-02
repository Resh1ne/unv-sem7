// src/main/java/com/example/imagecompressor/ChartGenerator.java
package com.example.imagecompressor.util;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ChartGenerator {

    public static void main(String[] args) throws IOException {
        // Создаем папку для сохранения графиков, если ее нет
        File chartDir = new File("charts");
        if (!chartDir.exists()) {
            chartDir.mkdirs();
        }

        // Генерируем все графики по данным из отчета
        generateIterationsVsCompressionRatioCharts();
        generateIterationsVsMaxErrorChart();
        generateIterationsVsLearningRateChart();
        generateIterationsVsMaxErrorMultiImageChart();

        System.out.println("Все графики были успешно сгенерированы и сохранены в папке 'charts'.");
    }

    /**
     * Создает графики зависимости итераций от коэффициента сжатия (Рис. 3, 4, 5)
     */
    private static void generateIterationsVsCompressionRatioCharts() throws IOException {
        // Данные из таблицы на стр. 12
        List<Double> z = List.of(4.5849, 2.8656, 2.2925, 1.5283, 1.1463);
        List<Integer> avgIterations = List.of(170, 139, 126, 109, 84);

        // График среднего количества итераций (Рис. 4)
        XYChart chartAvg = createBaseChart("Зависимость среднего кол-ва итераций от коэф. сжатия",
                "Коэффициент сжатия", "Кол-во итераций");
        chartAvg.addSeries("Среднее кол-во итераций", z, avgIterations).setMarker(SeriesMarkers.CIRCLE);

        // Сохраняем единственный график
        BitmapEncoder.saveBitmap(chartAvg, "./charts/chart_4_avg_iter_vs_z.png", BitmapEncoder.BitmapFormat.PNG);
        System.out.println("График зависимости среднего кол-ва итераций от коэф. сжатия сохранен.");
    }

    /**
     * Создает график зависимости итераций от максимально допустимой ошибки (Рис. 6)
     */
    private static void generateIterationsVsMaxErrorChart() throws IOException {
        // Данные из таблицы на стр. 13
        List<Double> e = List.of(3000.0, 5000.0, 10000.0, 15000.0, 20000.0);
        List<Integer> iterations = List.of(236, 101, 34, 19, 13);

        XYChart chart = createBaseChart("Зависимость кол-ва итераций от макс. допустимой ошибки",
                "Максимально допустимая ошибка", "Кол-во итераций");
        chart.addSeries("Итерации", e, iterations).setMarker(SeriesMarkers.CIRCLE);
        BitmapEncoder.saveBitmap(chart, "./charts/chart_6_iter_vs_error.png", BitmapEncoder.BitmapFormat.PNG);
    }

    /**
     * Создает график зависимости итераций от коэффициента обучения (Рис. 7)
     */
    private static void generateIterationsVsLearningRateChart() throws IOException {
        // Данные из таблицы на стр. 11
        List<Double> lr = List.of(0.000025, 0.000035, 0.000045, 0.000055, 0.000065, 0.00008, 0.0001);
        List<Integer> iterations = List.of(365, 282, 211, 172, 151, 127, 85);

        XYChart chart = createBaseChart("Зависимость кол-ва итераций от коэф. обучения",
                "Коэффициент обучения", "Кол-во итераций");
        chart.getStyler().setXAxisLabelRotation(45); // Поворот подписей для лучшей читаемости
        chart.addSeries("Итерации", lr, iterations).setMarker(SeriesMarkers.CIRCLE);
        BitmapEncoder.saveBitmap(chart, "./charts/chart_7_iter_vs_lr.png", BitmapEncoder.BitmapFormat.PNG);
    }

    /**
     * Создает график зависимости итераций от макс. Ошибки для разных изображений (Рис. 11)
     */
    private static void generateIterationsVsMaxErrorMultiImageChart() throws IOException {
        // Данные из таблицы на стр. 12
        List<Double> e = List.of(3000.0, 5000.0, 10000.0, 15000.0, 20000.0);
        List<Integer> iterImg1 = List.of(236, 101, 34, 19, 13);
        List<Integer> iterImg2 = List.of(335, 105, 39, 27, 17);
        List<Integer> iterImg3 = List.of(745, 233, 69, 38, 22);

        XYChart chart = createBaseChart("Зависимость кол-ва итераций от макс. ошибки (разные изображения)",
                "Максимально допустимая ошибка", "Кол-во итераций");
        chart.addSeries("Изображение 1 (медведь)", e, iterImg1).setMarker(SeriesMarkers.CIRCLE);
        chart.addSeries("Изображение 2 (лошадь)", e, iterImg2).setMarker(SeriesMarkers.SQUARE);
        chart.addSeries("Изображение 3 (природа)", e, iterImg3).setMarker(SeriesMarkers.DIAMOND);
        BitmapEncoder.saveBitmap(chart, "./charts/chart_11_iter_vs_error_multi_image.png", BitmapEncoder.BitmapFormat.PNG);
    }

    /**
     * Вспомогательный метод для создания базового стиля графика
     */
    private static XYChart createBaseChart(String title, String xTitle, String yTitle) {
        XYChart chart = new XYChartBuilder().width(800).height(600).title(title).xAxisTitle(xTitle).yAxisTitle(yTitle).build();
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.getStyler().setYAxisDecimalPattern("#,###");
        chart.getStyler().setXAxisDecimalPattern("#.#####");
        chart.getStyler().setPlotGridLinesVisible(true);
        return chart;
    }
}