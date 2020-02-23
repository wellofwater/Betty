package com.example.admin.betty.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.admin.betty.Activity.CalendarActivity;
import com.example.admin.betty.Activity.HomeActivity;
import com.example.admin.betty.Activity.ListActivity;

/**
 * 뷰 페이져 어댑터 이다.
 * @since 2018.09.27
 * @author 조수연
 */

public class PagerAdatper extends FragmentStatePagerAdapter {
    //탭의 갯수를 저장하고 있는 멤버변수
    private int mNumOfTabs;
    public PagerAdatper(FragmentManager fm, int numOfTabs) {
        super(fm);
        mNumOfTabs = numOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        //BaseAdapter에서 getView()메서드에 해당하는 메서드로써,
        //position 값이 곧! 현재 선택된 Tab의 Index번호를 나타낸다.

        switch (position){
            case 0:
                HomeActivity home = new HomeActivity();
                return home;
            case 1:
                CalendarActivity calendar = new CalendarActivity();
                return calendar;
            case 2:
                ListActivity list = new ListActivity();
                return list;

        }
        return null;
    }

    @Override
    public int getCount() {
        //전체 탭의 갯수를 넘긴다.
        return mNumOfTabs;
    }
}
