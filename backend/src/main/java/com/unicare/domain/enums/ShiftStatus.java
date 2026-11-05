/**
 * Enum định nghĩa trạng thái của ca khám:
 * OPEN (Còn trống), FULL (Đã đầy), ENDED (Đã kết thúc / Hủy).
 */
package com.unicare.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ShiftStatus {
    OPEN,
    FULL,
    ENDED;

    @JsonCreator
    public static ShiftStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        return switch (normalized) {
            case "OPEN", "CON_TRONG", "DANG_MO" -> OPEN;
            case "FULL", "DA_DAY" -> FULL;
            case "ENDED", "DA_HUY", "DA_DONG", "CLOSED" -> ENDED;
            default -> valueOf(normalized);
        };
    }
}
