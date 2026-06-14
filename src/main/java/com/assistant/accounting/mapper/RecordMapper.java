package com.assistant.accounting.mapper;

import com.assistant.accounting.entity.Record;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Data access layer for tb_record table.
 * Inherits basic CRUD from MyBatis-Plus BaseMapper.
 */
@Mapper
public interface RecordMapper extends BaseMapper<Record> {

    /**
     * Aggregate sum of amounts grouped by category.
     *
     * @return list of maps with keys: category, total
     */
    @Select("SELECT category AS \"category\", SUM(amount) AS \"total\" FROM tb_record GROUP BY category ORDER BY \"total\" DESC")
    List<Map<String, Object>> statsByCategory();

    /**
     * Query records by type and date range.
     * If type is null or blank, returns all types.
     *
     * @param type  INCOME / EXPENSE / null
     * @param start start date (inclusive)
     * @param end   end date (inclusive)
     * @return filtered records
     */
    @Select("<script>" +
            "SELECT * FROM tb_record WHERE 1=1" +
            "<if test='type != null and type != \"\"'> AND type = #{type}</if>" +
            "<if test='start != null'> AND record_date &gt;= #{start}</if>" +
            "<if test='end != null'> AND record_date &lt;= #{end}</if>" +
            " ORDER BY record_date DESC" +
            "</script>")
    List<Record> findByTypeAndDateRange(@Param("type") String type,
                                        @Param("start") java.time.LocalDate start,
                                        @Param("end") java.time.LocalDate end);
}
