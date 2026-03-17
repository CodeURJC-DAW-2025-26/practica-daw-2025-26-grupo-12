package es.codeurjc.grupo12.scissors_please.service.chart;

import es.codeurjc.grupo12.scissors_please.repository.UserRepository.MonthlyUserCount;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.TextStyle;
import java.util.List;
import javax.imageio.ImageIO;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Service;

@Service
public class ChartService {

  private final Color TEXT_COLOR = new Color(222, 226, 230);
  private final Color GREEN_WIN = new Color(34, 197, 94);
  private final Color RED_LOSS = new Color(239, 68, 68);
  private final Color SLATE_DRAW = new Color(148, 163, 184);
  private final Color PURPLE_LINE = new Color(139, 92, 246);
  private final Color TRANSPARENT = new Color(0, 0, 0, 0);

  public byte[] generateResultsPieChart(int wins, int losses, int draws) {
    DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
    dataset.setValue("Wins", wins);
    dataset.setValue("Losses", losses);
    dataset.setValue("Draws", draws);

    JFreeChart chart = ChartFactory.createPieChart(null, dataset, false, true, false);

    chart.setBackgroundPaint(TRANSPARENT);
    chart.setBorderVisible(false);

    PiePlot plot = (PiePlot) chart.getPlot();
    plot.setBackgroundPaint(TRANSPARENT);
    plot.setOutlineVisible(false);
    plot.setLabelGenerator(null);

    plot.setSectionPaint("Wins", GREEN_WIN);
    plot.setSectionPaint("Losses", RED_LOSS);
    plot.setSectionPaint("Draws", SLATE_DRAW);
    plot.setShadowPaint(null);

    return chartToBytes(chart, 400, 400);
  }

  public byte[] generateEloLineChart(List<Integer> eloHistory) {
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    for (int i = 0; i < eloHistory.size(); i++) {
      dataset.addValue(eloHistory.get(i), "ELO", "M" + (i + 1));
    }

    JFreeChart chart =
        ChartFactory.createLineChart(
            null, null, null, dataset, PlotOrientation.VERTICAL, false, true, false);

    chart.setBackgroundPaint(TRANSPARENT);

    var plot = chart.getCategoryPlot();
    plot.setBackgroundPaint(TRANSPARENT);
    plot.setRangeGridlinePaint(new Color(51, 65, 85));
    plot.setOutlineVisible(false);

    LineAndShapeRenderer renderer = new LineAndShapeRenderer();
    renderer.setSeriesPaint(0, PURPLE_LINE);
    renderer.setSeriesStroke(0, new BasicStroke(3.0f));
    renderer.setSeriesShapesVisible(0, true);
    plot.setRenderer(renderer);

    plot.getDomainAxis().setTickLabelPaint(TEXT_COLOR);
    plot.getRangeAxis().setTickLabelPaint(TEXT_COLOR);

    return chartToBytes(chart, 600, 300);
  }

  public byte[] generateUserHistory(List<MonthlyUserCount> monthlyData) {
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();

    for (MonthlyUserCount data : monthlyData) {
      String monthName =
          java.time.Month.of(data.month())
              .getDisplayName(TextStyle.SHORT, java.util.Locale.ENGLISH);
      String label = monthName + " " + String.valueOf(data.year()).substring(2);
      dataset.addValue(data.count(), "Users", label);
    }

    JFreeChart chart =
        ChartFactory.createBarChart(
            null, null, null, dataset, PlotOrientation.VERTICAL, false, true, false);

    Color LIGHT_BLUE = new Color(173, 216, 230);

    chart.setBackgroundPaint(TRANSPARENT);
    var plot = chart.getCategoryPlot();
    plot.setBackgroundPaint(TRANSPARENT);
    plot.setOutlineVisible(false);

    var domainAxis = plot.getDomainAxis();
    domainAxis.setTickLabelPaint(TEXT_COLOR);
    domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));

    domainAxis.setCategoryLabelPositions(
        CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 4.0));

    domainAxis.setLowerMargin(0.02);
    domainAxis.setUpperMargin(0.02);
    domainAxis.setCategoryMargin(0.1);

    var rangeAxis = (NumberAxis) plot.getRangeAxis();
    rangeAxis.setStandardTickUnits(org.jfree.chart.axis.NumberAxis.createIntegerTickUnits());
    rangeAxis.setTickLabelPaint(TEXT_COLOR);
    rangeAxis.setLowerBound(0.0);
    rangeAxis.setUpperMargin(0.15);

    var renderer = (BarRenderer) plot.getRenderer();
    renderer.setSeriesPaint(0, LIGHT_BLUE);
    renderer.setBarPainter(new StandardBarPainter());
    renderer.setShadowVisible(false);

    return chartToBytes(chart, 800, 400);
  }

  public byte[] generateProgressBar(int current, int max, Color barColor) {

    if (max <= 0) max = 1;
    final int finalCurrent = Math.max(0, Math.min(current, max));
    final int finalMax = max;

    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    dataset.addValue(finalCurrent, "Progress", "Value");
    dataset.addValue(finalMax - finalCurrent, "Remaining", "Value");

    JFreeChart chart =
        ChartFactory.createStackedBarChart(
            null, null, null, dataset, PlotOrientation.HORIZONTAL, false, false, false);

    chart.setBackgroundPaint(TRANSPARENT);

    CategoryPlot plot = chart.getCategoryPlot();
    plot.setBackgroundPaint(TRANSPARENT);
    plot.setOutlineVisible(false);

    plot.getDomainAxis().setVisible(false);
    plot.getRangeAxis().setVisible(false);
    plot.getRangeAxis().setRange(0, finalMax);

    StackedBarRenderer renderer = new StackedBarRenderer();
    renderer.setBarPainter(new StandardBarPainter());
    renderer.setShadowVisible(false);
    renderer.setDrawBarOutline(false);

    renderer.setSeriesPaint(0, barColor);
    renderer.setSeriesPaint(1, new Color(51, 65, 85, 120));

    renderer.setDefaultItemLabelGenerator(
        new org.jfree.chart.labels.CategoryItemLabelGenerator() {
          @Override
          public String generateLabel(CategoryDataset dataset, int row, int column) {
            if (row == 0) {
              return finalCurrent + " / " + finalMax;
            }
            return null;
          }

          @Override
          public String generateRowLabel(CategoryDataset dataset, int row) {
            return null;
          }

          @Override
          public String generateColumnLabel(CategoryDataset dataset, int column) {
            return null;
          }
        });

    renderer.setDefaultItemLabelsVisible(true);
    renderer.setDefaultPositiveItemLabelPosition(
        new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER));

    renderer.setDefaultItemLabelFont(new Font("SansSerif", Font.BOLD, 14));

    double luminance =
        (0.299 * barColor.getRed() + 0.587 * barColor.getGreen() + 0.114 * barColor.getBlue())
            / 255;
    renderer.setDefaultItemLabelPaint(luminance > 0.6 ? Color.BLACK : Color.WHITE);

    plot.setRenderer(renderer);

    plot.getDomainAxis().setLowerMargin(0);
    plot.getDomainAxis().setUpperMargin(0);
    plot.getRangeAxis().setLowerMargin(0);
    plot.getRangeAxis().setUpperMargin(0);

    return chartToBytes(chart, 600, 70);
  }

  private byte[] chartToBytes(JFreeChart chart, int width, int height) {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      BufferedImage bufferedImage =
          chart.createBufferedImage(width, height, BufferedImage.TYPE_INT_ARGB, null);

      ImageIO.write(bufferedImage, "png", baos);

      return baos.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException("The image couldn't be generated", e);
    }
  }
}
