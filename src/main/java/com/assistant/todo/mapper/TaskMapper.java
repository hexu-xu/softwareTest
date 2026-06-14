package com.assistant.todo.mapper;

import com.assistant.todo.entity.Task;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Data access layer for tb_task table.
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {

    /**
     * Get all tasks sorted by priority: HIGH > MEDIUM > LOW.
     * Uses CASE expression for custom ordering.
     */
    @Select("SELECT * FROM tb_task ORDER BY " +
            "CASE priority " +
            "  WHEN 'HIGH' THEN 1 " +
            "  WHEN 'MEDIUM' THEN 2 " +
            "  WHEN 'LOW' THEN 3 " +
            "  ELSE 4 END, " +
            "create_time DESC")
    List<Task> findAllSortedByPriority();
}
