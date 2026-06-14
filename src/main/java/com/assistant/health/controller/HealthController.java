package com.assistant.health.controller;

import com.assistant.common.Result;
import com.assistant.health.entity.HealthData;
import com.assistant.health.service.HealthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for the Health module.
 * Provides page endpoints (Thymeleaf HTML) and REST API endpoints (JSON).
 */
@Controller
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    // ==================== Page Endpoints ====================

    /**
     * GET /health — render the health main page with history and alerts.
     */
    @GetMapping("/health")
    public String index(Model model) {
        List<HealthData> allData = healthService.getLatest();
        List<String> alerts = healthService.getAlertMessages();
        model.addAttribute("healthDataList", allData);
        model.addAttribute("alerts", alerts);
        return "health";
    }

    /**
     * POST /health/add — form submission to record health data.
     */
    @PostMapping("/health/add")
    public String addHealthData(@RequestParam String dataType,
                                @RequestParam Double value1,
                                @RequestParam(required = false) Double value2,
                                @RequestParam(required = false) LocalDate recordDate,
                                @RequestParam(required = false) String note) {
        HealthData data = new HealthData();
        data.setDataType(dataType);
        data.setValue1(value1);
        data.setValue2(value2);
        data.setRecordDate(recordDate);
        data.setNote(note);
        healthService.recordHealthData(data);
        return "redirect:/health";
    }

    /**
     * GET /health/delete/{id} — delete a health record.
     */
    @GetMapping("/health/delete/{id}")
    public String deleteHealthData(@PathVariable Long id) {
        healthService.deleteHealthData(id);
        return "redirect:/health";
    }

    /**
     * POST /health/edit — form submission to update health data.
     */
    @PostMapping("/health/edit")
    public String editHealthData(@RequestParam Long id,
                                 @RequestParam String dataType,
                                 @RequestParam Double value1,
                                 @RequestParam(required = false) Double value2,
                                 @RequestParam(required = false) LocalDate recordDate,
                                 @RequestParam(required = false) String note) {
        HealthData data = new HealthData();
        data.setDataType(dataType);
        data.setValue1(value1);
        data.setValue2(value2);
        data.setRecordDate(recordDate);
        data.setNote(note);
        healthService.updateHealthData(id, data);
        return "redirect:/health";
    }

    // ==================== REST API Endpoints ====================

    /**
     * POST /api/health — create a health data record (JSON).
     */
    @PostMapping("/api/health")
    @ResponseBody
    public Result<HealthData> apiAdd(@RequestBody HealthData data) {
        try {
            HealthData created = healthService.recordHealthData(data);
            return Result.success("记录成功", created);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    /**
     * GET /api/health/history — query health history by type and optional date range (JSON).
     */
    @GetMapping("/api/health/history")
    @ResponseBody
    public Result<List<HealthData>> apiHistory(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end) {
        List<HealthData> history;
        if (start != null || end != null) {
            history = healthService.getHistoryByDateRange(type, start, end);
        } else if (type != null && !type.isBlank()) {
            history = healthService.getHistoryByType(type);
        } else {
            history = healthService.getLatest();
        }
        return Result.success(history);
    }

    /**
     * GET /api/health/alerts — get all current health alerts (JSON).
     */
    @GetMapping("/api/health/alerts")
    @ResponseBody
    public Result<List<HealthData>> apiAlerts() {
        List<HealthData> alerts = healthService.checkAlerts();
        return Result.success(alerts);
    }

    /**
     * GET /api/health/latest — get the latest record per type (JSON).
     */
    @GetMapping("/api/health/latest")
    @ResponseBody
    public Result<List<HealthData>> apiLatest() {
        List<HealthData> latest = healthService.getLatest();
        return Result.success(latest);
    }

    /**
     * DELETE /api/health/{id} — delete a health record (JSON).
     */
    @DeleteMapping("/api/health/{id}")
    @ResponseBody
    public Result<Void> apiDelete(@PathVariable Long id) {
        boolean deleted = healthService.deleteHealthData(id);
        if (deleted) {
            return Result.success("删除成功", null);
        }
        return Result.notFound("健康记录不存在: id=" + id);
    }

    /**
     * PUT /api/health/{id} — update a health record (JSON).
     */
    @PutMapping("/api/health/{id}")
    @ResponseBody
    public Result<HealthData> apiUpdate(@PathVariable Long id, @RequestBody HealthData data) {
        try {
            HealthData updated = healthService.updateHealthData(id, data);
            return Result.success("更新成功", updated);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }
}
