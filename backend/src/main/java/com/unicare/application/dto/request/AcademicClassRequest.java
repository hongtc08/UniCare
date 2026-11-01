package com.unicare.application.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AcademicClassRequest {

    @NotBlank(message = "Mã lớp không được để trống")
    @Size(max = 10, message = "Mã lớp không được vượt quá 10 ký tự")
    @JsonAlias({"code", "class_code"})
    private String classCode;

    @NotBlank(message = "Tên lớp không được để trống")
    @Size(max = 50, message = "Tên lớp không được vượt quá 50 ký tự")
    @JsonAlias({"name", "class_name"})
    private String className;

    @NotBlank(message = "Khóa học không được để trống")
    @Size(max = 10, message = "Khóa học không được vượt quá 10 ký tự")
    @JsonAlias({"academic_year", "cohort"})
    private String academicYear;

    @NotNull(message = "Khoa trực thuộc không được để trống")
    @JsonAlias({"faculty_id", "facultyId"})
    private Integer facultyId;

    public AcademicClassRequest() {
    }

    public AcademicClassRequest(String classCode, String className, String academicYear, Integer facultyId) {
        this.classCode = classCode;
        this.className = className;
        this.academicYear = academicYear;
        this.facultyId = facultyId;
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
}

