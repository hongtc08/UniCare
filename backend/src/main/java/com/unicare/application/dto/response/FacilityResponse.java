/**
 * DTO trả về thông tin Cơ sở y tế (ID và tên) phục vụ bộ lọc khi sinh viên đặt lịch khám (/api/facilities).
 */
package com.unicare.application.dto.response;

public class FacilityResponse {

    private Long id;
    private String name;

    public FacilityResponse() {
    }

    public FacilityResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
