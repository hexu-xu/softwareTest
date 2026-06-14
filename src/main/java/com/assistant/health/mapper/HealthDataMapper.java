package com.assistant.health.mapper;

import com.assistant.health.entity.HealthData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Data access layer for tb_health_data table.
 */
@Mapper
public interface HealthDataMapper extends BaseMapper<HealthData> {

    /**
     * Query health data records by type and optional date range.
     */
    @Select("<script>" +
            "SELECT * FROM tb_health_data WHERE 1=1" +
            "<if test='type != null and type != \"\"'> AND data_type = #{type}</if>" +
            "<if test='start != null'> AND record_date &gt;= #{start}</if>" +
            "<if test='end != null'> AND record_date &lt;= #{end}</if>" +
            " ORDER BY record_date DESC" +
            "</script>")
    List<HealthData> findByTypeAndDateRange(@Param("type") String type,
                                            @Param("start") java.time.LocalDate start,
                                            @Param("end") java.time.LocalDate end);

    /**
     * Get the latest record for each data type.
     * Uses a correlated subquery to find the max record_date per type.
     */
    @Select("SELECT * FROM tb_health_data WHERE id IN (" +
            "SELECT MAX(id) FROM tb_health_data GROUP BY data_type" +
            ") ORDER BY data_type")
    List<HealthData> findLatestByType();
}
