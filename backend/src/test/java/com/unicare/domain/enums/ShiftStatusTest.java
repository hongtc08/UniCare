package com.unicare.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShiftStatusTest {

    @Test
    @DisplayName("Kiểm tra parse ShiftStatus fromString với các giá trị khác nhau")
    void fromString_ValidValues() {
        assertThat(ShiftStatus.fromString("OPEN")).isEqualTo(ShiftStatus.OPEN);
        assertThat(ShiftStatus.fromString("CON_TRONG")).isEqualTo(ShiftStatus.OPEN);
        assertThat(ShiftStatus.fromString("DANG_MO")).isEqualTo(ShiftStatus.OPEN);

        assertThat(ShiftStatus.fromString("FULL")).isEqualTo(ShiftStatus.FULL);
        assertThat(ShiftStatus.fromString("DA_DAY")).isEqualTo(ShiftStatus.FULL);

        assertThat(ShiftStatus.fromString("ENDED")).isEqualTo(ShiftStatus.ENDED);
        assertThat(ShiftStatus.fromString("DA_HUY")).isEqualTo(ShiftStatus.ENDED);
        assertThat(ShiftStatus.fromString("DA_DONG")).isEqualTo(ShiftStatus.ENDED);
        assertThat(ShiftStatus.fromString("CLOSED")).isEqualTo(ShiftStatus.ENDED);

        assertThat(ShiftStatus.fromString(null)).isNull();
        assertThat(ShiftStatus.fromString("")).isNull();
        assertThat(ShiftStatus.fromString("   ")).isNull();

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> ShiftStatus.fromString("UNKNOWN"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Kiểm tra ShiftStatus values và valueOf")
    void enumBasics() {
        assertThat(ShiftStatus.values()).containsExactly(ShiftStatus.OPEN, ShiftStatus.FULL, ShiftStatus.ENDED);
        assertThat(ShiftStatus.valueOf("OPEN")).isEqualTo(ShiftStatus.OPEN);
    }
}
