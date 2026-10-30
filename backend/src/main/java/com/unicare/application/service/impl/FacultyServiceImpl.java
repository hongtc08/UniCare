package com.unicare.application.service.impl;

import com.unicare.application.dto.request.FacultyRequest;
import com.unicare.application.dto.response.FacultyResponse;
import com.unicare.application.service.FacultyService;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
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
        String cleanSearch = search == null ? "" : search.trim();
        List<Faculty> list = facultyRepository.searchFaculties(cleanSearch);
        return list.stream()
                .map(FacultyResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FacultyResponse getFacultyById(Integer id) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoa với mã ID: " + id));
        return FacultyResponse.fromEntity(faculty);
    }

    @Override
    @Transactional
    public FacultyResponse createFaculty(FacultyRequest request) {
        if (request == null) {
            throw new BadRequestException("Dữ liệu khoa không được để trống");
        }

        String cleanCode = request.getFacultyCode() != null ? request.getFacultyCode().trim() : "";
        String cleanName = request.getFacultyName() != null ? request.getFacultyName().trim() : "";

        if (cleanCode.isEmpty()) {
            throw new BadRequestException("Mã khoa không được để trống");
        }
        if (cleanName.isEmpty()) {
            throw new BadRequestException("Tên khoa không được để trống");
        }
        if (cleanCode.length() > 10) {
            throw new BadRequestException("Mã khoa không được vượt quá 10 ký tự");
        }
        if (cleanName.length() > 100) {
            throw new BadRequestException("Tên khoa không được vượt quá 100 ký tự");
        }

        if (facultyRepository.existsByFacultyCodeIgnoreCase(cleanCode)) {
            throw new DuplicateResourceException("Mã khoa '" + cleanCode + "' đã tồn tại trên hệ thống.");
        }
        if (facultyRepository.existsByFacultyNameIgnoreCase(cleanName)) {
            throw new DuplicateResourceException("Tên khoa '" + cleanName + "' đã tồn tại trên hệ thống.");
        }

        Faculty faculty = Faculty.builder()
                .facultyCode(cleanCode)
                .facultyName(cleanName)
                .build();

        Faculty savedFaculty = facultyRepository.save(faculty);
        return FacultyResponse.fromEntity(savedFaculty);
    }

    @Override
    @Transactional
    public FacultyResponse updateFaculty(Integer id, FacultyRequest request) {
        if (request == null) {
            throw new BadRequestException("Dữ liệu khoa không được để trống");
        }

        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoa với mã ID: " + id));

        String cleanCode = request.getFacultyCode() != null ? request.getFacultyCode().trim() : "";
        String cleanName = request.getFacultyName() != null ? request.getFacultyName().trim() : "";

        if (cleanCode.isEmpty()) {
            throw new BadRequestException("Mã khoa không được để trống");
        }
        if (cleanName.isEmpty()) {
            throw new BadRequestException("Tên khoa không được để trống");
        }
        if (cleanCode.length() > 10) {
            throw new BadRequestException("Mã khoa không được vượt quá 10 ký tự");
        }
        if (cleanName.length() > 100) {
            throw new BadRequestException("Tên khoa không được vượt quá 100 ký tự");
        }

        if (facultyRepository.existsByFacultyCodeIgnoreCaseAndIdNot(cleanCode, id)) {
            throw new DuplicateResourceException("Mã khoa '" + cleanCode + "' đã tồn tại trên hệ thống.");
        }
        if (facultyRepository.existsByFacultyNameIgnoreCaseAndIdNot(cleanName, id)) {
            throw new DuplicateResourceException("Tên khoa '" + cleanName + "' đã tồn tại trên hệ thống.");
        }

        faculty.setFacultyCode(cleanCode);
        faculty.setFacultyName(cleanName);

        Faculty updatedFaculty = facultyRepository.save(faculty);
        return FacultyResponse.fromEntity(updatedFaculty);
    }

    @Override
    @Transactional
    public void deleteFaculty(Integer id) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoa với mã ID: " + id));

        if (academicClassRepository.existsByFacultyId(id)) {
            throw new BadRequestException("Không thể xóa khoa vì đang có lớp học trực thuộc.");
        }

        facultyRepository.delete(faculty);
    }
}

