package com.example.smartpantrymanager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Small helper methods shared by several screens:
 * unit list, quantity formatting and expiry-date maths.
 */
public class AppUtils {

    // The units a user can pick when adding an ingredient
    public static final String[] UNITS = {"g", "kg", "ml", "l", "pcs", "cup", "tbsp", "tsp"};

    // An ingredient counts as "expiring soon" when it expires within this many days
    public static final int EXPIRY_WARNING_DAYS = 3;

    // Returned when an item has no (or an unreadable) expiry date
    public static final int NO_EXPIRY = Integer.MAX_VALUE;

    /** Shows 2.0 as "2" and 1.5 as "1.5" so quantities look clean on screen. */
    public static String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.valueOf((long) quantity);
        }
        String text = String.format(Locale.US, "%.2f", quantity);
        if (text.endsWith("0")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    /**
     * How many days from today until the given date (format yyyy-MM-dd).
     * Negative = already expired. NO_EXPIRY = no valid date was given.
     */
    public static int daysUntilExpiry(String date) {
        if (date == null || date.trim().isEmpty()) {
            return NO_EXPIRY;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            format.setLenient(false);
            Date expiry = format.parse(date.trim());

            // Today at midnight, so we compare whole days only
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            long differenceMillis = expiry.getTime() - today.getTimeInMillis();
            return (int) Math.round(differenceMillis / (24.0 * 60 * 60 * 1000));
        } catch (ParseException e) {
            return NO_EXPIRY;
        }
    }

    /** True if the item has a date and it is within the warning window (or already past). */
    public static boolean isExpiringSoon(String date) {
        int days = daysUntilExpiry(date);
        return days != NO_EXPIRY && days <= EXPIRY_WARNING_DAYS;
    }
}
