package demo.studentapi.controller;

import demo.studentapi.model.Student;
import demo.studentapi.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * 学生接口（Controller 层）。
 *
 * 职责边界：
 *   ✅ 接 HTTP 请求、解析参数、返回响应
 *   ✅ 把业务异常转成合适的状态码
 *   ❌ 不写 SQL
 *   ❌ 不写业务规则（校验、查重都在 Service）
 */
@Tag(name = "学生管理", description = "学生的增删改查、统计与批量导入")
@RestController
@RequestMapping("/api")
public class StudentController {

    private final StudentService studentService;

    /** 构造器注入：Spring 启动时自动把 Service 传进来 */
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // ==================== 读接口 ====================

    /** GET /api/hello/卢宇轩 */
    @GetMapping("/hello/{name}")
    public String hello(@PathVariable String name) {
        return "Hello," + name;
    }

    /** GET /api/students —— 查全部 */
    @Operation(summary = "查询全部学生")
    @GetMapping("/students")
    public List<Student> list() {
        return studentService.findAll();
    }

    /** GET /api/students/count —— 查总数 */
    @Operation(summary = "统计学生数量")
    @GetMapping("/students/count")
    public int count() {
        return studentService.findAll().size();
    }

    /** GET /api/statistics —— 统计信息 */
    @Operation(summary = "成绩统计", description = "总人数、语文平均分、数学最高分")
    @GetMapping("/statistics")
    public Map<String, Object> statistics() {
        return studentService.statistics();
    }

    /** GET /api/students/failing —— 有不及格科目的学生 */
    @Operation(summary = "查询有不及格科目的学生")
    @GetMapping("/students/failing")
    public List<Student> failing() {
        return studentService.findAll().stream()
                .filter(s -> s.getChinese() < 60 || s.getMath() < 60 || s.getEnglish() < 60)
                .toList();
    }

    /** GET /api/students/001 —— 查单个，查不到返回 404 */
    @Operation(summary = "按学号查询单个学生", description = "查不到返回 404")
    @GetMapping("/students/{id}")
    public Student getById(@PathVariable String id) {
        Student student = studentService.findById(id);
        if (student == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "未找到学号为 " + id + " 的学生");
        }
        return student;
    }

    // ==================== 写接口 ====================

    /** POST /api/students —— 新增 */
    @Operation(summary = "新增学生", description = "学号重复或成绩越界返回 400")
    @PostMapping("/students")
    public Student create(@RequestBody Student student) {
        return studentService.create(student);
    }

    /** POST /api/students/batch —— 批量导入，任何一条失败则全部回滚 */
    @Operation(summary = "批量导入", description = "任何一条失败则全部回滚")
    @PostMapping("/students/batch")
    public Map<String, Object> batchImport(@RequestBody List<Student> students) {
        return studentService.batchImport(students);
    }

    /** PUT /api/students/001 —— 修改（id 以路径为准） */
    @Operation(summary = "修改学生", description = "学号以路径参数为准")
    @PutMapping("/students/{id}")
    public Student update(@PathVariable String id, @RequestBody Student student) {
        return studentService.update(id, student);
    }


    /** DELETE /api/students/001 —— 删除 */
    @Operation(summary = "删除学生", description = "成功返回 204")
    @DeleteMapping("/students/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        studentService.delete(id);
    }

    // ==================== 统一异常处理 ====================

    /** Service 抛出的业务异常（校验失败、记录不存在）→ HTTP 400 */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(IllegalArgumentException e) {
        return Map.of("error", e.getMessage());
    }
}