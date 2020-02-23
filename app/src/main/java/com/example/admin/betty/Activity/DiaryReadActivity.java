package com.example.admin.betty.Activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.betty.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DiaryReadActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private FirebaseDatabase mscheduleDatabase;
    private DatabaseReference mscheduleReference;
    String Uid, date, title;

    private TextView textTitle, textDate, textContent, textPrepare;
    private Button btnUpdate, btnDelete;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_read);


        toolbar = (Toolbar)findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        date = intent.getStringExtra("date");
        title = intent.getStringExtra("title");

        mAuth = FirebaseAuth.getInstance();
        Uid = mAuth.getCurrentUser().getUid();
        mscheduleDatabase = FirebaseDatabase.getInstance();
        mscheduleReference = mscheduleDatabase.getReference().child("schedules").child(Uid).child(date).child(title);

        textTitle = (TextView)findViewById(R.id.textTitle);
        textDate = (TextView)findViewById(R.id.textDate);
        textContent = (TextView)findViewById(R.id.textContent);
        textPrepare = (TextView)findViewById(R.id.textPrepare);

        //DB에서 한 번 내용을 가져온다
        mscheduleReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ScheduleDTO schedule = dataSnapshot.getValue(ScheduleDTO.class);

                textTitle.setText(schedule.title);
                textDate.setText(schedule.date);
                textContent.setText(schedule.context);
                textPrepare.setText(schedule.prepare);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnUpdate = (Button)findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(this);
        btnDelete = (Button)findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(this);

    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.btnUpdate:
                Intent intent = new Intent(DiaryReadActivity.this, DiaryInsertActivity.class);
                intent.putExtra("date", textDate.getText().toString());
                intent.putExtra("title", textTitle.getText().toString());
                intent.putExtra("content", textContent.getText().toString());
                intent.putExtra("prepare", textPrepare.getText().toString());
                startActivity(intent);
                finish();
                break;
            case R.id.btnDelete:
                mscheduleReference.removeValue();
                finish();
                break;
        }
    }
}
