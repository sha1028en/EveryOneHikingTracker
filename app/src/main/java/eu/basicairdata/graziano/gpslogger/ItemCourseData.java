package eu.basicairdata.graziano.gpslogger;

public class ItemCourseData {

    private final String trackName;

    private String courseName;
    private int courseDistance;

    public ItemCourseData(final String trackName, String courseName) {
        this.trackName = trackName;
        this.courseName = courseName;
        this.courseDistance = 0;
    }

    public String getTrackName() {
        return trackName;
    }
    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
    public int getCourseDistance() {
        return courseDistance;
    }

    public void setCourseDistance(int courseDistance) {
        this.courseDistance = courseDistance;
    }
}
