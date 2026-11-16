package demo.studentapi.dao;

import demo.studentapi.model.Student;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 学生数据访问层（DAO）。
 *
 * 对比 hello-java（W7）你手写的 JDBC：
 *
 *   W7 的写法                              W9 的写法
 *   ----------------------------------     --------------------------------
 *   DbUtil.getConnection()                 不需要，JdbcTemplate 自动管理
 *   conn.prepareStatement(sql)             不需要
 *   ps.setString(1, id)                    直接把参数传给 query/update
 *   try-with-resources 关资源              不需要，框架处理
 *   ResultSet rs = ps.executeQuery()       不需要，框架帮你映射
 *   while (rs.next()) { ... }              不需要，一行代码拿到 List<Student>
 *
 * @Repository 注解：告诉 Spring「这是数据访问层的组件，帮我创建并管理它」
 */
@Repository
public class StudentDao {

    /** Spring 会自动注入一个可用的 JdbcTemplate（因为配好了数据源） */
    private final JdbcTemplate jdbcTemplate;
    /** ResultSet 的一行 → Student 对象（所有查询共用）*/
    private static final RowMapper<Student> STUDENT_ROW_MAPPER = (rs, rowNum) -> {
        Student s = new Student();
        s.setId(rs.getString("id"));
        s.setName(rs.getString("name"));
        s.setChinese(rs.getInt("chinese"));
        s.setMath(rs.getInt("math"));
        s.setEnglish(rs.getInt("english"));
        return s;
    };

    public StudentDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ============================================================
    //  TODO 1：查询全部
    // ============================================================
    /**
     * SQL：SELECT id, name, chinese, math, english FROM student ORDER BY id
     *
     * 模板：
     *
     *   String sql = "...";
     *   return jdbcTemplate.query(sql, (rs, rowNum) -> {
     *       Student s = new Student();
     *       s.setId(rs.getString("id"));
     *       s.setName(rs.getString("name"));
     *       s.setChinese(rs.getInt("chinese"));
     *       s.setMath(rs.getInt("math"));
     *       s.setEnglish(rs.getInt("english"));
     *       return s;
     *   });
     *
     * 说明：
     *   jdbcTemplate.query(sql, rowMapper) 会替你执行 SQL、遍历 ResultSet、
     *   把每一行交给括号里的 lambda 转成对象，最后返回 List。
     */
    public List<Student> findAll() {
        // TODO 1 写在这里
        String sql = "SELECT id, name, chinese, math, english FROM student ORDER BY id";
        return jdbcTemplate.query(sql, STUDENT_ROW_MAPPER);
    }

    // ============================================================
    //  TODO 2：按学号查单个
    // ============================================================
    /**
     * SQL：SELECT ... FROM student WHERE id = ?
     *
     * 模板：
     *
     *   String sql = "SELECT ... FROM student WHERE id = ?";
     *   List<Student> list = jdbcTemplate.query(sql, rowMapper, id);
     *   return list.isEmpty() ? null : list.get(0);
     *
     * 提示：
     *   - 参数直接跟在 rowMapper 后面，有几个 ? 就传几个，顺序对应
     *   - 查不到时返回的是空 List，要自己判断后返回 null
     *   - 更简洁的写法是用 jdbcTemplate.queryForObject + 捕获 EmptyResultDataAccessException，
     *     但初学建议先用 query 再判断
     */
    public Student findById(String id) {
        // TODO 2 写在这里
        String sql = "SELECT * FROM student WHERE id = ?";
        List<Student> list = jdbcTemplate.query(sql, STUDENT_ROW_MAPPER, id);
        if(list.isEmpty()){
            return null;
        }
        return list.getFirst();
    }

    // ============================================================
    //  TODO 3：新增
    // ============================================================
    /**
     * SQL：INSERT INTO student (id, name, chinese, math, english) VALUES (?, ?, ?, ?, ?)
     *
     * 模板：
     *
     *   String sql = "...";
     *   int rows = jdbcTemplate.update(sql,
     *           student.getId(), student.getName(),
     *           student.getChinese(), student.getMath(), student.getEnglish());
     *   return rows > 0;
     *
     * 说明：
     *   增删改都用 update()，返回「影响的行数」
     */
    public boolean insert(Student student) {
        // TODO 3 写在这里
        String sql = "INSERT INTO student (id, name, chinese, math, english) VALUES (?, ?, ?, ?, ?)";
        int row = jdbcTemplate.update(sql,
                student.getId(), student.getName(),
                student.getChinese(),student.getMath(), student.getEnglish());
        return row > 0;
    }

    // ============================================================
    //  TODO 4：修改
    // ============================================================
    /**
     * SQL：UPDATE student SET name = ?, chinese = ?, math = ?, english = ? WHERE id = ?
     *
     * 注意参数顺序：WHERE 的 id 放最后
     */
    public boolean update(String id, Student student) {
        // TODO 4 写在这里
        String sql = "UPDATE student SET name = ?, chinese = ?, math = ?, english = ? WHERE id = ?";
        int row = jdbcTemplate.update(sql,
                student.getName(),
                student.getChinese(), student.getMath(), student.getEnglish(),
                id);
        return row > 0;
    }

    // ============================================================
    //  TODO 5：删除
    // ============================================================
    /**
     * SQL：DELETE FROM student WHERE id = ?
     */
    public boolean deleteById(String id) {
        // TODO 5 写在这里
        String sql = "DELETE FROM student WHERE id = ?";
        int row = jdbcTemplate.update(sql, id);
        return row > 0;
    }

    // ============================================================
    //  TODO 6（进阶）：统计
    // ============================================================
    /**
     * 统计学生数量。
     *
     * 模板：
     *
     *   Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM student", Integer.class);
     *   return count == null ? 0 : count;
     */
    public int count() {
        // TODO 6 写在这里
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM student", Integer.class);
        return count == null ? 0 : count;
    }
}
