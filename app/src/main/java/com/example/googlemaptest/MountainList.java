package com.example.googlemaptest;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class MountainList {
    public String end;
    public Long length;
    public Long maxHeight;
    public String mname;
    public String path;
    public String starting;

    public MountainList(){}

    public MountainList(String end, Long length, Long maxHeight, String mname, String path, String starting){
        this.end = end;
        this.length = length;
        this.maxHeight = maxHeight;
        this.mname = mname;
        this.path = path;
        this.starting = starting;
    }
}
