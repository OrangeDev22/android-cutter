package com.example.cutter;

import android.graphics.drawable.Drawable;

public class IconPowerMenuItem {
    private Drawable icon;
    private String title;
    public IconPowerMenuItem(Drawable icon, String title){
        this.icon = icon;
        this.title = title;
    }
    public Drawable getIcon(){
        return icon;
    }
    public String getTitle(){
        return  title;
    }
}
