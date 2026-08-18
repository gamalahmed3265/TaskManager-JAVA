package com.taskmanager.controller;

import com.taskmanager.MainApp;
import com.taskmanager.model.*;
import com.taskmanager.service.*;
import com.taskmanager.util.DateTimeUtils;
import com.taskmanager.util.ExportUtils;
import com.taskmanager.util.IconHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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

    @FXML private HBox kanbanContainer;

    @FXML private Button addTaskButton;
    @FXML private Button smartSuggestButton;
    @FXML private Button autoScheduleButton;
    @FXML private Button focusModeButton;
    @FXML private Button exportButton;
    @FXML private ToggleButton themeToggle;

    @FXML private TextField searchField;
    @FXML private ComboBox<Category> categoryFilter;
    @FXML private ComboBox<com.taskmanager.model.Priority> priorityFilter;
    @FXML private ComboBox<TaskStatus> statusFilter;
    @FXML private ListView<String> frequentlyPostponedList;
    @FXML private Label bestHourLabel;

    @FXML private Label pointsLabel;
    @FXML private Label levelLabel;
    @FXML private Label badgeLabel;
    @FXML private Label completionLabel;

    private final TaskService taskService = new TaskService();
    private final TimeTrackingService timeTrackingService = new TimeTrackingService();
    private final SchedulingService schedulingService = new SchedulingService();
    private final GamificationService gamificationService = new GamificationService();
    private final StatisticsService statisticsService = new StatisticsService();

    private final ObservableList<Task> tasks = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        categoryFilter.setItems(FXCollections.observableArrayList(Category.values()));
        priorityFilter.setItems(FXCollections.observableArrayList(com.taskmanager.model.Priority.values()));
        statusFilter.setItems(FXCollections.observableArrayList(TaskStatus.values()));

        // تعيين أيقونات SVG Vectorية أنيقة لجميع أزرار شريط الأدوات العلوي
        if (addTaskButton != null) {
            addTaskButton.setGraphic(IconHelper.plus(15, "#ffffff"));
            addTaskButton.setContentDisplay(ContentDisplay.RIGHT);
        }
        if (smartSuggestButton != null) {
            smartSuggestButton.setGraphic(IconHelper.sparkles(15, "#4f46e5"));
            smartSuggestButton.setContentDisplay(ContentDisplay.RIGHT);
        }
        if (autoScheduleButton != null) {
            autoScheduleButton.setGraphic(IconHelper.calendar(15, "#4f46e5"));
            autoScheduleButton.setContentDisplay(ContentDisplay.RIGHT);
        }
        if (focusModeButton != null) {
            focusModeButton.setGraphic(IconHelper.target(15, "#4f46e5"));
            focusModeButton.setContentDisplay(ContentDisplay.RIGHT);
        }
        if (exportButton != null) {
            exportButton.setGraphic(IconHelper.export(15, "#4f46e5"));
            exportButton.setContentDisplay(ContentDisplay.RIGHT);
        }
        if (themeToggle != null) {
            themeToggle.setGraphic(IconHelper.moon(15, "#4f46e5"));
            themeToggle.setContentDisplay(ContentDisplay.RIGHT);
        }

        loadTasks();
        refreshStatusBar();
        refreshSidebarInsights();

        categoryFilter.valueProperty().addListener((o, a, b) -> applyFilters());
        priorityFilter.valueProperty().addListener((o, a, b) -> applyFilters());
        statusFilter.valueProperty().addListener((o, a, b) -> applyFilters());
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
        com.taskmanager.model.Priority pr = priorityFilter.getValue();
        TaskStatus st = statusFilter.getValue();

        List<Task> filtered = taskService.getAllTasks().stream()
                .filter(t -> query.isEmpty() || t.getTitle().toLowerCase().contains(query))
                .filter(t -> cat == null || t.getCategory() == cat)
                .filter(t -> pr == null || t.getPriority() == pr)
                .filter(t -> st == null || t.getStatus() == st)
                .toList();
        tasks.setAll(filtered);
        renderKanbanBoard();
    }

    private void renderKanbanBoard() {
        if (kanbanContainer == null) return;
        kanbanContainer.getChildren().clear();

        for (TaskStatus status : TaskStatus.values()) {
            VBox column = new VBox(12);
            column.getStyleClass().add("kanban-column");
            column.setPrefWidth(300);
            column.setMinWidth(280);
            column.setMaxWidth(340);

            List<Task> columnTasks = tasks.stream().filter(t -> t.getStatus() == status).toList();

            // ترويسة العمود
            HBox colHeader = new HBox(8);
            colHeader.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            Label title = new Label(status.getDisplayName());
            title.getStyleClass().add("kanban-column-title");
            
            Label countBadge = new Label(String.valueOf(columnTasks.size()));
            countBadge.setStyle("-fx-background-color: rgba(99, 102, 241, 0.12); -fx-text-fill: #4f46e5; -fx-font-weight: 800; -fx-padding: 3 9 3 9; -fx-background-radius: 12; -fx-font-size: 11px;");
            
            Region headerSpacer = new Region();
            HBox.setHgrow(headerSpacer, javafx.scene.layout.Priority.ALWAYS);
            colHeader.getChildren().addAll(title, headerSpacer, countBadge);
            column.getChildren().add(colHeader);

            // حاوية البطاقات مع ScrollPane عمودي سلس لكل عمود
            ScrollPane colScroll = new ScrollPane();
            colScroll.setFitToWidth(true);
            colScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            colScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            colScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");

            VBox cardsContainer = new VBox(12);
            cardsContainer.setStyle("-fx-background-color: transparent; -fx-padding: 4 2 8 2;");
            colScroll.setContent(cardsContainer);
            VBox.setVgrow(colScroll, javafx.scene.layout.Priority.ALWAYS);

            // Drag & Drop على مستوى العمود
            column.setOnDragOver(e -> {
                if (e.getGestureSource() != column && e.getDragboard().hasString()) {
                    e.acceptTransferModes(TransferMode.MOVE);
                    if (!column.getStyleClass().contains("drag-over")) {
                        column.getStyleClass().add("drag-over");
                    }
                }
                e.consume();
            });

            column.setOnDragExited(e -> {
                column.getStyleClass().remove("drag-over");
                e.consume();
            });

            column.setOnDragDropped(e -> {
                column.getStyleClass().remove("drag-over");
                Dragboard db = e.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    try {
                        long taskId = Long.parseLong(db.getString());
                        Task t = tasks.stream().filter(task -> task.getId() == taskId).findFirst().orElse(null);
                        if (t != null && t.getStatus() != status) {
                            t.setStatus(status);
                            taskService.updateTask(t);
                            loadTasks();
                            refreshStatusBar();
                        }
                        success = true;
                    } catch (Exception ex) {}
                }
                e.setDropCompleted(success);
                e.consume();
            });

            for (Task t : columnTasks) {
                VBox card = new VBox(10);
                card.getStyleClass().add("kanban-card");

                // تأثيرات تفاعلية ناعمة عند تمرير الفأرة
                card.setOnMouseEntered(e -> {
                    card.setScaleX(1.015);
                    card.setScaleY(1.015);
                });
                card.setOnMouseExited(e -> {
                    card.setScaleX(1.0);
                    card.setScaleY(1.0);
                });

                // شريط الشارات (الأولوية، التصنيف، الوقت المقدر)
                HBox badgesRow = new HBox(6);
                badgesRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                Label priorityBadge = new Label(t.getPriority().getDisplayName());
                priorityBadge.setGraphic(IconHelper.flag(11, t.getPriority().getColorHex()));
                priorityBadge.setContentDisplay(ContentDisplay.RIGHT);
                priorityBadge.setStyle("-fx-background-color: " + t.getPriority().getColorHex() + "18; -fx-text-fill: " + t.getPriority().getColorHex() + "; -fx-background-radius: 8; -fx-padding: 3 8 3 8; -fx-font-weight: 800; -fx-font-size: 11px;");

                Label categoryBadge = new Label(t.getCategory().getDisplayName());
                categoryBadge.setGraphic(IconHelper.tag(11, t.getCategory().getColorHex()));
                categoryBadge.setContentDisplay(ContentDisplay.RIGHT);
                categoryBadge.setStyle("-fx-background-color: " + t.getCategory().getColorHex() + "18; -fx-text-fill: " + t.getCategory().getColorHex() + "; -fx-background-radius: 8; -fx-padding: 3 8 3 8; -fx-font-weight: 800; -fx-font-size: 11px;");

                Label estTimeBadge = new Label(t.getEstimatedMinutes() + " د");
                estTimeBadge.setGraphic(IconHelper.clock(11, "#94a3b8"));
                estTimeBadge.setContentDisplay(ContentDisplay.RIGHT);
                estTimeBadge.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-font-weight: 700;");

                Region badgeSpacer = new Region();
                HBox.setHgrow(badgeSpacer, javafx.scene.layout.Priority.ALWAYS);

                badgesRow.getChildren().addAll(priorityBadge, categoryBadge, badgeSpacer, estTimeBadge);

                // عنوان المهمة
                Label cardTitle = new Label(t.getTitle());
                cardTitle.getStyleClass().add("kanban-card-title");
                cardTitle.setWrapText(true);

                // شريط الأزرار والمؤقت
                HBox actionsBox = new HBox(6);
                actionsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                Button editBtn = new Button();
                editBtn.setGraphic(IconHelper.edit(13, "#475569"));
                editBtn.setTooltip(new Tooltip("تعديل"));
                editBtn.getStyleClass().add("icon-button");

                Button delBtn = new Button();
                delBtn.setGraphic(IconHelper.trash(13, "#ef4444"));
                delBtn.setTooltip(new Tooltip("حذف"));
                delBtn.getStyleClass().add("icon-button-danger");

                editBtn.setOnAction(ev -> openTaskForm(t));
                delBtn.setOnAction(ev -> {
                    taskService.deleteTask(t.getId());
                    loadTasks();
                    refreshStatusBar();
                });

                Button startBtn = new Button();
                startBtn.setGraphic(IconHelper.play(11, "#ffffff"));
                startBtn.setTooltip(new Tooltip("بدء المؤقت"));
                startBtn.getStyleClass().addAll("button", "primary-button");

                Button pauseBtn = new Button();
                pauseBtn.setGraphic(IconHelper.pause(11, "#4f46e5"));
                pauseBtn.setTooltip(new Tooltip("إيقاف المؤقت"));
                pauseBtn.getStyleClass().add("icon-button");

                Label timeLabel = new Label("00:00:00");
                timeLabel.getStyleClass().add("status-chip");
                timeLabel.setStyle("-fx-font-size: 11px; -fx-padding: 4 8 4 8; -fx-font-weight: 800;");

                boolean running = timeTrackingService.isRunning(t.getId());
                startBtn.setVisible(!running);
                startBtn.setManaged(!running);
                pauseBtn.setVisible(running);
                pauseBtn.setManaged(running);
                long initialSecs = t.getActualSeconds();
                if (running) initialSecs += timeTrackingService.currentSessionSeconds(t.getId());
                long h = initialSecs / 3600;
                long m = (initialSecs % 3600) / 60;
                long s = initialSecs % 60;
                timeLabel.setText(String.format("%02d:%02d:%02d", h, m, s));

                if (running) {
                    timeLabel.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 8 4 8; -fx-font-weight: 800;");
                    
                    javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                        new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), ev -> {
                            long ts = t.getActualSeconds() + timeTrackingService.currentSessionSeconds(t.getId());
                            long hh = ts / 3600;
                            long mm = (ts % 3600) / 60;
                            long ss = ts % 60;
                            timeLabel.setText(String.format("%02d:%02d:%02d", hh, mm, ss));
                        })
                    );
                    timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
                    timeline.play();
                    
                    timeLabel.sceneProperty().addListener((obs, oldScene, newScene) -> {
                        if (newScene == null) {
                            timeline.stop();
                        }
                    });
                }

                startBtn.setOnAction(ev -> {
                    timeTrackingService.start(t);
                    loadTasks();
                });
                pauseBtn.setOnAction(ev -> {
                    timeTrackingService.pause(t);
                    loadTasks();
                    refreshStatusBar();
                });

                Region actionSpacer = new Region();
                HBox.setHgrow(actionSpacer, javafx.scene.layout.Priority.ALWAYS);
                actionsBox.getChildren().addAll(timeLabel, startBtn, pauseBtn, actionSpacer, editBtn, delBtn);

                card.getChildren().addAll(badgesRow, cardTitle, actionsBox);

                // سحب وإفلات بطاقة المهمة مع لقطة شفافة وحركة ناعمة
                card.setOnDragDetected(e -> {
                    Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent cc = new ClipboardContent();
                    cc.putString(String.valueOf(t.getId()));
                    db.setContent(cc);

                    SnapshotParameters sp = new SnapshotParameters();
                    sp.setFill(Color.TRANSPARENT);
                    WritableImage img = card.snapshot(sp, null);
                    db.setDragView(img);

                    card.getStyleClass().add("is-dragging");
                    e.consume();
                });

                card.setOnDragDone(e -> {
                    card.getStyleClass().remove("is-dragging");
                    e.consume();
                });

                cardsContainer.getChildren().add(card);
            }

            column.getChildren().add(colScroll);
            kanbanContainer.getChildren().add(column);
        }
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
        Task selected = tasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).findFirst().orElse(null);
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "يرجى وضع مهمة في عمود 'قيد التنفيذ' أولًا للتركيز عليها.").showAndWait();
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
        var file = chooser.showSaveDialog(kanbanContainer.getScene().getWindow());
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
        MainApp.applyTheme(kanbanContainer.getScene(), dark);
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

