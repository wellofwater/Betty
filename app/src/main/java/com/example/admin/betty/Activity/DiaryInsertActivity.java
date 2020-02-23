package com.example.admin.betty.Activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.betty.LoginActivity;
import com.example.admin.betty.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class DiaryInsertActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private DatabaseReference mscheduleDatabase;

    private Toolbar mToolbar;
    private EditText editTitle, editContext, editPrepare;
    private TextView textDate;
    private Button btnDate, btnSave;

    int mYear, mMonth, mDay;
    String Uid, title, date, context, prepare, beforetitle, beforedate, beforecontext, beforeprepare;
    long createdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_insert);

        mAuth = FirebaseAuth.getInstance();
        mscheduleDatabase = FirebaseDatabase.getInstance().getReference();

        Uid = mAuth.getCurrentUser().getUid();

        mToolbar = (Toolbar)findViewById(R.id.insert_toolbar);
        setSupportActionBar(mToolbar);

        //돌아가기 버튼
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_back);
        getSupportActionBar().setTitle("일정 추가");

        editTitle = (EditText)findViewById(R.id.editTitle);
        editContext = (EditText)findViewById(R.id.editContext);
        editPrepare = (EditText)findViewById(R.id.editPrepare);

        //현재 날짜 출력
        Calendar cal = new GregorianCalendar();
        mYear = cal.get(Calendar.YEAR);
        mMonth = cal.get(Calendar.MONTH);
        mDay = cal.get(Calendar.DAY_OF_MONTH);
        textDate = (TextView)findViewById(R.id.textDate);
        textDate.setText(String.format("%d/%d/%d", mYear, mMonth + 1, mDay));

        //수정일 경우 데이터 출력
        UpdateSchedule();

        //날짜 입력
        btnDate = (Button)findViewById(R.id.btnDate);
        btnDate.setOnClickListener(this);

        //저장
        btnSave = (Button)findViewById(R.id.btnSave);
            btnSave.setOnClickListener(this);
    }

    //추가된 소스, ToolBar에 menu.xml을 인플레이트함
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu, menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.insert_menu, menu);
        return true;
    }

    //추가된 소스, ToolBar에 추가된 항목의 select 이벤트를 처리하는 함수
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //return super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                // User chose the "Settings" item, show the app settings UI...
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.btnDate:
                //DatePiker를 통해 날짜 받아오기
                DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                textDate.setText(String.format("%d/%d/%d", year, monthOfYear + 1, dayOfMonth));
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
                break;
            case R.id.btnSave:
                title = editTitle.getText().toString();
                date = textDate.getText().toString();
                context = editContext.getText().toString();
                prepare = editPrepare.getText().toString();
                createdate = System.currentTimeMillis();
                //String userId = mAuth.getCurrentUser().getUid();

                if (title.isEmpty()){
                    Toast.makeText(this, "제목을 입력하십시오", Toast.LENGTH_SHORT).show();
                } else if (context.isEmpty()){
                    context = title;

                    //일정 저장
                    writeNewSchedule(Uid, title, date, context, prepare, createdate);

                    if (beforedate != null) {
                        beforedate = beforedate.replaceAll("/", "");
                        mscheduleDatabase.child("schedules").child(Uid).child(beforedate).child(beforetitle).removeValue();
                    }
                }else {
                    //일정 저장
                    writeNewSchedule(Uid, title, date, context, prepare, createdate);

                    if (beforedate != null) {
                        beforedate = beforedate.replaceAll("/", "");
                        mscheduleDatabase.child("schedules").child(Uid).child(beforedate).child(beforetitle).removeValue();
                    }
                }

                break;
        }
    }

    private void writeNewSchedule(String id, String title, String date, String context, String prepare, long createdate) {
        ScheduleDTO schedule = new ScheduleDTO(title, date, context, prepare, createdate);

        String scheduledate = date.replaceAll("/","");

        mscheduleDatabase.child("schedules").child(id).child(scheduledate).child(title).setValue(schedule);

        Intent intent = new Intent(DiaryInsertActivity.this, DiaryReadActivity.class);
        intent.putExtra("date", scheduledate);
        intent.putExtra("title", title);
        startActivity(intent);
        finish();
    }

    private void UpdateSchedule(){
        Intent intent = getIntent();
        beforedate = intent.getStringExtra("date");
        beforetitle = intent.getStringExtra("title");
        beforeprepare = intent.getStringExtra("prepare");
        beforecontext = intent.getStringExtra("content");

        if(beforedate != null){
            editTitle.setText(beforetitle);
            editPrepare.setText(beforeprepare);
            editContext.setText(beforecontext);
            textDate.setText(beforedate);
        }
    }

}
