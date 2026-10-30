package com.unicare.common.util;

import com.unicare.common.exception.BadRequestException;

public final class ValidationUtils {

    /**
     * Regex chuẩn mã thẻ BHYT tại Việt Nam:
     * - Mẫu mới: đúng 10 chữ số
     * - Mẫu cũ: 15 ký tự gồm 2 chữ cái đầu và 13 chữ số
     */
    public static final String BHYT_CARD_NUMBER_REGEX = "^(?:\\d{10}|[A-Z]{2}\\d{13})$";

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

    public static String requireValidBhytCardNumber(String cardNumber) {
        String cleaned = cardNumber != null ? cardNumber.trim().toUpperCase() : "";
        if (cleaned.isEmpty()) {
            throw new BadRequestException("Mã thẻ BHYT không được để trống");
        }
        if (!cleaned.matches(BHYT_CARD_NUMBER_REGEX)) {
            throw new BadRequestException(
                    "Mã thẻ BHYT không đúng định dạng chuẩn Việt Nam (gồm 10 chữ số hoặc 15 ký tự: 2 chữ cái đầu và 13 chữ số)");
        }
        return cleaned;
    }
}
