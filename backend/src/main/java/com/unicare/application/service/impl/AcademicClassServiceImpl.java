package com.unicare.application.service.impl;

import com.unicare.application.dto.request.AcademicClassRequest;
import com.unicare.application.dto.response.AcademicClassResponse;
import com.unicare.application.service.AcademicClassService;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.common.util.ValidationUtils;
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
        return academicClassRepository.searchClasses(ValidationUtils.cleanSearch(search), facultyId).stream()
                .map(AcademicClassResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AcademicClassResponse getClassById(Integer id) {
        return AcademicClassResponse.fromEntity(findClassById(id));
    }

    @Override
    @Transactional
    public AcademicClassResponse createClass(AcademicClassRequest request) {
        validateRequestNotNull(request);
        String cleanCode = ValidationUtils.requireBoundedText(request.getClassCode(), 10, "Mã lớp");
        String cleanName = ValidationUtils.requireBoundedText(request.getClassName(), 50, "Tên lớp");
        String cleanYear = ValidationUtils.requireBoundedText(request.getAcademicYear(), 10, "Khóa học");

        if (academicClassRepository.existsByClassCodeIgnoreCase(cleanCode)) {
            throw new DuplicateResourceException("Mã lớp '" + cleanCode + "' đã tồn tại trên hệ thống.");
        }

        Faculty faculty = findFacultyById(request.getFacultyId());
        AcademicClass savedClass = academicClassRepository.save(new AcademicClass(cleanCode, cleanName, cleanYear, faculty));
        return AcademicClassResponse.fromEntity(savedClass);
    }

    @Override
    @Transactional
    public AcademicClassResponse updateClass(Integer id, AcademicClassRequest request) {
        validateRequestNotNull(request);
        AcademicClass academicClass = findClassById(id);

        String cleanCode = ValidationUtils.requireBoundedText(request.getClassCode(), 10, "Mã lớp");
        String cleanName = ValidationUtils.requireBoundedText(request.getClassName(), 50, "Tên lớp");
        String cleanYear = ValidationUtils.requireBoundedText(request.getAcademicYear(), 10, "Khóa học");

        if (academicClassRepository.existsByClassCodeIgnoreCaseAndIdNot(cleanCode, id)) {
            throw new DuplicateResourceException("Mã lớp '" + cleanCode + "' đã tồn tại trên hệ thống.");
        }

        Faculty faculty = findFacultyById(request.getFacultyId());
        academicClass.setClassCode(cleanCode);
        academicClass.setClassName(cleanName);
        academicClass.setAcademicYear(cleanYear);
        academicClass.setFaculty(faculty);

        return AcademicClassResponse.fromEntity(academicClassRepository.save(academicClass));
    }

    @Override
    @Transactional
    public void deleteClass(Integer id) {
        AcademicClass academicClass = findClassById(id);
        if (studentRepository.existsByAcademicClassId(id)) {
            throw new BadRequestException("Không thể xóa lớp học vì đang có sinh viên thuộc lớp này.");
        }
        academicClassRepository.delete(academicClass);
    }

    private AcademicClass findClassById(Integer id) {
        return academicClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với mã ID: " + id));
    }

    private Faculty findFacultyById(Integer facultyId) {
        if (facultyId == null) {
            throw new BadRequestException("Khoa trực thuộc không được để trống");
        }
        return facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoa với mã ID: " + facultyId));
    }

    private void validateRequestNotNull(AcademicClassRequest request) {
        if (request == null) {
            throw new BadRequestException("Dữ liệu lớp học không được để trống");
        }
    }
}
