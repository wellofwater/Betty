package com.example.admin.betty.Activity;

import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.admin.betty.Adapter.PagerAdatper;
import com.example.admin.betty.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Locale;
import static android.speech.tts.TextToSpeech.ERROR;

public class MainActivity extends AppCompatActivity {
    private FirebaseDatabase mscheduleDatabase;
    private DatabaseReference mscheduleReference;
    private FirebaseAuth mAuth;
    private String Uid;
    String strSchedule = "";

    private TabLayout mTabLayout;
    private ViewPager mPager;
    private Toolbar toolbar;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, LoadingActivity.class);
        startActivity(intent);

        Calendar cal = Calendar.getInstance();
        int year = cal.get ( cal.YEAR );
        int month = cal.get ( cal.MONTH ) + 1 ;
        int date = cal.get ( cal.DATE ) ;
        String strcal = year + "" + month + "" + date;

        mAuth = FirebaseAuth.getInstance();
        Uid = mAuth.getCurrentUser().getUid();
        mscheduleDatabase = FirebaseDatabase.getInstance();
        mscheduleReference = mscheduleDatabase.getReference().child("schedules").child(Uid).child(strcal);

        mscheduleReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String title = snapshot.getKey();

                    mscheduleReference.child(title).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            ScheduleDTO schedule = dataSnapshot.getValue(ScheduleDTO.class);

                            if(schedule.prepare.isEmpty()){
                                //일정이 없음
                            } else {
                                strSchedule += schedule.prepare;
                            }
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

        //// TTS를 생성하고 OnInitListener로 초기화 한다.
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTabLayout = findViewById(R.id.tabLayout);
        mPager = findViewById(R.id.pager);

        mTabLayout.addTab(mTabLayout.newTab().setText("Home"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Calnedar"));
        mTabLayout.addTab(mTabLayout.newTab().setText("List"));

        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);//탭의 가로 전체 사이즈 지정
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);

        //탭 아이콘 추가
        mTabLayout.getTabAt(0).setIcon(R.drawable.ic_home_24dp);
        mTabLayout.getTabAt(1).setIcon(R.drawable.ic_today_24dp);
        mTabLayout.getTabAt(2).setIcon(R.drawable.ic_format_list_bulleted_24dp);

        //프래그먼트를 관리할 PagerAdapter를 생성한다.
        PagerAdatper pagerAdatper = new PagerAdatper(getSupportFragmentManager(),mTabLayout.getTabCount());
        mPager.setAdapter(pagerAdatper);

        //Tab 레이아웃과 ViewPager를 이벤트로 서로 연결시켜준다.
        //ViewPager가 움직였을 때, 탭이 바뀌게끔 한다.
        mPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        //탭레이아웃이 바뀌면 ViewPager의 Fragment도 바뀌는 작업을 연결한다.
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //현재 사용자가 바꾼 탭의 이벤트가 넘어온다.
                mPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    //추가된 소스, ToolBar에 menu.xml을 인플레이트함
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu, menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    //추가된 소스, ToolBar에 추가된 항목의 select 이벤트를 처리하는 함수
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //return super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                // Toast.makeText(getApplicationContext(), "내 정보 및 설정 클릭", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_play:
                // User chose the "Play" item, show the app settings UI...
                //내용
                // Toast.makeText(getApplicationContext(), "오늘 준비 물품은 " + strSchedule + "입니다", Toast.LENGTH_LONG).show();
                tts.speak("오늘 준비 물품은 " + strSchedule + " 입니다", TextToSpeech.QUEUE_FLUSH, null);
                return true;
        }
                return super.onOptionsItemSelected(item);
    }
}
