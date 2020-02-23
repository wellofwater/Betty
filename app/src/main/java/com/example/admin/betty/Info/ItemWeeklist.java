package com.example.admin.betty.Info;

import android.graphics.drawable.Drawable;

public class ItemWeeklist {
    private Drawable iconDrawable ;
    private String strListTitle;

    public void setIcon(Drawable icon) {
        iconDrawable = icon ;
    }
    public void setTitle(String title) {
        strListTitle = title ;
    }

    public Drawable getIcon() {
        return this.iconDrawable ;
    }
    public String getTitle() { return this.strListTitle ; }
}
