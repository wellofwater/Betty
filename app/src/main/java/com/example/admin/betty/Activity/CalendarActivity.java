package com.example.admin.betty.Activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.admin.betty.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarActivity extends Fragment {
    private FirebaseDatabase mscheduleDatabase;
    private DatabaseReference mscheduleReference;
    private ChildEventListener mChild;
    private FirebaseAuth mAuth;

    private FloatingActionButton scheduleadd;
    private CalendarView calendarView;
    private ListView calendar_list;
    private ArrayAdapter<String> adapter;
    List<Object> Array = new ArrayList<Object>();

    String Uid, scheduledate;
    boolean isSchdule = true;

    public CalendarActivity() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_calendar, container, false);

        Calendar cal = Calendar.getInstance();
        int year = cal.get ( cal.YEAR );
        int month = cal.get ( cal.MONTH ) + 1 ;
        int date = cal.get ( cal.DATE ) ;
        scheduledate = String.format("%d%d%d", year, month, date);

        mAuth = FirebaseAuth.getInstance();
        Uid = mAuth.getCurrentUser().getUid();
        mscheduleDatabase = FirebaseDatabase.getInstance();
        mscheduleReference = mscheduleDatabase.getReference().child("schedules").child(Uid).child(scheduledate);

        scheduleadd = (FloatingActionButton) view.findViewById(R.id.scheduleadd);
        calendarView = (CalendarView)view.findViewById(R.id.calendarView);

        calendar_list = (ListView)view.findViewById(R.id.schedule_list);
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
        calendar_list.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        final ValueEventListener scheduleListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                adapter.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    //ScheduleDTO schedule = dataSnapshot.getValue(ScheduleDTO.class);
                    String title = snapshot.getKey();
                    if(title == null){ //일정이 없음

                    } else {
                        Array.add(title);
                        adapter.add(title);
                    }
                }
                adapter.notifyDataSetChanged();
                calendar_list.setSelection(adapter.getCount() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        //오늘 날짜 일정 리스트 출력
        if (scheduledate != null) {
            mscheduleReference.addValueEventListener(scheduleListener);
        }


        scheduleadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DiaryInsertActivity.class);
                startActivity(intent);
            }
        });


        //날짜 클릭시 리스트 출력
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                scheduledate = String.format("%d%d%d", year, month+1, dayOfMonth);
                //Toast.makeText(getActivity(), scheduledate, Toast.LENGTH_SHORT).show();
                mscheduleReference = mscheduleDatabase.getReference().child("schedules").child(Uid).child(scheduledate);
                mscheduleReference.addValueEventListener(scheduleListener);
            }
        });
        calendar_list.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                calendar_list.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        //리스트 클릭시 이벤트 발생
        calendar_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String scheduletitle = adapter.getItem(position).toString();
                //Toast.makeText(getActivity(), scheduledate + " / " + scheduletitle, 0).show();

                Intent intent = new Intent(getActivity(), DiaryReadActivity.class);
                intent.putExtra("date", scheduledate);
                intent.putExtra("title", scheduletitle);
                startActivity(intent);

            }
        });
    }

    public void onRestart(){

        final ValueEventListener scheduleListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                adapter.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    //ScheduleDTO schedule = dataSnapshot.getValue(ScheduleDTO.class);
                    String title = snapshot.getKey();
                    if(title == null){ //일정이 없음

                    } else {
                        Array.add(title);
                        adapter.add(title);
                    }
                }
                adapter.notifyDataSetChanged();
                calendar_list.setSelection(adapter.getCount() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mscheduleReference.addValueEventListener(scheduleListener);
    }

    private void initDatabase() {
        mChild = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mscheduleReference.addChildEventListener(mChild);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        //mscheduleReference.removeEventListener(mChild);
    }

}