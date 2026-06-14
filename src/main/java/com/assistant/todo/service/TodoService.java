package com.assistant.todo.service;

import com.assistant.todo.entity.Task;
import com.assistant.todo.mapper.TaskMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business logic for todo task management module.
 * Contains 8 testable methods (white-box testing targets).
 */
@Service
public class TodoService {

    /** Maximum allowed title length */
    public static final int TITLE_MAX_LENGTH = 100;

    private final TaskMapper taskMapper;

    public TodoService(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    // ==================== Validation Methods ====================

    /**
     * Validate task title: not null, not empty, not whitespace-only, not too long.
     * <p>
     * White-box test points: null, empty string, whitespace-only, exactly 100 chars,
     * 101 chars, normal title.
     * Coverage target: equivalence class partitioning.
     *
     * @param title the title to validate
     * @throws IllegalArgumentException if title is invalid
     */
    public void validateTitle(String title) {
        if (title == null) {
            throw new IllegalArgumentException("任务标题不能为空");
        }
        if (title.isBlank()) {
            throw new IllegalArgumentException("任务标题不能为空或仅包含空格");
        }
        if (title.length() > TITLE_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "任务标题长度不能超过" + TITLE_MAX_LENGTH + "个字符，当前长度: " + title.length());
        }
    }

    // ==================== Business Methods ====================

    /**
     * Add a new task after validation.
     * <p>
     * White-box test points: title non-null, title length upper bound.
     * Coverage target: branch coverage.
     *
     * @param task the task to add
     * @return the added task with generated ID
     */
    public Task addTask(Task task) {
        validateTitle(task.getTitle());
        if (task.getPriority() == null || task.getPriority().isBlank()) {
            task.setPriority("MEDIUM");
        }
        if (task.getStatus() == null || task.getStatus().isBlank()) {
            task.setStatus("PENDING");
        }
        taskMapper.insert(task);
        return task;
    }

    /**
     * Retrieve all tasks.
     * <p>
     * White-box test points: empty list, data present.
     * Coverage target: statement coverage.
     *
     * @return list of all tasks
     */
    public List<Task> getAllTasks() {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Task::getCreateTime);
        return taskMapper.selectList(wrapper);
    }

    /**
     * Filter tasks by status.
     * <p>
     * White-box test points: PENDING, DONE, invalid status value.
     * Coverage target: branch coverage.
     *
     * @param status PENDING or DONE
     * @return filtered tasks
     */
    public List<Task> getTasksByStatus(String status) {
        if (status == null || status.isBlank()) {
            return getAllTasks();
        }
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getStatus, status)
               .orderByDesc(Task::getCreateTime);
        return taskMapper.selectList(wrapper);
    }

    /**
     * Get all tasks sorted by priority: HIGH first, then MEDIUM, then LOW.
     * <p>
     * White-box test points: mixed priorities sorted correctly.
     * Coverage target: logical verification.
     *
     * @return tasks sorted by priority
     */
    public List<Task> getTasksSortedByPriority() {
        return taskMapper.findAllSortedByPriority();
    }

    /**
     * Mark a task as DONE.
     * <p>
     * White-box test points: exists and PENDING, exists but already DONE,
     * does not exist.
     * Coverage target: branch coverage.
     *
     * @param id the task ID to mark as done
     * @return the updated task
     * @throws IllegalArgumentException if task not found
     */
    public Task markDone(Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: id=" + id);
        }
        if ("DONE".equals(task.getStatus())) {
            throw new IllegalArgumentException("任务已完成，无需重复标记");
        }
        task.setStatus("DONE");
        taskMapper.updateById(task);
        return task;
    }

    /**
     * Update task fields. Only non-null fields are applied (partial update).
     * <p>
     * White-box test points: partial field update, id not found.
     * Coverage target: branch coverage.
     *
     * @param id      the task ID to update
     * @param updates the fields to update (null fields are ignored)
     * @return the updated task
     * @throws IllegalArgumentException if task not found
     */
    public Task updateTask(Long id, Task updates) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: id=" + id);
        }
        if (updates.getTitle() != null) {
            validateTitle(updates.getTitle());
            task.setTitle(updates.getTitle());
        }
        if (updates.getPriority() != null) {
            task.setPriority(updates.getPriority());
        }
        if (updates.getStatus() != null) {
            task.setStatus(updates.getStatus());
        }
        if (updates.getDeadline() != null) {
            task.setDeadline(updates.getDeadline());
        }
        taskMapper.updateById(task);
        return task;
    }

    /**
     * Delete a task by ID.
     * <p>
     * White-box test points: delete existing, delete non-existent.
     * Coverage target: branch coverage.
     *
     * @param id the task ID to delete
     * @return true if deleted, false if not found
     */
    public boolean deleteTask(Long id) {
        if (id == null) {
            return false;
        }
        return taskMapper.deleteById(id) > 0;
    }
}
