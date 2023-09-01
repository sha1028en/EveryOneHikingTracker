package eu.basicairdata.graziano.gpslogger.management;

import static eu.basicairdata.graziano.gpslogger.GPSApplication.GPS_DISABLED;
import static eu.basicairdata.graziano.gpslogger.GPSApplication.GPS_OK;
import static eu.basicairdata.graziano.gpslogger.Track.TRACK_COURSE_TYPE_DIRT;
import static eu.basicairdata.graziano.gpslogger.Track.TRACK_COURSE_TYPE_WOOD_DECK;

import android.content.Context;
import android.location.Location;
import android.view.Gravity;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.LinkedList;

import eu.basicairdata.graziano.gpslogger.EventBusMSG;
import eu.basicairdata.graziano.gpslogger.EventBusMSGNormal;
import eu.basicairdata.graziano.gpslogger.GPSApplication;
import eu.basicairdata.graziano.gpslogger.LocationExtended;
import eu.basicairdata.graziano.gpslogger.R;
import eu.basicairdata.graziano.gpslogger.Track;

/**
 * @author dspark ( sha1028en )
 * @since 2023-08-14
 *
 * handling to start / stop record track. add placemark
 */
public class TrackRecordManager {
    private static TrackRecordManager instance = null;
    private WeakReference<Context> mLocalContext;
    private GPSApplication gpsApp = GPSApplication.getInstance();
    private LocationExtended locationExt = null;
    private int gpsState = GPS_DISABLED;

    private int totalSatellitesCnt = 0;
    private int availableSatellitesCnt = 0;

    private String courseName = "";
    private String trackName = "";

    private String poiName = "";

    private Toast toast;

    /**
     * Constructor<br>
     * create TrackRecordManager Singleton INSTANCE<br>
     * if already created, return them
     *
     * @param context Android Application Context ( Dont UI Context! )
     * @return TrackRecordManager Singleton INSTANCE
     */
    public static synchronized TrackRecordManager createInstance(@NonNull final Context context) {
        if(TrackRecordManager.instance == null) {
            TrackRecordManager.instance = new TrackRecordManager(context);
        }
        return TrackRecordManager.instance;
    }

    /**
     * return TrackRecordManager Singleton INSTANCE<br>
     * if doesnt created this Instance, return NULL
     *
     * @return TrackRecordManager Singleton INSTANCE or NULL
     */
    public static synchronized TrackRecordManager getInstance() {
        return TrackRecordManager.instance;
    }

    /**
     * Destructor<br>
     * destroy this Singleton Instance and release resources
     */
    public static synchronized void destroyInstance() {
        if(instance != null) {
            instance.destroy();
        }
        instance = null;
    }

    /**
     * init this instance<br>
     * NEVER CALL THIS OUTSIDE or DIRECTLY
     *
     * @param context Android Application Context ( NOT "UI" Context )
     */
    private TrackRecordManager(@NonNull final Context context) {
        this.mLocalContext = new WeakReference<>(context);
        this.toast = new Toast(this.mLocalContext.get());
        this.initEventBus();
    }

    /**
     * Destructor<br>
     * NEVER CALL THIS OUTSIDE or DIRECTLY
     */
    private void destroy() {
        if(this.mLocalContext != null) {
            this.mLocalContext.clear();
            this.mLocalContext = null;
        }
        EventBus.getDefault().unregister(this);
        this.gpsApp = null;
        this.toast = null;
    }

    /**
     * init EventBus which recev Gps, add mark... events!
     */
    public void initEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        EventBus.getDefault().register(this);
        EventBus.getDefault().post(EventBusMSG.APP_RESUME);
    }


    // Getter, Setter

    /**
     * @return count of ALL Satellites INCLUDE not Available satellites
     */
    public int getTotalSatellitesCnt() {
        return this.totalSatellitesCnt;
    }

    /**
     * @return count of Available Satellites
     */
    public int getAvailableSatellitesCnt() {
        return this.availableSatellitesCnt;
    }

    /**
     * The EventBus receiver for Short Messages.
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEvent(Short msg) {
        if (msg == EventBusMSG.UPDATE_TRACK) {

        } else if(msg == EventBusMSG.UPDATE_FIX) {
            this.locationExt = this.gpsApp.getCurrentLocationExtended();
            this.gpsState = this.gpsApp.getGPSStatus();

            if(this.gpsState == GPS_OK && this.locationExt != null) {
                this.totalSatellitesCnt = gpsApp.getNumberOfSatellitesTotal();
                this.availableSatellitesCnt = gpsApp.getNumberOfSatellitesUsedInFix();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEvent(EventBusMSGNormal msg) {}

    /**
     * record PlaceMarker when GPS provider ENABLED
     * @deprecated internal struct has been changed. still work successfully
     *
     * @param placeMarkDesc placemarker desc
     * @param parentTrackName placemarker's parent Track Name
     */
    public void addPlaceMark(@NonNull final String placeMarkDesc, @NonNull final String parentTrackName) {

        // add black desc place marker <-
//        this.gpsApp.setQuickPlacemarkRequest(true);
        if (!gpsApp.isStopButtonFlag() || this.gpsApp.getGPSStatus() != GPS_OK) {
            if (!gpsApp.isFirstFixFound() && this.gpsApp.getCurrentLocationExtended() == null) {
                toast.cancel();
                toast = Toast.makeText(gpsApp.getApplicationContext(), R.string.toast_annotate_when_gps_found, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM, 0, GPSApplication.TOAST_VERTICAL_OFFSET);
                toast.show();
                return;
            }
        }
        this.gpsApp.setPlacemarkType(placeMarkDesc);
        this.gpsApp.setCurrentTrackName(parentTrackName);
        this.gpsApp.setQuickPlacemarkRequest(true);
        this.gpsApp.setPlacemarkRequested(true);
        //        EventBus.getDefault().post(EventBusMSG.REQUEST_ADD_PLACEMARK);
    }

    public void addPlaceMark(@NonNull final String placeMarkName, @NonNull final String placeMarkType, @NonNull final String parentTrackRegion, @NonNull final String parentTrackName, final boolean isEnablePlaceMark) {
        if (!gpsApp.isStopButtonFlag() || this.gpsApp.getGPSStatus() != GPS_OK) {
            if (!gpsApp.isFirstFixFound() && this.gpsApp.getCurrentLocationExtended() == null) {
                toast.cancel();
                toast = Toast.makeText(gpsApp.getApplicationContext(), R.string.toast_annotate_when_gps_found, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM, 0, GPSApplication.TOAST_VERTICAL_OFFSET);
                toast.show();
                return;
            }
        }
        // this.gpsApp.setQuickPlacemarkRequest(true);
        this.gpsApp.setPlacemarkName(placeMarkName);
        this.gpsApp.setPlacemarkType(placeMarkType);
        this.gpsApp.setCurrentTrackName(parentTrackName);
        this.gpsApp.setCurrentTrackRegion(parentTrackRegion);
        this.gpsApp.setPlacemarkEnable(isEnablePlaceMark);
        this.gpsApp.setQuickPlacemarkRequest(true);
        this.gpsApp.setPlacemarkRequested(true);
    }

    /**
     * start Record Track when GPS provider is ENABLED
     */
    public void startRecordCourse(final String trackName, final String courseName, @NonNull final String trackRegion) {
        this.gpsApp.gpsDataBase.deleteLocation(trackName, courseName);

        this.gpsApp.setCurrentTrackName(trackName);
        this.gpsApp.setCurrentTrackDesc(courseName);
        this.gpsApp.setCurrentTrackRegion(trackRegion);
        this.gpsApp.setRecording(true);
    }

    /**
     * stop record track if it recording track
     * @see GPSApplication::onRequestStop(bool, bool)
     *
     * @param forceStop stop record track Forcefully???
     * @param trackName to save track Name
     * @param trackRegion to save track Region
     * @param courseName to save track Description
     * @param isWoodDeck to save track Type ( Wooden or Dirty )
     */
    public void stopRecordTrack(final boolean forceStop, @NonNull final String trackName, @NonNull final String courseName, @NonNull final String trackRegion, boolean isWoodDeck) {
        if (!gpsApp.isBottomBarLocked() || forceStop) {
            if (!gpsApp.isStopButtonFlag()) {
                gpsApp.setStopButtonFlag(true, gpsApp.getCurrentTrack().getNumberOfLocations() + gpsApp.getCurrentTrack().getNumberOfPlacemarks() > 0 ? 1000 : 300);
                gpsApp.setRecording(false);
                gpsApp.setPlacemarkRequested(false);
                //Update();

                Track currentTrack = this.gpsApp.getCurrentTrack();
                if (currentTrack.getNumberOfLocations() + currentTrack.getNumberOfPlacemarks() > 0) {
                    LinkedList<Track> trackList = new LinkedList<>(this.gpsApp.gpsDataBase.getTrackListByName(trackName));
                    if(!trackList.isEmpty()) {
                        for(Track victim : trackList) {
                            String deprecateTrackName = victim.getName();
                            String deprecateTrackDesc = victim.getDescription();

                            if(trackName.equals(deprecateTrackName) && courseName.equals(deprecateTrackDesc)) {
                                this.gpsApp.gpsDataBase.deleteTrack(deprecateTrackName, deprecateTrackDesc);
                            }
                        }
                    }
                    currentTrack.setTrackRegion(trackRegion);
                    currentTrack.setName(trackName);
                    currentTrack.setDescription(courseName);
                    currentTrack.setCourseType(isWoodDeck? TRACK_COURSE_TYPE_WOOD_DECK: TRACK_COURSE_TYPE_DIRT);
                    GPSApplication.getInstance().gpsDataBase.updateTrack(currentTrack);

                    EventBus.getDefault().post(EventBusMSG.NEW_TRACK);
                    toast = Toast.makeText(gpsApp.getApplicationContext(), R.string.toast_track_saved_into_tracklist, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.BOTTOM, 0, GPSApplication.TOAST_VERTICAL_OFFSET);
                    toast.show();

                } else {
                    toast.cancel();
                    toast = Toast.makeText(gpsApp.getApplicationContext(), R.string.toast_nothing_to_save, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.BOTTOM, 0, GPSApplication.TOAST_VERTICAL_OFFSET);
                    toast.show();
                }
            }

        } else {
            toast.cancel();
            toast = Toast.makeText(gpsApp.getApplicationContext(), R.string.toast_bottom_bar_locked, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM, 0, GPSApplication.TOAST_VERTICAL_OFFSET);
            toast.show();
        }
    }

    public void pauseRecordTrack() {
        if(this.gpsApp == null || !this.gpsApp.isRecording()) return;
        this.gpsApp.setRecording(false);
    }

    public void resumeRecordTrack() {
        if(this.gpsApp == null || this.gpsApp.isRecording()) return;
        this.gpsApp.setRecording(true);
    }

    public LinkedList<Track> getCourseListByTrackName(@NonNull final String trackName) {
        LinkedList<Track> courseList = new LinkedList<>();
        if(this.gpsApp != null) {
            courseList = new LinkedList<>(this.gpsApp.gpsDataBase.getTrackListByName(trackName));
        }
        return courseList;
    }

    public LinkedList<LocationExtended> getPlaceMarkByTrackName(@NonNull final String trackName) {
        LinkedList<LocationExtended> placeMarkList = new LinkedList<>();

        if(this.gpsApp != null) {
            placeMarkList = new LinkedList<>(this.gpsApp.gpsDataBase.getPlacemarksList(trackName));
        }
        return placeMarkList;
    }

    public void removeCourse(@NonNull final String trackName, @NonNull final String courseName) {
        if(this.gpsApp != null) {
            this.gpsApp.gpsDataBase.deleteTrack(trackName, courseName);
        }
    }

    public boolean isRecordingCourse() {
        boolean isRecording = false;
        if(this.gpsApp != null) {
            isRecording = this.gpsApp.isRecording();
        }
        return isRecording;
    }

    public void updateCourseType(@NonNull final String trackName, @NonNull final String courseName, boolean isWoodDeck) {
        if(this.gpsApp != null) {
            final String courseType = isWoodDeck? "wood_deck": "dirty";
            this.gpsApp.gpsDataBase.updateCourseType(trackName, courseName, courseType);
        }
    }

    public boolean createBlankTables(@NonNull final String trackName, @NonNull final String trackDesc, @NonNull final String trackRegion) {
        if(this.gpsApp == null) return false;

        // create EMPTY track
        Track emptyTrack = new Track();
        emptyTrack.setName(trackName);
        emptyTrack.setDescription(trackDesc);
        emptyTrack.setCourseType(TRACK_COURSE_TYPE_WOOD_DECK);
        emptyTrack.setTrackRegion(trackRegion);
        this.gpsApp.gpsDataBase.addTrack(emptyTrack);

        // then create EMPTY placemarks into track
        LocationExtended emptyRow;
        Location emptyLocation = new Location("DB");
        emptyLocation.setLatitude(0.0f);
        emptyLocation.setLongitude(0.0f);
        emptyLocation.setAltitude(0.0f);
        emptyLocation.setSpeed(0.0f);
        emptyLocation.setAccuracy(0.0f);
        emptyLocation.setBearing(0.0f);
        emptyLocation.setTime(0L);

        emptyTrack = this.gpsApp.gpsDataBase.getTrack(trackName, trackDesc);
        if(emptyTrack == null) return false;

        for(PlaceMarkType placeMarkType : PlaceMarkType.values()) {
            emptyRow = new LocationExtended(emptyLocation);
            emptyRow.setNumberOfSatellites(0);
            emptyRow.setNumberOfSatellitesUsedInFix(0);

            emptyRow.setType(placeMarkType.name());
            emptyRow.setTrackName(trackName);
            emptyRow.setName(this.getNameByPlacemarkType(placeMarkType));
            emptyRow.setEnable(true);
            this.gpsApp.gpsDataBase.addPlacemarkToTrack(emptyRow, emptyTrack);
        }
        return true;
    }

    private String getNameByPlacemarkType(PlaceMarkType type) {
        String placemarkType = "기타 시설물 1";

        switch (type) {
            case ENTRANCE -> placemarkType = "나눔길 입구";
            case PARKING -> placemarkType = "주차장";
            case TOILET -> placemarkType = "화장실 1";
            case REST -> placemarkType = "휴계 공간 1";
            case BUS_STOP -> placemarkType = "버스 정류장";
            case OBSERVATION_DECK -> placemarkType = "전망 데크 1";
            case ETC -> placemarkType = "기타 시설물 1";
//            default -> placemarkType = "기타 시설물 1";
        }
        return placemarkType;
    }
}
