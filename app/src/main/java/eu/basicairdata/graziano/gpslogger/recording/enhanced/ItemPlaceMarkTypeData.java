package eu.basicairdata.graziano.gpslogger.recording.enhanced;

import java.io.Serializable;

public class ItemPlaceMarkTypeData implements Serializable {
    private final int trackId;
    private final String placeMarkType;
    private boolean isEnable;

    public ItemPlaceMarkTypeData(int trackId, String placeMarkType, boolean isEnable) {
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
