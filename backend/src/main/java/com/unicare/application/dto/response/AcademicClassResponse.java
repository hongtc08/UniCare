package com.unicare.application.dto.response;

import com.unicare.domain.entity.AcademicClass;

public class AcademicClassResponse {

    private Integer id;
    private String classCode;
    private String className;
    private String academicYear;
    private Integer facultyId;
    private String facultyCode;
    private String facultyName;

    public AcademicClassResponse() {
    }

    public AcademicClassResponse(
            Integer id,
            String classCode,
            String className,
            String academicYear,
            Integer facultyId,
            String facultyCode,
            String facultyName
    ) {
        this.id = id;
        this.classCode = classCode;
        this.className = className;
        this.academicYear = academicYear;
        this.facultyId = facultyId;
        this.facultyCode = facultyCode;
        this.facultyName = facultyName;
    }

    public static AcademicClassResponse fromEntity(AcademicClass entity) {
        if (entity == null) {
            return null;
        }
        AcademicClassResponse response = new AcademicClassResponse();
        response.setId(entity.getId());
        response.setClassCode(entity.getClassCode());
        response.setClassName(entity.getClassName());
        response.setAcademicYear(entity.getAcademicYear());

        if (entity.getFaculty() != null) {
            response.setFacultyId(entity.getFaculty().getId());
            response.setFacultyCode(entity.getFaculty().getFacultyCode());
            response.setFacultyName(entity.getFaculty().getFacultyName());
        }

        return response;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public Integer getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(Integer facultyId) {
        this.facultyId = facultyId;
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
        return classCode;
    }

    public String getName() {
        return className;
    }

    public String getCohort() {
        return academicYear;
    }
}

