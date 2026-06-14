package com.assistant.accounting;

import com.assistant.accounting.entity.Record;
import com.assistant.accounting.mapper.RecordMapper;
import com.assistant.accounting.service.AccountingService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountingServiceTest {

    @Mock
    private RecordMapper recordMapper;

    @InjectMocks
    private AccountingService accountingService;

    private Record testRecord;

    @BeforeEach
    void setUp() {
        testRecord = new Record();
        testRecord.setId(1L);
        testRecord.setAmount(new BigDecimal("100.00"));
        testRecord.setType("INCOME");
        testRecord.setCategory("Salary");
        testRecord.setRecordDate(LocalDate.now());
    }

    // 1. validateAmount(BigDecimal amount)
    @Test
    void testValidateAmount_Null() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountingService.validateAmount(null);
        });
        assertEquals("金额不能为空", exception.getMessage());
    }

    @Test
    void testValidateAmount_Zero() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountingService.validateAmount(BigDecimal.ZERO);
        });
        assertEquals("金额必须大于0", exception.getMessage());
    }

    @Test
    void testValidateAmount_Negative() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountingService.validateAmount(new BigDecimal("-10.00"));
        });
        assertEquals("金额必须大于0", exception.getMessage());
    }

    @Test
    void testValidateAmount_Positive() {
        assertDoesNotThrow(() -> {
            accountingService.validateAmount(new BigDecimal("100.00"));
            accountingService.validateAmount(new BigDecimal("99999999.99"));
        });
    }

    // 2. validateType(String type)
    @Test
    void testValidateType_NullOrBlank() {
        assertThrows(IllegalArgumentException.class, () -> accountingService.validateType(null));
        assertThrows(IllegalArgumentException.class, () -> accountingService.validateType(""));
        assertThrows(IllegalArgumentException.class, () -> accountingService.validateType("   "));
    }

    @Test
    void testValidateType_Invalid() {
        assertThrows(IllegalArgumentException.class, () -> accountingService.validateType("INVALID"));
    }

    @Test
    void testValidateType_Valid() {
        assertDoesNotThrow(() -> accountingService.validateType("INCOME"));
        assertDoesNotThrow(() -> accountingService.validateType("EXPENSE"));
    }

    // 3. addRecord(Record record)
    @Test
    void testAddRecord_Success() {
        when(recordMapper.insert(any(Record.class))).thenReturn(1);
        
        Record saved = accountingService.addRecord(testRecord);
        
        assertNotNull(saved);
        assertEquals(new BigDecimal("100.00"), saved.getAmount());
        verify(recordMapper, times(1)).insert(any(Record.class));
    }
    
    @Test
    void testAddRecord_WithoutDate() {
        testRecord.setRecordDate(null);
        when(recordMapper.insert(any(Record.class))).thenReturn(1);
        
        Record saved = accountingService.addRecord(testRecord);
        
        assertNotNull(saved.getRecordDate());
        assertEquals(LocalDate.now(), saved.getRecordDate());
    }

    // 4. getAllRecords()
    @Test
    void testGetAllRecords_Empty() {
        when(recordMapper.selectList(any())).thenReturn(Collections.emptyList());
        List<Record> result = accountingService.getAllRecords();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllRecords_Normal() {
        when(recordMapper.selectList(any())).thenReturn(List.of(testRecord));
        List<Record> result = accountingService.getAllRecords();
        assertEquals(1, result.size());
        assertEquals(testRecord.getId(), result.get(0).getId());
    }

    // 5. getRecordById(Long id)
    @Test
    void testGetRecordById_NullId() {
        assertNull(accountingService.getRecordById(null));
    }

    @Test
    void testGetRecordById_Exists() {
        when(recordMapper.selectById(1L)).thenReturn(testRecord);
        Record result = accountingService.getRecordById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetRecordById_NotExists() {
        when(recordMapper.selectById(2L)).thenReturn(null);
        assertNull(accountingService.getRecordById(2L));
    }

    // 6. getRecordsByType(String type)
    @Test
    void testGetRecordsByType_NullOrBlank() {
        assertTrue(accountingService.getRecordsByType(null).isEmpty());
        assertTrue(accountingService.getRecordsByType("").isEmpty());
    }

    @Test
    void testGetRecordsByType_Normal() {
        when(recordMapper.selectList(any())).thenReturn(List.of(testRecord));
        List<Record> result = accountingService.getRecordsByType("INCOME");
        assertEquals(1, result.size());
        assertEquals("INCOME", result.get(0).getType());
    }

    // 7. getStatsByCategory()
    @Test
    void testGetStatsByCategory_Empty() {
        when(recordMapper.statsByCategory()).thenReturn(Collections.emptyList());
        List<Map<String, Object>> result = accountingService.getStatsByCategory();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetStatsByCategory_Normal() {
        Map<String, Object> stat = new HashMap<>();
        stat.put("category", "Salary");
        stat.put("total", new BigDecimal("100.00"));
        when(recordMapper.statsByCategory()).thenReturn(List.of(stat));
        
        List<Map<String, Object>> result = accountingService.getStatsByCategory();
        assertEquals(1, result.size());
        assertEquals("Salary", result.get(0).get("category"));
        assertEquals(new BigDecimal("100.00"), result.get(0).get("total"));
    }

    // 8. getRecordsByDateRange(LocalDate start, LocalDate end)
    @Test
    void testGetRecordsByDateRange_StartAfterEnd() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.minusDays(1);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountingService.getRecordsByDateRange(start, end);
        });
        assertEquals("开始日期不能晚于结束日期", exception.getMessage());
    }

    @Test
    void testGetRecordsByDateRange_Normal() {
        when(recordMapper.selectList(any())).thenReturn(List.of(testRecord));
        List<Record> result = accountingService.getRecordsByDateRange(LocalDate.now().minusDays(1), LocalDate.now());
        assertEquals(1, result.size());
    }

    @Test
    void testGetRecordsByDateRange_NullRange() {
        when(recordMapper.selectList(any())).thenReturn(List.of(testRecord));
        List<Record> result = accountingService.getRecordsByDateRange(null, null);
        assertEquals(1, result.size());
    }

    @Test
    void testGetRecordsByDateRange_NullStart() {
        when(recordMapper.selectList(any())).thenReturn(List.of(testRecord));
        List<Record> result = accountingService.getRecordsByDateRange(null, LocalDate.now());
        assertEquals(1, result.size());
    }

    @Test
    void testGetRecordsByDateRange_NullEnd() {
        when(recordMapper.selectList(any())).thenReturn(List.of(testRecord));
        List<Record> result = accountingService.getRecordsByDateRange(LocalDate.now().minusDays(1), null);
        assertEquals(1, result.size());
    }

    // 9. getFilteredRecords(String type, LocalDate start, LocalDate end)
    @Test
    void testGetFilteredRecords_StartAfterEnd() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.minusDays(1);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountingService.getFilteredRecords("INCOME", start, end);
        });
        assertEquals("开始日期不能晚于结束日期", exception.getMessage());
    }

    @Test
    void testGetFilteredRecords_Normal() {
        when(recordMapper.findByTypeAndDateRange(any(), any(), any())).thenReturn(List.of(testRecord));
        List<Record> result = accountingService.getFilteredRecords("INCOME", null, null);
        assertEquals(1, result.size());
    }

    // 10. deleteRecord(Long id)
    @Test
    void testDeleteRecord_NullId() {
        assertFalse(accountingService.deleteRecord(null));
    }

    @Test
    void testDeleteRecord_Exists() {
        when(recordMapper.deleteById(1L)).thenReturn(1);
        assertTrue(accountingService.deleteRecord(1L));
    }

    @Test
    void testDeleteRecord_NotExists() {
        when(recordMapper.deleteById(2L)).thenReturn(0);
        assertFalse(accountingService.deleteRecord(2L));
    }
}
