package com.taskmanager.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

/**
 * نموذج المهمة - يمثل صف في جدول tasks بقاعدة البيانات
 * يستخدم JavaFX Properties لدعم الربط التلقائي (Data Binding) مع الواجهة
 */
public class Task {

    private final IntegerProperty id = new SimpleIntegerProperty(this, "id", -1);
    private final StringProperty title = new SimpleStringProperty(this, "title", "");
    private final StringProperty description = new SimpleStringProperty(this, "description", "");
    private final ObjectProperty<Priority> priority = new SimpleObjectProperty<>(this, "priority", Priority.MEDIUM);
    private final ObjectProperty<Category> category = new SimpleObjectProperty<>(this, "category", Category.OTHER);
    private final ObjectProperty<LocalDateTime> deadline = new SimpleObjectProperty<>(this, "deadline");
    private final ObjectProperty<LocalDateTime> scheduledStart = new SimpleObjectProperty<>(this, "scheduledStart");
    private final IntegerProperty estimatedMinutes = new SimpleIntegerProperty(this, "estimatedMinutes", 30);
    private final IntegerProperty actualSeconds = new SimpleIntegerProperty(this, "actualSeconds", 0);
    private final ObjectProperty<TaskStatus> status = new SimpleObjectProperty<>(this, "status", TaskStatus.PENDING);
    private final IntegerProperty postponeCount = new SimpleIntegerProperty(this, "postponeCount", 0);
    private final IntegerProperty points = new SimpleIntegerProperty(this, "points", 0);
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>(this, "createdAt", LocalDateTime.now());
    private final ObjectProperty<LocalDateTime> completedAt = new SimpleObjectProperty<>(this, "completedAt");

    public Task() {}

    public Task(String title, String description, Priority priority, Category category,
                LocalDateTime deadline, int estimatedMinutes) {
        setTitle(title);
        setDescription(description);
        setPriority(priority);
        setCategory(category);
        setDeadline(deadline);
        setEstimatedMinutes(estimatedMinutes);
    }

    // id
    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    // title
    public String getTitle() { return title.get(); }
    public void setTitle(String value) { title.set(value); }
    public StringProperty titleProperty() { return title; }

    // description
    public String getDescription() { return description.get(); }
    public void setDescription(String value) { description.set(value); }
    public StringProperty descriptionProperty() { return description; }

    // priority
    public Priority getPriority() { return priority.get(); }
    public void setPriority(Priority value) { priority.set(value); }
    public ObjectProperty<Priority> priorityProperty() { return priority; }

    // category
    public Category getCategory() { return category.get(); }
    public void setCategory(Category value) { category.set(value); }
    public ObjectProperty<Category> categoryProperty() { return category; }

    // deadline
    public LocalDateTime getDeadline() { return deadline.get(); }
    public void setDeadline(LocalDateTime value) { deadline.set(value); }
    public ObjectProperty<LocalDateTime> deadlineProperty() { return deadline; }

    // scheduledStart
    public LocalDateTime getScheduledStart() { return scheduledStart.get(); }
    public void setScheduledStart(LocalDateTime value) { scheduledStart.set(value); }
    public ObjectProperty<LocalDateTime> scheduledStartProperty() { return scheduledStart; }

    // estimatedMinutes
    public int getEstimatedMinutes() { return estimatedMinutes.get(); }
    public void setEstimatedMinutes(int value) { estimatedMinutes.set(value); }
    public IntegerProperty estimatedMinutesProperty() { return estimatedMinutes; }

    // actualSeconds
    public int getActualSeconds() { return actualSeconds.get(); }
    public void setActualSeconds(int value) { actualSeconds.set(value); }
    public IntegerProperty actualSecondsProperty() { return actualSeconds; }

    // status
    public TaskStatus getStatus() { return status.get(); }
    public void setStatus(TaskStatus value) { status.set(value); }
    public ObjectProperty<TaskStatus> statusProperty() { return status; }

    // postponeCount
    public int getPostponeCount() { return postponeCount.get(); }
    public void setPostponeCount(int value) { postponeCount.set(value); }
    public IntegerProperty postponeCountProperty() { return postponeCount; }

    // points
    public int getPoints() { return points.get(); }
    public void setPoints(int value) { points.set(value); }
    public IntegerProperty pointsProperty() { return points; }

    // createdAt
    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime value) { createdAt.set(value); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }

    // completedAt
    public LocalDateTime getCompletedAt() { return completedAt.get(); }
    public void setCompletedAt(LocalDateTime value) { completedAt.set(value); }
    public ObjectProperty<LocalDateTime> completedAtProperty() { return completedAt; }

    public double getTimeAccuracyRatio() {
        if (getEstimatedMinutes() <= 0) return 0;
        return (double) getActualSeconds() / (getEstimatedMinutes() * 60.0);
    }

    public boolean isOverdue() {
        return getDeadline() != null
                && getStatus() != TaskStatus.COMPLETED
                && getDeadline().isBefore(LocalDateTime.now());
    }

    @Override
    public String toString() {
        return getTitle();
    }
}

