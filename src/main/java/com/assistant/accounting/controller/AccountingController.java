package com.assistant.accounting.controller;

import com.assistant.accounting.entity.Record;
import com.assistant.accounting.service.AccountingService;
import com.assistant.common.Result;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller for the Accounting module.
 * Provides page endpoints (Thymeleaf HTML) and REST API endpoints (JSON).
 */
@Controller
public class AccountingController {

    private final AccountingService accountingService;

    public AccountingController(AccountingService accountingService) {
        this.accountingService = accountingService;
    }

    // ==================== Page Endpoints ====================

    /**
     * GET /accounting — render the accounting main page with record list and stats.
     */
    @GetMapping("/accounting")
    public String index(Model model) {
        List<Record> records = accountingService.getAllRecords();
        List<Map<String, Object>> stats = accountingService.getStatsByCategory();
        model.addAttribute("records", records);
        model.addAttribute("stats", stats);
        return "accounting";
    }

    /**
     * POST /accounting/add — form submission to add a new record.
     */
    @PostMapping("/accounting/add")
    public String addRecord(@RequestParam BigDecimal amount,
                            @RequestParam String type,
                            @RequestParam String category,
                            @RequestParam(required = false) String description,
                            @RequestParam(required = false) LocalDate recordDate) {
        Record record = new Record();
        record.setAmount(amount);
        record.setType(type);
        record.setCategory(category);
        record.setDescription(description);
        record.setRecordDate(recordDate);
        accountingService.addRecord(record);
        return "redirect:/accounting";
    }

    /**
     * GET /accounting/delete/{id} — delete a record and redirect.
     */
    @GetMapping("/accounting/delete/{id}")
    public String deleteRecord(@PathVariable Long id) {
        accountingService.deleteRecord(id);
        return "redirect:/accounting";
    }

    // ==================== REST API Endpoints ====================

    /**
     * GET /api/records — list all records (JSON).
     */
    @GetMapping("/api/records")
    @ResponseBody
    public Result<List<Record>> apiGetAll(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end) {
        List<Record> records;
        if (type != null || start != null || end != null) {
            records = accountingService.getFilteredRecords(type, start, end);
        } else {
            records = accountingService.getAllRecords();
        }
        return Result.success(records);
    }

    /**
     * GET /api/records/{id} — get a single record by ID (JSON).
     */
    @GetMapping("/api/records/{id}")
    @ResponseBody
    public Result<Record> apiGetById(@PathVariable Long id) {
        Record record = accountingService.getRecordById(id);
        if (record == null) {
            return Result.notFound("记录不存在: id=" + id);
        }
        return Result.success(record);
    }

    /**
     * POST /api/records — create a new record (JSON).
     */
    @PostMapping("/api/records")
    @ResponseBody
    public Result<Record> apiAdd(@RequestBody Record record) {
        try {
            Record created = accountingService.addRecord(record);
            return Result.success("创建成功", created);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    /**
     * DELETE /api/records/{id} — delete a record (JSON).
     */
    @DeleteMapping("/api/records/{id}")
    @ResponseBody
    public Result<Void> apiDelete(@PathVariable Long id) {
        boolean deleted = accountingService.deleteRecord(id);
        if (deleted) {
            return Result.success("删除成功", null);
        }
        return Result.notFound("记录不存在: id=" + id);
    }

    /**
     * GET /api/records/stats — category statistics (JSON).
     */
    @GetMapping("/api/records/stats")
    @ResponseBody
    public Result<List<Map<String, Object>>> apiStats() {
        List<Map<String, Object>> stats = accountingService.getStatsByCategory();
        return Result.success(stats);
    }
}
