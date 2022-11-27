package com.example.googlemaptest;

public class Record {
    private String time, record, mname, UserID;

    public Record(String time, String record, String mname, String UserID){
        this.time = time;
        this.mname = mname;
        this.record = record;
        this.UserID = UserID;
    }

    public void setMname(String mname) {
        this.mname = mname;
    }

    public String getMname() {
        return mname;
    }

    public void setRecord(String record) {
        this.record = record;
    }

    public String getRecord() {
        return record;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getUserID() {
        return UserID;
    }
}
