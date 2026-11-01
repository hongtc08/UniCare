package com.unicare.common.util;

import com.unicare.common.exception.BadRequestException;

public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static String cleanSearch(String search) {
        return search == null ? "" : search.trim();
    }

    public static String requireBoundedText(String value, int maxLength, String fieldLabel) {
        String cleaned = value != null ? value.trim() : "";
        if (cleaned.isEmpty()) {
            throw new BadRequestException(fieldLabel + " không được để trống");
        }
        if (cleaned.length() > maxLength) {
            throw new BadRequestException(fieldLabel + " không được vượt quá " + maxLength + " ký tự");
        }
        return cleaned;
    }
}

