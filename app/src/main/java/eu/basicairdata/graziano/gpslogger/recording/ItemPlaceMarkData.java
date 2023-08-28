package eu.basicairdata.graziano.gpslogger.recording;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;


public class ItemPlaceMarkData {
    private String trackName;
    private String placeMarkTitle;
    private String placeMarkType;

    private String placeMarkDesc;
    private Boolean isPlaceMarkEnable;

    private float placeMarkLat = 0.0f;
    private float placeMarkLng = 0.0f;

    private Bitmap placeMarkImg0 = null;
    private Bitmap placeMarkImg1 = null;
    private Bitmap placeMarkImg2 = null;

    public ItemPlaceMarkData(String trackName, String placeMarkTitle, String placeMarkType, String placeMarkDesc, boolean isPlaceMarkEnable) {
        this.trackName = trackName;
        this.placeMarkTitle = placeMarkTitle;
        this.placeMarkType = placeMarkType;
        this.placeMarkDesc = placeMarkDesc;
        this.isPlaceMarkEnable = isPlaceMarkEnable;
    }

    public String getTrackName() {
        return this.trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getPlaceMarkTitle() {
        return placeMarkTitle;
    }

    public void setPlaceMarkTitle(String placeMarkTitle) {
        this.placeMarkTitle = placeMarkTitle;
    }

    public String getPlaceMarkType() {
        return placeMarkType;
    }

    public void setPlaceMarkType(String placeMarkType) {
        this.placeMarkType = placeMarkType;
    }

    public String getPlaceMarkDesc() {
        return this.placeMarkDesc;
    }

    public void setPlaceMarkDesc(String placeMarkDesc) {
        this.placeMarkDesc = placeMarkDesc;
    }

    public Boolean getPlaceMarkEnable() {
        return isPlaceMarkEnable;
    }

    public void setPlaceMarkEnable(Boolean placeMarkEnable) {
        isPlaceMarkEnable = placeMarkEnable;
    }

    public float getPlaceMarkLat() {
        return placeMarkLat;
    }

    public void setPlaceMarkLat(float placeMarkLat) {
        this.placeMarkLat = placeMarkLat;
    }

    public float getPlaceMarkLng() {
        return placeMarkLng;
    }

    public void setPlaceMarkLng(float placeMarkLng) {
        this.placeMarkLng = placeMarkLng;
    }

    public void setPlaceMarkImg(@NonNull Bitmap img, final int index) {
        switch (index) {
            case 0 -> this.setPlaceMarkImg0(img);
            case 1 -> this.setPlaceMarkImg1(img);
            case 2 -> this.setPlaceMarkImg2(img);
        }
    }

    public Bitmap getPlaceMarkImg0() {
        return placeMarkImg0;
    }

    public void setPlaceMarkImg0(Bitmap placeMarkImg0) {
        this.placeMarkImg0 = placeMarkImg0;
    }

    public Bitmap getPlaceMarkImg1() {
        return placeMarkImg1;
    }

    public void setPlaceMarkImg1(Bitmap placeMarkImg1) {
        this.placeMarkImg1 = placeMarkImg1;
    }

    public Bitmap getPlaceMarkImg2() {
        return placeMarkImg2;
    }

    public void setPlaceMarkImg2(Bitmap placeMarkImg2) {
        this.placeMarkImg2 = placeMarkImg2;
    }

    public void releasePlaceMarkImages() {
        if(this.placeMarkImg0 != null) {
            this.placeMarkImg0.recycle();
            this.placeMarkImg0 = null;
        }

        if(this.placeMarkImg1 != null) {
            this.placeMarkImg1.recycle();
            this.placeMarkImg1 = null;
        }

        if(this.placeMarkImg2 != null) {
            this.placeMarkImg2.recycle();
            this.placeMarkImg2 = null;
        }
    }
}
