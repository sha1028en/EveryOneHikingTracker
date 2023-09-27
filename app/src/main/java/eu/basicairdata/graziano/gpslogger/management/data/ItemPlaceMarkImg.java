package eu.basicairdata.graziano.gpslogger.management.data;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * a Single Image Data class
 *
 * @see ItemPlaceMark parent Data Class
 */
public class ItemPlaceMarkImg implements Serializable {
    private int TrackId; // this img's track Id ( parent )
    private final String placeMarkType; // this img's Type ( ETC, PARKING... )

    private int imgId; // IMG PRIMARY KEY
    private String imageUrl; // IMG URL
    private double imgLat = 0.0f; // img Lat
    private double imgLng = 0.0f; // img Lng

    public ItemPlaceMarkImg(final int TrackId, final int imageId, @NonNull final  String placeMarkType, @NonNull final String imageUrl) {
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

    @NonNull @Override
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
        ItemPlaceMarkImg imgData = (ItemPlaceMarkImg) o;
        return imgId == imgData.imgId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(imgId);
    }
}
