# 个人助手系统 — 软件测试实验项目

## 项目概述

一个基于 Spring Boot + Thymeleaf + H2 的个人助手 Web 应用，包含三个功能模块。用于软件质量保证与测试的实验（白盒/黑盒测试）和讨论（OO测试/API测试）。

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.3.x |
| 模板引擎 | Thymeleaf |
| 数据库 | H2（内嵌数据库，零安装） |
| ORM | MyBatis-Plus 3.5.x |
| 测试工具 | JUnit 5 |
| 构建工具 | Maven |

---

## 项目结构

```
src/main/java/com/assistant/
│
├── AssistantApplication.java               ← 启动类
│
├── accounting/                              ← 模块1：个人记账
│   ├── controller/
│   │   └── AccountingController.java       ← 页面请求 + REST API
│   ├── service/
│   │   └── AccountingService.java          ← 业务逻辑（白盒测试目标）
│   ├── entity/
│   │   └── Record.java                     ← 收支记录实体（映射 tb_record）
│   └── mapper/
│       └── RecordMapper.java               ← 数据访问层（继承 BaseMapper）
│
├── health/                                  ← 模块2：健康备忘录
│   ├── controller/
│   │   └── HealthController.java
│   ├── service/
│   │   └── HealthService.java
│   ├── entity/
│   │   └── HealthData.java
│   └── mapper/
│       └── HealthDataMapper.java
│
├── todo/                                    ← 模块3：待办事项
│   ├── controller/
│   │   └── TodoController.java
│   ├── service/
│   │   └── TodoService.java
│   ├── entity/
│   │   └── Task.java
│   └── mapper/
│       └── TaskMapper.java
│
└── common/
    └── Result.java                          ← 统一JSON响应格式

src/main/resources/
├── templates/
│   ├── accounting.html                      ← 记账页面
│   ├── health.html                          ← 健康页面
│   └── todo.html                            ← 待办页面
├── static/                                  ← CSS/JS 静态资源
├── application.yml                          ← 应用配置
└── data.sql                                 ← 测试初始数据（可选）

src/test/java/com/assistant/
├── accounting/
│   └── AccountingServiceTest.java           ← 模块1 白盒单元测试
├── health/
│   └── HealthServiceTest.java               ← 模块2 白盒单元测试
└── todo/
    └── TodoServiceTest.java                 ← 模块3 白盒单元测试
```

---

## 数据库设计

### 数据库名：`personal_assistant`

### 表1：`tb_record`（收支记录）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| amount | DECIMAL(10,2) | 金额（NOT NULL） |
| type | VARCHAR(20) | 收支类型：INCOME / EXPENSE |
| category | VARCHAR(50) | 类别：餐饮、交通、工资等 |
| description | VARCHAR(200) | 备注说明 |
| record_date | DATE | 记录日期 |
| create_time | DATETIME | 创建时间 |

### 表2：`tb_health_data`（健康数据）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| data_type | VARCHAR(20) | 类型：WEIGHT / BLOOD_PRESSURE / SLEEP |
| value1 | DOUBLE | 数值1（体重/收缩压/睡眠时长） |
| value2 | DOUBLE | 数值2（舒张压，血压类型时使用） |
| record_date | DATE | 记录日期 |
| note | VARCHAR(200) | 备注 |
| create_time | DATETIME | 创建时间 |

### 表3：`tb_task`（待办任务）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| title | VARCHAR(100) | 任务标题（NOT NULL） |
| priority | VARCHAR(10) | 优先级：HIGH / MEDIUM / LOW |
| status | VARCHAR(20) | 状态：PENDING / DONE |
| deadline | DATE | 截止日期 |
| create_time | DATETIME | 创建时间 |

---

## 各模块 Controller 接口设计

每个模块的 Controller 提供两类接口：
- **页面接口**：返回 Thymeleaf 渲染的 HTML 页面（用于实验2 黑盒手工测试）
- **API 接口**：返回 JSON 数据（用于讨论 Web API 测试）

### 模块1：记账 Controller — `AccountingController`

**页面接口：**

| 方法 | URL | 说明 |
|------|-----|------|
| GET | `/accounting` | 记账首页，展示收支列表和统计 |
| POST | `/accounting/add` | 表单提交，添加收支记录 |
| GET | `/accounting/delete/{id}` | 删除指定记录 |

**API 接口（返回 JSON）：**

| 方法 | URL | 说明 | 测试关注点 |
|------|-----|------|-----------|
| GET | `/api/records` | 查询全部收支记录 | 返回列表结构 |
| GET | `/api/records/{id}` | 查询单条记录 | 存在/不存在的处理 |
| POST | `/api/records` | 新增收支记录 | 参数校验、金额合法性 |
| DELETE | `/api/records/{id}` | 删除记录 | 幂等性 |
| GET | `/api/records/stats?type=` | 按类别统计金额 | 计算正确性、空数据 |
| GET | `/api/records?type=&start=&end=` | 按收支类型和日期范围筛选 | 参数组合、边界值 |

**API 数量：6 个**

### 模块2：健康 Controller — `HealthController`

**页面接口：**

| 方法 | URL | 说明 |
|------|-----|------|
| GET | `/health` | 健康首页，展示历史数据 |
| POST | `/health/add` | 表单提交，记录健康数据 |
| GET | `/health/delete/{id}` | 删除指定记录 |
| POST | `/health/edit` | 表单提交，修改健康数据 |

**API 接口（返回 JSON）：**

| 方法 | URL | 说明 | 测试关注点 |
|------|-----|------|-----------|
| POST | `/api/health` | 记录一条健康数据 | 数据范围校验 |
| GET | `/api/health/history?type=` | 按类型查询历史 | 筛选正确性 |
| GET | `/api/health/history?type=&start=&end=` | 按类型和日期范围查询 | 多条件组合 |
| GET | `/api/health/alerts` | 获取当前所有健康预警 | 预警逻辑正确性 |
| GET | `/api/health/latest` | 获取各类型最新一条数据 | 查询逻辑 |
| DELETE | `/api/health/{id}` | 删除一条健康数据 | 删除存在/不存在 |
| PUT | `/api/health/{id}` | 修改一条健康数据 | 字段更新、id不存在 |

**API 数量：7 个**

### 模块3：待办 Controller — `TodoController`

**页面接口：**

| 方法 | URL | 说明 |
|------|-----|------|
| GET | `/todo` | 待办首页，展示任务列表 |
| POST | `/todo/add` | 表单提交，添加任务 |
| GET | `/todo/done/{id}` | 标记完成 |
| GET | `/todo/delete/{id}` | 删除任务 |
| POST | `/todo/edit` | 表单提交，修改任务 |

**API 接口（返回 JSON）：**

| 方法 | URL | 说明 | 测试关注点 |
|------|-----|------|-----------|
| GET | `/api/todos` | 查询全部任务 | 列表返回 |
| GET | `/api/todos?sort=priority` | 按优先级排序 | 排序正确性 |
| GET | `/api/todos?status=PENDING` | 按状态筛选 | 筛选条件 |
| POST | `/api/todos` | 新增任务 | 标题非空校验 |
| PUT | `/api/todos/{id}/done` | 标记任务完成 | 状态切换、重复标记 |
| PUT | `/api/todos/{id}` | 更新任务信息 | 字段更新 |
| DELETE | `/api/todos/{id}` | 删除任务 | 删除存在/不存在 |

**API 数量：7 个**

### API 端点总计：6 + 7 + 7 = **20 个**。

---

## Service 层方法设计（白盒测试目标）

### AccountingService

| 方法 | 职责 | 白盒测试点 | 覆盖标准 |
|------|------|-----------|---------|
| `addRecord(Record r)` | 添加收支记录 | amount > 0 校验；type 枚举校验 | 语句覆盖 |
| `validateAmount(BigDecimal amount)` | 金额合法性校验 | 边界值：0、负数、极大值、null | 分支覆盖 |
| `validateType(String type)` | 收支类型校验 | INCOME/EXPENSE 合法值、非法输入 | 分支覆盖 |
| `getAllRecords()` | 查询全部记录 | 空列表、正常列表 | 语句覆盖 |
| `getRecordById(Long id)` | 按ID查询 | id存在、id不存在 | 分支覆盖 |
| `getRecordsByType(String type)` | 按收支类型筛选 | 空结果、有数据 | 条件覆盖 |
| `getStatsByCategory()` | 按类别分组统计金额 | 多条同类别求和精度、空数据统计 | 循环覆盖 |
| `getRecordsByDateRange(LocalDate s, LocalDate e)` | 日期范围筛选 | start>end、空范围、正常范围 | 分支覆盖 |
| `deleteRecord(Long id)` | 删除记录 | 删除存在/不存在记录 | 分支覆盖 |

**可测方法：9 个**

### HealthService

| 方法 | 职责 | 白盒测试点 | 覆盖标准 |
|------|------|-----------|---------|
| `recordHealthData(HealthData d)` | 记录一条健康数据 | 类型校验、数值范围 | 语句覆盖 |
| `validateWeight(double val)` | 体重范围校验 | 20-300kg 边界值：19.9, 20, 300, 300.1 | 边界值 |
| `validateBloodPressure(int high, int low)` | 血压范围校验 | high>low 逻辑、正常/异常范围 | 条件组合 |
| `validateSleepHours(double hours)` | 睡眠时长校验 | 0-24h 边界值 | 边界值 |
| `getHistoryByType(String type)` | 按类型查历史 | 空结果、正常结果 | 语句覆盖 |
| `getHistoryByDateRange(String type, LocalDate s, LocalDate e)` | 按类型+日期范围 | 多条件组合 | 条件覆盖 |
| `checkAlerts()` | 检查并生成健康预警 | 各阈值判断分支（高血压、低血压、过轻、过重、睡眠不足） | **分支全覆盖** |
| `getAlertMessages()` | 获取预警文案 | 多条/零条预警 | 循环覆盖 |
| `getLatest()` | 获取各类型最新数据 | 无数据、有数据 | 语句覆盖 |
| `deleteHealthData(Long id)` | 删除一条健康数据 | id存在、id不存在 | 分支覆盖 |
| `updateHealthData(Long id, HealthData d)` | 修改一条健康数据 | id存在更新、id不存在抛异常、数据合法性 | 分支覆盖 |

**可测方法：11 个**

### TodoService

| 方法 | 职责 | 白盒测试点 | 覆盖标准 |
|------|------|-----------|---------|
| `addTask(Task t)` | 添加任务 | title 非空、title 长度上限 | 分支覆盖 |
| `validateTitle(String title)` | 标题校验 | null、空串、纯空格、超长 | 等价类 |
| `getAllTasks()` | 查全部 | 空列表、有数据 | 语句覆盖 |
| `getTasksByStatus(String status)` | 按状态筛选 | PENDING/DONE、非法状态值 | 分支覆盖 |
| `getTasksSortedByPriority()` | 按优先级排序 | HIGH-MEDIUM-LOW 排序规则 | 逻辑验证 |
| `markDone(Long id)` | 标记完成 | 存在/不存在、已经是DONE | 分支覆盖 |
| `updateTask(Long id, Task t)` | 更新任务 | 部分字段更新、id不存在 | 分支覆盖 |
| `deleteTask(Long id)` | 删除 | 存在/不存在 | 分支覆盖 |

**可测方法：8 个**

### Service 层可测方法总计：9 + 11 + 8 = **28 个**。

---

## 数据流架构

```
┌──────────┐     HTTP请求      ┌────────────────┐    方法调用    ┌───────────────┐   MyBatis-Plus  ┌────────┐
│  浏览器   │ ─────────────────>│   Controller    │──────────────>│   Service      │────────────────>│  H2  │
│          │ <─────────────────│                 │<──────────────│                │<────────────────│         │
│ (HTML页面)│    Thymeleaf渲染   │  页面 + JSON   │   返回数据     │  业务逻辑处理   │   BaseMapper    │ 数据库  │
└──────────┘                   └────────────────┘               └───────────────┘                 └────────┘
                                                                       │
                                    ┌───────────────────────────────────┤
                                    │                                   │
                            ┌───────┴──────┐                     ┌──────┴──────┐
                            │  Postman/JMeter│                     │   JUnit 5   │
                            │  (讨论:API测试) │                     │  (实验1:白盒) │
                            └──────────────┘                     └─────────────┘
```

---

## 课程任务映射

| 课程要求 | 测什么 | 工具 | 目标对象 |
|----------|--------|------|---------|
| **实验1** 白盒测试 | Service 层 28 个方法 | JUnit 5 | 本项目 Service |
| **实验2** 黑盒测试 | 3 个 Thymeleaf 页面的增删改查操作 | 手工测试 | 本项目 HTML 页面 |
| **讨论1** OO测试 | 本项目 Service 方法 + 外部开源项目 | JUnit 5 | 本项目 + GitHub 项目 |
| **讨论2** Web API测试 | 20 个 REST API 端点 | Postman / JMeter | 本项目 Controller |



## 开发环境要求

- JDK 17+
- Maven 3.6+
- IDE：IntelliJ IDEA（推荐）或 Eclipse

> H2 是 Java 内嵌数据库，随项目启动自动运行，**无需单独安装任何数据库软件**。数据文件存在项目目录下，`clone → 启动 → 直接跑`，零配置。

## Maven 依赖（pom.xml 关键依赖）

```xml
<!-- Spring Boot 父工程 -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.5</version>
</parent>

<dependencies>
    <!-- Web + Thymeleaf -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>

    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        <version>3.5.9</version>
    </dependency>

    <!-- H2 内嵌数据库 -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Lombok（省写 getter/setter） -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- 测试 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## application.yml 配置

```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/assistant;AUTO_SERVER=TRUE;MODE=MySQL
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true                  # 启用 H2 控制台（浏览器访问 /h2-console 查看数据）
      path: /h2-console
  sql:
    init:
      mode: always                   # 启动时自动执行 schema.sql 和 data.sql
  thymeleaf:
    cache: false                     # 开发时关闭缓存，修改页面即时生效
    prefix: classpath:/templates/
    suffix: .html

# MyBatis-Plus 配置
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl   # 控制台打印SQL，方便调试
    map-underscore-to-camel-case: true                       # 下划线转驼峰（tb_record → Record）
  global-config:
    db-config:
      id-type: auto                                          # 主键自增
```

## H2 说明



### 数据存储方式

```
项目目录/
└── data/
    └── assistant.mv.db    ← H2 数据文件（自动生成，可删掉重建）
```

- `jdbc:h2:file:./data/assistant` — 数据存文件，重启不丢失
- `AUTO_SERVER=TRUE` — 允许多个连接同时访问
- `MODE=MySQL` — 兼容 MySQL 语法

### 浏览器查看数据

启动项目后访问 `http://localhost:8080/h2-console`，用以下信息登录：

| 项目 | 值 |
|------|-----|
| JDBC URL | `jdbc:h2:file:./data/assistant` |
| 用户名 | `sa` |
| 密码 | （留空） |

### 建表语句（src/main/resources/schema.sql）

```sql
CREATE TABLE IF NOT EXISTS tb_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    amount DECIMAL(10,2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    category VARCHAR(50),
    description VARCHAR(200),
    record_date DATE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_health_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    data_type VARCHAR(20) NOT NULL,
    value1 DOUBLE,
    value2 DOUBLE,
    record_date DATE,
    note VARCHAR(200),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tb_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    priority VARCHAR(10) DEFAULT 'MEDIUM',
    status VARCHAR(20) DEFAULT 'PENDING',
    deadline DATE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

