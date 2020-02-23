package com.example.admin.betty.Adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.admin.betty.Info.ItemWeeklist;
import com.example.admin.betty.R;

import java.util.ArrayList;

public class WeeklistAdapter extends BaseAdapter {
    private ArrayList<ItemWeeklist> weeklistItem = new ArrayList<ItemWeeklist>();

    //생성자
    public WeeklistAdapter() {

    }

    @Override
    public int getCount() {
        return weeklistItem.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_item, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        ImageView iconImageView = (ImageView) convertView.findViewById(R.id.weekImage) ;
        TextView titleTextView = (TextView) convertView.findViewById(R.id.weeklistTitle) ;

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        ItemWeeklist listViewItem = weeklistItem.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        iconImageView.setImageDrawable(listViewItem.getIcon());
        titleTextView.setText(listViewItem.getTitle());

        return convertView;
    }

    @Override
    public Object getItem(int position)
    {
        return weeklistItem.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    public void addItem(Drawable icon, String title) {
        ItemWeeklist item = new ItemWeeklist();

        item.setIcon(icon);
        item.setTitle(title);

        weeklistItem.add(item);
    }
}
