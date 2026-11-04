package com.unicare.application.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO nhận dữ liệu cập nhật thông tin cá nhân của sinh viên đang đăng nhập.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentPersonalInfoRequest {

    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự")
    @JsonAlias("full_name")
    private String fullName;

    @JsonAlias("date_of_birth")
    private LocalDate dateOfBirth;

    @Size(max = 10, message = "Giới tính không được vượt quá 10 ký tự")
    private String gender;

    @Size(max = 15, message = "Số điện thoại không được vượt quá 15 ký tự")
    @JsonAlias({"phone", "phone_number"})
    private String phoneNumber;

    @Email(message = "Định dạng email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @JsonAlias("class_id")
    private Integer classId;

    @JsonAlias("class_code")
    private String classCode;

    public StudentPersonalInfoRequest() {
    }

    public StudentPersonalInfoRequest(
            String fullName,
            LocalDate dateOfBirth,
            String gender,
            String phoneNumber,
            String email,
            Integer classId,
            String classCode
    ) {
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.classId = classId;
        this.classCode = classCode;
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
}
