/**
 * DTO trả về danh sách các ca cấp cứu hỗ trợ lọc và phân trang (GET /api/medical-records/emergencies).
 */
package com.unicare.application.dto.response;

import java.util.Collections;
import java.util.List;

public class EmergencyCasePageResponse {

    private List<MedicalCheckupDetailResponse> items = Collections.emptyList();
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    public EmergencyCasePageResponse() {
    }

    public EmergencyCasePageResponse(
            List<MedicalCheckupDetailResponse> items,
            int page,
            int size,
            long totalElements
    ) {
        this.items = items != null ? items : Collections.emptyList();
        this.page = Math.max(0, page);
        this.size = Math.max(1, size);
        this.totalElements = Math.max(0L, totalElements);
        this.totalPages = this.totalElements == 0 ? 0 : (int) Math.ceil((double) this.totalElements / this.size);
        this.hasNext = (this.page + 1) < this.totalPages;
        this.hasPrevious = this.page > 0;
    }

    public List<MedicalCheckupDetailResponse> getItems() {
        return items;
    }

    public void setItems(List<MedicalCheckupDetailResponse> items) {
        this.items = items != null ? items : Collections.emptyList();
    }

    /**
     * Aliases content & cases cho Frontend.
     */
    public List<MedicalCheckupDetailResponse> getContent() {
        return items;
    }

    public List<MedicalCheckupDetailResponse> getCases() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
}
