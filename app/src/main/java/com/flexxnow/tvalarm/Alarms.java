package com.flexxnow.tvalarm;
import android.app.*;
import android.content.*;
import android.os.Build;
final class Alarms {
    static android.content.SharedPreferences prefs(Context c) { return c.getSharedPreferences("alarm", 0); }
    static PendingIntent fire(Context c) {
        return PendingIntent.getBroadcast(c, 1, new Intent(c, AlarmReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
    static AlarmManager manager(Context c) { return (AlarmManager)c.getSystemService(Context.ALARM_SERVICE); }
    static void scheduleAt(Context c, long time) {
        if (Build.VERSION.SDK_INT >= 31 && !manager(c).canScheduleExactAlarms())
            throw new SecurityException("Permite alarmas exactas en los ajustes de Android.");
        PendingIntent show = PendingIntent.getActivity(c, 2, new Intent(c, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        manager(c).setAlarmClock(new AlarmManager.AlarmClockInfo(time, show), fire(c));
        prefs(c).edit().putLong("next", time).apply();
    }
    static void restoreSchedule(Context c) {
        long snooze=prefs(c).getLong("snooze",0);
        if(snooze>System.currentTimeMillis()){scheduleAt(c,snooze);return;}
        schedule(c);
    }
    static void schedule(Context c) {
        prefs(c).edit().remove("snooze").apply();
        if (!prefs(c).getBoolean("enabled", false)) { cancel(c); return; }
        scheduleAt(c, NextAlarm.calculate(System.currentTimeMillis(), prefs(c).getInt("hour",7), prefs(c).getInt("minute",0), prefs(c).getInt("days",127)));
    }
    static void cancel(Context c) { manager(c).cancel(fire(c)); prefs(c).edit().remove("next").remove("snooze").apply(); }
    static void fired(Context c) {
        prefs(c).edit().remove("next").remove("snooze").apply();
        if (prefs(c).getInt("days",127)==0) prefs(c).edit().putBoolean("enabled",false).apply();
        else {
            try { schedule(c); }
            catch (RuntimeException e) { prefs(c).edit().putBoolean("enabled",false).apply(); }
        }
    }
}
