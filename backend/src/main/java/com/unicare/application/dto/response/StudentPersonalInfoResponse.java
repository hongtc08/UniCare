package com.unicare.application.dto.response;

import com.unicare.domain.entity.AcademicClass;
import com.unicare.domain.entity.Faculty;
import com.unicare.domain.entity.Student;
import com.unicare.domain.entity.User;

import java.time.LocalDate;

/**
 * DTO trả về thông tin cá nhân & học vụ của sinh viên (Họ tên, Ngày sinh, Giới tính, SĐT, Email, Lớp, Khoa).
 */
public class StudentPersonalInfoResponse {

    private Long studentId;
    private String username;
    private String studentCode;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String phoneNumber;
    private String email;
    private Integer classId;
    private String classCode;
    private String className;
    private String academicYear;
    private Integer facultyId;
    private String facultyCode;
    private String facultyName;

    public StudentPersonalInfoResponse() {
    }

    public static StudentPersonalInfoResponse fromEntities(User user, Student student) {
        if (user == null && student == null) {
            return null;
        }
        StudentPersonalInfoResponse response = new StudentPersonalInfoResponse();

        User effectiveUser = user != null ? user : (student != null ? student.getUser() : null);
        if (effectiveUser != null) {
            response.setStudentId(effectiveUser.getId());
            response.setUsername(effectiveUser.getUsername());
            response.setStudentCode(effectiveUser.getUsername());
            response.setFullName(effectiveUser.getFullName());
            response.setPhoneNumber(effectiveUser.getPhoneNumber());
            response.setEmail(effectiveUser.getEmail());
        }

        if (student != null) {
            if (student.getId() != null) {
                response.setStudentId(student.getId());
            }
            if (student.getStudentCode() != null && !student.getStudentCode().isBlank()) {
                response.setStudentCode(student.getStudentCode());
            }
            response.setDateOfBirth(student.getDateOfBirth());
            response.setGender(student.getGender());

            AcademicClass academicClass = student.getAcademicClass();
            if (academicClass != null) {
                response.setClassId(academicClass.getId());
                response.setClassCode(academicClass.getClassCode());
                response.setClassName(academicClass.getClassName());
                response.setAcademicYear(academicClass.getAcademicYear());

                Faculty faculty = academicClass.getFaculty();
                if (faculty != null) {
                    response.setFacultyId(faculty.getId());
                    response.setFacultyCode(faculty.getFacultyCode());
                    response.setFacultyName(faculty.getFacultyName());
                }
            }
        }

        return response;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Alias trường phone cho Frontend.
     */
    public String getPhone() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
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
}
