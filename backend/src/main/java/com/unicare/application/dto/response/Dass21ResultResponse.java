package com.unicare.application.dto.response;

import com.unicare.domain.entity.Dass21Result;
import com.unicare.domain.entity.Student;
import com.unicare.domain.entity.User;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

/**
 * DTO trả về chi tiết một lần làm bài đánh giá tâm lý DASS-21 của sinh viên.
 */
public class Dass21ResultResponse {

    private Long id;
    private Long studentId;
    private String studentCode;
    private String studentName;
    private OffsetDateTime testDate;
    private List<Integer> answers = Collections.emptyList();
    private Integer depressionScore;
    private Integer anxietyScore;
    private Integer stressScore;
    private Integer totalScore;
    private String depressionLevel;
    private String anxietyLevel;
    private String stressLevel;

    public Dass21ResultResponse() {
    }

    public static Dass21ResultResponse fromEntity(Dass21Result entity) {
        if (entity == null) {
            return null;
        }
        Dass21ResultResponse response = new Dass21ResultResponse();
        response.setId(entity.getId());
        response.setTestDate(entity.getTestDate());
        response.setAnswers(entity.getAnswers() != null ? List.copyOf(entity.getAnswers()) : Collections.emptyList());
        response.setDepressionScore(entity.getDepressionScore());
        response.setAnxietyScore(entity.getAnxietyScore());
        response.setStressScore(entity.getStressScore());

        int dep = entity.getDepressionScore() != null ? entity.getDepressionScore() : 0;
        int anx = entity.getAnxietyScore() != null ? entity.getAnxietyScore() : 0;
        int str = entity.getStressScore() != null ? entity.getStressScore() : 0;
        response.setTotalScore(dep + anx + str);

        response.setDepressionLevel(entity.getDepressionLevel());
        response.setAnxietyLevel(entity.getAnxietyLevel());
        response.setStressLevel(entity.getStressLevel());

        Student student = entity.getStudent();
        if (student != null) {
            response.setStudentId(student.getId());
            User user = student.getUser();
            if (student.getStudentCode() != null && !student.getStudentCode().isBlank()) {
                response.setStudentCode(student.getStudentCode());
            } else if (user != null) {
                response.setStudentCode(user.getUsername());
            }
            if (user != null) {
                response.setStudentName(user.getFullName());
            }
        }

        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public OffsetDateTime getTestDate() {
        return testDate;
    }

    public void setTestDate(OffsetDateTime testDate) {
        this.testDate = testDate;
    }

    public List<Integer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Integer> answers) {
        this.answers = answers != null ? answers : Collections.emptyList();
    }

    public Integer getDepressionScore() {
        return depressionScore;
    }

    public void setDepressionScore(Integer depressionScore) {
        this.depressionScore = depressionScore;
    }

    public Integer getAnxietyScore() {
        return anxietyScore;
    }

    public void setAnxietyScore(Integer anxietyScore) {
        this.anxietyScore = anxietyScore;
    }

    public Integer getStressScore() {
        return stressScore;
    }

    public void setStressScore(Integer stressScore) {
        this.stressScore = stressScore;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public String getDepressionLevel() {
        return depressionLevel;
    }

    public void setDepressionLevel(String depressionLevel) {
        this.depressionLevel = depressionLevel;
    }

    public String getAnxietyLevel() {
        return anxietyLevel;
    }

    public void setAnxietyLevel(String anxietyLevel) {
        this.anxietyLevel = anxietyLevel;
    }

    public String getStressLevel() {
        return stressLevel;
    }

    public void setStressLevel(String stressLevel) {
        this.stressLevel = stressLevel;
    }
}
