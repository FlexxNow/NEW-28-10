package com.flexxnow.tvalarm;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

final class Ui {
    static int dp(Activity a, float value) {
        return Math.round(value * a.getResources().getDisplayMetrics().density);
    }

    static LinearLayout root(Activity a, String title) {
        ScrollView scroll = new ScrollView(a);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setBackgroundColor(Color.rgb(12, 20, 34));
        LinearLayout box = new LinearLayout(a);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(a, 40), dp(a, 24), dp(a, 40), dp(a, 32));
        scroll.addView(box, new ScrollView.LayoutParams(-1, -2));
        a.setContentView(scroll);
        text(a, box, title, 28);
        return box;
    }

    static TextView text(Activity a, LinearLayout box, String s, int size) {
        TextView t = new TextView(a);
        t.setText(s);
        t.setTextColor(Color.WHITE);
        t.setTextSize(size);
        t.setPadding(0, dp(a, 8), 0, dp(a, 8));
        box.addView(t, new LinearLayout.LayoutParams(-1, -2));
        return t;
    }

    static GradientDrawable surface(Activity a, int color, int border) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(a, 10));
        drawable.setStroke(dp(a, 2), border);
        return drawable;
    }

    static Button button(Activity a, LinearLayout box, String label, View.OnClickListener listener) {
        Button b = new Button(a);
        b.setText(label);
        b.setTextSize(18);
        b.setTextColor(Color.WHITE);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setSingleLine(false);
        b.setMinHeight(dp(a, 56));
        b.setMinimumHeight(dp(a, 56));
        b.setPadding(dp(a, 20), dp(a, 12), dp(a, 20), dp(a, 12));
        b.setFocusable(true);
        StateListDrawable background = new StateListDrawable();
        background.addState(new int[]{android.R.attr.state_pressed}, surface(a, Color.rgb(24, 76, 96), Color.WHITE));
        background.addState(new int[]{android.R.attr.state_focused}, surface(a, Color.rgb(29, 91, 118), Color.rgb(108, 231, 219)));
        background.addState(new int[]{}, surface(a, Color.rgb(30, 44, 65), Color.rgb(70, 90, 115)));
        b.setBackground(background);
        b.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(a, 6);
        params.bottomMargin = dp(a, 6);
        box.addView(b, params);
        return b;
    }

    static void error(Activity a, Exception e) {
        new android.app.AlertDialog.Builder(a).setTitle("No se pudo completar")
            .setMessage(e.getMessage()).setPositiveButton("Aceptar", null).show();
    }
}
