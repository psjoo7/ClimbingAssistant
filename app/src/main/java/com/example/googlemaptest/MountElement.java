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
    public int distance;

    public MountElement(){}

    public MountElement(String end, Long length, Integer maxHeight, String mname, String path, String starting, int distance){
        this.end = end;
        this.length = length;
        this.maxHeight = maxHeight;
        this.mname = mname;
        this.path = path;
        this.starting = starting;
        this.distance = distance;
    }
    @Override
    public String toString(){
        return mname;
    }

    @Override
    public int compareTo(MountElement mountElement) {
        return mountElement.distance - this.distance;
    }
}
