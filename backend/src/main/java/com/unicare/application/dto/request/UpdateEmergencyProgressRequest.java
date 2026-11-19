/**
 * DTO nhận dữ liệu cập nhật tiến độ (bước xử lý hiện tại) của ca cấp cứu (PUT /api/medical-records/{id}/progress).
 */
package com.unicare.application.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateEmergencyProgressRequest {

    @NotBlank(message = "Bước xử lý hiện tại (currentStep) không được để trống")
    @JsonAlias({"current_step", "step", "workflowStep", "workflow_step", "buocHienTai"})
    private String currentStep;

    @JsonAlias({"treatment_suggestions", "first_aid_notes", "firstAidNotes", "bienPhapSoCuu", "notes"})
    private String treatmentSuggestions;

    @JsonAlias({"diagnosis_notes", "chanDoan"})
    private String diagnosisNotes;

    public UpdateEmergencyProgressRequest() {
    }

    public UpdateEmergencyProgressRequest(String currentStep, String treatmentSuggestions, String diagnosisNotes) {
        this.currentStep = currentStep;
        this.treatmentSuggestions = treatmentSuggestions;
        this.diagnosisNotes = diagnosisNotes;
    }

    public String getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
    }

    public String getTreatmentSuggestions() {
        return treatmentSuggestions;
    }

    public void setTreatmentSuggestions(String treatmentSuggestions) {
        this.treatmentSuggestions = treatmentSuggestions;
    }

    public String getDiagnosisNotes() {
        return diagnosisNotes;
    }

    public void setDiagnosisNotes(String diagnosisNotes) {
        this.diagnosisNotes = diagnosisNotes;
    }
}
