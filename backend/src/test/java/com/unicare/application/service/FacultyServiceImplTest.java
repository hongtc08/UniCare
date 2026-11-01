package com.unicare.application.service;

import com.unicare.application.dto.request.FacultyRequest;
import com.unicare.application.dto.response.FacultyResponse;
import com.unicare.application.service.impl.FacultyServiceImpl;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.domain.entity.Faculty;
import com.unicare.domain.repository.AcademicClassRepository;
import com.unicare.domain.repository.FacultyRepository;
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
class FacultyServiceImplTest {

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private AcademicClassRepository academicClassRepository;

    @InjectMocks
    private FacultyServiceImpl facultyService;

    private Faculty sampleFaculty;

    @BeforeEach
    void setUp() {
        sampleFaculty = new Faculty(1, "CNPM", "Công nghệ phần mềm");
    }

    @Test
    @DisplayName("Lấy danh sách khoa thành công")
    void getFaculties_Success() {
        when(facultyRepository.searchFaculties("")).thenReturn(List.of(sampleFaculty));

        List<FacultyResponse> result = facultyService.getFaculties(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1);
        assertThat(result.get(0).getFacultyCode()).isEqualTo("CNPM");
        assertThat(result.get(0).getFacultyName()).isEqualTo("Công nghệ phần mềm");
        assertThat(result.get(0).getCode()).isEqualTo("CNPM");
        assertThat(result.get(0).getName()).isEqualTo("Công nghệ phần mềm");
    }

    @Test
    @DisplayName("Lấy thông tin chi tiết khoa theo ID thành công")
    void getFacultyById_Success() {
        when(facultyRepository.findById(1)).thenReturn(Optional.of(sampleFaculty));

        FacultyResponse result = facultyService.getFacultyById(1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getFacultyCode()).isEqualTo("CNPM");
    }

    @Test
    @DisplayName("Lấy chi tiết khoa thất bại khi ID không tồn tại (404)")
    void getFacultyById_NotFound() {
        when(facultyRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facultyService.getFacultyById(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy");
    }

    @Test
    @DisplayName("Tạo mới khoa thành công")
    void createFaculty_Success() {
        FacultyRequest request = new FacultyRequest("KHMT", "Khoa học máy tính");
        when(facultyRepository.existsByFacultyCodeIgnoreCase("KHMT")).thenReturn(false);
        when(facultyRepository.existsByFacultyNameIgnoreCase("Khoa học máy tính")).thenReturn(false);
        when(facultyRepository.save(any(Faculty.class))).thenAnswer(invocation -> {
            Faculty f = invocation.getArgument(0);
            f.setId(2);
            return f;
        });

        FacultyResponse result = facultyService.createFaculty(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2);
        assertThat(result.getFacultyCode()).isEqualTo("KHMT");
        assertThat(result.getFacultyName()).isEqualTo("Khoa học máy tính");
        verify(facultyRepository).save(any(Faculty.class));
    }

    @Test
    @DisplayName("Tạo khoa thất bại do trùng mã khoa (409)")
    void createFaculty_DuplicateCode_ThrowsException() {
        FacultyRequest request = new FacultyRequest("CNPM", "Công nghệ phần mềm mới");
        when(facultyRepository.existsByFacultyCodeIgnoreCase("CNPM")).thenReturn(true);

        assertThatThrownBy(() -> facultyService.createFaculty(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Mã khoa 'CNPM' đã tồn tại");

        verify(facultyRepository, never()).save(any(Faculty.class));
    }

    @Test
    @DisplayName("Tạo khoa thất bại do trùng tên khoa (409)")
    void createFaculty_DuplicateName_ThrowsException() {
        FacultyRequest request = new FacultyRequest("CNPM02", "Công nghệ phần mềm");
        when(facultyRepository.existsByFacultyCodeIgnoreCase("CNPM02")).thenReturn(false);
        when(facultyRepository.existsByFacultyNameIgnoreCase("Công nghệ phần mềm")).thenReturn(true);

        assertThatThrownBy(() -> facultyService.createFaculty(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Tên khoa 'Công nghệ phần mềm' đã tồn tại");

        verify(facultyRepository, never()).save(any(Faculty.class));
    }

    @Test
    @DisplayName("Cập nhật khoa thành công")
    void updateFaculty_Success() {
        FacultyRequest request = new FacultyRequest("CNPM", "Khoa Kỹ thuật Phần mềm");
        when(facultyRepository.findById(1)).thenReturn(Optional.of(sampleFaculty));
        when(facultyRepository.existsByFacultyCodeIgnoreCaseAndIdNot("CNPM", 1)).thenReturn(false);
        when(facultyRepository.existsByFacultyNameIgnoreCaseAndIdNot("Khoa Kỹ thuật Phần mềm", 1)).thenReturn(false);
        when(facultyRepository.save(any(Faculty.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FacultyResponse result = facultyService.updateFaculty(1, request);

        assertThat(result.getFacultyName()).isEqualTo("Khoa Kỹ thuật Phần mềm");
        verify(facultyRepository).save(sampleFaculty);
    }

    @Test
    @DisplayName("Cập nhật khoa thất bại do không tìm thấy ID (404)")
    void updateFaculty_NotFound() {
        FacultyRequest request = new FacultyRequest("CNPM", "Khoa");
        when(facultyRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facultyService.updateFaculty(99, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Xóa khoa thành công khi không có lớp học trực thuộc")
    void deleteFaculty_Success() {
        when(facultyRepository.findById(1)).thenReturn(Optional.of(sampleFaculty));
        when(academicClassRepository.existsByFacultyId(1)).thenReturn(false);

        facultyService.deleteFaculty(1);

        verify(facultyRepository).delete(sampleFaculty);
    }

    @Test
    @DisplayName("Xóa khoa thất bại khi đang có lớp học trực thuộc (400)")
    void deleteFaculty_HasClasses_ThrowsBadRequest() {
        when(facultyRepository.findById(1)).thenReturn(Optional.of(sampleFaculty));
        when(academicClassRepository.existsByFacultyId(1)).thenReturn(true);

        assertThatThrownBy(() -> facultyService.deleteFaculty(1))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("đang có lớp học trực thuộc");

        verify(facultyRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Xóa khoa thất bại khi không tìm thấy ID (404)")
    void deleteFaculty_NotFound() {
        when(facultyRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facultyService.deleteFaculty(99))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(facultyRepository, never()).delete(any());
    }
}

