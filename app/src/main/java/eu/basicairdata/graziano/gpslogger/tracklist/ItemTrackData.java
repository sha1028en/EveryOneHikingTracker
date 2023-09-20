package eu.basicairdata.graziano.gpslogger.tracklist;

import androidx.annotation.NonNull;

public class ItemTrackData {

    private final long trackId; // Track Id
    private final String trackName; // Track Name
    private final String trackAddress; // Track Address
    private final String trackRegion;

    final boolean isCompleteTrack; // is this Track Already Complete ( IF ITS TRUE, NEVER MODIFY THIS TRACK )
    boolean isDoneCourseInfo = false; // is CourseInfo has ???
    boolean isDonePlacemarkPic = false; // is placemark picture has ???

    public ItemTrackData(final long trackId, @NonNull final String trackName, @NonNull final String trackAddress, @NonNull final String trackRegion, final boolean isCompleteTrack) {
        this.trackId = trackId;
        this.trackName = trackName;
        this.trackAddress = trackAddress;
        this.trackRegion = trackRegion;
        this.isCompleteTrack = isCompleteTrack;
    }


    public long getTrackId() {
        return this.trackId;
    }
    public String getTrackName() {
        return trackName;
    }

    public String getTrackAddress() {
        return trackAddress;
    }

    public boolean isDoneCourseInfo() {
        return isDoneCourseInfo;
    }

    public void setDoneCourseInfo(boolean doneCourseInfo) {
        isDoneCourseInfo = doneCourseInfo;
    }

    public String getTrackRegion() {
        return this.trackRegion;
    }

    public boolean isDonePlacemarkPic() {
        return isDonePlacemarkPic;
    }

    public void setDonePlacemarkPic(boolean donePlacemarkPic) {
        isDonePlacemarkPic = donePlacemarkPic;
    }
}
