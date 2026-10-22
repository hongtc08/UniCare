package com.unicare.common.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    @DisplayName("Tạo ApiResponse thành công với dữ liệu")
    void successResponse_WithData() {
        ApiResponse<String> response = ApiResponse.success("Dữ liệu mẫu");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Thao tác thành công");
        assertThat(response.getData()).isEqualTo("Dữ liệu mẫu");
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Tạo ApiResponse thành công với thông điệp tùy biến")
    void successResponse_WithMessageAndData() {
        ApiResponse<Integer> response = ApiResponse.success("Đã tìm thấy", 42);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Đã tìm thấy");
        assertThat(response.getData()).isEqualTo(42);
    }

    @Test
    @DisplayName("Tạo ApiResponse lỗi")
    void errorResponse() {
        ApiResponse<Void> response = ApiResponse.error("Có lỗi xảy ra");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Có lỗi xảy ra");
        assertThat(response.getData()).isNull();
    }
}
