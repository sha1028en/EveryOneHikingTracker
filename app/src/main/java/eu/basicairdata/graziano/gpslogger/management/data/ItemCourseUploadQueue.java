package eu.basicairdata.graziano.gpslogger.management.data;

/**
 * Data Class which Remember Is this Course Send to Server Successfully ???
 */
public class ItemCourseUploadQueue {
    private final int trackId; // UNIQUE track's id
    private final String trackName; // UNIQUE track's name
    private final String courseName; // recorded course name
    private int isUploaded = -1; // is this course already uploaded to Server??? ( 1 = true / other = false )

    public ItemCourseUploadQueue(int trackId, String trackName, String courseName) {
        this.trackId = trackId;
        this.trackName = trackName;
        this.courseName = courseName;
    }

    public int getTrackId() {
        return trackId;
    }

    public String getTrackName() {
        return trackName;
    }

    public String getCourseName() {
        return courseName;
    }

    public int isUploaded() {
        return isUploaded;
    }

    public void setUploaded(int isUploaded) {
        this.isUploaded = isUploaded;
    }
}
