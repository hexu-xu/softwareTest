package com.assistant.health.service;

import com.assistant.health.entity.HealthData;
import com.assistant.health.mapper.HealthDataMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Business logic for health tracking module.
 * Contains 11 testable methods (white-box testing targets).
 */
@Service
public class HealthService {

    /** Thresholds for health alerts */
    public static final double WEIGHT_MIN = 20.0;
    public static final double WEIGHT_MAX = 300.0;
    public static final double WEIGHT_UNDERWEIGHT = 45.0;
    public static final double WEIGHT_OVERWEIGHT = 100.0;
    public static final int BP_SYSTOLIC_HIGH = 140;
    public static final int BP_DIASTOLIC_HIGH = 90;
    public static final int BP_SYSTOLIC_LOW = 90;
    public static final int BP_DIASTOLIC_LOW = 60;
    public static final double SLEEP_MIN = 0.0;
    public static final double SLEEP_MAX = 24.0;
    public static final double SLEEP_INSUFFICIENT = 6.0;
    public static final double SLEEP_EXCESSIVE = 10.0;

    private final HealthDataMapper healthDataMapper;

    public HealthService(HealthDataMapper healthDataMapper) {
        this.healthDataMapper = healthDataMapper;
    }

    // ==================== Validation Methods ====================

    /**
     * Validate weight is within 20-300 kg range.
     * <p>
     * White-box test points: 19.9, 20, 300, 300.1.
     * Coverage target: boundary value analysis.
     *
     * @param weight weight in kg
     * @throws IllegalArgumentException if weight is out of range
     */
    public void validateWeight(double weight) {
        if (weight < WEIGHT_MIN || weight > WEIGHT_MAX) {
            throw new IllegalArgumentException(
                    String.format("体重必须在 %.1f - %.1f kg 之间", WEIGHT_MIN, WEIGHT_MAX));
        }
    }

    /**
     * Validate blood pressure values.
     * Ensures systolic > diastolic and both are within reasonable ranges.
     * <p>
     * White-box test points: high > low logic, normal/abnormal ranges.
     * Coverage target: condition combination coverage.
     *
     * @param systolic  systolic pressure (higher number)
     * @param diastolic diastolic pressure (lower number)
     * @throws IllegalArgumentException if values are invalid
     */
    public void validateBloodPressure(int systolic, int diastolic) {
        if (systolic <= 0 || diastolic <= 0) {
            throw new IllegalArgumentException("血压值必须为正数");
        }
        if (systolic <= diastolic) {
            throw new IllegalArgumentException("收缩压(高压)必须大于舒张压(低压)");
        }
        if (systolic > 300 || diastolic > 200) {
            throw new IllegalArgumentException("血压值超出合理范围");
        }
    }

    /**
     * Validate sleep hours are within 0-24 range.
     * <p>
     * White-box test points: -0.1, 0, 24, 24.1.
     * Coverage target: boundary value analysis.
     *
     * @param hours sleep duration in hours
     * @throws IllegalArgumentException if hours are out of range
     */
    public void validateSleepHours(double hours) {
        if (hours < SLEEP_MIN || hours > SLEEP_MAX) {
            throw new IllegalArgumentException(
                    String.format("睡眠时长必须在 %.1f - %.1f 小时之间", SLEEP_MIN, SLEEP_MAX));
        }
    }

    /**
     * Validate health data type is one of the supported types.
     */
    private void validateDataType(String dataType) {
        if (dataType == null || dataType.isBlank()) {
            throw new IllegalArgumentException("数据类型不能为空");
        }
        if (!"WEIGHT".equals(dataType) && !"BLOOD_PRESSURE".equals(dataType) && !"SLEEP".equals(dataType)) {
            throw new IllegalArgumentException("数据类型必须为 WEIGHT、BLOOD_PRESSURE 或 SLEEP");
        }
    }

    // ==================== Business Methods ====================

    /**
     * Record a new health data entry after validation.
     * <p>
     * White-box test points: type validation, value range validation.
     * Coverage target: statement coverage.
     *
     * @param data the health data to record
     * @return the recorded data with generated ID
     */
    public HealthData recordHealthData(HealthData data) {
        validateDataType(data.getDataType());
        if (data.getValue1() == null) {
            throw new IllegalArgumentException("数值不能为空");
        }
        switch (data.getDataType()) {
            case "WEIGHT":
                validateWeight(data.getValue1());
                break;
            case "BLOOD_PRESSURE":
                if (data.getValue2() == null) {
                    throw new IllegalArgumentException("血压需要提供舒张压(低压)值");
                }
                validateBloodPressure(data.getValue1().intValue(), data.getValue2().intValue());
                break;
            case "SLEEP":
                validateSleepHours(data.getValue1());
                break;
        }
        if (data.getRecordDate() == null) {
            data.setRecordDate(LocalDate.now());
        }
        healthDataMapper.insert(data);
        return data;
    }

    /**
     * Get health data history filtered by type.
     * <p>
     * White-box test points: empty result, normal result.
     * Coverage target: statement coverage.
     *
     * @param type the data type to filter by
     * @return list of health data records
     */
    public List<HealthData> getHistoryByType(String type) {
        LambdaQueryWrapper<HealthData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthData::getDataType, type)
               .orderByDesc(HealthData::getRecordDate);
        return healthDataMapper.selectList(wrapper);
    }

    /**
     * Get health data history filtered by type and date range.
     * <p>
     * White-box test points: multi-condition combination.
     * Coverage target: condition coverage.
     *
     * @param type  the data type
     * @param start start date (inclusive)
     * @param end   end date (inclusive)
     * @return filtered records
     */
    public List<HealthData> getHistoryByDateRange(String type, LocalDate start, LocalDate end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }
        return healthDataMapper.findByTypeAndDateRange(type, start, end);
    }

    /**
     * Check health data and generate alerts based on thresholds.
     * Rules:
     * - Blood pressure: systolic >= 140 or diastolic >= 90 → high BP alert
     * - Blood pressure: systolic < 90 or diastolic < 60 → low BP alert
     * - Weight: < 45 kg → underweight; > 100 kg → overweight
     * - Sleep: < 6 hours → insufficient; > 10 hours → excessive
     * <p>
     * White-box test points: each threshold branch (high BP, low BP,
     * underweight, overweight, insufficient sleep, excessive sleep, normal).
     * Coverage target: full branch coverage.
     *
     * @return list of newly generated HealthData alert records, or existing alerts
     */
    public List<HealthData> checkAlerts() {
        List<HealthData> alerts = new ArrayList<>();
        List<HealthData> allLatest = getLatest();

        for (HealthData data : allLatest) {
            switch (data.getDataType()) {
                case "WEIGHT":
                    double weight = data.getValue1();
                    if (weight < WEIGHT_UNDERWEIGHT) {
                        HealthData alert = buildAlert("WEIGHT", weight, null,
                                String.format("体重过轻警告: %.1f kg，低于 %.1f kg", weight, WEIGHT_UNDERWEIGHT));
                        alerts.add(alert);
                    } else if (weight > WEIGHT_OVERWEIGHT) {
                        HealthData alert = buildAlert("WEIGHT", weight, null,
                                String.format("体重过重警告: %.1f kg，超过 %.1f kg", weight, WEIGHT_OVERWEIGHT));
                        alerts.add(alert);
                    }
                    break;
                case "BLOOD_PRESSURE":
                    int systolic = data.getValue1().intValue();
                    int diastolic = data.getValue2() != null ? data.getValue2().intValue() : 0;
                    if (systolic >= BP_SYSTOLIC_HIGH || diastolic >= BP_DIASTOLIC_HIGH) {
                        HealthData alert = buildAlert("BLOOD_PRESSURE", (double) systolic, (double) diastolic,
                                String.format("高血压警告: 收缩压 %d / 舒张压 %d", systolic, diastolic));
                        alerts.add(alert);
                    } else if (systolic < BP_SYSTOLIC_LOW || diastolic < BP_DIASTOLIC_LOW) {
                        HealthData alert = buildAlert("BLOOD_PRESSURE", (double) systolic, (double) diastolic,
                                String.format("低血压警告: 收缩压 %d / 舒张压 %d", systolic, diastolic));
                        alerts.add(alert);
                    }
                    break;
                case "SLEEP":
                    double hours = data.getValue1();
                    if (hours < SLEEP_INSUFFICIENT) {
                        HealthData alert = buildAlert("SLEEP", hours, null,
                                String.format("睡眠不足警告: %.1f 小时，少于 %.1f 小时", hours, SLEEP_INSUFFICIENT));
                        alerts.add(alert);
                    } else if (hours > SLEEP_EXCESSIVE) {
                        HealthData alert = buildAlert("SLEEP", hours, null,
                                String.format("睡眠过多警告: %.1f 小时，超过 %.1f 小时", hours, SLEEP_EXCESSIVE));
                        alerts.add(alert);
                    }
                    break;
            }
        }
        return alerts;
    }

    /**
     * Build a HealthData alert object using setters.
     */
    private HealthData buildAlert(String dataType, Double value1, Double value2, String note) {
        HealthData alert = new HealthData();
        alert.setDataType(dataType);
        alert.setValue1(value1);
        alert.setValue2(value2);
        alert.setRecordDate(LocalDate.now());
        alert.setNote(note);
        return alert;
    }

    /**
     * Get alert messages for all current health data.
     * Delegates to checkAlerts() and extracts the note text.
     * <p>
     * White-box test points: multiple alerts, zero alerts.
     * Coverage target: loop coverage.
     *
     * @return list of alert message strings
     */
    public List<String> getAlertMessages() {
        List<HealthData> alerts = checkAlerts();
        List<String> messages = new ArrayList<>();
        for (HealthData alert : alerts) {
            messages.add(alert.getNote());
        }
        return messages;
    }

    /**
     * Get the latest health data entry for each type.
     * <p>
     * White-box test points: no data, data present.
     * Coverage target: statement coverage.
     *
     * @return list containing the latest record per type
     */
    public List<HealthData> getLatest() {
        return healthDataMapper.findLatestByType();
    }

    /**
     * Delete a health data record by ID.
     * <p>
     * White-box test points: existing id, non-existent id, null id.
     * Coverage target: branch coverage.
     *
     * @param id the record ID to delete
     * @return true if deleted, false if not found
     */
    public boolean deleteHealthData(Long id) {
        if (id == null) {
            return false;
        }
        return healthDataMapper.deleteById(id) > 0;
    }

    /**
     * Update an existing health data record.
     * Validates the new data before updating.
     * <p>
     * White-box test points: id exists + valid data, id not found,
     * id exists + invalid data.
     * Coverage target: branch coverage.
     *
     * @param id   the record ID to update
     * @param data the new health data values
     * @return the updated record
     * @throws IllegalArgumentException if id not found or data invalid
     */
    public HealthData updateHealthData(Long id, HealthData data) {
        HealthData existing = healthDataMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("健康记录不存在: id=" + id);
        }
        // Validate new data if provided
        if (data.getDataType() != null) {
            existing.setDataType(data.getDataType());
        }
        if (data.getValue1() != null) {
            existing.setValue1(data.getValue1());
        }
        if (data.getValue2() != null) {
            existing.setValue2(data.getValue2());
        }
        if (data.getRecordDate() != null) {
            existing.setRecordDate(data.getRecordDate());
        }
        if (data.getNote() != null) {
            existing.setNote(data.getNote());
        }

        // Re-validate the merged data
        validateDataType(existing.getDataType());
        switch (existing.getDataType()) {
            case "WEIGHT":
                validateWeight(existing.getValue1());
                break;
            case "BLOOD_PRESSURE":
                if (existing.getValue2() == null) {
                    throw new IllegalArgumentException("血压需要提供舒张压(低压)值");
                }
                validateBloodPressure(existing.getValue1().intValue(), existing.getValue2().intValue());
                break;
            case "SLEEP":
                validateSleepHours(existing.getValue1());
                break;
        }

        healthDataMapper.updateById(existing);
        return existing;
    }
}
