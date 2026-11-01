package com.unicare.application.service.impl;

import com.unicare.application.dto.request.FacultyRequest;
import com.unicare.application.dto.response.FacultyResponse;
import com.unicare.application.service.FacultyService;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.common.util.ValidationUtils;
import com.unicare.domain.entity.Faculty;
import com.unicare.domain.repository.AcademicClassRepository;
import com.unicare.domain.repository.FacultyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FacultyServiceImpl implements FacultyService {

    private final FacultyRepository facultyRepository;
    private final AcademicClassRepository academicClassRepository;

    public FacultyServiceImpl(FacultyRepository facultyRepository, AcademicClassRepository academicClassRepository) {
        this.facultyRepository = facultyRepository;
        this.academicClassRepository = academicClassRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacultyResponse> getFaculties(String search) {
        return facultyRepository.searchFaculties(ValidationUtils.cleanSearch(search)).stream()
                .map(FacultyResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FacultyResponse getFacultyById(Integer id) {
        return FacultyResponse.fromEntity(findFacultyById(id));
    }

    @Override
    @Transactional
    public FacultyResponse createFaculty(FacultyRequest request) {
        validateRequestNotNull(request);
        String cleanCode = ValidationUtils.requireBoundedText(request.getFacultyCode(), 10, "Mã khoa");
        String cleanName = ValidationUtils.requireBoundedText(request.getFacultyName(), 100, "Tên khoa");

        if (facultyRepository.existsByFacultyCodeIgnoreCase(cleanCode)) {
            throw new DuplicateResourceException("Mã khoa '" + cleanCode + "' đã tồn tại trên hệ thống.");
        }
        if (facultyRepository.existsByFacultyNameIgnoreCase(cleanName)) {
            throw new DuplicateResourceException("Tên khoa '" + cleanName + "' đã tồn tại trên hệ thống.");
        }

        Faculty savedFaculty = facultyRepository.save(new Faculty(cleanCode, cleanName));
        return FacultyResponse.fromEntity(savedFaculty);
    }

    @Override
    @Transactional
    public FacultyResponse updateFaculty(Integer id, FacultyRequest request) {
        validateRequestNotNull(request);
        Faculty faculty = findFacultyById(id);

        String cleanCode = ValidationUtils.requireBoundedText(request.getFacultyCode(), 10, "Mã khoa");
        String cleanName = ValidationUtils.requireBoundedText(request.getFacultyName(), 100, "Tên khoa");

        if (facultyRepository.existsByFacultyCodeIgnoreCaseAndIdNot(cleanCode, id)) {
            throw new DuplicateResourceException("Mã khoa '" + cleanCode + "' đã tồn tại trên hệ thống.");
        }
        if (facultyRepository.existsByFacultyNameIgnoreCaseAndIdNot(cleanName, id)) {
            throw new DuplicateResourceException("Tên khoa '" + cleanName + "' đã tồn tại trên hệ thống.");
        }

        faculty.setFacultyCode(cleanCode);
        faculty.setFacultyName(cleanName);

        return FacultyResponse.fromEntity(facultyRepository.save(faculty));
    }

    @Override
    @Transactional
    public void deleteFaculty(Integer id) {
        Faculty faculty = findFacultyById(id);
        if (academicClassRepository.existsByFacultyId(id)) {
            throw new BadRequestException("Không thể xóa khoa vì đang có lớp học trực thuộc.");
        }
        facultyRepository.delete(faculty);
    }

    private Faculty findFacultyById(Integer id) {
        return facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoa với mã ID: " + id));
    }

    private void validateRequestNotNull(FacultyRequest request) {
        if (request == null) {
            throw new BadRequestException("Dữ liệu khoa không được để trống");
        }
    }
}
