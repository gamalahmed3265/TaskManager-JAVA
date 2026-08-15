package com.taskmanager.controller;

import com.taskmanager.MainApp;
import com.taskmanager.model.*;
import com.taskmanager.service.*;
import com.taskmanager.util.DateTimeUtils;
import com.taskmanager.util.ExportUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class MainController {

    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, String> timeColumn;
    @FXML private TableColumn<Task, String> titleColumn;
    @FXML private TableColumn<Task, Category> categoryColumn;
    @FXML private TableColumn<Task, Priority> priorityColumn;
    @FXML private TableColumn<Task, Number> estimatedColumn;
    @FXML private TableColumn<Task, Number> actualColumn;
    @FXML private TableColumn<Task, TaskStatus> statusColumn;
    @FXML private TableColumn<Task, Void> timerColumn;
    @FXML private TableColumn<Task, Void> actionsColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<Category> categoryFilter;
    @FXML private ComboBox<Priority> priorityFilter;
    @FXML private ComboBox<TaskStatus> statusFilter;
    @FXML private ListView<String> frequentlyPostponedList;
    @FXML private Label bestHourLabel;

    @FXML private Label pointsLabel;
    @FXML private Label levelLabel;
    @FXML private Label badgeLabel;
    @FXML private Label completionLabel;
    @FXML private ToggleButton themeToggle;

    private final TaskService taskService = new TaskService();
    private final TimeTrackingService timeTrackingService = new TimeTrackingService();
    private final SchedulingService schedulingService = new SchedulingService();
    private final GamificationService gamificationService = new GamificationService();
    private final StatisticsService statisticsService = new StatisticsService();

    private final ObservableList<Task> tasks = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        categoryFilter.setItems(FXCollections.observableArrayList(Category.values()));
        priorityFilter.setItems(FXCollections.observableArrayList(Priority.values()));
        statusFilter.setItems(FXCollections.observableArrayList(TaskStatus.values()));

        setupTable();
        loadTasks();
        refreshStatusBar();
        refreshSidebarInsights();
        enableDragAndDrop();

        categoryFilter.valueProperty().addListener((o, a, b) -> applyFilters());
        priorityFilter.valueProperty().addListener((o, a, b) -> applyFilters());
        statusFilter.valueProperty().addListener((o, a, b) -> applyFilters());
    }

    private void setupTable() {
        timeColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                DateTimeUtils.formatTime(data.getValue().getScheduledStart())));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        estimatedColumn.setCellValueFactory(data -> data.getValue().estimatedMinutesProperty());
        actualColumn.setCellValueFactory(data -> data.getValue().actualMinutesProperty());
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        priorityColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Priority item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else {
                    setText(item.getDisplayName());
                    setStyle("-fx-text-fill: " + item.getColorHex() + "; -fx-font-weight: bold;");
                }
            }
        });

        timerColumn.setCellFactory(col -> new TableCell<>() {
            private final Button startBtn = new Button("▶ بدء");
            private final Button pauseBtn = new Button("⏸ إيقاف");
            private final HBox box = new HBox(6, startBtn, pauseBtn);
            {
                startBtn.setOnAction(e -> {
                    Task t = getTableView().getItems().get(getIndex());
                    timeTrackingService.start(t);
                    taskTable.refresh();
                });
                pauseBtn.setOnAction(e -> {
                    Task t = getTableView().getItems().get(getIndex());
                    timeTrackingService.pause(t);
                    taskTable.refresh();
                    refreshStatusBar();
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("✏");
            private final Button doneBtn = new Button("✅");
            private final Button delBtn = new Button("🗑");
            private final HBox box = new HBox(6, editBtn, doneBtn, delBtn);
            {
                editBtn.setOnAction(e -> openTaskForm(getTableView().getItems().get(getIndex())));
                doneBtn.setOnAction(e -> {
                    Task t = getTableView().getItems().get(getIndex());
                    taskService.completeTask(t);
                    loadTasks();
                    refreshStatusBar();
                });
                delBtn.setOnAction(e -> {
                    Task t = getTableView().getItems().get(getIndex());
                    taskService.deleteTask(t.getId());
                    loadTasks();
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        taskTable.setItems(tasks);
    }

    /** سحب وإفلات لإعادة ترتيب المهام يدويًا داخل جدول اليوم */
    private void enableDragAndDrop() {
        taskTable.setRowFactory(tv -> {
            TableRow<Task> row = new TableRow<>();
            row.setOnDragDetected(event -> {
                if (row.isEmpty()) return;
                Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent cc = new ClipboardContent();
                cc.putString(String.valueOf(row.getIndex()));
                db.setContent(cc);
                event.consume();
            });
            row.setOnDragOver(event -> {
                if (event.getGestureSource() != row && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });
            row.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasString()) {
                    int draggedIndex = Integer.parseInt(db.getString());
                    int dropIndex = row.isEmpty() ? tasks.size() - 1 : row.getIndex();
                    swapScheduledTimes(draggedIndex, dropIndex);
                    event.setDropCompleted(true);
                }
                event.consume();
            });
            return row;
        });
    }

    private void swapScheduledTimes(int i, int j) {
        if (i == j || i < 0 || j < 0 || i >= tasks.size() || j >= tasks.size()) return;
        Task a = tasks.get(i);
        Task b = tasks.get(j);
        var tmp = a.getScheduledStart();
        a.setScheduledStart(b.getScheduledStart());
        b.setScheduledStart(tmp);
        taskService.updateTask(a);
        taskService.updateTask(b);
        loadTasks();
    }

    private void loadTasks() {
        List<Task> all = taskService.getAllTasks();
        taskService.refreshOverdueStatuses(all);
        tasks.setAll(all);
        applyFilters();
        refreshSidebarInsights();
    }

    private void applyFilters() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        Category cat = categoryFilter.getValue();
        Priority pr = priorityFilter.getValue();
        TaskStatus st = statusFilter.getValue();

        List<Task> filtered = taskService.getAllTasks().stream()
                .filter(t -> query.isEmpty() || t.getTitle().toLowerCase().contains(query))
                .filter(t -> cat == null || t.getCategory() == cat)
                .filter(t -> pr == null || t.getPriority() == pr)
                .filter(t -> st == null || t.getStatus() == st)
                .toList();
        tasks.setAll(filtered);
    }

    private void refreshSidebarInsights() {
        List<Task> postponed = taskService.getFrequentlyPostponedTasks(taskService.getAllTasks());
        frequentlyPostponedList.setItems(FXCollections.observableArrayList(
                postponed.stream().map(t -> t.getTitle() + " (أُجّلت " + t.getPostponeCount() + " مرات)").toList()));

        statisticsService.bestProductivityHour().ifPresentOrElse(
                hour -> bestHourLabel.setText("أفضل وقت لإنتاجيتك: الساعة " + hour + ":00"),
                () -> bestHourLabel.setText("أفضل وقت لإنتاجيتك: لا توجد بيانات كافية بعد")
        );
    }

    private void refreshStatusBar() {
        GamificationService.GamificationStatus status = gamificationService.getStatus();
        pointsLabel.setText("النقاط: " + status.totalPoints);
        levelLabel.setText("المستوى: " + status.level);
        badgeLabel.setText(gamificationService.getBadgeForPoints(status.totalPoints));

        List<Task> today = taskService.getTasksForDate(LocalDate.now());
        double rate = statisticsService.completionRate(today);
        completionLabel.setText(String.format("نسبة إنجاز اليوم: %.0f%%", rate));
    }

    @FXML
    private void handleSearch() { applyFilters(); }

    @FXML
    private void handleAddTask() { openTaskForm(null); }

    private void openTaskForm(Task existing) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/task_form.fxml"));
            Parent root = loader.load();
            TaskFormController controller = loader.getController();
            controller.setTask(existing);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(existing == null ? "مهمة جديدة" : "تعديل المهمة");
            Scene scene = new Scene(root);
            MainApp.applyTheme(scene, MainApp.isDarkMode());
            dialog.setScene(scene);

            controller.setOnSaved(task -> {
                if (existing == null) {
                    taskService.createTask(task);
                } else {
                    taskService.updateTask(task);
                }
                loadTasks();
                dialog.close();
            });
            controller.setOnCancelled(dialog::close);

            dialog.showAndWait();
        } catch (IOException e) {
            showError("تعذّر فتح نموذج المهمة", e.getMessage());
        }
    }

    @FXML
    private void handleSmartSuggest() {
        TextInputDialog durationDialog = new TextInputDialog("30");
        durationDialog.setHeaderText("اقتراح ذكي لأفضل وقت متاح");
        durationDialog.setContentText("مدة المهمة بالدقائق:");
        Optional<String> result = durationDialog.showAndWait();
        result.ifPresent(value -> {
            try {
                int minutes = Integer.parseInt(value.trim());
                SchedulingService.SchedulingOptions options = new SchedulingService.SchedulingOptions();
                var suggestion = schedulingService.suggestBestSlot(
                        tasks, LocalDate.now(), minutes,
                        new SchedulingService.PeakHours(LocalTime.of(9, 0), LocalTime.of(12, 0)),
                        options);
                if (suggestion == null) {
                    new Alert(Alert.AlertType.INFORMATION, "لا يوجد وقت فراغ كافٍ اليوم لهذه المهمة.").showAndWait();
                } else {
                    new Alert(Alert.AlertType.INFORMATION,
                            "أفضل وقت متاح مقترح: " + DateTimeUtils.formatDateTime(suggestion)).showAndWait();
                }
            } catch (NumberFormatException ex) {
                showError("قيمة غير صحيحة", "يرجى إدخال عدد صحيح من الدقائق");
            }
        });
    }

    @FXML
    private void handleAutoSchedule() {
        SchedulingService.SchedulingOptions options = new SchedulingService.SchedulingOptions();
        options.usePomodoro = false;
        var scheduled = schedulingService.autoSchedule(
                tasks, LocalDate.now(),
                new SchedulingService.PeakHours(LocalTime.of(9, 0), LocalTime.of(12, 0)),
                options);
        scheduled.forEach(taskService::updateTask);
        loadTasks();
    }

    @FXML
    private void handleFocusMode() {
        Task selected = taskTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "يرجى اختيار مهمة أولًا من الجدول.").showAndWait();
            return;
        }
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/focus_mode.fxml"));
            Parent root = loader.load();
            FocusModeController controller = loader.getController();
            controller.setTask(selected, timeTrackingService, () -> {
                loadTasks();
                refreshStatusBar();
            });

            Stage focusStage = new Stage();
            focusStage.setTitle("وضع التركيز");
            Scene scene = new Scene(root, 600, 400);
            MainApp.applyTheme(scene, MainApp.isDarkMode());
            focusStage.setScene(scene);
            focusStage.setFullScreen(true);
            focusStage.setFullScreenExitHint("اضغط ESC أو زر الخروج لإنهاء وضع التركيز");
            focusStage.show();
        } catch (IOException e) {
            showError("تعذّر فتح وضع التركيز", e.getMessage());
        }
    }

    @FXML
    private void handleExport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("تصدير تقرير الإنتاجية");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV", "*.csv"),
                new FileChooser.ExtensionFilter("Excel", "*.xlsx"),
                new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        var file = chooser.showSaveDialog(taskTable.getScene().getWindow());
        if (file == null) return;

        try {
            Path path = file.toPath();
            String name = file.getName().toLowerCase();
            List<Task> all = taskService.getAllTasks();
            if (name.endsWith(".xlsx")) {
                ExportUtils.exportToExcel(all, path);
            } else if (name.endsWith(".pdf")) {
                ExportUtils.exportToPdf(all, path, "تقرير الإنتاجية");
            } else {
                ExportUtils.exportToCsv(all, path);
            }
            new Alert(Alert.AlertType.INFORMATION, "تم تصدير التقرير بنجاح.").showAndWait();
        } catch (IOException e) {
            showError("فشل تصدير التقرير", e.getMessage());
        }
    }

    @FXML
    private void handleThemeToggle() {
        boolean dark = themeToggle.isSelected();
        MainApp.applyTheme(taskTable.getScene(), dark);
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
