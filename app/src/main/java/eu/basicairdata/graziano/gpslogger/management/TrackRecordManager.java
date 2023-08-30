package eu.basicairdata.graziano.gpslogger.management;

import static eu.basicairdata.graziano.gpslogger.GPSApplication.GPS_DISABLED;
import static eu.basicairdata.graziano.gpslogger.GPSApplication.GPS_OK;

import android.content.Context;
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
        // this.gpsApp.setQuickPlacemarkRequest(true);
        this.gpsApp.setPlacemarkDescription(placeMarkDesc);
        this.gpsApp.setQuickPlacemarkRequest(true);
        this.gpsApp.setPlacemarkRequested(true);
        this.gpsApp.setCurrentTrackName(parentTrackName);
//        EventBus.getDefault().post(EventBusMSG.REQUEST_ADD_PLACEMARK);
    }

    /**
     * start Record Track when GPS provider is ENABLED
     */
    public void startRecordTrack(final String trackName, final String trackDesc) {
        this.gpsApp.gpsDataBase.deleteLocation(trackName, trackDesc);

        this.gpsApp.setCurrentTrackName(trackName);
        this.gpsApp.setCurrentTrackDesc(trackDesc);
        this.gpsApp.setRecording(true);
    }

    /**
     * stop record track if it recording track
     * @see GPSApplication::onRequestStop(bool, bool)
     *
     * @param forceStop stop record track Forcefully???
     * @param trackName to save track Name
     * @param trackDesc to save track Description
     * @param isWoodDeck to save track Type ( Wooden or Dirty )
     */
    public void stopRecordTrack(final boolean forceStop, final String trackName, final String trackDesc, boolean isWoodDeck) {
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

                            if(trackName.equals(deprecateTrackName) && trackDesc.equals(deprecateTrackDesc)) {
                                this.gpsApp.gpsDataBase.deleteTrack(deprecateTrackName, deprecateTrackDesc);
                            }
                        }
                    }
                    currentTrack.setName(trackName);
                    currentTrack.setDescription(trackDesc);
                    currentTrack.setCourseType(isWoodDeck? "wood_deck": "dirty");
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

    public static String typeToTitle(@NonNull String type) {
        final String title;

        switch (PlaceMarkType.valueOf(type)) {
            case ENTRANCE -> title = "나눔길 입구";
            case PARKING ->  title = "주차장";
            case TOILET -> title = "화장실";
            case REST ->  title = "휴계 공간";
            case BUS_STOP -> title = "버스 정류장";
            case OBSERVATION_DECK -> title = "전망 데크";
            default -> title = "기타 시설물";
        }
        return title;
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
}
