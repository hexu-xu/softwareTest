package com.assistant.todo.controller;

import com.assistant.common.Result;
import com.assistant.todo.entity.Task;
import com.assistant.todo.service.TodoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for the Todo module.
 * Provides page endpoints (Thymeleaf HTML) and REST API endpoints (JSON).
 */
@Controller
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    // ==================== Page Endpoints ====================

    /**
     * GET /todo — render the todo main page with task list.
     */
    @GetMapping("/todo")
    public String index(Model model) {
        List<Task> tasks = todoService.getTasksSortedByPriority();
        model.addAttribute("tasks", tasks);
        return "todo";
    }

    /**
     * POST /todo/add — form submission to add a new task.
     */
    @PostMapping("/todo/add")
    public String addTask(@RequestParam String title,
                          @RequestParam(required = false, defaultValue = "MEDIUM") String priority,
                          @RequestParam(required = false) LocalDate deadline) {
        Task task = new Task();
        task.setTitle(title);
        task.setPriority(priority);
        task.setStatus("PENDING");
        task.setDeadline(deadline);
        todoService.addTask(task);
        return "redirect:/todo";
    }

    /**
     * GET /todo/done/{id} — mark a task as done.
     */
    @GetMapping("/todo/done/{id}")
    public String markDone(@PathVariable Long id) {
        todoService.markDone(id);
        return "redirect:/todo";
    }

    /**
     * GET /todo/delete/{id} — delete a task.
     */
    @GetMapping("/todo/delete/{id}")
    public String deleteTask(@PathVariable Long id) {
        todoService.deleteTask(id);
        return "redirect:/todo";
    }

    /**
     * POST /todo/edit — form submission to update a task.
     */
    @PostMapping("/todo/edit")
    public String editTask(@RequestParam Long id,
                           @RequestParam String title,
                           @RequestParam String priority,
                           @RequestParam(required = false) LocalDate deadline,
                           @RequestParam(required = false) String status) {
        Task updates = new Task();
        updates.setTitle(title);
        updates.setPriority(priority);
        updates.setStatus(status);
        updates.setDeadline(deadline);
        todoService.updateTask(id, updates);
        return "redirect:/todo";
    }

    // ==================== REST API Endpoints ====================

    /**
     * GET /api/todos — list all tasks, with optional sort and status filter (JSON).
     */
    @GetMapping("/api/todos")
    @ResponseBody
    public Result<List<Task>> apiGetAll(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String status) {
        List<Task> tasks;
        if (status != null && !status.isBlank()) {
            tasks = todoService.getTasksByStatus(status);
        } else if ("priority".equals(sort)) {
            tasks = todoService.getTasksSortedByPriority();
        } else {
            tasks = todoService.getAllTasks();
        }
        return Result.success(tasks);
    }

    /**
     * POST /api/todos — create a new task (JSON).
     */
    @PostMapping("/api/todos")
    @ResponseBody
    public Result<Task> apiAdd(@RequestBody Task task) {
        try {
            Task created = todoService.addTask(task);
            return Result.success("创建成功", created);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    /**
     * PUT /api/todos/{id}/done — mark a task as done (JSON).
     */
    @PutMapping("/api/todos/{id}/done")
    @ResponseBody
    public Result<Task> apiMarkDone(@PathVariable Long id) {
        try {
            Task updated = todoService.markDone(id);
            return Result.success("任务已标记为完成", updated);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    /**
     * PUT /api/todos/{id} — update a task (JSON).
     */
    @PutMapping("/api/todos/{id}")
    @ResponseBody
    public Result<Task> apiUpdate(@PathVariable Long id, @RequestBody Task updates) {
        try {
            Task updated = todoService.updateTask(id, updates);
            return Result.success("更新成功", updated);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    /**
     * DELETE /api/todos/{id} — delete a task (JSON).
     */
    @DeleteMapping("/api/todos/{id}")
    @ResponseBody
    public Result<Void> apiDelete(@PathVariable Long id) {
        boolean deleted = todoService.deleteTask(id);
        if (deleted) {
            return Result.success("删除成功", null);
        }
        return Result.notFound("任务不存在: id=" + id);
    }
}
