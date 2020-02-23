package com.example.admin.betty.Activity;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.admin.betty.Fragment.TimePickerFragment;
import com.example.admin.betty.Info.BroadcastD;
import com.example.admin.betty.LoginActivity;
import com.example.admin.betty.Manifest;
import com.example.admin.betty.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth auth;
    private FirebaseDatabase mscheduleDatabase;
    private DatabaseReference mscheduleReference;
    private DatabaseReference msettingReference;
    String Uid;
    String mPrepare = null;

    AlarmManager mAlarmManager;

    private TextView emailtext;
    private TextView popuptime;
    private TextView logout;
    private TextView nfcTag;
    private TextView weekrepeat;
    private Button btnTimeset;
    private Switch switchPush;

    int mHour, mMinute;
    boolean isPush;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Calendar cal = Calendar.getInstance();
        int year = cal.get ( cal.YEAR );
        int month = cal.get ( cal.MONTH ) + 1 ;
        int date = cal.get ( cal.DATE ) ;
        String strcal = year + "" + month + "" + date;

        auth = FirebaseAuth.getInstance();
        Uid = auth.getCurrentUser().getUid();
        mscheduleDatabase = FirebaseDatabase.getInstance();
        mscheduleReference = mscheduleDatabase.getReference().child("schedules").child(Uid).child(strcal);
        msettingReference = mscheduleDatabase.getReference().child("information").child(Uid).child("setting");

        mscheduleReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String title = snapshot.getKey();

                    mscheduleReference.child(title).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            ScheduleDTO schedule = dataSnapshot.getValue(ScheduleDTO.class);

                            if(mPrepare == null){ mPrepare = schedule.prepare;
                            }else { mPrepare += ", " + schedule.prepare; }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        emailtext = (TextView)findViewById(R.id.account);
        emailtext.setText(auth.getCurrentUser().getEmail());

        switchPush = (Switch)findViewById(R.id.switchpopup);
        weekrepeat = (TextView)findViewById(R.id.week_repeat);
        popuptime = (TextView)findViewById(R.id.popup_time);
        btnTimeset = (Button)findViewById(R.id.btnTimeset);
        nfcTag = (TextView)findViewById(R.id.nfcTag);
        logout = (TextView)findViewById(R.id.logout);
        getuserInformation();

        //푸시알람 설정
        switchPush.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked == true){ //Toast.makeText(SettingActivity.this, "팝업 On", Toast.LENGTH_SHORT).show();
                    isPush = true;
                } else { //Toast.makeText(SettingActivity.this, "팝업 Off", Toast.LENGTH_SHORT).show();
                    isPush = false;
                }
                writeuserInformation();
            }
        });

        weekrepeat.setOnClickListener(this);
        btnTimeset.setOnClickListener(this);
        nfcTag.setOnClickListener(this);
        logout.setOnClickListener(this);
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.logout:
                Toast.makeText(SettingActivity.this, "logout", Toast.LENGTH_SHORT).show();
                auth.signOut();
                finish();
                Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
                startActivity(intent);
                break;
            case R.id.btnTimeset:

                if(isPush) {
                    boolean isRepeat = false;
                    final String getweek = weekrepeat.getText().toString();
                    //final boolean[] bWeek = {false, false, false, false, false, false, false, false, false};
                    if (getweek.contains("일") || getweek.contains("월") || getweek.contains("화") || getweek.contains("수") ||
                            getweek.contains("목") || getweek.contains("금") || getweek.contains("토")) {
                        isRepeat = true;
                    }

                    final boolean finalIsRepeat = isRepeat;
                    TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                            mHour = hourOfDay;
                            mMinute = minute;

                            popuptime.setText(String.format("%02d : %02d", hourOfDay, minute));
                            writeuserInformation();

                            mAlarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

                            Calendar calendar = Calendar.getInstance();
                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            calendar.set(Calendar.MINUTE, minute);
                            calendar.set(Calendar.SECOND, 0);

                            Intent alarm_intent = new Intent(SettingActivity.this, BroadcastD.class);
                            alarm_intent.putExtra("week", getweek);
                            alarm_intent.putExtra("ispush", isPush);
                            alarm_intent.putExtra("hour", mHour);
                            alarm_intent.putExtra("minute", mMinute);
                            if(mPrepare == null){
                                alarm_intent.putExtra("prepare", "없음");
                            } else {alarm_intent.putExtra("prepare", mPrepare); }
                            PendingIntent sender = PendingIntent.getBroadcast(SettingActivity.this, 0, alarm_intent, PendingIntent.FLAG_UPDATE_CURRENT);

                            // 알람셋팅
                            if(finalIsRepeat){ //Toast.makeText(SettingActivity.this, "반복", Toast.LENGTH_LONG).show();
                                long intervalTime = 24 * 60 * 60 * 1000;// 24시간
                                mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intervalTime, sender);String dd;
                            } else { //Toast.makeText(SettingActivity.this, "반복없음", Toast.LENGTH_LONG).show();
                                mAlarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
                            }
                        }
                    }, mHour, mMinute, true);
                    timePickerDialog.show();
                } else {
                    Toast.makeText(this, "알림을 설정해주세요", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.week_repeat:
                final List selectedItems = new ArrayList();
                final String[] weekSelect = new String[]{"월", "화", "수", "목", "금", "토", "일"};
                final boolean checked[] = {false, false, false, false, false, false, false};

                String getweek = weekrepeat.getText().toString();
                if (getweek.contains("월")) { checked[0] = true; selectedItems.add("월"); }
                if (getweek.contains("화")) { checked[1] = true; selectedItems.add("화"); }
                if (getweek.contains("수")) { checked[2] = true; selectedItems.add("수"); }
                if (getweek.contains("목")) { checked[3] = true; selectedItems.add("목"); }
                if (getweek.contains("금")) { checked[4] = true; selectedItems.add("금"); }
                if (getweek.contains("토")) { checked[5] = true; selectedItems.add("토"); }
                if (getweek.contains("일")) { checked[6] = true; selectedItems.add("일"); }

                AlertDialog.Builder weekdialog = new AlertDialog.Builder(SettingActivity.this);
                weekdialog.setTitle("반복")
                        .setMultiChoiceItems(weekSelect, checked,
                                new DialogInterface.OnMultiChoiceClickListener(){
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                        if (isChecked){
                                            Toast.makeText(SettingActivity.this, weekSelect[which], Toast.LENGTH_SHORT).show();
                                            selectedItems.add(weekSelect[which]);
                                        } else {
                                            selectedItems.remove(weekSelect[which]);
                                        }
                                    }
                                })
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (selectedItems.size() == 0) {
                                    Toast.makeText(SettingActivity.this, "반복 없음", Toast.LENGTH_SHORT).show();
                                    weekrepeat.setText("반복 없음");
                                    writeuserInformation();
                                } else {
                                    String items = "";

                                    for (Object selectedItem : selectedItems){ items += selectedItem + ", "; }

                                    selectedItems.clear();
                                    items = items.substring(0, items.length() - 2);
                                    Toast.makeText(SettingActivity.this, items, Toast.LENGTH_SHORT).show();

                                    weekrepeat.setText(items);
                                    writeuserInformation();
                                }
                            }
                        }).create().show();

                break;
            case R.id.nfcTag:
                Intent nfcintent = new Intent(SettingActivity.this, NfcActivity.class);
                startActivity(nfcintent);
                break;
        }

    }

    //파이어베이스에서 설정값 가져오기
    private void getuserInformation(){
        msettingReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SettingDTO settingDTO = dataSnapshot.getValue(SettingDTO.class);

                mHour = settingDTO.userhour;
                mMinute = settingDTO.userminute;
                popuptime.setText(String.format("%02d : %02d", settingDTO.userhour, settingDTO.userminute));
                weekrepeat.setText(settingDTO.userweek);
                //writeuserInformation();
                if(settingDTO.userpush){ //isPush = true;
                    switchPush.setChecked(true);
                } else { //isPush = false;
                    switchPush.setChecked(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //파이어베이스에 설정 저장
    private void writeuserInformation(){
        String week = weekrepeat.getText().toString();
        SettingDTO setting = new SettingDTO(mHour, mMinute, isPush, week);
        msettingReference.setValue(setting);
    }
}
