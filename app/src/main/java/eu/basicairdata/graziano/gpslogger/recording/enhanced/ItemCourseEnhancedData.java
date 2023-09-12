package eu.basicairdata.graziano.gpslogger.recording.enhanced;

public class ItemCourseEnhancedData {
    private final String trackName;
    private String courseName;

    // to server query
    private int courseId;
    private int trackId;

    // UI
    private double courseDistance;
    private boolean isWoodDeck;
    private boolean isClicked;

    public ItemCourseEnhancedData(final String trackName, String courseName, final int trackId, final int courseId, double distance, boolean isWoodDeck) {
        this.trackName = trackName;
        this.courseName = courseName;
        this.courseId = courseId;
        this.trackId = trackId;
        this.courseDistance = distance;
        this.isWoodDeck = isWoodDeck;
        this.isClicked = false;
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

    public int getCourseId() {
        return this.courseId;
    }

    public void setCourseId(final int courseId) {
        this.courseId = courseId;
    }
    public int getTrackId() {
        return trackId;
    }
    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }

    public double getCourseDistance() {
        return courseDistance;
    }
    public void setCourseDistance(double courseDistance) {
        this.courseDistance = courseDistance;
    }

    public boolean isWoodDeck() {
        return isWoodDeck;
    }

    public void setWoodDeck(boolean woodDeck) {
        isWoodDeck = woodDeck;
    }

    public void setClicked(final boolean isClicked) {
        this.isClicked = isClicked;
    }
    public void toggleClicked() {
        this.isClicked = !this.isClicked;
    }

    public boolean getIsClicked() {
        return this.isClicked;
    }
}
