package com.unicare.application.service;

import com.unicare.application.dto.request.UpdatePrescriptionRequest;
import com.unicare.application.dto.response.MedicalCheckupDetailResponse;

public interface MedicalCheckupService {

    MedicalCheckupDetailResponse getCheckupDetail(Long checkupId, String currentUsername);

    MedicalCheckupDetailResponse updatePrescription(Long checkupId, UpdatePrescriptionRequest request, String currentUsername);
}
