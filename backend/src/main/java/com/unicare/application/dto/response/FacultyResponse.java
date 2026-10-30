package com.unicare.application.dto.response;

import com.unicare.domain.entity.Faculty;

public class FacultyResponse {

    private Integer id;
    private String facultyCode;
    private String facultyName;

    public FacultyResponse() {
    }

    public FacultyResponse(Integer id, String facultyCode, String facultyName) {
        this.id = id;
        this.facultyCode = facultyCode;
        this.facultyName = facultyName;
    }

    public static FacultyResponse fromEntity(Faculty entity) {
        if (entity == null) {
            return null;
        }
        return new FacultyResponse(
                entity.getId(),
                entity.getFacultyCode(),
                entity.getFacultyName()
        );
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFacultyCode() {
        return facultyCode;
    }

    public void setFacultyCode(String facultyCode) {
        this.facultyCode = facultyCode;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getCode() {
        return facultyCode;
    }

    public String getName() {
        return facultyName;
    }
}

