package com.flexxnow.tvalarm;
import android.app.*;
import android.content.*;
import android.os.*;
import android.view.WindowManager;
import android.widget.*;
public class RingActivity extends Activity {
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);setShowWhenLocked(true);setTurnScreenOn(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        LinearLayout box=Ui.root(this,"Es hora · Alarma TV");
        Ui.text(this,box,"La alarma se detiene automáticamente a los 5 minutos.",19);
        Ui.button(this,box,"Detener",v->{stopService(new Intent(this,AlarmService.class));finish();}).requestFocus();
        Ui.button(this,box,"Posponer 10 minutos",v->{
            if(AlarmService.ringing)startService(new Intent(this,AlarmService.class).setAction("SNOOZE"));
            finish();
        });
    }
    @Override public void onBackPressed(){stopService(new Intent(this,AlarmService.class));super.onBackPressed();}
}
