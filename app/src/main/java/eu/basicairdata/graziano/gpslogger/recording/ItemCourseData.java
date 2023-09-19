package eu.basicairdata.graziano.gpslogger.recording;

/**
 * @deprecated {@link eu.basicairdata.graziano.gpslogger.recording.enhanced.ItemCourseEnhancedData}
 */
public class ItemCourseData {
    private final String trackName;
    private String courseName;
    private int coursePrimaryIndex;

    private int courseDistance;
    private boolean isWoodDeck;

    private boolean isClicked;

    public ItemCourseData(final String trackName, String courseName, final int coursePrimaryIndex, int distance, boolean isWoodDeck) {
        this.trackName = trackName;
        this.courseName = courseName;
        this.coursePrimaryIndex = coursePrimaryIndex;
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

    public int getCoursePrimaryIndex() {
        return this.coursePrimaryIndex;
    }

    public void setCoursePrimaryIndex(final int coursePrimaryIndex) {
        this.coursePrimaryIndex = coursePrimaryIndex;
    }

    public int getCourseDistance() {
        return courseDistance;
    }
    public void setCourseDistance(int courseDistance) {
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
