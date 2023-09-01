package eu.basicairdata.graziano.gpslogger.recording;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Objects;

import eu.basicairdata.graziano.gpslogger.management.PlaceMarkType;


public class ItemPlaceMarkData implements Comparator<ItemPlaceMarkData> {
    private String trackName;               // this placemark's Parent Track Name
    private String placeMarkTitle;          // this placemark Name by placemark type
    private String placeMarkType;           // this placemark type ( REST, ETC... )
    private String placeMarkDesc;           // this placemark desc ( selection information )


    private boolean isPlaceMarkEnable;      // it is not collectable placemark
    private boolean isPlaceMarkHidden;      // when it hide from Window

    private double placeMarkLat = 0.0f;      // this placeamrk lat
    private double placeMarkLng = 0.0f;      // this placemark lng

    private Bitmap placeMarkImg0 = null;    // this placemark Image
    private Bitmap placeMarkImg1 = null;    // this placemark Image
    private Bitmap placeMarkImg2 = null;    // this placemark Image

    public ItemPlaceMarkData(String trackName, String placeMarkTitle, String placeMarkType, String placeMarkDesc, boolean isPlaceMarkEnable) {
        this.trackName = trackName;
        this.placeMarkTitle = placeMarkTitle;
        this.placeMarkType = placeMarkType;
        this.placeMarkDesc = placeMarkDesc;
        this.isPlaceMarkEnable = isPlaceMarkEnable;
        this.isPlaceMarkHidden = false;
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


    public boolean getPlaceMarkEnable() {
        return isPlaceMarkEnable;
    }

    public void setPlaceMarkEnable(Boolean placeMarkEnable) {
        isPlaceMarkEnable = placeMarkEnable;
    }

    public boolean isPlaceMarkEnable() {
        return isPlaceMarkEnable;
    }

    public void setPlaceMarkEnable(boolean placeMarkEnable) {
        isPlaceMarkEnable = placeMarkEnable;
    }

    public boolean isPlaceMarkHidden() {
        return isPlaceMarkHidden;
    }

    public void setPlaceMarkHidden(boolean placeMarkHidden) {
        isPlaceMarkHidden = placeMarkHidden;
    }

    public double getPlaceMarkLat() {
        return placeMarkLat;
    }

    public void setPlaceMarkLat(double placeMarkLat) {
        this.placeMarkLat = placeMarkLat;
    }

    public double getPlaceMarkLng() {
        return placeMarkLng;
    }

    public void setPlaceMarkLng(double placeMarkLng) {
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

    /**
     * sort list by placemark type and title
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return compare result
     */
    @Override
    public int compare(ItemPlaceMarkData o1, ItemPlaceMarkData o2) {
        // add placemark infomation label must be place roof
        if(o1.trackName.equals("label") && o1.placeMarkDesc.equals("label") && o1.placeMarkTitle.equals("label") && o1.placeMarkType.equals(PlaceMarkType.ETC.name())) return 1; // o1 is high
        else if(o2.trackName.equals("label") && o2.placeMarkDesc.equals("label") && o2.placeMarkTitle.equals("label") && o2.placeMarkType.equals(PlaceMarkType.ETC.name())) return -1; // o2 is high

        final PlaceMarkType o1Type = PlaceMarkType.valueOf(o1.getPlaceMarkType());
        final PlaceMarkType o2Type = PlaceMarkType.valueOf(o2.getPlaceMarkType());
        final int isDiffType = o1Type.convertIntType() - o2Type.convertIntType();

        // is same placemark TYPE???
        if(isDiffType == 0) {
            // compare by Placemark visbilty NAME!
            return o1.placeMarkTitle.hashCode() - o2.placeMarkTitle.hashCode();
        }
        // diff type? return them!
        return isDiffType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPlaceMarkData that = (ItemPlaceMarkData) o;
        return trackName.equals(that.trackName) &&
                placeMarkTitle.equals(that.placeMarkTitle) &&
                placeMarkType.equals(that.placeMarkType) &&
                placeMarkDesc.equals(that.placeMarkDesc);
    }
    @Override
    public int hashCode() {
        return Objects.hash(trackName, placeMarkTitle, placeMarkType, placeMarkDesc);
    }
}
