package com.nstut.createprecisecontrols;

public final class ExactAmount {
    private ExactAmount() {}

    public static int parseAndClamp(String text, int min, int max) {
        if (min > max) throw new IllegalArgumentException("min > max");
        if (text == null || text.isBlank()) throw new NumberFormatException("empty amount");
        long parsed = Long.parseLong(text.trim());
        if (parsed < min) return min;
        if (parsed > max) return max;
        return (int) parsed;
    }

    /** Parses an exact value and rejects values outside the advertised UI range. */
    public static int parseInRange(String text, int min, int max) {
        if (min > max) throw new IllegalArgumentException("min > max");
        if (text == null || text.isBlank()) throw new NumberFormatException("empty amount");
        long parsed = Long.parseLong(text.trim());
        if (parsed < min || parsed > max) throw new NumberFormatException("amount out of range");
        return (int) parsed;
    }
}
