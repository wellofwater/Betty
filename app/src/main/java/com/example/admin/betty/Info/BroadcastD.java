package com.example.admin.betty.Info;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.betty.Activity.MainActivity;
import com.example.admin.betty.Activity.ScheduleDTO;
import com.example.admin.betty.Activity.SettingDTO;
import com.example.admin.betty.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class BroadcastD extends BroadcastReceiver {
    String mPre;
    String INTENT_ACTION = Intent.ACTION_BOOT_COMPLETED;

    boolean ispush;
    String pushweek;
    boolean[] week = {false, false, false, false, false, false, false, false};
    int pushHour;
    int pushMinute;

    @Override
    public void onReceive(final Context context, Intent intent) {
        Calendar cal = Calendar.getInstance();
        int year = cal.get ( cal.YEAR );
        final int month = cal.get ( cal.MONTH ) + 1 ;
        int date = cal.get ( cal.DATE ) ;
        String strcal = year + "" + month + "" + date;

        pushweek = intent.getStringExtra("week");
        ispush = intent.getBooleanExtra("ispush", false);
        pushHour = intent.getIntExtra("hour", 6);
        pushMinute = intent.getIntExtra("minute", 0);
        mPre = intent.getStringExtra("prepare");

        //첫번째는 반복여부, 1~7까지 일~토 t/f
        //String dd = intent.getStringExtra("week");
        //String pp = intent.getStringExtra("prepare");
        if (pushweek == "반복 없음") { week[0] = false; }
        if (pushweek.contains("일")) { week[0] = true; week[1] = true; }
        if (pushweek.contains("월")) { week[0] = true; week[2] = true; }
        if (pushweek.contains("화")) { week[0] = true; week[3] = true; }
        if (pushweek.contains("수")) { week[0] = true; week[4] = true; }
        if (pushweek.contains("목")) { week[0] = true; week[5] = true; }
        if (pushweek.contains("금")) { week[0] = true; week[6] = true; }
        if (pushweek.contains("토")) { week[0] = true; week[7] = true; }


        if (!week[0]) {
            NotificationManager notificationmanager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

            Notification.Builder builder = new Notification.Builder(context);
            builder.setSmallIcon(R.drawable.pudding).setTicker("HETT").setWhen(System.currentTimeMillis())
                    .setNumber(1).setContentTitle("오늘의 준비").setContentText(mPre)
                    .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE).setContentIntent(pendingIntent).setAutoCancel(true);
            notificationmanager.notify(1, builder.build());
        }else {
            if (week[cal.get(Calendar.DAY_OF_WEEK)]) {NotificationManager notificationmanager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

                Notification.Builder builder = new Notification.Builder(context);
                builder.setSmallIcon(R.drawable.pudding).setTicker("HETT").setWhen(System.currentTimeMillis())
                        .setNumber(1).setContentTitle("오늘의 준비").setContentText(mPre)
                        .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE).setContentIntent(pendingIntent).setAutoCancel(true);
                notificationmanager.notify(1, builder.build());
            }
        }
    }

}
