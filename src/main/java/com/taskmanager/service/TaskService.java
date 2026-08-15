package com.taskmanager.service;

import com.taskmanager.database.TaskDAO;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * يوفر عمليات إدارة المهام (إضافة/تعديل/حذف/تأجيل/إكمال) فوق طبقة DAO،
 * ويطبّق قواعد العمل مثل تحديث الحالة تلقائيًا ومنح النقاط عند الإنجاز.
 */
public class TaskService {

    private final TaskDAO taskDAO = new TaskDAO();
    private final GamificationService gamificationService = new GamificationService();

    public Task createTask(Task task) {
        task.setCreatedAt(LocalDateTime.now());
        task.setStatus(TaskStatus.PENDING);
        return taskDAO.insert(task);
    }

    public void updateTask(Task task) {
        taskDAO.update(task);
    }

    public void deleteTask(int taskId) {
        taskDAO.delete(taskId);
    }

    public List<Task> getAllTasks() {
        return taskDAO.findAll();
    }

    public List<Task> getTasksForDate(LocalDate date) {
        return taskDAO.findByDate(date);
    }

    /** تعليم المهمة كمكتملة، وحساب النقاط بناءً على الالتزام بالموعد والوقت المقدّر */
    public void completeTask(Task task) {
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        int earnedPoints = gamificationService.calculatePointsForCompletion(task);
        task.setPoints(task.getPoints() + earnedPoints);
        taskDAO.update(task);
        gamificationService.addPoints(earnedPoints);
    }

    /** تأجيل مهمة إلى موعد جديد، مع زيادة عدّاد التأجيل لتتبع "المهام المؤجلة كثيرًا" */
    public void postponeTask(Task task, LocalDateTime newScheduledStart) {
        task.setScheduledStart(newScheduledStart);
        task.setPostponeCount(task.getPostponeCount() + 1);
        task.setStatus(TaskStatus.POSTPONED);
        taskDAO.update(task);
    }

    /** يفحص المهام المتأخرة (تجاوزت الموعد النهائي دون إنجاز) ويحدّث حالتها */
    public void refreshOverdueStatuses(List<Task> tasks) {
        for (Task t : tasks) {
            if (t.isOverdue() && t.getStatus() != TaskStatus.OVERDUE) {
                t.setStatus(TaskStatus.OVERDUE);
                taskDAO.update(t);
            }
        }
    }

    /** المهام التي أُجّلت أكثر من مرة واحدة - لتنبيه المستخدم */
    public List<Task> getFrequentlyPostponedTasks(List<Task> tasks) {
        return tasks.stream().filter(t -> t.getPostponeCount() >= 2).toList();
    }
}
