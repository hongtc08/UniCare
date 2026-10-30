package com.unicare.application.dto.response;

import com.unicare.domain.entity.HealthInsurance;
import com.unicare.domain.entity.Student;
import com.unicare.domain.enums.HealthInsuranceStatus;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class HealthInsuranceResponse {

    private Long id;
    private String cardNumber;
    private Long studentId;
    private String studentCode;
    private String studentName;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;
    private String initialHospital;
    private String status;
    private String statusDescription;
    private Long daysRemaining;

    public HealthInsuranceResponse() {
    }

    public HealthInsuranceResponse(
            Long id,
            String cardNumber,
            Long studentId,
            String studentCode,
            String studentName,
            LocalDate effectiveDate,
            LocalDate expirationDate,
            String initialHospital,
            String status,
            String statusDescription,
            Long daysRemaining
    ) {
        this.id = id;
        this.cardNumber = cardNumber;
        this.studentId = studentId;
        this.studentCode = studentCode;
        this.studentName = studentName;
        this.effectiveDate = effectiveDate;
        this.expirationDate = expirationDate;
        this.initialHospital = initialHospital;
        this.status = status;
        this.statusDescription = statusDescription;
        this.daysRemaining = daysRemaining;
    }

    public static HealthInsuranceResponse fromEntity(HealthInsurance entity) {
        return fromEntity(entity, LocalDate.now());
    }

    public static HealthInsuranceResponse fromEntity(HealthInsurance entity, LocalDate today) {
        if (entity == null) {
            return null;
        }
        LocalDate referenceDate = today != null ? today : LocalDate.now();

        HealthInsuranceResponse response = new HealthInsuranceResponse();
        response.setId(entity.getId());
        response.setCardNumber(entity.getCardNumber());
        response.setEffectiveDate(entity.getEffectiveDate());
        response.setExpirationDate(entity.getExpirationDate());
        response.setInitialHospital(entity.getInitialHospital());

        Student student = entity.getStudent();
        if (student != null) {
            response.setStudentId(student.getId());
            response.setStudentCode(student.getStudentCode());
            if (student.getUser() != null) {
                response.setStudentName(student.getUser().getFullName());
            }
        }

        if (entity.getExpirationDate() != null) {
            HealthInsuranceStatus computedStatus = HealthInsuranceStatus.calculate(entity.getExpirationDate(), referenceDate);
            response.setStatus(computedStatus.name());
            response.setStatusDescription(computedStatus.getDescription());
            response.setDaysRemaining(ChronoUnit.DAYS.between(referenceDate, entity.getExpirationDate()));
        }

        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getInitialHospital() {
        return initialHospital;
    }

    public void setInitialHospital(String initialHospital) {
        this.initialHospital = initialHospital;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public Long getDaysRemaining() {
        return daysRemaining;
    }

    public void setDaysRemaining(Long daysRemaining) {
        this.daysRemaining = daysRemaining;
    }

    // Aliases to seamlessly support common frontend field namings
    public LocalDate getIssueDate() {
        return effectiveDate;
    }

    public String getRegisteredHospital() {
        return initialHospital;
    }

    public String getStudentFullName() {
        return studentName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String cardNumber;
        private Long studentId;
        private String studentCode;
        private String studentName;
        private LocalDate effectiveDate;
        private LocalDate expirationDate;
        private String initialHospital;
        private String status;
        private String statusDescription;
        private Long daysRemaining;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder cardNumber(String cardNumber) {
            this.cardNumber = cardNumber;
            return this;
        }

        public Builder studentId(Long studentId) {
            this.studentId = studentId;
            return this;
        }

        public Builder studentCode(String studentCode) {
            this.studentCode = studentCode;
            return this;
        }

        public Builder studentName(String studentName) {
            this.studentName = studentName;
            return this;
        }

        public Builder effectiveDate(LocalDate effectiveDate) {
            this.effectiveDate = effectiveDate;
            return this;
        }

        public Builder issueDate(LocalDate issueDate) {
            this.effectiveDate = issueDate;
            return this;
        }

        public Builder expirationDate(LocalDate expirationDate) {
            this.expirationDate = expirationDate;
            return this;
        }

        public Builder initialHospital(String initialHospital) {
            this.initialHospital = initialHospital;
            return this;
        }

        public Builder registeredHospital(String registeredHospital) {
            this.initialHospital = registeredHospital;
            return this;
        }

        public Builder status(HealthInsuranceStatus status) {
            this.status = status != null ? status.name() : null;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder statusDescription(String statusDescription) {
            this.statusDescription = statusDescription;
            return this;
        }

        public Builder daysRemaining(Long daysRemaining) {
            this.daysRemaining = daysRemaining;
            return this;
        }

        public Builder studentFullName(String studentFullName) {
            this.studentName = studentFullName;
            return this;
        }

        public HealthInsuranceResponse build() {
            return new HealthInsuranceResponse(
                    id, cardNumber, studentId, studentCode, studentName,
                    effectiveDate, expirationDate, initialHospital,
                    status, statusDescription, daysRemaining
            );
        }
    }
}
