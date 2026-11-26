package com.unicare.application.dto.response;

import com.unicare.domain.entity.AcademicClass;
import com.unicare.domain.entity.ManagementStaff;
import com.unicare.domain.entity.MedicalStaff;
import com.unicare.domain.entity.Student;
import com.unicare.domain.entity.User;
import com.unicare.domain.enums.Role;
import com.unicare.domain.enums.UserStatus;

public class UserResponse {

    private Long id;
    private String username;
    private String fullName;
    private Role role;
    private UserStatus status;
    private String email;
    private String phoneNumber;
    private String studentCode;
    private Integer classId;
    private String classCode;
    private String className;
    private String facultyName;
    private String staffCode;
    private String title;
    private String specialty;

    public UserResponse() {
    }

    public UserResponse(Long id, String username, String fullName, Role role, UserStatus status) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.status = status;
    }

    public static UserResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        UserResponse response = new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.getStatus()
        );
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());

        Student student = user.getStudent();
        if (student != null) {
            response.setStudentCode(student.getStudentCode());
            AcademicClass academicClass = student.getAcademicClass();
            if (academicClass != null) {
                response.setClassId(academicClass.getId());
                response.setClassCode(academicClass.getClassCode());
                response.setClassName(academicClass.getClassName());
                if (academicClass.getFaculty() != null) {
                    response.setFacultyName(academicClass.getFaculty().getFacultyName());
                }
            }
        }

        MedicalStaff medicalStaff = user.getMedicalStaff();
        if (medicalStaff != null) {
            response.setStaffCode(medicalStaff.getStaffCode());
            response.setTitle(medicalStaff.getTitle());
            response.setSpecialty(medicalStaff.getSpecialty());
        } else {
            ManagementStaff managementStaff = user.getManagementStaff();
            if (managementStaff != null) {
                response.setStaffCode(managementStaff.getStaffCode());
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
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

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getStaffCode() {
        return staffCode;
    }

    public void setStaffCode(String staffCode) {
        this.staffCode = staffCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }
}
