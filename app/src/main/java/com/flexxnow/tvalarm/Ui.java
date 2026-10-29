package com.flexxnow.tvalarm;
import android.app.Activity;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
final class Ui {
    static LinearLayout root(Activity a, String title) {
        ScrollView scroll=new ScrollView(a); scroll.setFillViewport(true);
        LinearLayout box=new LinearLayout(a); box.setOrientation(LinearLayout.VERTICAL);box.setPadding(48,24,48,24);box.setBackgroundColor(Color.rgb(12,20,34));scroll.addView(box);a.setContentView(scroll);
        text(a,box,title,28); return box;
    }
    static TextView text(Activity a,LinearLayout box,String s,int size) {
        TextView t=new TextView(a);t.setText(s);t.setTextColor(Color.WHITE);t.setTextSize(size);t.setPadding(0,8,0,8);box.addView(t);return t;
    }
    static Button button(Activity a,LinearLayout box,String label,View.OnClickListener listener) {
        Button b=new Button(a); b.setText(label);b.setAllCaps(false);b.setOnClickListener(listener);box.addView(b,new LinearLayout.LayoutParams(-1,56)); return b;
    }
    static void error(Activity a,Exception e) { new android.app.AlertDialog.Builder(a).setTitle("No se pudo completar").setMessage(e.getMessage()).setPositiveButton("Aceptar",null).show(); }
}
