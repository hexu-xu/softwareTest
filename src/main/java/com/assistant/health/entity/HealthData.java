package com.assistant.health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Health data entity, mapped to tb_health_data table.
 * Supports WEIGHT, BLOOD_PRESSURE, and SLEEP data types.
 */
@TableName("tb_health_data")
public class HealthData {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** Data type: WEIGHT / BLOOD_PRESSURE / SLEEP */
    private String dataType;

    /** Primary value (weight in kg / systolic BP / sleep hours) */
    private Double value1;

    /** Secondary value (diastolic BP, null for weight/sleep) */
    private Double value2;

    private LocalDate recordDate;

    private String note;

    private LocalDateTime createTime;

    public HealthData() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Double getValue1() {
        return value1;
    }

    public void setValue1(Double value1) {
        this.value1 = value1;
    }

    public Double getValue2() {
        return value2;
    }

    public void setValue2(Double value2) {
        this.value2 = value2;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "HealthData{" +
                "id=" + id +
                ", dataType='" + dataType + '\'' +
                ", value1=" + value1 +
                ", value2=" + value2 +
                ", recordDate=" + recordDate +
                '}';
    }
}
