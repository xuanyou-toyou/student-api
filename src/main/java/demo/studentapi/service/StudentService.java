package demo.studentapi.service;

import demo.studentapi.dao.StudentDao;
import demo.studentapi.model.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 日志对象。
     *
     * 为什么用 static final：
     *   - static：每个类一份，不随对象创建而重复
     *   - final ：初始化后不再变
     *
     * 为什么用 SLF4J 而不是 System.out：
     *   - 有级别（debug / info / warn / error），可以按级别过滤
     *   - 自动带时间、线程、类名
     *   - 可以配置输出到文件、按天切分
     *   - 用 {} 占位符，日志级别不够时不会执行字符串拼接
     */
    private static final Logger log = LoggerFactory.getLogger(StudentService.class);

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
        log.debug("收到新增学生请求: {}", student);

        // ① 参数校验（最便宜，最先做）
        if (student.getId() == null || student.getId().isEmpty()) {
            log.warn("新增失败：学号为空");
            throw new IllegalArgumentException("学号不能为空");
        }
        if (student.getName() == null || student.getName().isEmpty()) {
            log.warn("新增失败：姓名为空, id={}", student.getId());
            throw new IllegalArgumentException("姓名不能为空");
        }

        // ② 取值范围校验
        if (student.getChinese() > 100 || student.getChinese() < 0
                || student.getMath() > 100 || student.getMath() < 0
                || student.getEnglish() > 100 || student.getEnglish() < 0) {
            log.warn("新增失败：成绩越界, id={}, chinese={}, math={}, english={}",
                    student.getId(), student.getChinese(), student.getMath(), student.getEnglish());
            throw new IllegalArgumentException("成绩必须在 0-100 之间");
        }

        // ③ 唯一性校验（要查数据库，放后面）
        if (findById(student.getId()) != null) {
            log.warn("新增失败：学号已存在, id={}", student.getId());
            throw new IllegalArgumentException("学号 " + student.getId() + " 已存在");
        }

        // ④ 写库
        if (!studentDao.insert(student)) {
            log.error("新增失败：数据库写入失败, id={}", student.getId());
            throw new IllegalStateException("添加失败，请稍后重试");
        }
        log.info("新增学生成功: id={}, name={}", student.getId(), student.getName());
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
        log.debug("收到修改学生请求: id={}, body={}", id, student);

        // ① 参数校验（便宜的先做）
        if (student.getName() == null || student.getName().isEmpty()) {
            log.warn("修改失败：姓名为空, id={}", id);
            throw new IllegalArgumentException("姓名不能为空");
        }

        // ② 取值范围校验
        if (student.getChinese() > 100 || student.getChinese() < 0
                || student.getMath() > 100 || student.getMath() < 0
                || student.getEnglish() > 100 || student.getEnglish() < 0) {
            log.warn("修改失败：成绩越界, id={}", id);
            throw new IllegalArgumentException("成绩必须在 0-100 之间");
        }

        // ③ 存在性校验（要查数据库，放后面）
        if (findById(id) == null) {
            log.warn("修改失败：学号不存在, id={}", id);
            throw new IllegalArgumentException("未找到学号为 " + id + " 的学生");
        }

        // ④ 写库
        if (!studentDao.update(id, student)) {
            log.error("修改失败：数据库写入失败, id={}", id);
            throw new IllegalStateException("更新失败，请稍后重试");
        }
        // 返回给前端的对象补上 id，让响应完整（请求体里本来就没有 id）
        student.setId(id);
        log.info("修改学生成功: id={}, name={}", id, student.getName());
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
        log.debug("收到删除学生请求: id={}", id);
        if (findById(id) == null) {
            log.warn("删除失败：学号不存在, id={}", id);
            throw new IllegalArgumentException("未找到学号为 " + id + " 的学生");
        }
        studentDao.deleteById(id);
        log.info("删除学生成功: id={}", id);
    }

    // ============================================================
    //  批量导入（事务演示）
    // ============================================================
    /**
     * 批量导入学生 —— 要么全部成功，要么全部回滚。
     *
     * @Transactional 的三个要点：
     *
     *   1) 加在 Service 上，不是 Controller
     *      Spring 的事务是「方法级切面」，加在 Controller 上也能生效，
     *      但事务边界应该包住「业务操作」而不是「HTTP 请求解析」。
     *
     *   2) rollbackFor = Exception.class
     *      Spring 默认只在遇到 RuntimeException（和 Error）时回滚，
     *      受检异常（checked exception）不回滚。加上这个参数才保险。
     *
     *   3) 内部不要 try-catch 吞掉异常
     *      异常被 catch 住，Spring 认为方法正常结束 → 提交事务 → 脏数据留下。
     *      要捕获就捕获后重新抛出，或者标记回滚。
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchImport(List<Student> students) {
        if (students == null || students.isEmpty()) {
            throw new IllegalArgumentException("导入列表不能为空");
        }

        log.info("开始批量导入: 共 {} 条", students.size());

        int success = 0;
        for (Student student : students) {
            // 这里刻意不写 try-catch：
            // 任何一条失败，异常向上抛出 → 事务回滚 → 之前插入的全部撤销
            create(student);
            success++;
        }

        log.info("批量导入完成: 成功 {} 条", success);
        return Map.of("imported", success);
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
