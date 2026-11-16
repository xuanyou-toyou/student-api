package demo.studentapi.service;

import demo.studentapi.dao.StudentDao;
import demo.studentapi.model.Student;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 学生业务层（Service）。
 *
 * 为什么要多这一层？Controller 只负责「接请求、回响应」，
 * 校验、统计、业务规则放在 Service，数据库操作放在 DAO。
 *
 *   Controller  →  接 HTTP 请求，转换参数
 *   Service     →  业务规则：校验、逻辑、组合多次 DAO 调用
 *   DAO         →  只管 SQL
 *
 * @Service 注解：告诉 Spring「这是业务层组件，帮我创建并管理它」
 */
@Service
public class StudentService {

    private final StudentDao studentDao;

    public StudentService(StudentDao studentDao) {
        this.studentDao = studentDao;
    }

    // ============================================================
    //  TODO 7：查询全部
    // ============================================================
    /** 直接转调 studentDao.findAll() 即可 */
    public List<Student> findAll() {
        // TODO 7
        return studentDao.findAll();
    }

    // ============================================================
    //  TODO 8：按学号查询
    // ============================================================
    /** 直接转调 studentDao.findById(id) */
    public Student findById(String id) {
        // TODO 8
        return studentDao.findById(id);
    }

    // ============================================================
    //  TODO 9：新增（带业务校验）
    // ============================================================
    /**
     * 规则：
     *   1) 学号、姓名不能为空
     *   2) 三科成绩必须在 0~100 之间
     *   3) 学号不能重复（先查 findById，存在就抛异常）
     *   4) 都通过后调 studentDao.insert(...)
     *
     * 校验失败时抛 IllegalArgumentException("提示信息")，
     * Controller 会捕获并返回 400。
     */
    public Student create(Student student) {
        // TODO 9
        // ① 参数校验（最便宜，最先做）
        if (student.getId() == null || student.getId().isEmpty()) {
            throw new IllegalArgumentException("学号不能为空");
        }
        if (student.getName() == null || student.getName().isEmpty()) {
            throw new IllegalArgumentException("姓名不能为空");
        }

        // ② 取值范围校验
        if (student.getChinese() > 100 || student.getChinese() < 0
                || student.getMath() > 100 || student.getMath() < 0
                || student.getEnglish() > 100 || student.getEnglish() < 0) {
            throw new IllegalArgumentException("成绩必须在 0-100 之间");
        }

        // ③ 唯一性校验（要查数据库，放后面）
        if (findById(student.getId()) != null) {
            throw new IllegalArgumentException("学号 " + student.getId() + " 已存在");
        }

        // ④ 写库
        if (!studentDao.insert(student)) {
            throw new IllegalStateException("添加失败，请稍后重试");
        }
        System.out.println("学生添加成功：" + student.getId());
        return student;
    }

    // ============================================================
    //  TODO 10：修改
    // ============================================================
    /**
     * 规则：
     *   1) 学号必须存在（findById 判断）
     *   2) 成绩范围校验
     *   3) 调 studentDao.update(...)
     *
     * 不存在时抛 IllegalArgumentException("未找到学号为 xxx 的学生")
     */
    public Student update(String id, Student student) {
        // TODO 10
        // ① 参数校验（便宜的先做）
        if (student.getName() == null || student.getName().isEmpty()) {
            throw new IllegalArgumentException("姓名不能为空");
        }

        // ② 取值范围校验
        if (student.getChinese() > 100 || student.getChinese() < 0
                || student.getMath() > 100 || student.getMath() < 0
                || student.getEnglish() > 100 || student.getEnglish() < 0) {
            throw new IllegalArgumentException("成绩必须在 0-100 之间");
        }

        // ③ 存在性校验（要查数据库，放后面）
        if (findById(id) == null) {
            throw new IllegalArgumentException("未找到学号为 " + id + " 的学生");
        }

        // ④ 写库
        if (!studentDao.update(id, student)) {
            throw new IllegalStateException("更新失败，请稍后重试");
        }
        // 返回给前端的对象补上 id，让响应完整（请求体里本来就没有 id）
        student.setId(id);
        return student;
    }

    // ============================================================
    //  TODO 11：删除
    // ============================================================
    /**
     * 不存在时抛 IllegalArgumentException("未找到学号为 xxx 的学生")
     */
    public void delete(String id) {
        // TODO 11
        if (findById(id) == null) {
            throw new IllegalArgumentException("未找到学号为 " + id + " 的学生");
        }
        studentDao.deleteById(id);
    }

    // ============================================================
    //  TODO 12：统计（多个 DAO 结果组合）
    // ============================================================
    /**
     * 返回：
     *   {"count": 学生总数,
     *    "avgChinese": 语文平均分(2 位小数),
     *    "maxMath": 数学最高分}
     *
     * 做法：拿到 findAll() 的列表后在内存里算，比写三条聚合 SQL 简单。
     *
     * 提示：
     *   int count = list.size();
     *   double avgChinese = list.stream().mapToInt(Student::getChinese).average().orElse(0);
     *   int maxMath = list.stream().mapToInt(Student::getMath).max().orElse(0);
     *   return Map.of("count", count,
     *                 "avgChinese", Math.round(avgChinese * 100) / 100.0,
     *                 "maxMath", maxMath);
     */
    public Map<String, Object> statistics() {
        // TODO 12
        List<Student> list = findAll();
        int count = list.size();
        double avgChinese = list.stream().mapToInt(Student::getChinese).average().orElse(0);
        int maxMath = list.stream().mapToInt(Student::getMath).max().orElse(0);
        return Map.of("count", count, "avgChinese", Math.round(avgChinese * 100) / 100.0,"maxMath", maxMath);
    }
}
