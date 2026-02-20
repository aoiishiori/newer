package Client.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * CommonUtils --- shared utility class for XML parsing, date handling,
 * and other common operations used across the client application.
 */
public class CommonUtils {

    // =====================================================
    // XML UTILITIES
    // =====================================================

    /**
     * Extracts the text content inside XML tags.
     */
    public static String extractTag(String xml, String tagName) {
        if (xml == null || tagName == null) return "";

        String open = "<" + tagName + ">";
        String close = "</" + tagName + ">";
        int start = xml.indexOf(open);
        int end = xml.indexOf(close);

        if (start == -1 || end == -1) return "";
        return xml.substring(start + open.length(), end).trim();
    }

    /**
     * Escapes special characters for safe XML content.
     * Converts: & < > " ' to their XML entity equivalents.
     */
    public static String escapeXML(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    // =====================================================
    // DATE UTILITIES
    // =====================================================

    /** Standard date format used throughout the application (yyyy-MM-dd) */
    public static final DateTimeFormatter STANDARD_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Normalizes a date string to the standard format (yyyy-MM-dd - ISO).
     */
    public static String normalizeExpiryDate(String raw) {
        if (raw == null) return null;

        String v = raw.trim();
        if (v.isEmpty() || "YYYY-MM-DD".equalsIgnoreCase(v)) return null;

        // Try parsing with different date formats
        LocalDate d = tryParseDate(v, STANDARD_DATE_FORMAT);
        if (d == null) d = tryParseDate(v, DateTimeFormatter.ofPattern("M/d/uuuu"));
        if (d == null) d = tryParseDate(v, DateTimeFormatter.ofPattern("MM/dd/uuuu"));
        if (d == null) d = tryParseDate(v, DateTimeFormatter.ofPattern("uuuu/M/d"));
        if (d == null) d = tryParseDate(v, DateTimeFormatter.ofPattern("uuuu/MM/dd"));

        if (d == null) return null;
        return d.format(STANDARD_DATE_FORMAT);
    }

    /**
     * Attempts to parse a date string using the specified format.
     */
    public static LocalDate tryParseDate(String value, DateTimeFormatter fmt) {
        if (value == null || fmt == null) return null;
        try {
            return LocalDate.parse(value, fmt);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    /**
     * Validates if a date string is in a valid format.
     */
    public static boolean isValidDate(String dateStr) {
        return normalizeExpiryDate(dateStr) != null;
    }

    /**
     * Gets the current date in standard format (yyyy-MM-dd).
     */
    public static String getCurrentDate() {
        return LocalDate.now().format(STANDARD_DATE_FORMAT);
    }

    /**
     * Checks if a date is in the past (expired).
     */
    public static boolean isExpired(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return false;

        LocalDate date = tryParseDate(dateStr, STANDARD_DATE_FORMAT);
        if (date == null) return false;

        return date.isBefore(LocalDate.now());
    }

    /**
     * Checks if a date is in the future.
     */
    public static boolean isFutureDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return false;

        LocalDate date = tryParseDate(dateStr, STANDARD_DATE_FORMAT);
        if (date == null) return false;

        return date.isAfter(LocalDate.now());
    }
}