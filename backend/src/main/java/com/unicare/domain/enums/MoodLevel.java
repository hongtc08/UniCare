package com.unicare.domain.enums;

import java.util.Arrays;
import java.util.Optional;

/**
 * Enum định nghĩa thang điểm mức tâm trạng hằng ngày (1 đến 5) của sinh viên.
 */
public enum MoodLevel {
    VERY_BAD(1, "Rất tệ"),
    BAD(2, "Tệ"),
    NORMAL(3, "Bình thường"),
    HAPPY(4, "Vui vẻ"),
    GREAT(5, "Tuyệt vời");

    private final int level;
    private final String label;

    MoodLevel(int level, String label) {
        this.level = level;
        this.label = label;
    }

    public int getLevel() {
        return level;
    }

    public String getLabel() {
        return label;
    }

    public static Optional<MoodLevel> fromLevel(Integer level) {
        if (level == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(item -> item.level == level)
                .findFirst();
    }

    public static String labelOf(Integer level) {
        return fromLevel(level)
                .map(MoodLevel::getLabel)
                .orElse(null);
    }

    public static boolean isValid(Integer level) {
        return fromLevel(level).isPresent();
    }
}
