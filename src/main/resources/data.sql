-- Initial test data (uses MERGE to avoid duplicates on restart)
MERGE INTO tb_record (id, amount, type, category, description, record_date) KEY(id) VALUES
(1, 150.00, 'EXPENSE', '餐饮', '午餐消费', '2025-06-10'),
(2, 5000.00, 'INCOME', '工资', '六月工资', '2025-06-01'),
(3, 200.00, 'EXPENSE', '交通', '地铁充值', '2025-06-09'),
(4, 89.90, 'EXPENSE', '购物', '超市采购', '2025-06-08'),
(5, 3200.00, 'INCOME', '兼职', '自由职业收入', '2025-06-05');

MERGE INTO tb_health_data (id, data_type, value1, value2, record_date, note) KEY(id) VALUES
(1, 'WEIGHT', 70.5, NULL, '2025-06-10', '晨起空腹体重'),
(2, 'BLOOD_PRESSURE', 120, 80, '2025-06-10', '早晨测量'),
(3, 'SLEEP', 7.5, NULL, '2025-06-10', '睡眠质量良好'),
(4, 'WEIGHT', 71.0, NULL, '2025-06-03', '上周体重');

MERGE INTO tb_task (id, title, priority, status, deadline) KEY(id) VALUES
(1, '完成软件测试作业', 'HIGH', 'PENDING', '2025-06-15'),
(2, '复习JUnit框架', 'MEDIUM', 'PENDING', '2025-06-18'),
(3, '阅读测试理论书', 'LOW', 'DONE', '2025-06-10'),
(4, '准备实验报告', 'HIGH', 'PENDING', '2025-06-14');
