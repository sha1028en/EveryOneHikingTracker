package eu.basicairdata.graziano.gpslogger.recording.enhanced;

import androidx.annotation.NonNull;

import java.util.Objects;

public class ItemPlaceMarkImgData {
    private int TrackId;
    private final String placeMarkType;

    private int imgId; // IMG PRIMARY KEY
    private String imageUrl; // IMG URL
    private double imgLat = 0.0f;
    private double imgLng = 0.0f;

    public ItemPlaceMarkImgData(final int TrackId, final int imageId, @NonNull final  String placeMarkType, @NonNull final String imageUrl) {
        this.TrackId = TrackId;
        this.imgId = imageId;
        this.placeMarkType = placeMarkType;
        this.imageUrl = imageUrl;
    }

    public void setTrackId(int trackId) {
        this.TrackId = trackId;
    }

    public int getTrackId() {
        return TrackId;
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

    @Override
    public String toString() {
        return  "{" +
                "imgId=" + imgId +
                ", imageUrl='" + imageUrl + '\'' +
                ", imgLat=" + imgLat +
                ", imgLng=" + imgLng +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPlaceMarkImgData imgData = (ItemPlaceMarkImgData) o;
        return imgId == imgData.imgId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(imgId);
    }
}
