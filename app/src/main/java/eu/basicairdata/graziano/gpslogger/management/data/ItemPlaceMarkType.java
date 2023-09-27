package eu.basicairdata.graziano.gpslogger.management.data;

import java.io.Serializable;

/**
 * kind of Placemark list DataClass
 * from Server
 */
public class ItemPlaceMarkType implements Serializable {
    private final int trackId; // this placemark's trackId
    private final String placeMarkType; // this placemark's Type
    private boolean isEnable; // is this placemark Enable ( set by user Input )

    public ItemPlaceMarkType(int trackId, String placeMarkType, boolean isEnable) {
        this.trackId = trackId;
        this.placeMarkType = placeMarkType;
        this.isEnable = isEnable;
    }

    public int getTrackId() {
        return trackId;
    }

    public String getPlaceMarkType() {
        return placeMarkType;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }
}
