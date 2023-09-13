package eu.basicairdata.graziano.gpslogger.recording.enhanced;

import androidx.annotation.NonNull;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Objects;

import eu.basicairdata.graziano.gpslogger.management.PlaceMarkType;

public class ItemPlaceMarkEnhancedData implements Comparator<ItemPlaceMarkEnhancedData>, Comparable<ItemPlaceMarkEnhancedData> {
    private int placeMarkId = -1;           // this placemark's UNIQUE id
    private String trackName;               // this placemark's Parent Track Name
    private String placeMarkTitle;          // this placemark Name by placemark type
    private String placeMarkType;           // this placemark type ( REST, ETC... )
    private String placeMarkDesc;           // this placemark desc ( selection information )

    private LinkedList<ItemPlaceMarkImgData> placeMarkImgItemList;

    private double lat = 0.0f;
    private double lng = 0.0f;

    private boolean isPlaceMarkEnable;      // it is not collectable placemark
    private boolean isPlaceMarkHidden;      // when it hide from Window


    public ItemPlaceMarkEnhancedData(String trackName, String placeMarkTitle, String placeMarkType, String placeMarkDesc, boolean isPlaceMarkEnable) {
        this.trackName = trackName;
        this.placeMarkTitle = placeMarkTitle;
        this.placeMarkType = placeMarkType;
        this.placeMarkDesc = placeMarkDesc;
        this.isPlaceMarkEnable = isPlaceMarkEnable;
        this.isPlaceMarkHidden = false;

        this.placeMarkImgItemList = new LinkedList<>();
    }

    public int getPlaceMarkId() {
        return placeMarkId;
    }

    public void setPlaceMarkId(int placeMarkId) {
        this.placeMarkId = placeMarkId;
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

    public LinkedList<ItemPlaceMarkImgData> getPlaceMarkImgItemList() {
        return placeMarkImgItemList;
    }

    public void setPlaceMarkImgItemList(LinkedList<ItemPlaceMarkImgData> placeMarkImgItemList) {
        this.placeMarkImgItemList = placeMarkImgItemList;
    }

    public void addPlaceMarkImgItem(@NonNull final ItemPlaceMarkImgData item) {
        this.placeMarkImgItemList.add(item);
    }

    public void removePlaceMarkImgItem(@NonNull final ItemPlaceMarkImgData item) {
        this.placeMarkImgItemList.remove(item);
    }

    public void setPlaceMarkImgItem(@NonNull final ItemPlaceMarkImgData item) {
        if(this.getPlaceMarkImgItemList().isEmpty()) return;

        int i = 0;
        for(ItemPlaceMarkImgData buffer : this.getPlaceMarkImgItemList()) {
            if(buffer.equals(item)) {
                this.getPlaceMarkImgItemList().set(i, item);
            }
            ++i;
        }
    }

    /**
     * sort list by placemark type and title
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return compare result
     */
    @Override
    public int compare(ItemPlaceMarkEnhancedData o1, ItemPlaceMarkEnhancedData o2) {
        // add placemark infomation label must be place roof
        if(o1.trackName.equals("label") && o1.placeMarkDesc.equals("label") && o1.placeMarkTitle.equals("label") && o1.placeMarkType.equals(PlaceMarkType.ETC.name())) return 1; // o1 is high
        else if(o2.trackName.equals("label") && o2.placeMarkDesc.equals("label") && o2.placeMarkTitle.equals("label") && o2.placeMarkType.equals(PlaceMarkType.ETC.name())) return -1; // o2 is high

        final PlaceMarkType o1Type = PlaceMarkType.valueOf(o1.getPlaceMarkType());
        final PlaceMarkType o2Type = PlaceMarkType.valueOf(o2.getPlaceMarkType());
        final int isDiffType = o1Type.convertIntType() - o2Type.convertIntType();

//        // is same placemark TYPE???
//        if(isDiffType == 0) {
//            // compare by Placemark visbilty NAME!
//            return o2.placeMarkTitle.hashCode() - o1.placeMarkTitle.hashCode();
//        }
//        // diff type? return them!
        return isDiffType;
    }

    @Override
    public int compareTo(ItemPlaceMarkEnhancedData o) {
        // add placemark infomation label must be place roof
        if(o.trackName.equals("label") && o.placeMarkDesc.equals("label") && o.placeMarkTitle.equals("label") && o.placeMarkType.equals(PlaceMarkType.ETC.name())) return 1; // o is high
        else if(this.trackName.equals("label") && this.placeMarkDesc.equals("label") && this.placeMarkTitle.equals("label") && this.placeMarkType.equals(PlaceMarkType.ETC.name())) return -1; // o2 is high

        final PlaceMarkType o1Type = PlaceMarkType.valueOf(o.getPlaceMarkType());
        final PlaceMarkType o2Type = PlaceMarkType.valueOf(this.getPlaceMarkType());
        final int isDiffType = o1Type.convertIntType() - o2Type.convertIntType();

//        // is same placemark TYPE???
//        if(isDiffType == 0) {
//            // compare by Placemark visbilty NAME!
//            return this.placeMarkTitle.hashCode() - o.placeMarkTitle.hashCode();
//        }
//        // diff type? return them!
        return isDiffType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPlaceMarkEnhancedData that = (ItemPlaceMarkEnhancedData) o;
        return trackName.equals(that.trackName) &&
                placeMarkTitle.equals(that.placeMarkTitle) &&
                placeMarkType.equals(that.placeMarkType);
    }
    @Override
    public int hashCode() {
        return Objects.hash(trackName, placeMarkTitle, placeMarkType);
    }

    @Override
    public String toString() {
        return "ItemPlaceMarkData{" +
                "toServerId=" + placeMarkId +
                ", trackName='" + trackName + '\'' +
                ", placeMarkTitle='" + placeMarkTitle + '\'' +
                ", placeMarkType='" + placeMarkType + '\'' +
                ", isPlaceMarkEnable=" + isPlaceMarkEnable +
                ", placeMarkLat=" + lat +
                ", placeMarkLng=" + lng +
                ", placeMarkImgList=" + placeMarkImgItemList.toString() +
                '}';
    }
}
