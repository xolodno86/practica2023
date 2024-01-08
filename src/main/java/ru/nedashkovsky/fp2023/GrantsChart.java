package ru.nedashkovsky.fp2023;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;

public class GrantsChart extends JFrame {

    public GrantsChart(Map<Integer, Double> averageJobsByYear) {
        super("Average Number of Jobs by Fiscal Year");
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<Integer, Double> entry : averageJobsByYear.entrySet()) {
            dataset.addValue(entry.getValue(), "Jobs", String.valueOf(entry.getKey()));
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Average Number of Jobs by Fiscal Year",
                "Year",
                "Average Jobs",
                dataset
        );

        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        setContentPane(chartPanel);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                System.exit(0);
            }
        });
    }
}