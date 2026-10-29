package com.flexxnow.tvalarm;
import java.util.*;
public class NextAlarmTest {
 static long t(int day,int hour,int minute){Calendar c=Calendar.getInstance();c.clear();c.set(2026,Calendar.OCTOBER,day,hour,minute);return c.getTimeInMillis();}
 static void eq(long a,long b){if(a!=b)throw new AssertionError(new Date(a)+" != "+new Date(b));}
 public static void main(String[] args){
  TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
  eq(NextAlarm.calculate(t(29,6,0),7,0,127),t(29,7,0));
  eq(NextAlarm.calculate(t(29,7,0),7,0,127),t(30,7,0));
  eq(NextAlarm.calculate(t(29,8,0),7,0,0),t(30,7,0));
  eq(NextAlarm.calculate(t(29,8,0),7,0,1),t(2,7,0)+31L*24*3600000); // November 2, next Monday
  eq(NextAlarm.calculate(t(29,8,0),7,0,1<<3),t(29,7,0)+7L*24*3600000);
  for(int mask=0;mask<128;mask++){
   long next=NextAlarm.calculate(t(29,8,0),7,0,mask);
   if(next<=t(29,8,0)||next>t(29,8,0)+7L*24*3600000)throw new AssertionError("Range");
   Calendar c=Calendar.getInstance();c.setTimeInMillis(next);
   int d=(c.get(Calendar.DAY_OF_WEEK)+5)%7;
   if(mask!=0&&(mask&(1<<d))==0)throw new AssertionError("Wrong weekday");
  }
  boolean invalid=false;try{NextAlarm.calculate(0,24,0,0);}catch(IllegalArgumentException e){invalid=true;}
  if(!invalid)throw new AssertionError("Invalid hour accepted");
  System.out.println("PASS: 134 scheduling cases");
 }
}
