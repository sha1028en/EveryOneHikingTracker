package eu.basicairdata.graziano.gpslogger.management.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

import eu.basicairdata.graziano.gpslogger.management.define.CourseRoadType;

/**
 * A Course Record, which show list
 *
 * @see eu.basicairdata.graziano.gpslogger.recording.RecordEnhancedActivity
 * @see eu.basicairdata.graziano.gpslogger.recording.adapter.CourseRecyclerAdapter
 */
public class ItemCourse implements Serializable {
    private final String trackName; // this course's track Name
    private String courseName; // this course Name

    // to server query
    private int courseId; // this course Id when it uploaded
    private int trackId; // this course's track Id

    private String courseFileUrl; // this course's *.gpx file url if it uploaded
    private ArrayList<Double> latList; // this course lat List if it uploaded
    private ArrayList<Double> lngList; // this course lng List if it uploaded

    // UI
    private double courseDistance; // this course Distance if it uploaded
    private String courseType; // this course Type if it Recorded
    private boolean isClicked; // is this course Selected by User Input ( touch )

    public ItemCourse(final String trackName, String courseName, final int trackId, final int courseId, double distance, String courseType) {
        this.trackName = trackName;
        this.courseName = courseName;
        this.courseId = courseId;
        this.trackId = trackId;
        this.courseDistance = distance;
        this.courseType = courseType;
        this.isClicked = false;
        this.latList = new ArrayList<>();
        this.lngList = new ArrayList<>();
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

    public String getCourseType() {
        return courseType;
    }
    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

    public void calcCourseType(boolean isWoodDeck, boolean isDirt) {
        if(isWoodDeck && isDirt) this.courseType = CourseRoadType.WOOD_DECK_N_DIRT.name();
        else if (isWoodDeck) this.courseType = CourseRoadType.WOOD_DECK.name();
        else if (isDirt) this.courseType = CourseRoadType.DIRT.name();
        else this.courseType = CourseRoadType.UNDEFINED.name();
    }

    public boolean calcIsCheck(String toCheckBoxType) {
        if(this.courseType.equalsIgnoreCase(CourseRoadType.WOOD_DECK_N_DIRT.name())) return true; // ALL SELECTED!
        else if(this.courseType.equalsIgnoreCase(CourseRoadType.UNDEFINED.name())) return false; // ALL DESELECTED!
        return this.courseType.equalsIgnoreCase(toCheckBoxType);
    }

    public void setClicked(final boolean isClicked) {
        this.isClicked = isClicked;
    }
//    public void toggleClicked() {
//        this.isClicked = !this.isClicked;
//    }

    public boolean isClicked() {
        return this.isClicked;
    }


    public String getCourseFileUrl() {
        return courseFileUrl;
    }

    public void setCourseFileUrl(String courseFileUrl) {
        this.courseFileUrl = courseFileUrl;
    }

    public ArrayList<Double> getLatList() {
        return latList;
    }

    public void setLatList(ArrayList<Double> latList) {
        this.latList = latList;
    }

    public ArrayList<Double> getLngList() {
        return lngList;
    }

    public void setLngList(ArrayList<Double> lngList) {
        this.lngList = lngList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemCourse that = (ItemCourse) o;
        return courseName.equals(that.courseName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseName);
    }
}
