package eu.basicairdata.graziano.gpslogger.tracklist;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

import eu.basicairdata.graziano.gpslogger.GPSApplication;
import eu.basicairdata.graziano.gpslogger.R;
import eu.basicairdata.graziano.gpslogger.Track;
import eu.basicairdata.graziano.gpslogger.databinding.ActivityTrackListBinding;
import eu.basicairdata.graziano.gpslogger.management.BackGroundAsyncTask;
import eu.basicairdata.graziano.gpslogger.management.TrackRecordManager;
import eu.basicairdata.graziano.gpslogger.management.TrackRegionType;
import eu.basicairdata.graziano.gpslogger.recording.RecordingActivity;
import kotlinx.coroutines.Dispatchers;

public class TrackListActivity extends AppCompatActivity {
    private ActivityTrackListBinding bind; // View n Layout Instance
    private TrackRecordManager recordManager; // Track, Course, Placemark control Manager
    private TrackRecyclerAdapter trackListAdapter; // Track List View Adapter ( RecyclerView )

    private BackGroundAsyncTask<LinkedList<ItemTrackData>> requestTrackTask; // request Track List from Server
    private BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<LinkedList<ItemTrackData>> requestTrackListListener; // Call Back Listener when request from Server

   @Override
   protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setTheme(R.style.MyMaterialTheme);
        super.onCreate(savedInstanceState);

        this.bind = ActivityTrackListBinding.inflate(this.getLayoutInflater());
        setContentView(this.bind.getRoot());
        this.recordManager = TrackRecordManager.createInstance(this);

       this.initViewListener();

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        EventBus.getDefault().register(this);
   }

   private void initViewListener() {
        this.bind.selectRegion.setOnClickListener(v -> {
            ChooseRegionListDialog selectRegionDialog = new ChooseRegionListDialog(this, (msg, viewId) -> {
                if(this.bind == null || this.requestTrackTask == null) return;
                if(this.requestTrackTask.isTaskAlive()) this.requestTrackTask.cancelTask();
                this.bind.selectRegion.setText(msg);
                this.requestTrackList(msg);
            });
            selectRegionDialog.show();
        });

       LinearLayoutManager linearLayoutManager = new LinearLayoutManager(bind.getRoot().getContext());
       this.bind.trackList.setLayoutManager(linearLayoutManager);

       this.trackListAdapter = new TrackRecyclerAdapter((item, pos) -> {
           Intent intent = new Intent(bind.getRoot().getContext(), RecordingActivity.class);
           intent.putExtra(GPSApplication.ATX_EXTRA_TRACK_TITLE, item.getTrackName());
           intent.putExtra(GPSApplication.ATV_EXTRA_TRACK_REGION, item.getTrackRegion());
           intent.putExtra(GPSApplication.ATV_EXTRA_TRACK_ID, item.getTrackId());
           startActivity(intent);
       });
       this.bind.trackList.setAdapter(trackListAdapter);

       this.requestTrackTask = new BackGroundAsyncTask<>(Dispatchers.getIO());
       this.requestTrackList(TrackRegionType.SEOUL.getRegionName());
   }

   private void requestTrackList(@Nullable final String requestRegion) {
       if(this.requestTrackTask == null || this.bind == null || this.trackListAdapter == null) return;
       this.requestTrackListListener = new BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<>() {
           @Override
           public void preTask() {}

           @Override
           public LinkedList<ItemTrackData> doTask() {
               HttpURLConnection connection = null;
               LinkedList<ItemTrackData> trackList = new LinkedList<>();
               String requestRegionParam;

               try {
                   if(requestRegion == null || requestRegion.isBlank()) requestRegionParam = ""; // request All tracks
                   else requestRegionParam = "?sido=" + requestRegion; // request some tracks

                   URL serverUrl = new URL("http://cmrd-tracker.touring.city/api/cmrd" + requestRegionParam);
                   connection = (HttpURLConnection) serverUrl.openConnection();
                   connection.setRequestProperty("Accept", "application/json");
//                   connection.setRequestProperty("Accept", "*/*");
                   connection.setRequestProperty("Authorization", "anwkddosksnarlf");
                   connection.setRequestMethod("GET");
                   connection.connect();

                   BufferedReader readStream = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                   String buffer;
                   StringBuilder response = new StringBuilder();
                   try (readStream) {
                       while ((buffer = readStream.readLine()) != null) {
                           response.append(buffer);
                       }
                   }
                   JSONObject rawJsonResponse;
                   JSONArray jsonTrackList = new JSONObject(response.toString()).getJSONArray("data");

                   int trackId;
                   String trackName;
                   String trackAddress;
                   String trackRegion;
                   for(int i = 0; i < jsonTrackList.length(); i++) {
                       rawJsonResponse = jsonTrackList.getJSONObject(i);
                       trackId = rawJsonResponse.getInt("cmrdId");
                       trackName = rawJsonResponse.getString("name");
                       trackAddress = rawJsonResponse.getString("addr");
                       trackRegion = rawJsonResponse.getString("sido");

                       ItemTrackData track = new ItemTrackData(trackId, trackName, trackAddress, trackRegion);
                       if(recordManager != null) {
                           LinkedList<Track> courses = recordManager.getCourseListByTrackName(trackName);
                           boolean isTrackHasCourse = false;
                           // is this track has Course Record???
                           if(!courses.isEmpty()) {
                               for(Track course : courses) {
                                   // is this course has valid record???
                                   if(course.getDurationMoving() > 1.0f && course.getNumberOfLocations() > 1)  {
                                       isTrackHasCourse = true;
                                       break;
                                   }
                               }
                           }
                           track.setDoneCourseInfo(isTrackHasCourse);
                       }
                       trackList.add(track);
                   }

               } catch (IOException | JSONException | IndexOutOfBoundsException e) {
                   e.printStackTrace();

               } finally {
                   if(connection != null) connection.disconnect();
               }
               return trackList;
           }

           @Override
           public void endTask(LinkedList<ItemTrackData> value) {
               runOnUiThread(() -> {
                   if(bind == null || trackListAdapter == null) return;
                   trackListAdapter.clearItems();
                   trackListAdapter.addItems(value);
               });
           }

           @Override
           public void failTask(@NonNull Throwable throwable) {
               throwable.printStackTrace();
           }
       };
       this.requestTrackTask.executeTask(this.requestTrackListListener);
   }

   @Override
   protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        TrackRecordManager.destroyInstance();
        this.recordManager = null;

        if(this.trackListAdapter != null) {
            this.trackListAdapter.release();
            this.trackListAdapter = null;
        }

        if(this.requestTrackTask != null) {
            this.requestTrackTask.cancelTask();
            this.requestTrackTask = null;
            this.requestTrackListListener = null;
        }
        this.bind = null;
   }

   @Subscribe(threadMode = ThreadMode.BACKGROUND)
   public void onEvent(Short msg) {
//        if (msg == EventBusMSG.UPDATE_FIX) {
//
//        }
   }
}
