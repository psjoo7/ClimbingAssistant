package com.example.googlemaptest;

public class MountainInfo {
    private String frtrlNm;
    private String lat;
    private String lot;
    private String aslAltide;
    private String placeNm;

    public MountainInfo(){}

    public String getAslAltide() {
        return aslAltide;
    }

    public String getFrtrlNm() {
        return frtrlNm;
    }

    public String getLat() {
        return lat;
    }

    public String getLot() {
        return lot;
    }

    public String getPlaceNm() {
        return placeNm;
    }

    public void setAslAltide(String aslAltide) {
        this.aslAltide = aslAltide;
    }

    public void setFrtrlNm(String frtrlNm) {
        this.frtrlNm = frtrlNm;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public void setLot(String lot) {
        this.lot = lot;
    }

    public void setPlaceNm(String placeNm) {
        this.placeNm = placeNm;
    }
}

