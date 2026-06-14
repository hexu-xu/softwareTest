package com.assistant.accounting.service;

import com.assistant.accounting.entity.Record;
import com.assistant.accounting.mapper.RecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Business logic for personal accounting module.
 * Contains 9 testable methods (white-box testing targets).
 */
@Service
public class AccountingService {

    private final RecordMapper recordMapper;

    public AccountingService(RecordMapper recordMapper) {
        this.recordMapper = recordMapper;
    }

    // ==================== Validation Methods ====================

    /**
     * Validate that the amount is positive and not null.
     * <p>
     * White-box test points: boundary values — 0, negative, very large, null.
     * Coverage target: branch coverage.
     *
     * @param amount the amount to validate
     * @throws IllegalArgumentException if amount is null, zero, or negative
     */
    public void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("金额不能为空");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("金额必须大于0");
        }
    }

    /**
     * Validate that the type is either INCOME or EXPENSE.
     * <p>
     * White-box test points: valid INCOME, valid EXPENSE, invalid random string, null, empty.
     * Coverage target: branch coverage.
     *
     * @param type the type to validate
     * @throws IllegalArgumentException if type is not INCOME or EXPENSE
     */
    public void validateType(String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("收支类型不能为空");
        }
        if (!"INCOME".equals(type) && !"EXPENSE".equals(type)) {
            throw new IllegalArgumentException("收支类型必须为 INCOME 或 EXPENSE");
        }
    }

    // ==================== Business Methods ====================

    /**
     * Add a new income/expense record after validation.
     * <p>
     * White-box test points: amount > 0 check; type enum check.
     * Coverage target: statement coverage.
     *
     * @param record the record to add
     * @return the added record with generated ID
     */
    public Record addRecord(Record record) {
        validateAmount(record.getAmount());
        validateType(record.getType());
        if (record.getRecordDate() == null) {
            record.setRecordDate(LocalDate.now());
        }
        recordMapper.insert(record);
        return record;
    }

    /**
     * Retrieve all records ordered by date descending.
     * <p>
     * White-box test points: empty list, normal list.
     * Coverage target: statement coverage.
     *
     * @return list of all records
     */
    public List<Record> getAllRecords() {
        LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Record::getRecordDate);
        return recordMapper.selectList(wrapper);
    }

    /**
     * Find a record by its ID.
     * <p>
     * White-box test points: id exists, id does not exist, null id.
     * Coverage target: branch coverage.
     *
     * @param id the record ID
     * @return the found record, or null if not found
     */
    public Record getRecordById(Long id) {
        if (id == null) {
            return null;
        }
        return recordMapper.selectById(id);
    }

    /**
     * Filter records by income/expense type.
     * <p>
     * White-box test points: empty result, data present, null type.
     * Coverage target: condition coverage.
     *
     * @param type INCOME or EXPENSE
     * @return filtered records
     */
    public List<Record> getRecordsByType(String type) {
        if (type == null || type.isBlank()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Record::getType, type)
               .orderByDesc(Record::getRecordDate);
        return recordMapper.selectList(wrapper);
    }

    /**
     * Aggregate spending/income amounts grouped by category.
     * <p>
     * White-box test points: multiple records same category sum precision,
     * empty data statistics.
     * Coverage target: loop coverage.
     *
     * @return list of maps with category and total keys
     */
    public List<Map<String, Object>> getStatsByCategory() {
        return recordMapper.statsByCategory();
    }

    /**
     * Filter records within a date range.
     * Validates that start date is not after end date.
     * <p>
     * White-box test points: start > end, empty range, normal range.
     * Coverage target: branch coverage.
     *
     * @param start start date (inclusive)
     * @param end   end date (inclusive)
     * @return filtered records
     * @throws IllegalArgumentException if start is after end
     */
    public List<Record> getRecordsByDateRange(LocalDate start, LocalDate end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }
        LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
        if (start != null) {
            wrapper.ge(Record::getRecordDate, start);
        }
        if (end != null) {
            wrapper.le(Record::getRecordDate, end);
        }
        wrapper.orderByDesc(Record::getRecordDate);
        return recordMapper.selectList(wrapper);
    }

    /**
     * Query records by type and date range combined.
     * Used by the API endpoint with optional type, start, end parameters.
     *
     * @param type  optional type filter
     * @param start optional start date
     * @param end   optional end date
     * @return filtered records
     */
    public List<Record> getFilteredRecords(String type, LocalDate start, LocalDate end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }
        return recordMapper.findByTypeAndDateRange(type, start, end);
    }

    /**
     * Delete a record by its ID.
     * <p>
     * White-box test points: delete existing record, delete non-existent record.
     * Coverage target: branch coverage.
     *
     * @param id the record ID to delete
     * @return true if a record was deleted, false if the record did not exist
     */
    public boolean deleteRecord(Long id) {
        if (id == null) {
            return false;
        }
        return recordMapper.deleteById(id) > 0;
    }
}
