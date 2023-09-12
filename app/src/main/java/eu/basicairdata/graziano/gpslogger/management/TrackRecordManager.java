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

import java.io.File;
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
    private static TrackRecordManager instance = null; // this SINGLETON INSTANCE. never use outside
    private WeakReference<Context> mLocalContext;
    private GPSApplication gpsApp = GPSApplication.getInstance(); // GPS Observer, Track Recorder Instance
    private LocationExtended locationExt = null; // current LocationExtends

    private int gpsState = GPS_DISABLED; // current GPS State
    private boolean isRecording = false; // now REALLY Recording Track???

    private int totalSatellitesCnt = 0; // total satellite Count
    private int availableSatellitesCnt = 0; // available satellite Count

    private double lastObserveLat = 0.0f; // last observe lat;
    private double lastObserveLng = 0.0f; // last observe lng;

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
     * destroy this Singleton Instance and release this resources
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
        if(this.gpsApp != null) {
            this.gpsApp.stopAndUnbindGPSService();
            this.gpsApp = null;
        }
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

        } else if(msg == EventBusMSG.UPDATE_FIX && this.gpsApp != null) {
            this.locationExt = this.gpsApp.getCurrentLocationExtended();
            this.gpsState = this.gpsApp.getGPSStatus();
//            this.gpsApp.getCurrentLocationExtended();

            if(this.gpsState == GPS_OK && this.locationExt != null) {
                this.totalSatellitesCnt = gpsApp.getNumberOfSatellitesTotal();
                this.availableSatellitesCnt = gpsApp.getNumberOfSatellitesUsedInFix();

                LocationExtended lastObserveLocation = this.gpsApp.getCurrentLocationExtended();
                if(lastObserveLocation != null) {
                    final double observeLat = lastObserveLocation.getLatitude();
                    final double observeLng = lastObserveLocation.getLongitude();

                    if(observeLat > 0.0f && observeLng > 0.0f) {
                        this.lastObserveLat = observeLat;
                        this.lastObserveLng = observeLng;
                    }
                    lastObserveLocation = null; // GC!
                }
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

    public void updatePlaceMark(final int placeMarkId, final double lat, final double lng) {
        if(this.gpsApp == null || lat <= 0.0f || lng <= 0.0f ) return;
        this.gpsApp.gpsDataBase.updatePlacemark(placeMarkId, lat, lng);
    }

    public void updatePlaceMark(final String trackName, final String courseName, final boolean isEnable) {
        if(this.gpsApp == null) return;
        this.gpsApp.gpsDataBase.updatePlacemark(trackName, courseName, isEnable);
    }

    /**
     * start Record Track when GPS provider is ENABLED
     */
    public void startRecordCourse(final long trackId ,@NonNull final String trackName, @NonNull final String courseName, @NonNull final String trackRegion) {
        if(this.isRecording /*|| this.availableSatellitesCnt > 4*/) return;

        this.gpsApp.gpsDataBase.deleteLocation(trackName, courseName);

        this.gpsApp.setCurrentTrackId(trackId);
        this.gpsApp.setCurrentTrackName(trackName);
        this.gpsApp.setCurrentTrackDesc(courseName);
        this.gpsApp.setCurrentTrackRegion(trackRegion);
        this.gpsApp.setRecording(true);
        this.isRecording = true;
    }

    /**
     * stop record track if it recording track
     *
     * @param trackId     to save track Id
     * @param trackName   to save track Name
     * @param courseName  to save track Description
     * @param trackRegion to save track Region
     * @param isWoodDeck  to save track Type ( Wooden or Dirty )
     * @see GPSApplication::onRequestStop(bool, bool)
     */
    public void stopRecordTrack(final long trackId, @NonNull final String trackName, @NonNull final String courseName, @NonNull final String trackRegion, boolean isWoodDeck) {
        if(this.gpsApp == null || !this.isRecording) return;

        this.gpsApp.setStopButtonFlag(true, gpsApp.getCurrentTrack().getNumberOfLocations() + gpsApp.getCurrentTrack().getNumberOfPlacemarks() > 0 ? 1000 : 300);
        this.gpsApp.setRecording(false);
        this.gpsApp.setPlacemarkRequested(false);

        Track currentTrack = this.gpsApp.getCurrentTrack();
        if (currentTrack.getNumberOfLocations() + currentTrack.getNumberOfPlacemarks() > 0) {
            Track course = this.gpsApp.gpsDataBase.getTrack(trackName, courseName);

            if (course != null) {
                String deprecateTrackName = course.getName();
                String deprecateTrackDesc = course.getDescription();

                if (trackName.equals(deprecateTrackName) && courseName.equals(deprecateTrackDesc)) {
                    this.gpsApp.gpsDataBase.deleteTrackOnly(deprecateTrackName, deprecateTrackDesc);
                }
            }
            currentTrack.setTrackId(trackId);
            currentTrack.setTrackRegion(trackRegion);
            currentTrack.setName(trackName);
            currentTrack.setDescription(courseName);
            currentTrack.setCourseType(isWoodDeck ? TRACK_COURSE_TYPE_WOOD_DECK : TRACK_COURSE_TYPE_DIRT);
            GPSApplication.getInstance().gpsDataBase.updateTrack(currentTrack);
            this.isRecording = false;

            EventBus.getDefault().post(EventBusMSG.NEW_TRACK);
            this.toast = Toast.makeText(this.gpsApp.getApplicationContext(), R.string.toast_track_saved_into_tracklist, Toast.LENGTH_SHORT);
            this.toast.setGravity(Gravity.BOTTOM, 0, GPSApplication.TOAST_VERTICAL_OFFSET);
            this.toast.show();

        } else {
            toast.cancel();
            toast = Toast.makeText(gpsApp.getApplicationContext(), R.string.toast_nothing_to_save, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM, 0, GPSApplication.TOAST_VERTICAL_OFFSET);
            toast.show();
        }
    }

    public void pauseRecordTrack() {
        if(this.gpsApp == null || !this.gpsApp.isRecording()) return;
        this.gpsApp.setRecording(false);
    }

    public void resumeRecordCourse() {
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

    public LinkedList<LocationExtended> getCourseLocationList(@NonNull final String trackName, @NonNull final String courseName) {
        LinkedList<LocationExtended> courseList = new LinkedList<>();
        if(this.gpsApp != null) {
            courseList = new LinkedList<>(this.gpsApp.gpsDataBase.getLocationsList(trackName, courseName));
        }
        return courseList;
    }

    public LinkedList<Track> getCourseList(@NonNull final String trackName, @NonNull final String courseName) {
        LinkedList<Track> courseList = new LinkedList<>();
        if(this.gpsApp != null) {
            courseList = new LinkedList<>(this.gpsApp.gpsDataBase.getTrackList(trackName, courseName));
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

    public double getLastObserveLat() {
        return this.lastObserveLat;
    }

    public double getLastObserveLng() {
        return this.lastObserveLng;
    }

    public boolean isRecordingCourse() {
        return this.isRecording;
    }

    public boolean isPauseRecordingCourse() {
        boolean isPauseRecording = false;
        if(this.gpsApp != null) {
            isPauseRecording = this.gpsApp.isRecording();
        }
        return isPauseRecording;
    }

    /**
     * @return is it Recording Course or add Placemark
     */
    public boolean isAvailableRecord() {
        if(this.gpsApp == null || this.mLocalContext == null || this.mLocalContext.get() == null) return false;
        return this.gpsState == GPS_OK;
    }

    public void removeCourse(@NonNull final String trackName, @NonNull final String courseName) {
        if(this.gpsApp != null) {
            this.gpsApp.gpsDataBase.deleteTrack(trackName, courseName);
        }
    }

    public void updateCourseType(@NonNull final String trackName, @NonNull final String courseName, boolean isWoodDeck) {
        if(this.gpsApp != null) {
            final String courseType = isWoodDeck? "wood_deck": "dirty";
            this.gpsApp.gpsDataBase.updateCourseType(trackName, courseName, courseType);
        }
    }

    public boolean createBlankTables(final long trackId, @NonNull final String trackName, @NonNull final String trackDesc, @NonNull final String trackRegion) {
        if(this.gpsApp == null) return false;

        // create EMPTY track
        Track emptyTrack = new Track();
        emptyTrack.setTrackId(trackId);
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
            case TOILET -> placemarkType = "화장실";
            case REST_AREA -> placemarkType = "쉼터";
            case BUS_STOP -> placemarkType = "버스";
            case OBSERVATION_DECK -> placemarkType = "전망대";
            case ETC -> placemarkType = "기타 시설물";
//            default -> placemarkType = "기타 시설물 1";
        }
        return placemarkType;
    }
}
