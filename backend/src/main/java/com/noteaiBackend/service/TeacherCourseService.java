package com.noteaiBackend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.noteaiBackend.dto.NoteFindByTaskIdWithCommentDTO;
import com.noteaiBackend.repository.ClassJoinedRepository;
import com.noteaiBackend.repository.NoteRepository;
import com.noteaiBackend.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

/**
 * 教师课程管理服务层
 * 负责教师端课程详情、作业提交状态等业务逻辑
 */
@Service
@RequiredArgsConstructor
public class TeacherCourseService {

    private final ClassJoinedRepository classJoinedRepository;
    private final NoteRepository noteRepository;
    private final TaskRepository taskRepository;

    /**
     * 获取课程详情（含统计数据和学生列表）
     *
     * @param classId 课程ID
     * @return 包含课程统计和学生列表的Map
     */
    public Map<String, Object> getCourseDetails(Integer classId) {
        Map<String, Object> data = new HashMap<>();

        // 1. 统计数据
        List<Object[]> resultList = noteRepository.findCourseStats(classId);
        Object[] statsRaw = (resultList != null && !resultList.isEmpty()) ? resultList.get(0) : null;

        long totalStudents = classJoinedRepository.countByClassId(classId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents", totalStudents);
        if (statsRaw != null && statsRaw.length > 0) {
            stats.put("activeStudents", statsRaw[0] != null ? ((Number) statsRaw[0]).longValue() : 0L);
            stats.put("submittedAssignments", statsRaw[1] != null ? ((Number) statsRaw[1]).longValue() : 0L);
            stats.put("averageScore", statsRaw[2] != null ? ((Number) statsRaw[2]).doubleValue() : 0.0);
            stats.put("totalAssignments", taskRepository.countByClassId(classId));

            long totalAssignments = (long) stats.get("totalAssignments");
            if (totalStudents > 0 && totalAssignments > 0) {
                long submitted = (long) stats.get("submittedAssignments");
                double completionRate = (double) submitted / (totalStudents * totalAssignments) * 100;
                stats.put("completionRate", Math.round(completionRate * 10) / 10.0);
            } else {
                stats.put("completionRate", 0);
            }
        }
        data.put("stats", stats);

        // 2. 学生列表
        List<Object[]> studentsRaw = classJoinedRepository.findStudentsByClassIdWithUnfinishedCount(classId);
        List<Map<String, Object>> students = new ArrayList<>();
        for (Object[] row : studentsRaw) {
            Map<String, Object> student = new HashMap<>();
            student.put("id", row[0]);
            student.put("username", row[1]);
            student.put("realName", row[2]);
            student.put("phone", row[3]);
            student.put("avatar", row[4]);
            student.put("joinTime", row[5]);
            student.put("unfinishedCount", row[6]);
            students.add(student);
        }
        data.put("students", students);

        return data;
    }

    /**
     * 获取作业提交状态（包含班级所有学生的提交情况）
     *
     * @param taskId 作业ID
     * @return 学生提交状态列表
     */
    public List<Map<String, Object>> getSubmissionStatus(Integer taskId) {
        // 1. 获取任务所属班级
        Integer classId = taskRepository.findById(taskId)
                .map(t -> t.getClassId())
                .orElseThrow(() -> new RuntimeException("任务不存在"));

        // 2. 获取班级所有学生
        List<Object[]> studentsRaw = classJoinedRepository.findStudentsByClassId(classId);

        // 3. 获取已提交的学生记录
        List<NoteFindByTaskIdWithCommentDTO> submissions = noteRepository.findByTaskIdWithComment(taskId);
        Map<Integer, NoteFindByTaskIdWithCommentDTO> submissionMap = new HashMap<>();
        for (NoteFindByTaskIdWithCommentDTO sub : submissions) {
            submissionMap.put(sub.getUserId(), sub);
        }

        // 4. 组装每个学生的提交状态
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : studentsRaw) {
            Integer studentId = row[0] != null ? ((Number) row[0]).intValue() : null;
            Map<String, Object> item = new HashMap<>();
            item.put("userId", studentId);
            item.put("userName", row[1]);
            item.put("realName", row[2]);

            if (submissionMap.containsKey(studentId)) {
                NoteFindByTaskIdWithCommentDTO sub = submissionMap.get(studentId);
                item.put("noteId", sub.getNoteId());
                item.put("isScore", sub.getIsScore());
                item.put("score", sub.getScore());
                item.put("comment", sub.getComment());
                item.put("submitted", true);
            } else {
                item.put("noteId", null);
                item.put("isScore", 0);
                item.put("score", null);
                item.put("comment", null);
                item.put("submitted", false);
            }
            result.add(item);
        }
        return result;
    }
}
