package com.example.admin.betty.Activity;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.admin.betty.Adapter.WeeklistAdapter;
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

public class ListActivity extends Fragment {
    private FirebaseDatabase mscheduleDatabase;
    private DatabaseReference mscheduleReference;
    private FirebaseAuth mAuth;
    String Uid;

    private ListView weekList;
    private WeeklistAdapter adapter;
    private Drawable weekImage;
    List<Object> tArray = new ArrayList<Object>();

    Calendar calendar;

    public ListActivity() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_list, container, false);

        //TODO 여기에 View를 찾고 이벤트를 등록하고 등등의 처리를 할 수 있다.
        //getView().findViewById(R.id.btn1).setOnClickListener();
        mAuth = FirebaseAuth.getInstance();
        Uid = mAuth.getCurrentUser().getUid();
        mscheduleDatabase = FirebaseDatabase.getInstance();
        mscheduleReference = mscheduleDatabase.getReference().child("schedules").child(Uid);

        calendar = Calendar.getInstance();

        weekList = (ListView)view.findViewById(R.id.weeklist);
        adapter = new WeeklistAdapter();
        weekList.setAdapter(adapter);

        weekList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                weekList.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();


        for (int i = 1; i < 8; i++) {
            calendar.set(Calendar.DAY_OF_WEEK, i);

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            final int day = calendar.get(Calendar.DAY_OF_MONTH);
            String date = year + "" + month + "" + day;

            Calendar today = Calendar.getInstance();
            final int dayofweek = today.get(Calendar.DAY_OF_WEEK);
            final int num = i;
            mscheduleReference.child(date).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        if(num > dayofweek) {
                            weekImage = getResources().getDrawable(R.drawable.ic_check_list);
                        } else if(num == dayofweek) {
                            weekImage = getResources().getDrawable(R.drawable.ic_check_date);
                        } else {
                            weekImage = getResources().getDrawable(R.drawable.ic_check_accept);
                        }

                        final String title = snapshot.getKey();
                        tArray.add(title);
                        adapter.addItem(weekImage, title);
                    }
                    adapter.notifyDataSetChanged();
                    weekList.setSelection(adapter.getCount() - 1);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }
}