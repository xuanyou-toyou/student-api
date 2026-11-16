package demo.studentapi.model;

/**
 * 学生实体。
 *
 * 对应数据库表 student_db.student：
 *   id VARCHAR(20) PRIMARY KEY
 *   name VARCHAR(20)
 *   chinese / math / english INT
 *
 * 注意字段顺序：构造方法第一个参数是 name，第二个是 id ——
 * 这是从 hello-java 项目继承下来的约定，别搞反（W4 踩过这个坑）。
 */
public class Student {

    private String name;
    private String id;
    private int chinese;
    private int math;
    private int english;

    public Student() {
    }

    public Student(String name, String id, int chinese, int math, int english) {
        this.name = name;
        this.id = id;
        this.chinese = chinese;
        this.math = math;
        this.english = english;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getChinese() {
        return chinese;
    }

    public void setChinese(int chinese) {
        this.chinese = chinese;
    }

    public int getMath() {
        return math;
    }

    public void setMath(int math) {
        this.math = math;
    }

    public int getEnglish() {
        return english;
    }

    public void setEnglish(int english) {
        this.english = english;
    }

    /** 总分 —— 有 getter 才会出现在 JSON 里 */
    public int getTotal() {
        return chinese + math + english;
    }

    /** 平均分 */
    public double getAverage() {
        return Math.round(getTotal() / 3.0 * 10) / 10.0;
    }

    @Override
    public String toString() {
        return "Student{id='" + id + "', name='" + name + "', chinese=" + chinese
                + ", math=" + math + ", english=" + english + '}';
    }
}
