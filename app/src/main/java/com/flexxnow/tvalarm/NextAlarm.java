package com.flexxnow.tvalarm;
import java.util.Calendar;
public final class NextAlarm {
    private NextAlarm() {}
    // Bit 0 = Monday, bit 6 = Sunday. Zero means once, at the next occurrence.
    public static long calculate(long now, int hour, int minute, int days) {
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59 || days < 0 || days > 127)
            throw new IllegalArgumentException("Invalid alarm");
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(now);
        for (int offset = 0; offset <= 7; offset++) {
            Calendar candidate = (Calendar)c.clone();
            candidate.add(Calendar.DATE, offset);
            candidate.set(Calendar.HOUR_OF_DAY, hour);
            candidate.set(Calendar.MINUTE, minute);
            candidate.set(Calendar.SECOND, 0);
            candidate.set(Calendar.MILLISECOND, 0);
            int day = (candidate.get(Calendar.DAY_OF_WEEK) + 5) % 7;
            if (candidate.getTimeInMillis() > now && (days == 0 || (days & (1 << day)) != 0))
                return candidate.getTimeInMillis();
        }
        throw new IllegalStateException("No next alarm");
    }
}
