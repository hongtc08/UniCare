package com.unicare.application.service;

import com.unicare.application.dto.request.AcademicClassRequest;
import com.unicare.application.dto.response.AcademicClassResponse;
import com.unicare.application.service.impl.AcademicClassServiceImpl;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.domain.entity.AcademicClass;
import com.unicare.domain.entity.Faculty;
import com.unicare.domain.repository.AcademicClassRepository;
import com.unicare.domain.repository.FacultyRepository;
import com.unicare.domain.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcademicClassServiceImplTest {

    @Mock
    private AcademicClassRepository academicClassRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private AcademicClassServiceImpl academicClassService;

    private Faculty sampleFaculty;
    private AcademicClass sampleClass;

    @BeforeEach
    void setUp() {
        sampleFaculty = new Faculty(1, "CNPM", "Công nghệ phần mềm");
        sampleClass = new AcademicClass(1, "SE104.O11", "Kỹ thuật phần mềm 1", "K19", sampleFaculty);
    }

    @Test
    @DisplayName("Lấy danh sách lớp học thành công")
    void getClasses_Success() {
        when(academicClassRepository.searchClasses("", null)).thenReturn(List.of(sampleClass));

        List<AcademicClassResponse> result = academicClassService.getClasses(null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1);
        assertThat(result.get(0).getClassCode()).isEqualTo("SE104.O11");
        assertThat(result.get(0).getClassName()).isEqualTo("Kỹ thuật phần mềm 1");
        assertThat(result.get(0).getAcademicYear()).isEqualTo("K19");
        assertThat(result.get(0).getFacultyId()).isEqualTo(1);
        assertThat(result.get(0).getCode()).isEqualTo("SE104.O11");
        assertThat(result.get(0).getName()).isEqualTo("Kỹ thuật phần mềm 1");
        assertThat(result.get(0).getCohort()).isEqualTo("K19");
    }

    @Test
    @DisplayName("Lấy thông tin chi tiết lớp học theo ID thành công")
    void getClassById_Success() {
        when(academicClassRepository.findById(1)).thenReturn(Optional.of(sampleClass));

        AcademicClassResponse result = academicClassService.getClassById(1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getClassCode()).isEqualTo("SE104.O11");
    }

    @Test
    @DisplayName("Lấy chi tiết lớp học thất bại khi ID không tồn tại (404)")
    void getClassById_NotFound() {
        when(academicClassRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> academicClassService.getClassById(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy");
    }

    @Test
    @DisplayName("Tạo mới lớp học thành công")
    void createClass_Success() {
        AcademicClassRequest request = new AcademicClassRequest("SE104.O12", "Kỹ thuật phần mềm 2", "K19", 1);
        when(academicClassRepository.existsByClassCodeIgnoreCase("SE104.O12")).thenReturn(false);
        when(facultyRepository.findById(1)).thenReturn(Optional.of(sampleFaculty));
        when(academicClassRepository.save(any(AcademicClass.class))).thenAnswer(invocation -> {
            AcademicClass c = invocation.getArgument(0);
            c.setId(2);
            return c;
        });

        AcademicClassResponse result = academicClassService.createClass(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2);
        assertThat(result.getClassCode()).isEqualTo("SE104.O12");
        assertThat(result.getFacultyId()).isEqualTo(1);
        verify(academicClassRepository).save(any(AcademicClass.class));
    }

    @Test
    @DisplayName("Tạo lớp học thất bại do trùng mã lớp (409)")
    void createClass_DuplicateCode_ThrowsException() {
        AcademicClassRequest request = new AcademicClassRequest("SE104.O11", "Kỹ thuật phần mềm", "K19", 1);
        when(academicClassRepository.existsByClassCodeIgnoreCase("SE104.O11")).thenReturn(true);

        assertThatThrownBy(() -> academicClassService.createClass(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Mã lớp 'SE104.O11' đã tồn tại");

        verify(academicClassRepository, never()).save(any(AcademicClass.class));
    }

    @Test
    @DisplayName("Tạo lớp học thất bại do không tìm thấy khoa (404)")
    void createClass_FacultyNotFound_ThrowsException() {
        AcademicClassRequest request = new AcademicClassRequest("SE104.O12", "Lớp", "K19", 99);
        when(academicClassRepository.existsByClassCodeIgnoreCase("SE104.O12")).thenReturn(false);
        when(facultyRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> academicClassService.createClass(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy khoa");

        verify(academicClassRepository, never()).save(any(AcademicClass.class));
    }

    @Test
    @DisplayName("Cập nhật lớp học thành công")
    void updateClass_Success() {
        AcademicClassRequest request = new AcademicClassRequest("SE104.O11", "Kỹ thuật phần mềm Mới", "K20", 1);
        when(academicClassRepository.findById(1)).thenReturn(Optional.of(sampleClass));
        when(academicClassRepository.existsByClassCodeIgnoreCaseAndIdNot("SE104.O11", 1)).thenReturn(false);
        when(facultyRepository.findById(1)).thenReturn(Optional.of(sampleFaculty));
        when(academicClassRepository.save(any(AcademicClass.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AcademicClassResponse result = academicClassService.updateClass(1, request);

        assertThat(result.getClassName()).isEqualTo("Kỹ thuật phần mềm Mới");
        assertThat(result.getAcademicYear()).isEqualTo("K20");
        verify(academicClassRepository).save(sampleClass);
    }

    @Test
    @DisplayName("Xóa lớp học thành công khi không có sinh viên")
    void deleteClass_Success() {
        when(academicClassRepository.findById(1)).thenReturn(Optional.of(sampleClass));
        when(studentRepository.existsByAcademicClassId(1)).thenReturn(false);

        academicClassService.deleteClass(1);

        verify(academicClassRepository).delete(sampleClass);
    }

    @Test
    @DisplayName("Xóa lớp học thất bại khi đang có sinh viên trực thuộc (400)")
    void deleteClass_HasStudents_ThrowsBadRequest() {
        when(academicClassRepository.findById(1)).thenReturn(Optional.of(sampleClass));
        when(studentRepository.existsByAcademicClassId(1)).thenReturn(true);

        assertThatThrownBy(() -> academicClassService.deleteClass(1))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("đang có sinh viên thuộc lớp này");

        verify(academicClassRepository, never()).delete(any());
    }
}

