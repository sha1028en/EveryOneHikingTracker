package eu.basicairdata.graziano.gpslogger.tracklist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

    private BackGroundAsyncTask<LinkedList<ItemTrackData>> requestTrackList; // request Track List from Server
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
        this.requestTrackList = new BackGroundAsyncTask<>(Dispatchers.getIO());
        this.requestTrackListListener = new BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<>() {
            @Override
            public void preTask() {
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(bind.getRoot().getContext());
                bind.trackList.setLayoutManager(linearLayoutManager);

                trackListAdapter = new TrackRecyclerAdapter((item, pos) -> {
                    Intent intent = new Intent(bind.getRoot().getContext(), RecordingActivity.class);
                    intent.putExtra(GPSApplication.ATX_EXTRA_TRACK_TITLE, item.getTrackName());
                    intent.putExtra(GPSApplication.ATV_EXTRA_TRACK_REGION, item.getTrackRegion());
                    startActivity(intent);
                });
                bind.trackList.setAdapter(trackListAdapter);

//                ItemTrackData itemTrackData = new ItemTrackData("서울시 대모산 무장애 나눔길", "서울 강남구 일원동 산52-14 일대", TrackRegionType.SEOUL.name());
//                trackListAdapter.addItem(itemTrackData);
//
//                itemTrackData = new ItemTrackData("곡성 치유의숲 무장애 나눔길", "전남 곡성군 곡성읍 신기리 1177-6", TrackRegionType.JEOLLA_SOUTH.name());
//                trackListAdapter.addItem(itemTrackData);
            }

            @Override
            public LinkedList<ItemTrackData> doTask() {
                HttpURLConnection connection = null;
                LinkedList<ItemTrackData> trackList = new LinkedList<>();

                try {
                    URL serverUrl = new URL("http://cmrd-tracker.touring.city/api/cmrd" + "?sido=서울");
                    connection = (HttpURLConnection) serverUrl.openConnection();
                    connection.setRequestProperty("Accept", "application/json");
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

                        ItemTrackData track = new ItemTrackData(trackName, trackAddress, trackRegion);
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
        this.requestTrackList.executeTask(this.requestTrackListListener);

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        EventBus.getDefault().register(this);
   }

   private void initViewListener() {
        this.bind.selectRegion.setOnClickListener(v -> {
            ChooseRegionListDialog selectRegionDialog = new ChooseRegionListDialog(this, (msg, viewId) -> {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            });
            selectRegionDialog.show();
        });
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

        if(this.requestTrackList != null) {
            this.requestTrackList.cancelTask();
            this.requestTrackList = null;
            this.requestTrackListListener = null;
        }
        this.bind = null;
   }

   @Subscribe(threadMode = ThreadMode.MAIN)
   public void onEvent(Short msg) {
//        if (msg == EventBusMSG.UPDATE_FIX) {
//
//        }
   }
}
