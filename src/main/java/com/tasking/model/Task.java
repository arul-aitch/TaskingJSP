package com.tasking.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Task implements Serializable {
    private Long id;
    private Long userId;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String description;
    private LocalDate deadline;
    private boolean completed;
    private boolean pinned;
    private List<Subtask> subtasks;
    private int subtaskTotal;
    private int subtaskCompleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }
    public List<Subtask> getSubtasks() { return subtasks; }
    public void setSubtasks(List<Subtask> subtasks) { this.subtasks = subtasks; }
    public int getSubtaskTotal() { return subtaskTotal; }
    public void setSubtaskTotal(int subtaskTotal) { this.subtaskTotal = subtaskTotal; }
    public int getSubtaskCompleted() { return subtaskCompleted; }
    public void setSubtaskCompleted(int subtaskCompleted) { this.subtaskCompleted = subtaskCompleted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }

    /**
     * Deadline indicator, matching the original Laravel Blade logic:
     * - "overdue": deadline passed and task not completed
     * - "urgent":  deadline within the next two days (inclusive) and not completed
     * - "normal":  everything else / no deadline
     */
    public String getDeadlineStatus() {
        if (deadline == null || completed) {
            return "normal";
        }
        LocalDate today = LocalDate.now();
        if (deadline.isBefore(today)) {
            return "overdue";
        }
        if (!deadline.isAfter(today.plusDays(2))) {
            return "urgent";
        }
        return "normal";
    }

    /** Presentation string, matching the original Blade date formatting. */
    public String getDeadlineLabel() {
        if (deadline == null) return null;
        return deadline.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }

    public int getProgressPercent() {
        if (subtaskTotal == 0) return 0;
        return Math.round((subtaskCompleted * 100f) / subtaskTotal);
    }

    /** Description made safe for embedding in a single-line JSON data attribute. */
    public String getDescriptionForJs() {
        if (description == null) return "";
        return description.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", " ").replace("\n", "\\n");
    }
}