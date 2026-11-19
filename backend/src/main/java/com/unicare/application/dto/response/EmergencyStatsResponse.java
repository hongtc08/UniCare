/**
 * DTO trả về Số liệu thống kê các ca cấp cứu (GET /api/medical-records/emergencies/stats):
 * Số ca đang xử lý, đang sơ cứu, đang chuyển viện và đã kết thúc hôm nay.
 */
package com.unicare.application.dto.response;

public class EmergencyStatsResponse {

    private long inProgressCount;
    private long firstAidCount;
    private long transferringCount;
    private long completedTodayCount;

    public EmergencyStatsResponse() {
    }

    public EmergencyStatsResponse(
            long inProgressCount,
            long firstAidCount,
            long transferringCount,
            long completedTodayCount
    ) {
        this.inProgressCount = inProgressCount;
        this.firstAidCount = firstAidCount;
        this.transferringCount = transferringCount;
        this.completedTodayCount = completedTodayCount;
    }

    public long getInProgressCount() {
        return inProgressCount;
    }

    public void setInProgressCount(long inProgressCount) {
        this.inProgressCount = inProgressCount;
    }

    public long getProcessingCount() {
        return inProgressCount;
    }

    public long getFirstAidCount() {
        return firstAidCount;
    }

    public void setFirstAidCount(long firstAidCount) {
        this.firstAidCount = firstAidCount;
    }

    public long getTransferringCount() {
        return transferringCount;
    }

    public void setTransferringCount(long transferringCount) {
        this.transferringCount = transferringCount;
    }

    public long getCompletedTodayCount() {
        return completedTodayCount;
    }

    public void setCompletedTodayCount(long completedTodayCount) {
        this.completedTodayCount = completedTodayCount;
    }
}
