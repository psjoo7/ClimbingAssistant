package com.example.googlemaptest;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class MountElement implements Comparable<MountElement>{
    public String end;
    public Long length;
    public Integer maxHeight;
    public String mname;
    public String path;
    public String starting;

    public MountElement(){}

    public MountElement(String end, Long length, Integer maxHeight, String mname, String path, String starting){
        this.end = end;
        this.length = length;
        this.maxHeight = maxHeight;
        this.mname = mname;
        this.path = path;
        this.starting = starting;
    }

    @Override
    public int compareTo(MountElement mountElement) {
        return this.maxHeight.compareTo(mountElement.maxHeight);
    }
}
