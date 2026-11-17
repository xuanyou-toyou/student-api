# student-api

基于 Spring Boot 的学生管理 REST 服务 —— 从「控制台程序」演进为「可交付的后端服务」。

## 功能

- 学生的增、删、改、查（学号唯一）
- 成绩统计（总人数、语文平均分、数学最高分）
- 批量导入（事务保证：要么全部成功，要么全部回滚）
- 参数校验与统一异常处理（业务错误 400，资源不存在 404）
- 在线接口文档（Swagger UI）

## 技术栈

| 分类 | 技术 |
|---|---|
| 语言 | Java 21 |
| 框架 | Spring Boot 4.1.2（Spring Framework 7）|
| 持久层 | JdbcTemplate + HikariCP 连接池 |
| 数据库 | MySQL 8.0 |
| 构建 | Maven 3.9.9 |
| 文档 | springdoc-openapi 3.2.0 |
| 测试 | JUnit 5 + Mockito 5 |
| 日志 | SLF4J + Logback |

## 目录结构

```
src/main/java/demo/studentapi/
├── StudentApiApplication.java        启动类
├── controller/StudentController.java 接口层：路由、参数绑定、状态码
├── service/StudentService.java       业务层：校验规则、事务、日志
├── dao/StudentDao.java               数据层：JdbcTemplate + SQL
└── model/Student.java                实体

src/test/java/demo/studentapi/
└── service/StudentServiceTest.java   单元测试（6 个用例，Mockito 隔离 DAO）
```

### 三层架构

```
Controller  →  接 HTTP 请求、参数绑定、返回响应（不写 SQL，不写业务规则）
Service     →  校验、业务逻辑、事务边界（不写 SQL，不含 HTTP）
DAO         →  只管 SQL
```

## 环境要求

| 项 | 版本 |
|---|---|
| JDK | 21+ |
| Maven | 3.9+ |
| MySQL | 8.0+ |

## 快速开始

### 1. 建库建表

```sql
CREATE DATABASE IF NOT EXISTS student_db DEFAULT CHARSET utf8mb4;

USE student_db;

CREATE TABLE student (
    id      VARCHAR(20) PRIMARY KEY COMMENT '学号',
    name    VARCHAR(20) NOT NULL    COMMENT '姓名',
    chinese INT NOT NULL DEFAULT 0  COMMENT '语文',
    math    INT NOT NULL DEFAULT 0  COMMENT '数学',
    english INT NOT NULL DEFAULT 0  COMMENT '英语'
) COMMENT '学生表';
```

### 2. 配置数据库连接

复制模板并填入自己的密码：

```bash
cp src/main/resources/application-local.properties.example \
   src/main/resources/application-local.properties
```

编辑 `application-local.properties`：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/student_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=你的密码
```

> `application-local.properties` 已在 `.gitignore` 中，不会提交到仓库。

### 3. 启动

```bash
mvn spring-boot:run
```

服务地址：http://localhost:8080

### 4. 查看接口文档

```
http://localhost:8080/swagger-ui.html
```

可以直接在页面上「Try it out」调试每个接口。

## 接口一览

| 方法 | 路径 | 说明 | 成功 |
|---|---|---|---|
| GET | `/api/students` | 查询全部 | 200 |
| GET | `/api/students/{id}` | 按学号查询 | 200 / 404 |
| GET | `/api/students/count` | 统计数量 | 200 |
| GET | `/api/students/failing` | 有不及格科目的学生 | 200 |
| GET | `/api/statistics` | 成绩统计 | 200 |
| POST | `/api/students` | 新增 | 200 / 400 |
| POST | `/api/students/batch` | 批量导入（事务）| 200 / 400 |
| PUT | `/api/students/{id}` | 修改 | 200 / 400 |
| DELETE | `/api/students/{id}` | 删除 | 204 / 400 |

### 示例

```bash
# 新增
curl -X POST http://localhost:8080/api/students \
  -H "Content-Type: application/json" \
  -d '{"id":"001","name":"张三","chinese":85,"math":92,"english":78}'

# 查询全部
curl http://localhost:8080/api/students

# 批量导入（第 2 条学号重复 → 全部回滚）
curl -X POST http://localhost:8080/api/students/batch \
  -H "Content-Type: application/json" \
  -d '[{"id":"101","name":"甲","chinese":80,"math":80,"english":80},
       {"id":"001","name":"乙","chinese":80,"math":80,"english":80}]'
```

## 测试

```bash
mvn test
```

6 个单元测试（毫秒级完成，不连数据库），覆盖：

- 参数校验（学号/姓名非空、成绩范围）
- 业务规则（学号重复拒绝、修改时 id 以路径为准）
- 异常路径（DAO 失败、记录不存在）
- 批量导入（空列表、中途失败异常传播）

## 开发记录

| 阶段 | 内容 |
|---|---|
| W8 | Spring Boot 入门，内存数据的 6 个接口 |
| W9 | 接入 MySQL，Controller / Service / DAO 三层架构，JdbcTemplate |
| W10 | 日志、批量导入与事务、单元测试、Swagger 文档 |
