package com.flexxnow.tvalarm;
import android.content.*;
public class AlarmReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent i) {
        long expected = Alarms.prefs(c).getLong("next", 0);
        Alarms.prefs(c).edit().putLong("last_fired", System.currentTimeMillis())
            .putLong("last_expected", expected).apply();
        Alarms.fired(c);
        try {
            c.startForegroundService(new Intent(c, AlarmService.class).setAction("RING"));
        } catch (RuntimeException e) {
            Alarms.prefs(c).edit().putString("error", "No se pudo iniciar el sonido: " + e.getMessage()).apply();
        }
    }
}
