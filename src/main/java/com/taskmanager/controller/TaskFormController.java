package com.taskmanager.controller;

import com.taskmanager.model.Category;
import com.taskmanager.model.Priority;
import com.taskmanager.model.Task;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.function.Consumer;

public class TaskFormController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<Priority> priorityCombo;
    @FXML private ComboBox<Category> categoryCombo;
    @FXML private DatePicker deadlineDatePicker;
    @FXML private TextField deadlineTimeField;
    @FXML private Spinner<Integer> estimatedMinutesSpinner;
    @FXML private Label errorLabel;

    private Task task;
    private Consumer<Task> onSaved;
    private Runnable onCancelled;

    @FXML
    public void initialize() {
        priorityCombo.setItems(FXCollections.observableArrayList(Priority.values()));
        categoryCombo.setItems(FXCollections.observableArrayList(Category.values()));
        estimatedMinutesSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 600, 30, 5));
    }

    public void setTask(Task existing) {
        this.task = existing != null ? existing : new Task();
        titleField.setText(task.getTitle());
        descriptionField.setText(task.getDescription());
        priorityCombo.setValue(task.getPriority() != null ? task.getPriority() : Priority.MEDIUM);
        categoryCombo.setValue(task.getCategory() != null ? task.getCategory() : Category.OTHER);
        estimatedMinutesSpinner.getValueFactory().setValue(
                task.getEstimatedMinutes() > 0 ? task.getEstimatedMinutes() : 30);
        if (task.getDeadline() != null) {
            deadlineDatePicker.setValue(task.getDeadline().toLocalDate());
            deadlineTimeField.setText(task.getDeadline().toLocalTime().toString().substring(0, 5));
        }
    }

    public void setOnSaved(Consumer<Task> onSaved) { this.onSaved = onSaved; }
    public void setOnCancelled(Runnable onCancelled) { this.onCancelled = onCancelled; }

    @FXML
    private void handleSave() {
        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        if (title.isEmpty()) {
            showError("يرجى إدخال عنوان للمهمة");
            return;
        }
        if (priorityCombo.getValue() == null || categoryCombo.getValue() == null) {
            showError("يرجى اختيار الأولوية والتصنيف");
            return;
        }

        LocalDateTime deadline = null;
        if (deadlineDatePicker.getValue() != null) {
            LocalDate date = deadlineDatePicker.getValue();
            LocalTime time = LocalTime.of(23, 59);
            String timeText = deadlineTimeField.getText();
            if (timeText != null && !timeText.isBlank()) {
                try {
                    time = LocalTime.parse(timeText.trim());
                } catch (DateTimeParseException e) {
                    showError("صيغة الوقت غير صحيحة، استخدم HH:mm مثل 18:30");
                    return;
                }
            }
            deadline = LocalDateTime.of(date, time);
        }

        task.setTitle(title);
        task.setDescription(descriptionField.getText());
        task.setPriority(priorityCombo.getValue());
        task.setCategory(categoryCombo.getValue());
        task.setDeadline(deadline);
        task.setEstimatedMinutes(estimatedMinutesSpinner.getValue());

        if (onSaved != null) onSaved.accept(task);
    }

    @FXML
    private void handleCancel() {
        if (onCancelled != null) onCancelled.run();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
