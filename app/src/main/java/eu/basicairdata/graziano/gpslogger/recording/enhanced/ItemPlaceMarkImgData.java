package eu.basicairdata.graziano.gpslogger.recording.enhanced;

import androidx.annotation.NonNull;

public class ItemPlaceMarkImgData {
    private final int placeMarkId;
    private final String placeMarkType;

    private int imgId = -1;
    private String imageUrl = "";
    private double imgLat = 0.0f;
    private double imgLng = 0.0f;

    public ItemPlaceMarkImgData(final int placeMarkId, @NonNull final  String placeMarkType, @NonNull final String imageUrl) {
        this.placeMarkId = placeMarkId;
        this.placeMarkType = placeMarkType;
        this.imageUrl = imageUrl;
    }

    public int getPlaceMarkId() {
        return placeMarkId;
    }

    public String getPlaceMarkType() {
        return placeMarkType;
    }

    public int getImgId() {
        return imgId;
    }

    public void setImgId(int imgId) {
        this.imgId = imgId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getImgLat() {
        return imgLat;
    }

    public void setImgLat(double imgLat) {
        this.imgLat = imgLat;
    }

    public double getImgLng() {
        return imgLng;
    }

    public void setImgLng(double imgLng) {
        this.imgLng = imgLng;
    }
}
