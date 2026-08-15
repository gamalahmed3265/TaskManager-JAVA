package com.taskmanager.controller;

import com.taskmanager.model.Task;
import com.taskmanager.service.TaskService;
import com.taskmanager.service.TimeTrackingService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;

public class FocusModeController {

    @FXML private Label taskTitleLabel;
    @FXML private Label timerLabel;
    @FXML private Label progressLabel;
    @FXML private Button pauseResumeButton;

    private final TaskService taskService = new TaskService();
    private TimeTrackingService timeTrackingService;
    private Task task;
    private Runnable onExit;
    private Timeline ticker;
    private long elapsedSecondsAtStart;

    public void setTask(Task task, TimeTrackingService timeTrackingService, Runnable onExit) {
        this.task = task;
        this.timeTrackingService = timeTrackingService;
        this.onExit = onExit;
        taskTitleLabel.setText(task.getTitle());
        elapsedSecondsAtStart = task.getActualMinutes() * 60L;

        if (!timeTrackingService.isRunning(task.getId())) {
            timeTrackingService.start(task);
        }
        startTicker();
    }

    private void startTicker() {
        ticker = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateDisplay()));
        ticker.setCycleCount(Timeline.INDEFINITE);
        ticker.play();
        updateDisplay();
    }

    private void updateDisplay() {
        long sessionSeconds = timeTrackingService.currentSessionMinutes(task.getId()) * 60L;
        long totalSeconds = elapsedSecondsAtStart + sessionSeconds;
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d:%02d", h, m, s));
        progressLabel.setText((totalSeconds / 60) + " / " + task.getEstimatedMinutes() + " دقيقة");
    }

    @FXML
    private void handlePauseResume() {
        if (timeTrackingService.isRunning(task.getId())) {
            timeTrackingService.pause(task);
            pauseResumeButton.setText("▶ استئناف");
        } else {
            timeTrackingService.resume(task);
            pauseResumeButton.setText("⏸ إيقاف مؤقت");
        }
    }

    @FXML
    private void handleComplete() {
        if (timeTrackingService.isRunning(task.getId())) {
            timeTrackingService.pause(task);
        }
        taskService.completeTask(task);
        exit();
    }

    @FXML
    private void handleExit() {
        if (timeTrackingService.isRunning(task.getId())) {
            timeTrackingService.pause(task);
        }
        exit();
    }

    private void exit() {
        if (ticker != null) ticker.stop();
        if (onExit != null) onExit.run();
        Stage stage = (Stage) taskTitleLabel.getScene().getWindow();
        stage.close();
    }
}
