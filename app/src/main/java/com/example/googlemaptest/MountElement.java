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
    public Double realdist;

    public MountElement(){}

    public MountElement(String end, Long length, Integer maxHeight, String mname, String path, String starting, int distance){
        this.end = end;
        this.length = length;
        this.maxHeight = maxHeight;
        this.mname = mname;
        this.path = path;
        this.starting = starting;
        this.distance = distance;
        this.realdist = 0.0;
    }

    public void setDistance(double lat, double lng) {
        double lat1 = Math.cos(lat);
        double X = Math.cos(lat) * 6400 * 2 * 3.14 / 360 * Math.abs(lng - Double.parseDouble(this.end.split(" ")[0]));
        double Y = Y = 111 * Math.abs(lat - Double.parseDouble(this.end.split(" ")[0]));

        this.distance = Math.sqrt(X*X + Y*Y);
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
