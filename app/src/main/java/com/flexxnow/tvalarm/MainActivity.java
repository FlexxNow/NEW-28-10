package com.flexxnow.tvalarm;
import android.app.*;
import android.content.*;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.widget.*;
import java.text.DateFormat;
import java.util.*;
public class MainActivity extends Activity {
    int hour,minute,days,volume; TextView status; Button time,tone; CheckBox enabled; SeekBar level; CheckBox[] checks=new CheckBox[7];
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)!=android.content.pm.PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS},50);
        if(!AlarmService.ringing)AlarmService.restoreVolume(this);
        android.content.SharedPreferences p=Alarms.prefs(this);
        hour=p.getInt("hour",7);minute=p.getInt("minute",0);days=p.getInt("days",127);volume=p.getInt("volume",60);
        LinearLayout box=Ui.root(this,"Alarma TV");
        status=Ui.text(this,box,"",17);
        enabled=new CheckBox(this);enabled.setText("Alarma activada");enabled.setTextColor(-1);enabled.setChecked(p.getBoolean("enabled",false));enabled.setTextSize(18);enabled.setMinHeight(Ui.dp(this,48));box.addView(enabled);
        time=Ui.button(this,box,String.format(Locale.getDefault(),"Hora: %02d:%02d",hour,minute),v -> chooseTime());
        Ui.text(this,box,"Repetir (ningún día = una sola vez)",16);
        LinearLayout row=new LinearLayout(this);box.addView(row);
        String[] names={"Lun","Mar","Mié","Jue","Vie","Sáb","Dom"};
        for(int n=0;n<7;n++){checks[n]=new CheckBox(this);checks[n].setText(names[n]);checks[n].setTextColor(-1);checks[n].setTextSize(16);checks[n].setChecked((days&(1<<n))!=0);row.addView(checks[n],new LinearLayout.LayoutParams(0,Ui.dp(this,56),1));}
        tone=Ui.button(this,box,"Elegir tono (predeterminado: tono propio)",v->{
            Intent i=new Intent(RingtoneManager.ACTION_RINGTONE_PICKER).putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_ALARM).putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT,false);
            try{startActivityForResult(i,10);}catch(ActivityNotFoundException e){Ui.error(this,new IllegalStateException("Este firmware no incluye selector de tonos. Puedes usar el tono propio."));}
        });
        Ui.button(this,box,"Usar tono propio",v-> {p.edit().remove("tone").apply();tone.setText("Tono propio seleccionado");});
        TextView vol=Ui.text(this,box,"Volumen: "+volume+" %",17);
        level=new SeekBar(this);level.setMax(100);level.setProgress(volume);level.setMinimumHeight(Ui.dp(this,48));box.addView(level,new LinearLayout.LayoutParams(-1,Ui.dp(this,48)));
        level.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){ public void onProgressChanged(SeekBar s,int value,boolean user){volume=value;vol.setText("Volumen: "+value+" %");}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){} });
        Ui.text(this,box,"Al sonar se ajusta temporalmente el volumen de alarma. Se restaura al detenerla. Usa primero un nivel bajo. Se detiene sola a los 5 minutos.",14);
        Ui.button(this,box,"Guardar",v->{
            if(enabled.isChecked() && volume==0){Ui.error(this,new IllegalArgumentException("Elige un volumen mayor que cero."));return;}
            int mask=0;for(int n=0;n<7;n++)if(checks[n].isChecked())mask|=1<<n;
            p.edit().putInt("hour",hour).putInt("minute",minute).putInt("days",mask).putInt("volume",volume).putBoolean("enabled",enabled.isChecked()).remove("error").apply();
            try{Alarms.schedule(this);refresh();Toast.makeText(this,"Guardado",Toast.LENGTH_SHORT).show();}
            catch(RuntimeException e){p.edit().putBoolean("enabled",false).apply();Alarms.cancel(this);enabled.setChecked(false);Ui.error(this,e);refresh();}
        });
        Ui.button(this,box,"Probar sonido ahora",v->{p.edit().putInt("volume",Math.max(5,volume)).apply();startForegroundService(new Intent(this,AlarmService.class).setAction("RING"));startActivity(new Intent(this,RingActivity.class));});
        Ui.button(this,box,"Detener sonido",v->stopService(new Intent(this,AlarmService.class)));
        if(Build.VERSION.SDK_INT>=31)Ui.button(this,box,"Permitir alarmas exactas",v->{try{startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getPackageName())));}catch(RuntimeException e){Ui.error(this,e);}});
        Ui.text(this,box,"Para probar desde reposo: guarda una alarma para dentro de 2 minutos, vuelve a Inicio y apaga con el control. No desenchufes. El encendido y el audio dependen del firmware y de la salida de sonido. Forzar detención cancela las alarmas hasta abrir y guardar de nuevo.",14);
        refresh();time.requestFocus();
    }
    void chooseTime() {
        android.view.View content = getLayoutInflater().inflate(R.layout.alarm_time_picker, null);
        TimePicker picker = content.findViewById(R.id.alarm_time);
        picker.setIs24HourView(true);
        picker.setHour(hour);
        picker.setMinute(minute);
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Hora de alarma · 24 horas")
            .setView(content)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Aceptar", (d, which) -> {
                picker.clearFocus();
                hour = picker.getHour();
                minute = picker.getMinute();
                time.setText(String.format(Locale.getDefault(), "Hora: %02d:%02d", hour, minute));
            }).create();
        dialog.setOnShowListener(d -> {
            picker.requestFocus();
        });
        dialog.show();
    }

    void refresh(){if(status==null)return;long next=Alarms.prefs(this).getLong("next",0);status.setText((next>0?"Próxima: "+DateFormat.getDateTimeInstance().format(new Date(next)):"Sin alarma programada")+"\n"+Alarms.prefs(this).getString("error",""));}
    @Override public void onResume(){super.onResume();refresh();}
    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==10&&result==RESULT_OK&&data!=null){Uri uri=data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);if(uri!=null){Alarms.prefs(this).edit().putString("tone",uri.toString()).apply();tone.setText("Tono del sistema seleccionado");}}}
}
