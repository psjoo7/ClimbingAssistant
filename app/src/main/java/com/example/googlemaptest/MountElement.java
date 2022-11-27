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
    public double distance;

    public MountElement(){}

    public MountElement(String end, Long length, Integer maxHeight, String mname, String path, String starting){
        this.end = end;
        this.length = length;
        this.maxHeight = maxHeight;
        this.mname = mname;
        this.path = path;
        this.starting = starting;
    }


    public void setDistance(double lat, double lng) {
        this.distance = Math.sqrt(Math.pow((lat - Double.parseDouble(this.end.split(" ")[1])),2) + Math.pow(lng - Double.parseDouble(this.end.split(" ")[0]),2));
    }

    @Override
    public String toString(){
        return mname;
    }

    @Override
    public int compareTo(MountElement mountElement) {
        if(mountElement.distance - this.distance > 0)
        {
            return -1;
        }
        else if(mountElement.distance - this.distance == 0)
        {
            return 0;
        }
        else
        {
            return 1;
        }

    }
}
