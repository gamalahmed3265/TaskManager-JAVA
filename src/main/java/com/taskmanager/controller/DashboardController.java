package com.taskmanager.controller;

import com.taskmanager.model.Category;
import com.taskmanager.model.Task;
import com.taskmanager.service.StatisticsService;
import com.taskmanager.service.TaskService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class DashboardController {

    @FXML private Label todayCompletionLabel;
    @FXML private Label weekCompletionLabel;
    @FXML private Label wastedTimeLabel;
    @FXML private Label topCategoryLabel;
    @FXML private PieChart categoryPieChart;
    @FXML private BarChart<String, Number> productivityTrendChart;
    @FXML private BarChart<String, Number> productiveHoursChart;
    @FXML private ListView<String> dailyTimelineList;

    private final TaskService taskService = new TaskService();
    private final StatisticsService statisticsService = new StatisticsService();

    @FXML
    public void initialize() {
        refreshStatistics();
    }

    @FXML
    private void refreshStatistics() {
        List<Task> allTasks = taskService.getAllTasks();

        List<Task> today = taskService.getTasksForDate(LocalDate.now());
        todayCompletionLabel.setText(String.format("%.0f%%", statisticsService.completionRate(today)));

        List<Task> weekTasks = allTasks.stream()
                .filter(t -> t.getCreatedAt() != null &&
                        t.getCreatedAt().toLocalDate().isAfter(LocalDate.now().minusDays(7)))
                .toList();
        weekCompletionLabel.setText(String.format("%.0f%%", statisticsService.completionRate(weekTasks)));

        wastedTimeLabel.setText(statisticsService.wastedMinutes(allTasks) + " د");

        Map<Category, Integer> byCategory = statisticsService.timeByCategory(allTasks);
        categoryPieChart.setData(FXCollections.observableArrayList(
                byCategory.entrySet().stream()
                        .filter(e -> e.getValue() > 0)
                        .map(e -> new PieChart.Data(e.getKey().getDisplayName(), e.getValue()))
                        .toList()
        ));

        byCategory.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresentOrElse(
                        e -> topCategoryLabel.setText(e.getKey().getDisplayName()),
                        () -> topCategoryLabel.setText("--"));

        // اتجاه الإنتاجية لآخر 7 أيام
        Map<LocalDate, Double> trend = statisticsService.dailyCompletionTrend(allTasks, 7);
        XYChart.Series<String, Number> trendSeries = new XYChart.Series<>();
        trendSeries.setName("نسبة الإنجاز");
        trend.forEach((day, rate) -> trendSeries.getData().add(
                new XYChart.Data<>(day.getDayOfMonth() + "/" + day.getMonthValue(), rate)));
        productivityTrendChart.getData().setAll(trendSeries);

        // أكثر ساعات اليوم إنتاجية
        Map<Integer, Long> hours = statisticsService.mostProductiveHours();
        XYChart.Series<String, Number> hourSeries = new XYChart.Series<>();
        hourSeries.setName("عدد الجلسات");
        hours.forEach((hour, count) -> hourSeries.getData().add(
                new XYChart.Data<>(hour + ":00", count)));
        productiveHoursChart.getData().setAll(hourSeries);

        // الجدول الزمني لليوم
        if (dailyTimelineList != null) {
            List<String> timeline = statisticsService.getDailyTimelineStrings(allTasks);
            dailyTimelineList.setItems(FXCollections.observableArrayList(timeline));
        }
    }
}
