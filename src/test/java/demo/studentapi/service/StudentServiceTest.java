package demo.studentapi.service;

import demo.studentapi.dao.StudentDao;
import demo.studentapi.model.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * StudentService 单元测试。
 *
 * 单元测试的原则：
 *   1. 只测「被测类」自己的逻辑，隔离一切外部依赖
 *   2. 不启动 Spring、不连数据库、不启 Web 服务器 —— 毫秒级跑完
 *   3. 测试名要说清「什么条件下应该怎样」
 *
 * @ExtendWith(MockitoExtension.class) 让 Mockito 接管：
 *   @Mock       创建一个假对象（所有方法返回默认值：null / 0 / false）
 *   @InjectMocks 把上面的假对象注入到被测对象里
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StudentService 业务规则")
class StudentServiceTest {

    /** 假的 DAO —— 不连数据库 */
    @Mock
    private StudentDao studentDao;

    /** 被测对象，Mockito 会把 studentDao 注进去 */
    @InjectMocks
    private StudentService studentService;

    /** 构造一个学生的小工具方法（注意构造参数顺序：name 在前，id 在后） */
    private static Student stu(String id, String name, int chinese, int math, int english) {
        return new Student(name, id, chinese, math, english);
    }

    // ==================== 新增：参数校验 ====================

    @Nested
    @DisplayName("新增学生 - 参数校验")
    class CreateValidation {

        @Test
        @DisplayName("学号为空 → 抛异常，且不访问数据库")
        void emptyId_rejected() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> studentService.create(stu("", "张三", 80, 80, 80)));

            assertEquals("学号不能为空", ex.getMessage());
            // 参数校验是「最便宜」的一步，不合格就不该查库
            verifyNoInteractions(studentDao);
        }

        @Test
        @DisplayName("学号为 null → 抛异常")
        void nullId_rejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> studentService.create(stu(null, "张三", 80, 80, 80)));
            verifyNoInteractions(studentDao);
        }

        @Test
        @DisplayName("姓名为空 → 抛异常")
        void emptyName_rejected() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> studentService.create(stu("001", "", 80, 80, 80)));

            assertEquals("姓名不能为空", ex.getMessage());
        }

        @Test
        @DisplayName("成绩超过 100 → 抛异常")
        void scoreTooHigh_rejected() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> studentService.create(stu("001", "张三", 150, 80, 80)));

            assertEquals("成绩必须在 0-100 之间", ex.getMessage());
        }

        @Test
        @DisplayName("成绩为负数 → 抛异常")
        void scoreNegative_rejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> studentService.create(stu("001", "张三", 80, -1, 80)));
        }
    }

    // ==================== 新增：正常与重复 ====================

    @Nested
    @DisplayName("新增学生 - 业务逻辑")
    class CreateBusiness {

        @Test
        @DisplayName("学号已存在 → 抛异常，且不插入")
        void duplicateId_rejected() {
            when(studentDao.findById("001")).thenReturn(stu("001", "已有的人", 80, 80, 80));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> studentService.create(stu("001", "张三", 80, 80, 80)));

            assertTrue(ex.getMessage().contains("已存在"));
            // 学号重复就不该再插入
            verify(studentDao, never()).insert(any());
        }

        @Test
        @DisplayName("合法数据 → 调用 insert，返回带 id 的对象")
        void validStudent_inserted() {
            when(studentDao.findById("001")).thenReturn(null);
            when(studentDao.insert(any(Student.class))).thenReturn(true);

            Student input = stu("001", "张三", 85, 92, 78);
            Student result = studentService.create(input);

            assertEquals("001", result.getId());
            assertEquals("张三", result.getName());

            // 用 ArgumentCaptor 抓出真正传给 DAO 的参数，确认没传错
            ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
            verify(studentDao).insert(captor.capture());
            assertEquals("张三", captor.getValue().getName());
            assertEquals(85, captor.getValue().getChinese());
        }

        @Test
        @DisplayName("DAO 插入失败 → 抛 IllegalStateException")
        void insertFailed_throws() {
            when(studentDao.findById("001")).thenReturn(null);
            when(studentDao.insert(any(Student.class))).thenReturn(false);

            assertThrows(IllegalStateException.class,
                    () -> studentService.create(stu("001", "张三", 80, 80, 80)));
        }
    }

    // ==================== 修改 ====================

    @Nested
    @DisplayName("修改学生")
    class UpdateBusiness {

        @Test
        @DisplayName("学号不存在 → 抛异常")
        void notFound_rejected() {
            when(studentDao.findById("999")).thenReturn(null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> studentService.update("999", stu(null, "张三", 80, 80, 80)));

            assertTrue(ex.getMessage().contains("999"));
        }

        @Test
        @DisplayName("合法修改 → id 以路径参数为准，覆盖请求体里的值")
        void valid_updateUsesPathId() {
            when(studentDao.findById("001")).thenReturn(stu("001", "张三", 80, 80, 80));
            when(studentDao.update(any(), any(Student.class))).thenReturn(true);

            // 请求体里故意塞一个别的 id，应该是路径上的 001 生效
            Student body = stu("错误ID", "张三改", 90, 90, 90);
            Student result = studentService.update("001", body);

            assertEquals("001", result.getId());
            verify(studentDao).update(eq("001"), any(Student.class));
        }
    }

    // ==================== 删除 ====================

    @Nested
    @DisplayName("删除学生")
    class DeleteBusiness {

        @Test
        @DisplayName("学号不存在 → 抛异常，且不调用删除")
        void notFound_rejected() {
            when(studentDao.findById("999")).thenReturn(null);

            assertThrows(IllegalArgumentException.class, () -> studentService.delete("999"));
            verify(studentDao, never()).deleteById(any());
        }

        @Test
        @DisplayName("学号存在 → 调用删除")
        void existing_deleted() {
            when(studentDao.findById("001")).thenReturn(stu("001", "张三", 80, 80, 80));
            when(studentDao.deleteById("001")).thenReturn(true);

            studentService.delete("001");

            verify(studentDao).deleteById("001");
        }
    }

    // ==================== 批量导入 ====================

    @Nested
    @DisplayName("批量导入")
    class BatchImport {

        @Test
        @DisplayName("空列表 → 抛异常")
        void emptyList_rejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> studentService.batchImport(List.of()));
        }

        @Test
        @DisplayName("null → 抛异常")
        void nullList_rejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> studentService.batchImport(null));
        }

        @Test
        @DisplayName("全部合法 → 返回导入条数")
        void allValid_imported() {
            when(studentDao.findById(any())).thenReturn(null);
            when(studentDao.insert(any(Student.class))).thenReturn(true);

            Map<String, Object> result = studentService.batchImport(List.of(
                    stu("101", "甲", 80, 80, 80),
                    stu("102", "乙", 90, 90, 90)));

            assertEquals(2, result.get("imported"));
            verify(studentDao, times(2)).insert(any(Student.class));
        }

        @Test
        @DisplayName("中途有重复学号 → 异常向上抛（事务才能回滚）")
        void duplicateInMiddle_throws() {
            when(studentDao.findById("101")).thenReturn(null);
            when(studentDao.findById("102")).thenReturn(stu("102", "已存在", 80, 80, 80));
            when(studentDao.insert(any(Student.class))).thenReturn(true);

            assertThrows(IllegalArgumentException.class, () ->
                    studentService.batchImport(List.of(
                            stu("101", "甲", 80, 80, 80),
                            stu("102", "乙", 80, 80, 80))));

            // 关键：异常必须抛出去，不能被吞 —— 吞了事务就不会回滚
            // 这里只能验证「抛了」，真正的回滚验证要连数据库做集成测试
        }
    }
}
