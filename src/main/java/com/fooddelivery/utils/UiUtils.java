package com.fooddelivery.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.Snackbar;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility class for UI-related operations
 */
public class UiUtils {

    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("#0.00");

    /**
     * Formats a price value with 2 decimal places and € symbol
     * @param price The price to format
     * @return Formatted price string
     */
    public static String formatPrice(double price) {
        return PRICE_FORMAT.format(price) + " €";
    }

    /**
     * Formats a rating with 1 decimal place and star symbol
     * @param rating The rating value
     * @param reviewCount The number of reviews
     * @return Formatted rating string
     */
    public static String formatRating(double rating, int reviewCount) {
        NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
        format.setMaximumFractionDigits(1);
        return format.format(rating) + " ★ (" + reviewCount + " reviews)";
    }

    /**
     * Converts dp to pixel
     * @param dp The dp value to convert
     * @return Equivalent value in pixels
     */
    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * Shows a simple toast message
     * @param context Context
     * @param message Message to display
     */
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Shows a long toast message
     * @param context Context
     * @param message Message to display
     */
    public static void showLongToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Shows a Snackbar with a message
     * @param view View to attach the Snackbar to
     * @param message Message to display
     */
    public static void showSnackbar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Shows a Snackbar with a message and action
     * @param view View to attach the Snackbar to
     * @param message Message to display
     * @param actionText Text for the action button
     * @param action Action to perform when button is clicked
     */
    public static void showSnackbarWithAction(View view, String message, String actionText, View.OnClickListener action) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setAction(actionText, action)
                .show();
    }

    /**
     * Creates and shows a simple alert dialog
     * @param context Context
     * @param title Dialog title
     * @param message Dialog message
     */
    public static void showAlertDialog(Context context, String title, String message) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * Creates and shows a confirmation dialog
     * @param context Context
     * @param title Dialog title
     * @param message Dialog message
     * @param positiveAction Action to perform when positive button is clicked
     */
    public static void showConfirmationDialog(Context context, String title, String message, 
                                             Runnable positiveAction) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> positiveAction.run())
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Hides the software keyboard
     * @param activity Current activity
     */
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Truncates a string to a maximum length with ellipsis
     * @param text Text to truncate
     * @param maxLength Maximum length allowed
     * @return Truncated text
     */
    public static String truncateText(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}