package com.kudche.cafebillingmanagement.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.kudche.cafebillingmanagement.R;

public class ToastUtils {

    public static void showSuccess(Context context, String message) {
        showCustomToast(context, message, R.color.success_green, android.R.drawable.ic_dialog_info);
    }

    public static void showError(Context context, String message) {
        showCustomToast(context, message, R.color.error_red, android.R.drawable.stat_notify_error);
    }

    public static void showInfo(Context context, String message) {
        showCustomToast(context, message, R.color.cafe_dark, android.R.drawable.ic_dialog_info);
    }

    private static void showCustomToast(Context context, String message, int bgColorRes, int iconRes) {
        if (context == null) return;

        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.custom_toast_layout, null);

        LinearLayout container = layout.findViewById(R.id.toast_container);
        ImageView icon = layout.findViewById(R.id.toast_icon);
        TextView text = layout.findViewById(R.id.toast_text);

        container.setBackgroundColor(ContextCompat.getColor(context, bgColorRes));
        icon.setImageResource(iconRes);
        text.setText(message);

        Toast toast = new Toast(context.getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}