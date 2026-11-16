package demo.studentapi.controller;

import demo.studentapi.model.Student;
import demo.studentapi.service.StudentService;
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
    @GetMapping("/students")
    public List<Student> list() {
        return studentService.findAll();
    }

    /** GET /api/students/count —— 查总数 */
    @GetMapping("/students/count")
    public int count() {
        return studentService.findAll().size();
    }

    /** GET /api/statistics —— 统计信息 */
    @GetMapping("/statistics")
    public Map<String, Object> statistics() {
        return studentService.statistics();
    }

    /** GET /api/students/failing —— 有不及格科目的学生 */
    @GetMapping("/students/failing")
    public List<Student> failing() {
        return studentService.findAll().stream()
                .filter(s -> s.getChinese() < 60 || s.getMath() < 60 || s.getEnglish() < 60)
                .toList();
    }

    /** GET /api/students/001 —— 查单个，查不到返回 404 */
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
    @PostMapping("/students")
    public Student create(@RequestBody Student student) {
        return studentService.create(student);
    }

    /** PUT /api/students/001 —— 修改（id 以路径为准） */
    @PutMapping("/students/{id}")
    public Student update(@PathVariable String id, @RequestBody Student student) {
        return studentService.update(id, student);
    }

    /** DELETE /api/students/001 —— 删除 */
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
