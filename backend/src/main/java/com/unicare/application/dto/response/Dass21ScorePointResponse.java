package com.unicare.application.dto.response;

import com.unicare.domain.entity.Dass21Result;

import java.time.OffsetDateTime;

/**
 * DTO đại diện cho một điểm dữ liệu trong chuỗi điểm DASS-21 theo tiến trình thời gian (phục vụ biểu đồ xu hướng).
 */
public class Dass21ScorePointResponse {

    private Long id;
    private OffsetDateTime testDate;
    private Integer depressionScore;
    private Integer anxietyScore;
    private Integer stressScore;
    private Integer totalScore;
    private String depressionLevel;
    private String anxietyLevel;
    private String stressLevel;

    public Dass21ScorePointResponse() {
    }

    public static Dass21ScorePointResponse fromEntity(Dass21Result entity) {
        if (entity == null) {
            return null;
        }
        Dass21ScorePointResponse point = new Dass21ScorePointResponse();
        point.setId(entity.getId());
        point.setTestDate(entity.getTestDate());
        point.setDepressionScore(entity.getDepressionScore());
        point.setAnxietyScore(entity.getAnxietyScore());
        point.setStressScore(entity.getStressScore());

        int dep = entity.getDepressionScore() != null ? entity.getDepressionScore() : 0;
        int anx = entity.getAnxietyScore() != null ? entity.getAnxietyScore() : 0;
        int str = entity.getStressScore() != null ? entity.getStressScore() : 0;
        point.setTotalScore(dep + anx + str);

        point.setDepressionLevel(entity.getDepressionLevel());
        point.setAnxietyLevel(entity.getAnxietyLevel());
        point.setStressLevel(entity.getStressLevel());
        return point;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OffsetDateTime getTestDate() {
        return testDate;
    }

    public void setTestDate(OffsetDateTime testDate) {
        this.testDate = testDate;
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
