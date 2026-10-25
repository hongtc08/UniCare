/**
 * Enum định nghĩa và kiểm tra các nhóm máu hợp lệ (A, B, AB, O).
 */
package com.unicare.domain.enums;

import java.util.Set;

public enum BloodType {
    A,
    B,
    AB,
    O;

    private static final Set<String> VALID_NAMES = Set.of("A", "B", "AB", "O");

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return VALID_NAMES.contains(value.trim().toUpperCase());
    }
}
