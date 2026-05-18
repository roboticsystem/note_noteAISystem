package com.noteaiBackend.config;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.noteaiBackend.entity.AssignmentSummary;
import com.noteaiBackend.entity.ClassJoined;
import com.noteaiBackend.entity.Clazz;
import com.noteaiBackend.entity.Note;
import com.noteaiBackend.entity.NoteAtt;
import com.noteaiBackend.entity.NoteComment;
import com.noteaiBackend.entity.NoteInteraction;
import com.noteaiBackend.entity.NoteTag;
import com.noteaiBackend.entity.NoteVersion;
import com.noteaiBackend.entity.Report;
import com.noteaiBackend.entity.Tag;
import com.noteaiBackend.entity.Task;
import com.noteaiBackend.entity.User;
import com.noteaiBackend.repository.AssignmentSummaryRepository;
import com.noteaiBackend.repository.ClassJoinedRepository;
import com.noteaiBackend.repository.ClazzRepository;
import com.noteaiBackend.repository.NoteAttRepository;
import com.noteaiBackend.repository.NoteCommentRepository;
import com.noteaiBackend.repository.NoteInteractionRepository;
import com.noteaiBackend.repository.NoteRepository;
import com.noteaiBackend.repository.NoteTagRepository;
import com.noteaiBackend.repository.NoteVersionRepository;
import com.noteaiBackend.repository.ReportRepository;
import com.noteaiBackend.repository.TagRepository;
import com.noteaiBackend.repository.TaskRepository;
import com.noteaiBackend.repository.UserRepository;

/**
 * 数据初始化器 - 在首次启动时自动加载演示种子数据
 * 
 * 工作原理：
 * 1. 检查数据库中是否已有用户数据（以 user 表是否有记录为准）
 * 2. 如果没有数据，则执行初始化插入
 * 3. 如果已有数据，则跳过初始化
 * 
 * 适用场景：
 * - Coolify 部署：首次启动会自动填充数据
 * - Docker Compose 部署：同上
 * - 本地开发：首次启动自动填充
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ClazzRepository clazzRepository;
    private final ClassJoinedRepository classJoinedRepository;
    private final TaskRepository taskRepository;
    private final NoteRepository noteRepository;
    private final NoteCommentRepository noteCommentRepository;
    private final NoteInteractionRepository noteInteractionRepository;
    private final NoteVersionRepository noteVersionRepository;
    private final TagRepository tagRepository;
    private final NoteTagRepository noteTagRepository;
    private final NoteAttRepository noteAttRepository;
    private final AssignmentSummaryRepository assignmentSummaryRepository;
    private final ReportRepository reportRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ClazzRepository clazzRepository,
                           ClassJoinedRepository classJoinedRepository,
                           TaskRepository taskRepository,
                           NoteRepository noteRepository,
                           NoteCommentRepository noteCommentRepository,
                           NoteInteractionRepository noteInteractionRepository,
                           NoteVersionRepository noteVersionRepository,
                           TagRepository tagRepository,
                           NoteTagRepository noteTagRepository,
                           NoteAttRepository noteAttRepository,
                           AssignmentSummaryRepository assignmentSummaryRepository,
                           ReportRepository reportRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.clazzRepository = clazzRepository;
        this.classJoinedRepository = classJoinedRepository;
        this.taskRepository = taskRepository;
        this.noteRepository = noteRepository;
        this.noteCommentRepository = noteCommentRepository;
        this.noteInteractionRepository = noteInteractionRepository;
        this.noteVersionRepository = noteVersionRepository;
        this.tagRepository = tagRepository;
        this.noteTagRepository = noteTagRepository;
        this.noteAttRepository = noteAttRepository;
        this.assignmentSummaryRepository = assignmentSummaryRepository;
        this.reportRepository = reportRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("数据库已有数据，跳过种子数据初始化");
            return;
        }

        log.info("================================");
        log.info("开始初始化演示种子数据...");
        log.info("================================");

        String encodedPassword = passwordEncoder.encode("123456");
        LocalDateTime now = LocalDateTime.now();

        // ============================================================
        // 1. 用户数据
        // ============================================================
        User student1 = createUser(1, "张三丰", "student001", encodedPassword, "张三", "13800138001", null, 20, "好好学习，天天向上", 1, now.minusMonths(3).minusDays(15), 1, 1);
        User student2 = createUser(2, "李思琪", "student002", encodedPassword, "李思琪", "13800138002", null, 21, "热爱编程的女生", 1, now.minusMonths(3).minusDays(14), 0, 1);
        User student3 = createUser(3, "王小明", "student003", encodedPassword, "王小明", "13800138003", null, 19, "", 0, now.minusMonths(3).minusDays(13), 1, 1); // 被屏蔽
        User student4 = createUser(4, "赵雪婷", "student004", encodedPassword, "赵雪婷", "13800138004", null, 22, "努力学习中...", 1, now.minusMonths(3).minusDays(12), 0, 1);
        User student5 = createUser(5, "陈浩宇", "student005", encodedPassword, "陈浩宇", "13800138005", null, 20, "梦想是成为全栈工程师", 1, now.minusMonths(3).minusDays(11), 1, 1);
        User student6 = createUser(6, "林小雅", "student006", encodedPassword, "林小雅", "13800138006", null, 21, "前端开发爱好者", 1, now.minusMonths(3).minusDays(10), 0, 1);
        User teacher1 = createUser(7, "周明远", "teacher001", encodedPassword, "周明远", "13900139001", null, 35, "计算机科学教授，研究方向：人工智能", 1, now.minusMonths(4), 1, 2);
        User teacher2 = createUser(8, "吴芳华", "teacher002", encodedPassword, "吴芳华", "13900139002", null, 32, "软件工程讲师，全栈开发实践者", 1, now.minusMonths(4).plusDays(5), 0, 2);
        User admin = createUser(9, "系统管理员", "admin001", encodedPassword, "管理员", "13000130001", null, 28, "系统管理员", 1, now.minusMonths(5), 1, 3);

        userRepository.saveAll(List.of(student1, student2, student3, student4, student5, student6, teacher1, teacher2, admin));
        log.info("✓ 已创建 9 个用户（6学生 + 2教师 + 1管理员），默认密码：123456");

        // ============================================================
        // 2. 课程数据
        // ============================================================
        Clazz c1 = createClazz(1, "Java面向对象程序设计", 7, "Java OOP 基础课程，涵盖类、继承、多态、接口等核心概念", now.minusMonths(3).minusDays(6), 1);
        Clazz c2 = createClazz(2, "数据结构与算法", 7, "常见数据结构（链表、栈、队列、树、图）及经典算法", now.minusMonths(3).minusDays(6), 1);
        Clazz c3 = createClazz(3, "人工智能导论", 7, "AI 基础概念、机器学习、深度学习入门", now.minusMonths(3).minusDays(5), 1);
        Clazz c4 = createClazz(4, "Web前端开发技术", 8, "HTML/CSS/JavaScript/Vue.js 全栈前端开发", now.minusMonths(3).minusDays(4), 1);
        Clazz c5 = createClazz(5, "软件工程实践", 8, "软件工程方法论与实践", now.minusMonths(3).minusDays(3), 0); // 被屏蔽

        clazzRepository.saveAll(List.of(c1, c2, c3, c4, c5));
        log.info("✓ 已创建 5 门课程（含 1 门已屏蔽）");

        // ============================================================
        // 3. 学生选课数据
        // ============================================================
        classJoinedRepository.saveAll(List.of(
            // 学生1（张三）
            createClassJoined(1, 1, 1, 1, now.minusMonths(3).minusDays(4)),
            createClassJoined(2, 2, 1, 1, now.minusMonths(3).minusDays(4)),
            createClassJoined(3, 3, 1, 1, now.minusMonths(3).minusDays(4)),
            createClassJoined(4, 4, 1, 1, now.minusMonths(3).minusDays(3)),
            // 学生2（李思琪）
            createClassJoined(5, 1, 2, 1, now.minusMonths(3).minusDays(4)),
            createClassJoined(6, 2, 2, 1, now.minusMonths(3).minusDays(4)),
            createClassJoined(7, 4, 2, 1, now.minusMonths(3).minusDays(3)),
            // 学生3（王小明-被屏蔽）
            createClassJoined(8, 1, 3, 1, now.minusMonths(3).minusDays(4)),
            createClassJoined(9, 3, 3, 0, now.minusMonths(3).minusDays(4)), // 已退课
            // 学生4（赵雪婷）
            createClassJoined(10, 2, 4, 1, now.minusMonths(3).minusDays(3)),
            createClassJoined(11, 4, 4, 1, now.minusMonths(3).minusDays(3)),
            createClassJoined(12, 5, 4, 1, now.minusMonths(3).minusDays(3)), // 加入被屏蔽课程
            // 学生5（陈浩宇）
            createClassJoined(13, 1, 5, 1, now.minusMonths(3).minusDays(2)),
            createClassJoined(14, 2, 5, 1, now.minusMonths(3).minusDays(2)),
            createClassJoined(15, 3, 5, 1, now.minusMonths(3).minusDays(2)),
            createClassJoined(16, 4, 5, 1, now.minusMonths(3).minusDays(2)),
            // 学生6（林小雅）
            createClassJoined(17, 1, 6, 1, now.minusMonths(3).minusDays(1)),
            createClassJoined(18, 4, 6, 1, now.minusMonths(3).minusDays(1))
        ));
        log.info("✓ 已创建 18 条选课记录（含 1 条退课记录）");

        // ============================================================
        // 4. 作业数据
        // ============================================================
        Task t1 = createTask(1, 1, "Java基础语法练习", "编写一个学生信息管理系统，包含增删改查功能", now.minusMonths(2).minusDays(15), false, now.minusMonths(3), null);
        Task t2 = createTask(2, 1, "面向对象设计-图书管理系统", "使用面向对象思想设计图书管理系统，包含继承和多态", now.minusMonths(2).minusDays(1), false, now.minusMonths(2).minusDays(15), null);
        Task t3 = createTask(3, 1, "Java GUI计算器", "使用Swing或JavaFX实现一个简易计算器", now.plusDays(12), true, now, null); // 未过期
        Task t4 = createTask(4, 2, "链表实现与应用", "实现单向链表、双向链表，并完成链表反转算法", now.minusMonths(2).minusDays(6), false, now.minusMonths(2).minusDays(20), null);
        Task t5 = createTask(5, 2, "二叉树遍历算法", "实现二叉树的前序、中序、后序、层序遍历", now.plusDays(7), false, now.minusDays(8), null); // 未过期
        Task t6 = createTask(6, 3, "机器学习基础概念报告", "撰写一份关于监督学习和无监督学习区别的报告", now.minusMonths(1).minusDays(25), false, now.minusMonths(2).minusDays(15), null);
        Task t7 = createTask(7, 3, "KNN算法实现", "使用Python实现K近邻分类算法，并在鸢尾花数据集上测试", now.plusDays(14), true, now.minusDays(3), null); // 未过期
        Task t8 = createTask(8, 4, "HTML+CSS静态页面", "模仿淘宝首页制作一个静态页面", now.minusMonths(2).minusDays(11), false, now.minusMonths(3), null);
        Task t9 = createTask(9, 4, "JavaScript交互效果", "实现一个待办事项列表（To-Do List），支持增删改", now.minusMonths(1).minusDays(21), false, now.minusMonths(2).minusDays(10), null);
        Task t10 = createTask(10, 4, "Vue组件化开发", "使用Vue3组合式API开发一个简单的博客系统前端", now.plusDays(23), true, now.minusDays(1), null); // 未过期
        Task t11 = createTask(11, 5, "需求分析文档", "编写一份完整的软件需求规格说明书", now.minusMonths(1).minusDays(15), false, now.minusMonths(2).minusDays(15), null);

        taskRepository.saveAll(List.of(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11));
        log.info("✓ 已创建 11 个作业（含 4 个未过期作业）");

        // ============================================================
        // 5. 笔记数据
        // ============================================================
        Note n1 = createNote(1, "学生信息管理系统 - 张三的实现", "详细的实现代码...", 1, 1, (byte)1, now.minusMonths(2).minusDays(18), now.minusMonths(2).minusDays(17), 1, "Java, OOP", "实现了基本的学生信息增删改查功能", 1);
        Note n2 = createNote(2, "链表实现详解 - 张三", "链表实现代码...", 1, 4, (byte)2, now.minusMonths(2).minusDays(8), now.minusMonths(2).minusDays(7), 1, "数据结构, 链表", "完成了单向和双向链表的实现", 1);
        Note n3 = createNote(3, "监督学习与无监督学习对比分析", "对比分析内容...", 1, 6, (byte)0, now.minusMonths(1).minusDays(28), now.minusMonths(1).minusDays(28), 1, "AI, 机器学习", "对比分析了监督学习和无监督学习的核心区别", 0);
        Note n4 = createNote(4, "学生管理系统 - 思琪版", "使用HashMap实现...", 2, 1, (byte)1, now.minusMonths(2).minusDays(18), now.minusMonths(2).minusDays(17), 1, "Java, 集合", "使用HashMap实现学生管理系统", 1);
        Note n5 = createNote(5, "仿淘宝首页 - 完整实现", "完整的HTML/CSS代码...", 2, 8, (byte)2, now.minusMonths(2).minusDays(13), now.minusMonths(2).minusDays(12), 1, "HTML, CSS, 前端", "完整实现了仿淘宝首页", 1);
        Note n6 = createNote(6, "To-Do List 交互实现", "JavaScript实现...", 2, 9, (byte)1, now.minusMonths(1).minusDays(23), now.minusMonths(1).minusDays(22), 1, "JavaScript, 前端", "实现了完整待办事项功能", 0);
        Note n7 = createNote(7, "Java作业 - 简单实现", "简单的实现...", 3, 1, (byte)1, now.minusMonths(2).minusDays(19), now.minusMonths(2).minusDays(19), 0, "Java", "简单的实现", 1); // 被屏蔽
        Note n8 = createNote(8, "ML学习笔记", "机器学习笔记内容...", 3, 6, (byte)0, now.minusMonths(1).minusDays(27), now.minusMonths(1).minusDays(27), 1, "AI", "机器学习基本概念笔记", 0);
        Note n9 = createNote(9, "二叉树遍历 - 完整实现", "Python实现二叉树遍历...", 4, 5, (byte)1, now.minusDays(1), now, 1, "数据结构, 二叉树, Python", "实现了二叉树四种遍历算法", 0); // 未批改
        Note n10 = createNote(10, "淘宝首页仿写 - 雪婷版", "Flex+Grid布局...", 4, 8, (byte)2, now.minusMonths(2).minusDays(12), now.minusMonths(2).minusDays(11), 1, "HTML, CSS, Flexbox", "使用Flex+Grid布局仿写淘宝首页", 1);
        Note n11 = createNote(11, "在线书店系统 - 需求规格说明书", "完整SRS文档...", 4, 11, (byte)1, now.minusMonths(1).minusDays(17), now.minusMonths(1).minusDays(17), 1, "软件工程, 文档", "完成了一份完整的SRS", 0);
        Note n12 = createNote(12, "图书管理系统 - 完整的OOP设计", "OOP设计实现...", 5, 2, (byte)0, now.minusMonths(2).minusDays(2), now.minusMonths(2).minusDays(1), 1, "Java, OOP, 设计模式", "运用OOP特性设计图书管理系统", 1);
        Note n13 = createNote(13, "JavaFX计算器 - 开发中", "JavaFX实现...", 5, 3, (byte)1, now.plusDays(1), now.plusDays(1), 1, "Java, JavaFX, GUI", "使用JavaFX开发计算器", 0); // 未过期任务笔记
        Note n14 = createNote(14, "To-Do List - 优雅实现", "使用class语法实现...", 6, 9, (byte)1, now.minusMonths(1).minusDays(22), now.minusMonths(1).minusDays(21), 1, "JavaScript, 前端, 本地存储", "实现了带本地存储和拖拽排序的Todo应用", 1);
        Note n15 = createNote(15, "初学Java - 学生管理", "简单的CRUD操作...", 6, 1, (byte)0, now.minusMonths(2).minusDays(18), now.minusMonths(2).minusDays(18), 1, "Java", "初学Java的练习作品", 0);
        Note n16 = createNote(16, "Vue3博客系统 - 组件化实现", "Vue3组件化实现...", 6, 10, (byte)2, now.minusDays(1), now, 1, "Vue, 前端, 组件化", "使用Vue3开发博客系统", 0); // 未批改
        // 自由笔记（非作业关联）
        Note n17 = createNote(17, "我的Java学习心得", "Java学习总结...", 1, null, (byte)2, now.minusMonths(1).minusDays(6), now.minusMonths(1).minusDays(4), 1, "Java, JVM, 学习心得", "总结了Java学习的关键知识点", 0);
        Note n18 = createNote(18, "前端学习笔记 - CSS Grid布局", "CSS Grid完全指南...", 2, null, (byte)2, now.minusMonths(1).minusDays(11), now.minusMonths(1).minusDays(10), 1, "CSS, 前端, 布局", "CSS Grid布局总结", 0);
        Note n19 = createNote(19, "算法刷题笔记", "刷题进度记录...", 5, null, (byte)0, now.minusDays(17), now.minusDays(3), 1, "算法, 刷题, 数据结构", "算法刷题进度追踪", 0);
        Note n20 = createNote(20, "违规内容", "包含不当言论的内容...", 2, null, (byte)1, now.minusMonths(2).minusDays(15), now.minusMonths(2).minusDays(15), 0, "违规", null, 0); // 被屏蔽

        noteRepository.saveAll(List.of(n1, n2, n3, n4, n5, n6, n7, n8, n9, n10, n11, n12, n13, n14, n15, n16, n17, n18, n19, n20));
        log.info("✓ 已创建 20 篇笔记（含 2 篇已屏蔽，覆盖各种可见性和批改状态）");

        // ============================================================
        // 6. 笔记评价数据
        // ============================================================
        noteCommentRepository.saveAll(List.of(
            createNoteComment(1, 7, 1, "代码结构清晰，功能完整。建议增加异常处理和数据持久化功能。", 85, now.minusMonths(2).minusDays(14)),
            createNoteComment(2, 7, 2, "链表实现完整，反转算法思路清晰。测试用例覆盖全面，非常好！", 92, now.minusMonths(2).minusDays(6)),
            createNoteComment(3, 7, 4, "使用HashMap的思路很好，性能优于ArrayList版本。但缺少注释说明。", 78, now.minusMonths(2).minusDays(14)),
            createNoteComment(4, 7, 7, "代码过于简单，缺少必要的功能模块，请完善后再提交。", 50, now.minusMonths(2).minusDays(15)),
            createNoteComment(5, 7, 12, "OOP设计非常出色，使用了接口隔离和依赖注入原则，体现了扎实的设计功底。", 95, now.minusMonths(2)),
            createNoteComment(6, 8, 5, "页面结构完整，CSS还原度很高！建议增加移动端自适应。", 90, now.minusMonths(2).minusDays(10)),
            createNoteComment(7, 8, 10, "Flex和Grid结合得很好，轮播图效果流畅。响应式设计到位。", 88, now.minusMonths(2).minusDays(10)),
            createNoteComment(8, 8, 14, "代码非常优雅，使用了ES6+的新特性。拖拽排序功能实现巧妙。", 93, now.minusMonths(1).minusDays(21))
        ));
        log.info("✓ 已创建 8 条教师评价");

        // ============================================================
        // 7. 笔记互动数据
        // ============================================================
        noteInteractionRepository.saveAll(List.of(
            // 已阅记录 (type=1)
            createNoteInteraction(1, 2, 2, 1, now.minusMonths(2).minusDays(6)),
            createNoteInteraction(2, 5, 1, 1, now.minusMonths(2).minusDays(10)),
            createNoteInteraction(3, 5, 4, 1, now.minusMonths(2).minusDays(10)),
            createNoteInteraction(4, 10, 1, 1, now.minusMonths(2).minusDays(9)),
            createNoteInteraction(5, 10, 2, 1, now.minusMonths(2).minusDays(9)),
            createNoteInteraction(6, 12, 1, 1, now.minusMonths(2)),
            createNoteInteraction(7, 2, 5, 1, now.minusMonths(2).minusDays(5)),
            createNoteInteraction(8, 18, 1, 1, now.minusMonths(1).minusDays(9)),
            createNoteInteraction(9, 17, 5, 1, now.minusMonths(1).minusDays(3)),
            createNoteInteraction(10, 14, 2, 1, now.minusMonths(1).minusDays(21)),
            // 点赞记录 (type=2)
            createNoteInteraction(11, 2, 2, 2, now.minusMonths(2).minusDays(6)),
            createNoteInteraction(12, 5, 4, 2, now.minusMonths(2).minusDays(10)),
            createNoteInteraction(13, 10, 1, 2, now.minusMonths(2).minusDays(9)),
            createNoteInteraction(14, 12, 1, 2, now.minusMonths(2)),
            createNoteInteraction(15, 14, 2, 2, now.minusMonths(1).minusDays(21)),
            createNoteInteraction(16, 18, 1, 2, now.minusMonths(1).minusDays(9)),
            createNoteInteraction(17, 5, 1, 2, now.minusMonths(2).minusDays(10)),
            createNoteInteraction(18, 17, 5, 2, now.minusMonths(1).minusDays(3)),
            createNoteInteraction(19, 2, 4, 2, now.minusMonths(2).minusDays(4)),
            createNoteInteraction(20, 9, 1, 2, now)
        ));
        log.info("✓ 已创建 20 条互动记录（10条已阅 + 10条点赞）");

        // ============================================================
        // 8. 笔记版本历史
        // ============================================================
        noteVersionRepository.saveAll(List.of(
            createNoteVersion(1, 1, 1, "学生管理系统 - V1", "初始版本：基本CRUD功能", 1, "首次提交", now.minusMonths(2).minusDays(18)),
            createNoteVersion(2, 1, 2, "学生管理系统 - 最终版", "增加了搜索和排序功能，优化了代码结构", 1, "完善功能和代码优化", now.minusMonths(2).minusDays(17)),
            createNoteVersion(3, 2, 1, "链表实现 - 基础版", "单向链表基本实现", 1, "首次提交", now.minusMonths(2).minusDays(8)),
            createNoteVersion(4, 2, 2, "链表实现 - 双向版", "增加了双向链表实现", 1, "添加双向链表", now.minusMonths(2).minusDays(8)),
            createNoteVersion(5, 2, 3, "链表实现详解 - 完整版", "增加了反转算法和测试用例", 1, "添加反转算法和详细注释", now.minusMonths(2).minusDays(7)),
            createNoteVersion(6, 5, 1, "仿淘宝首页 - 初稿", "基础页面结构和CSS样式", 2, "首次提交", now.minusMonths(2).minusDays(13)),
            createNoteVersion(7, 5, 2, "仿淘宝首页 - 完整实现", "增加了轮播图、分类导航、商品展示等所有模块", 2, "完善所有模块和交互效果", now.minusMonths(2).minusDays(12)),
            createNoteVersion(8, 12, 1, "图书管理系统 - V1", "基础类设计和接口", 5, "首次提交", now.minusMonths(2).minusDays(2)),
            createNoteVersion(9, 12, 2, "图书管理系统 - 完整的OOP设计", "增加了多态应用和设计模式", 5, "重构代码，引入设计模式", now.minusMonths(2).minusDays(1))
        ));
        log.info("✓ 已创建 9 条版本历史记录（4篇笔记有多版本）");

        // ============================================================
        // 9. 标签数据
        // ============================================================
        Tag tag1 = createTag(1, "Java", "Java编程语言相关", "技术", "#007396", 5);
        Tag tag2 = createTag(2, "数据结构", "数据结构相关", "学科", "#4CAF50", 3);
        Tag tag3 = createTag(3, "AI", "人工智能相关", "学科", "#FF5722", 3);
        Tag tag4 = createTag(4, "前端", "前端开发技术", "技术", "#42B883", 5);
        Tag tag5 = createTag(5, "JavaScript", "JavaScript语言", "技术", "#F7DF1E", 3);
        Tag tag6 = createTag(6, "HTML", "HTML标记语言", "技术", "#E34F26", 2);
        Tag tag7 = createTag(7, "CSS", "CSS样式表", "技术", "#1572B6", 3);
        Tag tag8 = createTag(8, "OOP", "面向对象编程", "学科", "#6A1B9A", 2);
        Tag tag9 = createTag(9, "Python", "Python编程语言", "技术", "#3776AB", 1);
        Tag tag10 = createTag(10, "Vue", "Vue.js框架", "技术", "#42B883", 1);
        Tag tag11 = createTag(11, "软件工程", "软件工程方法学", "学科", "#607D8B", 1);
        Tag tag12 = createTag(12, "JVM", "Java虚拟机", "技术", "#FF6F00", 1);
        Tag tag13 = createTag(13, "算法", "算法设计与分析", "学科", "#E91E63", 1);
        Tag tag14 = createTag(14, "JavaFX", "JavaFX GUI框架", "技术", "#5382A1", 1);
        Tag tag15 = createTag(15, "设计模式", "软件设计模式", "学科", "#009688", 1);

        tagRepository.saveAll(List.of(tag1, tag2, tag3, tag4, tag5, tag6, tag7, tag8, tag9, tag10, tag11, tag12, tag13, tag14, tag15));
        log.info("✓ 已创建 15 个标签");

        // ============================================================
        // 10. 笔记-标签关联
        // ============================================================
        noteTagRepository.saveAll(List.of(
            createNoteTag(1, 1), createNoteTag(1, 8),
            createNoteTag(2, 2),
            createNoteTag(3, 3),
            createNoteTag(4, 1),
            createNoteTag(5, 6), createNoteTag(5, 7), createNoteTag(5, 4),
            createNoteTag(6, 5), createNoteTag(6, 4),
            createNoteTag(9, 2), createNoteTag(9, 9),
            createNoteTag(10, 6), createNoteTag(10, 7), createNoteTag(10, 4),
            createNoteTag(11, 11),
            createNoteTag(12, 1), createNoteTag(12, 8), createNoteTag(12, 15),
            createNoteTag(13, 1), createNoteTag(13, 14),
            createNoteTag(14, 5), createNoteTag(14, 4),
            createNoteTag(15, 1),
            createNoteTag(16, 10), createNoteTag(16, 4),
            createNoteTag(17, 1), createNoteTag(17, 12),
            createNoteTag(18, 7), createNoteTag(18, 4),
            createNoteTag(19, 13), createNoteTag(19, 2)
        ));
        log.info("✓ 已创建 30 条笔记-标签关联");

        // ============================================================
        // 11. 笔记附件
        // ============================================================
        noteAttRepository.saveAll(List.of(
            createNoteAtt(1, 2, "linked_list_demo.java", "/file/document/267a7e20-6262-4c1a-b4af-50b5d3ed9422.jpeg", 15),
            createNoteAtt(2, 5, "taobao_homepage.html", "/file/document/a2c41fa4-3ab1-4368-b702-bcfbe171773a.jpeg", 128),
            createNoteAtt(3, 9, "binary_tree.py", "/file/document/a2613c4b-15d4-433f-b4fb-0f8ee20ea7f7.jpeg", 8),
            createNoteAtt(4, 12, "library_system_diagram.png", "/file/document/d0a5b3b5-1cff-4c9e-81b5-e8a6f32200b6.jpeg", 256)
        ));
        log.info("✓ 已创建 4 条附件记录");

        // ============================================================
        // 12. AI 作业总结
        // ============================================================
        assignmentSummaryRepository.saveAll(List.of(
            createAssignmentSummary(1L, 1L, "Java面向对象程序设计课程作业总结：\n- 共收到5份学生管理系统作业，平均分82分\n- 大部分同学掌握了基本的CRUD操作和集合框架使用\n- 陈浩宇同学的图书管理系统设计最为出色（95分），运用了多种设计模式\n- 建议下节课重点讲解异常处理和数据持久化", now.minusMonths(2).minusDays(11)),
            createAssignmentSummary(2L, 4L, "Web前端开发技术作业总结：\n- 共收到4份淘宝首页仿写作业，整体质量较高\n- 李思琪同学的作品页面还原度最高（90分）\n- 赵雪婷同学的Flex+Grid布局方案值得推荐\n- 下节课将讲解移动端适配和性能优化", now.minusMonths(2).minusDays(8))
        ));
        log.info("✓ 已创建 2 条 AI 作业总结");

        // ============================================================
        // 13. 举报数据
        // ============================================================
        reportRepository.saveAll(List.of(
            createReport(1, 2, 20, 5, "该笔记包含不当言论，建议管理员处理", now.minusMonths(2).minusDays(14), 1),  // 已受理
            createReport(2, 2, 7, 4, "这份笔记太敷衍了，代码只有几行，不像认真完成的作业", now.minusMonths(2).minusDays(13), 0), // 未受理
            createReport(3, 1, 3, 1, "该用户疑似批量提交低质量内容", now.minusMonths(2).minusDays(6), 1)           // 已受理
        ));
        log.info("✓ 已创建 3 条举报记录");

        log.info("========================================");
        log.info("✅ 种子数据初始化完成！");
        log.info("   总数据量：9用户 + 5课程 + 11作业 + 20笔记 + 8评价 + 20互动 + 9版本 + 15标签 + 4附件 + 2总结 + 3举报 = 106条");
        log.info("========================================");
        log.info("📋 登录账号信息：");
        log.info("   学生：student001 ~ student006 / 123456");
        log.info("   教师：teacher001 ~ teacher002 / 123456");
        log.info("   管理员：admin001 / 123456");
        log.info("========================================");
    }

    // ========== 辅助创建方法 ==========

    private User createUser(Integer id, String username, String account, String password, String realName,
                            String phone, String avatar, Integer age, String signature, Integer status,
                            LocalDateTime createTime, Integer gender, Integer role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setUserAccount(account);
        user.setPassword(password);
        user.setRealName(realName);
        user.setPhone(phone);
        user.setAvatar(avatar);
        user.setAge(age);
        user.setSignature(signature);
        user.setStatus(status);
        user.setCreateTime(createTime);
        user.setGender(gender);
        user.setRole(role);
        return user;
    }

    private Clazz createClazz(Integer id, String className, Integer teacherId, String describe,
                              LocalDateTime createTime, Integer status) {
        Clazz clazz = new Clazz();
        clazz.setId(id);
        clazz.setClassName(className);
        clazz.setTeacherId(teacherId);
        clazz.setDescribe(describe);
        clazz.setCreateTime(createTime);
        clazz.setStatus(status);
        return clazz;
    }

    private ClassJoined createClassJoined(Integer id, Integer classId, Integer studentId,
                                          Integer status, LocalDateTime joinTime) {
        ClassJoined cj = new ClassJoined();
        cj.setId(id);
        cj.setClassId(classId);
        cj.setStudentId(studentId);
        cj.setStatus(status);
        cj.setJoinTime(joinTime);
        return cj;
    }

    private Task createTask(Integer id, Integer classId, String title, String content,
                            LocalDateTime deadline, Boolean attachmentRequired, LocalDateTime createTime, String att) {
        Task task = new Task();
        task.setId(id);
        task.setClassId(classId);
        task.setTitle(title);
        task.setContent(content);
        task.setDeadline(deadline);
        task.setAttachmentRequired(attachmentRequired);
        task.setCreateTime(createTime);
        task.setAtt(att);
        return task;
    }

    private Note createNote(Integer id, String title, String content, Integer userId, Integer taskId,
                            Byte vision, LocalDateTime createTime, LocalDateTime updateTime,
                            Integer status, String tag, String summary, Integer isScore) {
        Note note = new Note();
        note.setId(id);
        note.setTitle(title);
        note.setContent(content);
        note.setUserId(userId);
        note.setTaskId(taskId);
        note.setVision(vision);
        note.setCreateTime(createTime);
        note.setUpdateTime(updateTime);
        note.setStatus(status);
        note.setTag(tag);
        note.setSummary(summary);
        note.setIsScore(isScore);
        return note;
    }

    private NoteComment createNoteComment(Integer id, Integer teacherId, Integer noteId,
                                          String comment, Integer score, LocalDateTime updateTime) {
        NoteComment nc = new NoteComment();
        nc.setId(id);
        nc.setTeacherId(teacherId);
        nc.setNoteId(noteId);
        nc.setComment(comment);
        nc.setScore(score);
        nc.setUpdateTime(updateTime);
        return nc;
    }

    private NoteInteraction createNoteInteraction(Integer id, Integer noteId, Integer userId,
                                                  Integer type, LocalDateTime createTime) {
        NoteInteraction ni = new NoteInteraction();
        ni.setId(id);
        ni.setNoteId(noteId);
        ni.setUserId(userId);
        ni.setType(type);
        ni.setCreateTime(createTime);
        return ni;
    }

    private NoteVersion createNoteVersion(Integer id, Integer noteId, Integer version, String title,
                                          String content, Integer userId, String changeSummary, LocalDateTime createdAt) {
        NoteVersion nv = new NoteVersion();
        nv.setId(id);
        nv.setNoteId(noteId);
        nv.setVersion(version);
        nv.setTitle(title);
        nv.setContent(content);
        nv.setUserId(userId);
        nv.setChangeSummary(changeSummary);
        nv.setCreatedAt(createdAt);
        return nv;
    }

    private Tag createTag(Integer id, String name, String description, String category,
                          String color, Integer usageCount) {
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(name);
        tag.setDescription(description);
        tag.setCategory(category);
        tag.setColor(color);
        tag.setUsageCount(usageCount);
        return tag;
    }

    private NoteTag createNoteTag(Integer noteId, Integer tagId) {
        NoteTag nt = new NoteTag();
        nt.setNoteId(noteId);
        nt.setTagId(tagId);
        return nt;
    }

    private NoteAtt createNoteAtt(Integer id, Integer noteId, String fileName, String fileUrl, Integer fileSize) {
        NoteAtt na = new NoteAtt();
        na.setId(id);
        na.setNoteId(noteId);
        na.setFileName(fileName);
        na.setFileUrl(fileUrl);
        na.setFileSize(fileSize);
        return na;
    }

    private AssignmentSummary createAssignmentSummary(Long id, Long classId, String summary, LocalDateTime createTime) {
        AssignmentSummary as = new AssignmentSummary();
        as.setId(id);
        as.setClassId(classId);
        as.setSummary(summary);
        as.setCreateTime(createTime);
        return as;
    }

    private Report createReport(Integer id, Integer type, Integer typeId, Integer userId,
                                String info, LocalDateTime createTime, Integer status) {
        Report report = new Report();
        report.setId(id);
        report.setType(type);
        report.setTypeId(typeId);
        report.setUserId(userId);
        report.setInfo(info);
        report.setCreateTime(createTime);
        report.setStatus(status);
        return report;
    }
}
