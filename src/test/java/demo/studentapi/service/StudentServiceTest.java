package demo.studentapi.service;

import demo.studentapi.dao.StudentDao;
import demo.studentapi.model.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * StudentService 单元测试。
 *
 * 原则：
 *   1. 只测 StudentService 自己的业务逻辑，隔离 DAO
 *   2. 不启动 Spring、不连数据库 —— 毫秒级完成
 *   3. 测试名说清「什么条件 → 什么结果」
 */
@ExtendWith(MockitoExtension.class)      // ① 让 Mockito 生效
@DisplayName("StudentService 业务规则")
class StudentServiceTest {

    @Mock                                // ② 假 DAO
    private StudentDao studentDao;

    @InjectMocks                         // ③ 被测对象，假 DAO 会自动注进去
    private StudentService studentService;

    // ④ 小工具：造学生对象（注意构造器顺序 name 在前！）
    private static Student stu(String id, String name, int chinese, int math, int english) {
        return new Student(name, id, chinese, math, english);
    }

    // ============================================================
    //  新增 —— 参数校验
    // ============================================================

    @Test
    @DisplayName("学号为空 -> 抛异常，且不访问数据库")
    void emptyId_rejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> studentService.create(stu("", "张三", 80, 80, 80)));

        assertEquals("学号不能为空", ex.getMessage());
        // 校验是最便宜的一步，不合格就不该查库
        verifyNoInteractions(studentDao);
    }

    @Test
    @DisplayName("姓名为空 -> 抛异常")
    void emptyName_rejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> studentService.create(stu("001", "", 80, 80, 80)));

        assertEquals("姓名不能为空", ex.getMessage());
    }

    @Test
    @DisplayName("成绩超过 100 -> 抛异常")
    void scoreTooHigh_rejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> studentService.create(stu("001", "张三", 180, 80, 80)));

        assertEquals("成绩必须在 0-100 之间", ex.getMessage());
    }

    // ============================================================
    //  新增 —— 业务逻辑
    // ============================================================

    @Test
    @DisplayName("学号已存在 -> 抛异常，且不执行插入")
    void duplicateId_rejected() {
        when(studentDao.findById("001")).thenReturn(stu("001", "已存在的人", 80, 80, 80));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> studentService.create(stu("001", "张三", 80, 80, 80)));

        assertTrue(ex.getMessage().contains("已存在"));
        // 学号重复就不该再插入
        verify(studentDao, never()).insert(any());
    }

    @Test
    @DisplayName("合法数据 -> 调用 insert，返回带 id 的对象")
    void validStudent_inserted() {
        when(studentDao.findById("001")).thenReturn(null);
        when(studentDao.insert(any(Student.class))).thenReturn(true);

        Student result = studentService.create(stu("001", "张三", 85, 92, 78));

        assertEquals("001", result.getId());
        assertEquals("张三", result.getName());
        verify(studentDao).insert(any(Student.class));   // 确认真的调了
    }

    // ============================================================
    //  批量导入
    // ============================================================

    @Test
    @DisplayName("批量导入空列表 -> 抛异常")
    void batchImport_empty_rejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> studentService.batchImport(List.of()));

        assertEquals("导入列表不能为空", ex.getMessage());
    }
}
