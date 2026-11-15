package demo.studentapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 学生接口。
 *
 * @RestController = @Controller + @ResponseBody
 *                   返回值自动转成 JSON
 */
@RestController
@RequestMapping("/api")
public class StudentController {

    /** W8 先用内存数据，W9 再接数据库 */
    private final List<Map<String, Object>> students = List.of(
            Map.of("id", "001", "name", "张三", "chinese", 85, "math", 92, "english", 78),
            Map.of("id", "002", "name", "李四", "chinese", 99, "math", 62, "english", 75),
            Map.of("id", "003", "name", "王五", "chinese", 58, "math", 58, "english", 58)
    );

    /** GET /api/hello */
    @GetMapping("/hello/{name}")
    public String hello(@PathVariable String name) {
        return "Hello," + name;
    }

    /** GET /api/students —— 返回全部学生 */
    @GetMapping("/students")
    public List<Map<String, Object>> list() {
        return students;
    }

    /** GET /api/students/count —— 查数量 */
    @GetMapping("/students/count")
    public int count() {
        return students.size();
    }

    /** GET /api/students/failing —— 返回有不及格科目的学生 */
    @GetMapping("/students/failing")
    public List<Map<String, Object>> failing() {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> m : students) {
            int chinese = (int) m.get("chinese");
            int math    = (int) m.get("math");
            int english = (int) m.get("english");

            if (chinese < 60 || math < 60 || english < 60) {
                result.add(m);
            }
        }
        return result;
    }

    /** GET /api/statistics —— 返回统计信息 */
    @GetMapping("/statistics")
    public Map<String, Object> statistics() {
        int count = students.size();

        double avgChinese = students.stream()
                .mapToInt(s -> (int) s.get("chinese"))
                .average()
                .orElse(0);

        int maxMath = students.stream()
                .mapToInt(s -> (int) s.get("math"))
                .max()
                .orElse(0);

        return Map.of(
                "count", count,
                "avgChinese", Math.round(avgChinese * 100) / 100.0,
                "maxMath", maxMath);
    }

    /** GET /api/students/001 —— 路径参数 */
    @GetMapping("/students/{id}")
    public Map<String, Object> getById(@PathVariable String id) {
        return students.stream()
                .filter(s -> s.get("id").equals(id))
                .findFirst()
                .orElse(Map.of("error", "未找到学号为 " + id + " 的学生"));
    }
}