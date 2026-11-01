package com.unicare.application.service;

import com.unicare.application.dto.request.FacultyRequest;
import com.unicare.application.dto.response.FacultyResponse;

import java.util.List;

public interface FacultyService {

    List<FacultyResponse> getFaculties(String search);

    FacultyResponse getFacultyById(Integer id);

    FacultyResponse createFaculty(FacultyRequest request);

    FacultyResponse updateFaculty(Integer id, FacultyRequest request);

    void deleteFaculty(Integer id);
}

