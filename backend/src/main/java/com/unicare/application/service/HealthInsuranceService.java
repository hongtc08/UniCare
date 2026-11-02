package com.unicare.application.service;

import com.unicare.application.dto.request.HealthInsuranceRequest;
import com.unicare.application.dto.response.HealthInsuranceResponse;

import java.util.List;

public interface HealthInsuranceService {

    List<HealthInsuranceResponse> getHealthInsurances(String search, String status, String currentUsername);

    HealthInsuranceResponse getHealthInsuranceById(Long id, String currentUsername);

    HealthInsuranceResponse getHealthInsuranceByStudentId(Long studentId, String currentUsername);

    HealthInsuranceResponse createHealthInsurance(HealthInsuranceRequest request, String currentUsername);

    HealthInsuranceResponse updateHealthInsurance(Long id, HealthInsuranceRequest request, String currentUsername);

    void deleteHealthInsurance(Long id, String currentUsername);
}

