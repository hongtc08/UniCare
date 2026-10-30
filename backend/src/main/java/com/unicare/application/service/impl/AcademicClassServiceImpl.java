package com.unicare.application.service.impl;

import com.unicare.application.dto.request.AcademicClassRequest;
import com.unicare.application.dto.response.AcademicClassResponse;
import com.unicare.application.service.AcademicClassService;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.domain.entity.AcademicClass;
import com.unicare.domain.entity.Faculty;
import com.unicare.domain.repository.AcademicClassRepository;
import com.unicare.domain.repository.FacultyRepository;
import com.unicare.domain.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AcademicClassServiceImpl implements AcademicClassService {

    private final AcademicClassRepository academicClassRepository;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;

    public AcademicClassServiceImpl(
            AcademicClassRepository academicClassRepository,
            FacultyRepository facultyRepository,
            StudentRepository studentRepository
    ) {
        this.academicClassRepository = academicClassRepository;
        this.facultyRepository = facultyRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcademicClassResponse> getClasses(String search, Integer facultyId) {
        String cleanSearch = search == null ? "" : search.trim();
        List<AcademicClass> list = academicClassRepository.searchClasses(cleanSearch, facultyId);
        return list.stream()
                .map(AcademicClassResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AcademicClassResponse getClassById(Integer id) {
        AcademicClass academicClass = academicClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với mã ID: " + id));
        return AcademicClassResponse.fromEntity(academicClass);
    }

    @Override
    @Transactional
    public AcademicClassResponse createClass(AcademicClassRequest request) {
        if (request == null) {
            throw new BadRequestException("Dữ liệu lớp học không được để trống");
        }

        String cleanCode = request.getClassCode() != null ? request.getClassCode().trim() : "";
        String cleanName = request.getClassName() != null ? request.getClassName().trim() : "";
        String cleanYear = request.getAcademicYear() != null ? request.getAcademicYear().trim() : "";

        if (cleanCode.isEmpty()) {
            throw new BadRequestException("Mã lớp không được để trống");
        }
        if (cleanName.isEmpty()) {
            throw new BadRequestException("Tên lớp không được để trống");
        }
        if (cleanYear.isEmpty()) {
            throw new BadRequestException("Khóa học không được để trống");
        }
        if (request.getFacultyId() == null) {
            throw new BadRequestException("Khoa trực thuộc không được để trống");
        }
        if (cleanCode.length() > 10) {
            throw new BadRequestException("Mã lớp không được vượt quá 10 ký tự");
        }
        if (cleanName.length() > 50) {
            throw new BadRequestException("Tên lớp không được vượt quá 50 ký tự");
        }
        if (cleanYear.length() > 10) {
            throw new BadRequestException("Khóa học không được vượt quá 10 ký tự");
        }

        if (academicClassRepository.existsByClassCodeIgnoreCase(cleanCode)) {
            throw new DuplicateResourceException("Mã lớp '" + cleanCode + "' đã tồn tại trên hệ thống.");
        }

        Faculty faculty = facultyRepository.findById(request.getFacultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoa với mã ID: " + request.getFacultyId()));

        AcademicClass academicClass = AcademicClass.builder()
                .classCode(cleanCode)
                .className(cleanName)
                .academicYear(cleanYear)
                .faculty(faculty)
                .build();

        AcademicClass savedClass = academicClassRepository.save(academicClass);
        return AcademicClassResponse.fromEntity(savedClass);
    }

    @Override
    @Transactional
    public AcademicClassResponse updateClass(Integer id, AcademicClassRequest request) {
        if (request == null) {
            throw new BadRequestException("Dữ liệu lớp học không được để trống");
        }

        AcademicClass academicClass = academicClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với mã ID: " + id));

        String cleanCode = request.getClassCode() != null ? request.getClassCode().trim() : "";
        String cleanName = request.getClassName() != null ? request.getClassName().trim() : "";
        String cleanYear = request.getAcademicYear() != null ? request.getAcademicYear().trim() : "";

        if (cleanCode.isEmpty()) {
            throw new BadRequestException("Mã lớp không được để trống");
        }
        if (cleanName.isEmpty()) {
            throw new BadRequestException("Tên lớp không được để trống");
        }
        if (cleanYear.isEmpty()) {
            throw new BadRequestException("Khóa học không được để trống");
        }
        if (request.getFacultyId() == null) {
            throw new BadRequestException("Khoa trực thuộc không được để trống");
        }
        if (cleanCode.length() > 10) {
            throw new BadRequestException("Mã lớp không được vượt quá 10 ký tự");
        }
        if (cleanName.length() > 50) {
            throw new BadRequestException("Tên lớp không được vượt quá 50 ký tự");
        }
        if (cleanYear.length() > 10) {
            throw new BadRequestException("Khóa học không được vượt quá 10 ký tự");
        }

        if (academicClassRepository.existsByClassCodeIgnoreCaseAndIdNot(cleanCode, id)) {
            throw new DuplicateResourceException("Mã lớp '" + cleanCode + "' đã tồn tại trên hệ thống.");
        }

        Faculty faculty = facultyRepository.findById(request.getFacultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoa với mã ID: " + request.getFacultyId()));

        academicClass.setClassCode(cleanCode);
        academicClass.setClassName(cleanName);
        academicClass.setAcademicYear(cleanYear);
        academicClass.setFaculty(faculty);

        AcademicClass updatedClass = academicClassRepository.save(academicClass);
        return AcademicClassResponse.fromEntity(updatedClass);
    }

    @Override
    @Transactional
    public void deleteClass(Integer id) {
        AcademicClass academicClass = academicClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với mã ID: " + id));

        if (studentRepository.existsByAcademicClassId(id)) {
            throw new BadRequestException("Không thể xóa lớp học vì đang có sinh viên thuộc lớp này.");
        }

        academicClassRepository.delete(academicClass);
    }
}

