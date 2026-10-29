package com.flexxnow.tvalarm;
import android.app.*;
import android.content.*;
import android.media.*;
import android.net.Uri;
import android.os.*;
public class AlarmService extends Service {
    public static volatile boolean ringing;
    MediaPlayer player;AudioManager audio;PowerManager.WakeLock lock;int original=-1,applied=-1;
    Handler handler=new Handler(Looper.getMainLooper());
    final Runnable timeout=()->stopSelf();
    static final String CHANNEL="alarm-ring-v1";
    PendingIntent activity(){return PendingIntent.getActivity(this,4,new Intent(this,RingActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
    @Override public int onStartCommand(Intent intent,int flags,int id){
        if(intent==null){stopSelf();return START_NOT_STICKY;}
        String action=intent.getAction();
        if("STOP".equals(action)){stopSelf();return START_NOT_STICKY;}
        if("SNOOZE".equals(action)){
            try{long at=System.currentTimeMillis()+10*60*1000L;Alarms.scheduleAt(this,at);Alarms.prefs(this).edit().putLong("snooze",at).apply();}
            catch(RuntimeException e){Alarms.prefs(this).edit().putString("error","No se pudo posponer: "+e.getMessage()).apply();}
            stopSelf();return START_NOT_STICKY;
        }
        if(!"RING".equals(action)){stopSelf();return START_NOT_STICKY;}
        NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel ch=new NotificationChannel(CHANNEL,"Alarma sonando",NotificationManager.IMPORTANCE_HIGH);
        ch.setSound(null,null);ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);nm.createNotificationChannel(ch);
        PendingIntent stop=PendingIntent.getService(this,5,new Intent(this,AlarmService.class).setAction("STOP"),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification notification=new Notification.Builder(this,CHANNEL).setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("Alarma TV").setContentText("Pulsa para detener o posponer").setCategory(Notification.CATEGORY_ALARM).setVisibility(Notification.VISIBILITY_PUBLIC).setOngoing(true).setContentIntent(activity()).setFullScreenIntent(activity(),true).addAction(new Notification.Action.Builder(android.R.drawable.ic_delete,"Detener",stop).build()).build();
        startForeground(1,notification,android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        if(ringing)return START_NOT_STICKY;
        ringing=true;
        PowerManager pm=(PowerManager)getSystemService(POWER_SERVICE);
        lock=pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"AlarmaTV:sonido");lock.acquire(6*60*1000L);
        restoreVolume(this);
        audio=(AudioManager)getSystemService(AUDIO_SERVICE);
        original=audio.getStreamVolume(AudioManager.STREAM_ALARM);
        applied=Math.max(1,Math.round(audio.getStreamMaxVolume(AudioManager.STREAM_ALARM)*Alarms.prefs(this).getInt("volume",60)/100f));
        try{audio.setStreamVolume(AudioManager.STREAM_ALARM,applied,0);}catch(RuntimeException e){original=-1;}
        // Recover a volume left by an abrupt service/process termination on the next app launch.
        if(original>=0)Alarms.prefs(this).edit().putInt("restore_volume",original).putInt("applied_volume",applied).apply();
        try{
            player=new MediaPlayer();
            player.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build());
            String tone=Alarms.prefs(this).getString("tone","");
            try{
                if(tone.isEmpty()) throw new IllegalStateException("Use bundled tone");
                player.setDataSource(this,Uri.parse(tone));player.prepare();
            }catch(Exception e){
                player.reset();player.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build());
                try(android.content.res.AssetFileDescriptor afd=getResources().openRawResourceFd(R.raw.alarm)){
                    player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());player.prepare();
                }
            }
            player.setLooping(true);player.setVolume(1f,1f);
            player.setOnErrorListener((mp,what,extra)->{Alarms.prefs(this).edit().putString("error","Error de reproducción: "+what+" / "+extra).apply();stopSelf();return true;});
            player.start();
        }catch(Exception e){Alarms.prefs(this).edit().putString("error","No se pudo reproducir: "+e.getMessage()).apply();stopSelf();return START_NOT_STICKY;}
        handler.postDelayed(timeout,5*60*1000L);
        return START_NOT_STICKY;
    }
    static void restoreVolume(Context c){
        android.content.SharedPreferences p=Alarms.prefs(c);
        if(!p.contains("restore_volume"))return;
        AudioManager a=(AudioManager)c.getSystemService(AUDIO_SERVICE);
        try{if(a.getStreamVolume(AudioManager.STREAM_ALARM)==p.getInt("applied_volume",-1))a.setStreamVolume(AudioManager.STREAM_ALARM,p.getInt("restore_volume",0),0);}catch(RuntimeException ignored){}
        p.edit().remove("restore_volume").remove("applied_volume").apply();
    }
    @Override public void onDestroy(){
        handler.removeCallbacks(timeout);
        if(player!=null){player.release();player=null;}
        restoreVolume(this);
        if(lock!=null&&lock.isHeld())lock.release();
        ringing=false;stopForeground(true);super.onDestroy();
    }
    @Override public IBinder onBind(Intent i){return null;}
}
