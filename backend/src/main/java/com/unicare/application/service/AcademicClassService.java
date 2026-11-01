package com.unicare.application.service;

import com.unicare.application.dto.request.AcademicClassRequest;
import com.unicare.application.dto.response.AcademicClassResponse;

import java.util.List;

public interface AcademicClassService {

    List<AcademicClassResponse> getClasses(String search, Integer facultyId);

    AcademicClassResponse getClassById(Integer id);

    AcademicClassResponse createClass(AcademicClassRequest request);

    AcademicClassResponse updateClass(Integer id, AcademicClassRequest request);

    void deleteClass(Integer id);
}

