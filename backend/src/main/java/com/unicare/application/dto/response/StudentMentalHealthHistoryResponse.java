package com.unicare.application.dto.response;

import java.util.Collections;
import java.util.List;

/**
 * DTO tổng hợp Lịch sử kết quả DASS-21 (chuỗi điểm qua các lần test) và Nhật ký tâm trạng của Sinh viên
 * phục vụ Bác sĩ / Chuyên gia tâm lý trong ca khám / tư vấn.
 */
public class StudentMentalHealthHistoryResponse {

    private StudentPersonalInfoResponse studentInfo;
    private int totalDass21Tests;
    private Dass21ResultResponse latestDass21;
    private List<Dass21ScorePointResponse> dass21ScoreTrend = Collections.emptyList();
    private List<Dass21ResultResponse> dass21Results = Collections.emptyList();
    private MoodLogHistoryResponse moodHistory;

    public StudentMentalHealthHistoryResponse() {
    }

    public StudentMentalHealthHistoryResponse(
            StudentPersonalInfoResponse studentInfo,
            int totalDass21Tests,
            Dass21ResultResponse latestDass21,
            List<Dass21ScorePointResponse> dass21ScoreTrend,
            List<Dass21ResultResponse> dass21Results,
            MoodLogHistoryResponse moodHistory
    ) {
        this.studentInfo = studentInfo;
        this.totalDass21Tests = totalDass21Tests;
        this.latestDass21 = latestDass21;
        this.dass21ScoreTrend = dass21ScoreTrend != null ? dass21ScoreTrend : Collections.emptyList();
        this.dass21Results = dass21Results != null ? dass21Results : Collections.emptyList();
        this.moodHistory = moodHistory;
    }

    public StudentPersonalInfoResponse getStudentInfo() {
        return studentInfo;
    }

    public void setStudentInfo(StudentPersonalInfoResponse studentInfo) {
        this.studentInfo = studentInfo;
    }

    public int getTotalDass21Tests() {
        return totalDass21Tests;
    }

    public void setTotalDass21Tests(int totalDass21Tests) {
        this.totalDass21Tests = totalDass21Tests;
    }

    public Dass21ResultResponse getLatestDass21() {
        return latestDass21;
    }

    public void setLatestDass21(Dass21ResultResponse latestDass21) {
        this.latestDass21 = latestDass21;
    }

    public List<Dass21ScorePointResponse> getDass21ScoreTrend() {
        return dass21ScoreTrend;
    }

    public void setDass21ScoreTrend(List<Dass21ScorePointResponse> dass21ScoreTrend) {
        this.dass21ScoreTrend = dass21ScoreTrend != null ? dass21ScoreTrend : Collections.emptyList();
    }

    public List<Dass21ResultResponse> getDass21Results() {
        return dass21Results;
    }

    public void setDass21Results(List<Dass21ResultResponse> dass21Results) {
        this.dass21Results = dass21Results != null ? dass21Results : Collections.emptyList();
    }

    public MoodLogHistoryResponse getMoodHistory() {
        return moodHistory;
    }

    public void setMoodHistory(MoodLogHistoryResponse moodHistory) {
        this.moodHistory = moodHistory;
    }
}
