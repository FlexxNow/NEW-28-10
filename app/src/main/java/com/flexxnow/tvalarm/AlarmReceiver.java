package com.flexxnow.tvalarm;
import android.content.*;
public class AlarmReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent i) {
        Alarms.fired(c);
        c.startForegroundService(new Intent(c, AlarmService.class).setAction("RING"));
    }
}
