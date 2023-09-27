package eu.basicairdata.graziano.gpslogger.management.data;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Objects;

import eu.basicairdata.graziano.gpslogger.management.define.PlaceMarkType;

/**
 * a Type of PlaceMark DataClass which show List
 */
public class ItemPlaceMark implements Comparator<ItemPlaceMark>, Comparable<ItemPlaceMark>, Serializable {
//    private int placeMarkId = -1;           // this placemark's UNIQUE id
    private String trackName;               // this placemark's Parent Track Name
    private String placeMarkTitle;          // this placemark Name by placemark type
    private String placeMarkType;           // this placemark type ( REST, ETC... )
    private String placeMarkDesc;           // this placemark desc ( selection information )

    private LinkedList<ItemPlaceMarkImg> placeMarkImgItemList; // kind of placemark types Image List

    private double lat = 0.0f; // not use
    private double lng = 0.0f; // not use

    private boolean isPlaceMarkEnable; // it is not collectable placemark
    private boolean isPlaceMarkStateChange; // when this placemark state has changed???


    public ItemPlaceMark(String trackName, String placeMarkTitle, String placeMarkType, String placeMarkDesc, boolean isPlaceMarkEnable) {
        this.trackName = trackName;
        this.placeMarkTitle = placeMarkTitle;
        this.placeMarkType = placeMarkType;
        this.placeMarkDesc = placeMarkDesc;
        this.isPlaceMarkEnable = isPlaceMarkEnable;
        this.isPlaceMarkStateChange = false;

        this.placeMarkImgItemList = new LinkedList<>();
    }

//    public int getPlaceMarkId() {
//        return placeMarkId;
//    }
//
//    public void setPlaceMarkId(int placeMarkId) {
//        this.placeMarkId = placeMarkId;
//    }

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


    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public boolean isPlaceMarkEnable() {
        return isPlaceMarkEnable;
    }

    public void setPlaceMarkEnable(boolean placeMarkEnable) {
        isPlaceMarkEnable = placeMarkEnable;
    }

    public boolean isPlaceMarkStateChange() {
        return isPlaceMarkStateChange;
    }

    public void setPlaceMarkStateChange() {
        isPlaceMarkStateChange = true;
    }

    public LinkedList<ItemPlaceMarkImg> getPlaceMarkImgItemList() {
        return placeMarkImgItemList;
    }

    public void addPlaceMarkImgItemList(ItemPlaceMarkImg placeMarkImgItem) {
        this.placeMarkImgItemList.add(placeMarkImgItem);
    }
    public void setPlaceMarkImgItemList(LinkedList<ItemPlaceMarkImg> placeMarkImgItemList) {
        this.placeMarkImgItemList = placeMarkImgItemList;
    }

    public void addPlaceMarkImgItem(@NonNull final ItemPlaceMarkImg item) {
        this.placeMarkImgItemList.add(item);
    }

    public void removePlaceMarkImgItem(@NonNull final ItemPlaceMarkImg item) {
        this.placeMarkImgItemList.remove(item);
    }

    public void setPlaceMarkImgItem(@NonNull final ItemPlaceMarkImg item) {
        if(this.getPlaceMarkImgItemList().isEmpty()) return;

        int i = 0;
        for(ItemPlaceMarkImg buffer : this.getPlaceMarkImgItemList()) {
            if(buffer.equals(item)) {
                this.getPlaceMarkImgItemList().set(i, item);
            }
            ++i;
        }
    }

    /**
     * sort list by placemark type and title
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return compare result
     */
    @Override
    public int compare(ItemPlaceMark o1, ItemPlaceMark o2) {
        // add placemark infomation label must be placed roof
        if(o1.trackName.equals("label") && o1.placeMarkDesc.equals("label") && o1.placeMarkTitle.equals("label") && o1.placeMarkType.equals(PlaceMarkType.ETC.name())) return 1; // o1 is high
        else if(o2.trackName.equals("label") && o2.placeMarkDesc.equals("label") && o2.placeMarkTitle.equals("label") && o2.placeMarkType.equals(PlaceMarkType.ETC.name())) return -1; // o2 is high

        final PlaceMarkType o1Type = PlaceMarkType.valueOf(o1.getPlaceMarkType());
        final PlaceMarkType o2Type = PlaceMarkType.valueOf(o2.getPlaceMarkType());
        final int isDiffType = o1Type.convertIntType() - o2Type.convertIntType();

        return isDiffType;
    }

    @Override
    public int compareTo(ItemPlaceMark o) {
        // add placemark infomation label must be placed roof
        if(o.trackName.equals("label") && o.placeMarkDesc.equals("label") && o.placeMarkTitle.equals("label") && o.placeMarkType.equals(PlaceMarkType.ETC.name())) return 1; // o is high
        else if(this.trackName.equals("label") && this.placeMarkDesc.equals("label") && this.placeMarkTitle.equals("label") && this.placeMarkType.equals(PlaceMarkType.ETC.name())) return -1; // o2 is high

        final PlaceMarkType o1Type = PlaceMarkType.valueOf(o.getPlaceMarkType());
        final PlaceMarkType o2Type = PlaceMarkType.valueOf(this.getPlaceMarkType());
        final int isDiffType = o1Type.convertIntType() - o2Type.convertIntType();

        return isDiffType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPlaceMark that = (ItemPlaceMark) o;
        return trackName.equals(that.trackName) &&
                placeMarkTitle.equals(that.placeMarkTitle) &&
                placeMarkType.equals(that.placeMarkType);
    }
    @Override
    public int hashCode() {
        return Objects.hash(trackName, placeMarkTitle, placeMarkType);
    }

    @NonNull @Override
    public String toString() {
        return "ItemPlaceMarkData{" +
                "trackName='" + trackName + '\'' +
                ", placeMarkTitle='" + placeMarkTitle + '\'' +
                ", placeMarkType='" + placeMarkType + '\'' +
                ", isPlaceMarkEnable=" + isPlaceMarkEnable +
                ", placeMarkLat=" + lat +
                ", placeMarkLng=" + lng +
                ", placeMarkImgList=" + placeMarkImgItemList.toString() +
                '}';
    }
}
