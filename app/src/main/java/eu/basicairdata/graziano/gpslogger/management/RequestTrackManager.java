package eu.basicairdata.graziano.gpslogger.management;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

import eu.basicairdata.graziano.gpslogger.Track;
import eu.basicairdata.graziano.gpslogger.tracklist.ItemTrackData;
import kotlinx.coroutines.Dispatchers;

public class RequestTrackManager {
    private BackGroundAsyncTask<LinkedList<ItemTrackData>> requestTask;

    private WeakReference<Context> localContext;
    public interface OnRequestResponse<V> {
        void onRequestResponse(final V response, final boolean isSuccess);
    }

    public RequestTrackManager(@NonNull final Context context) {
        this.localContext = new WeakReference<>(context);
    }

    public void release() {
        if(this.localContext != null) {
            this.localContext.clear();
            this.localContext = null;
        }

        if(this.requestTask != null) {
            this.requestTask.cancelTask();
            this.requestTask = null;
        }
    }

    /**
     * request Track List to Server
     *
     * @param requestRegion what kind of Track Type? ( if it null, request All Tracks )
     * @param recordManager TrackRecordManager
     * @param listener Response CallBack Listener
     */
    public void requestTrackList(@Nullable final String requestRegion, @NonNull final TrackRecordManager recordManager, @NonNull final OnRequestResponse<LinkedList<ItemTrackData>> listener) {
        if(this.requestTask != null && this.requestTask.isTaskAlive()) this.requestTask.cancelTask();
        BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<LinkedList<ItemTrackData>> responseReceiver = new BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<>() {
            @Override
            public void preTask() {}

            @Override
            public LinkedList<ItemTrackData> doTask() {
                HttpURLConnection connection = null;
                LinkedList<ItemTrackData> trackList = new LinkedList<>();
                String requestRegionParam;

                try {
                    if (requestRegion == null || requestRegion.isBlank()) requestRegionParam = ""; // request All tracks
                    else requestRegionParam = "?sido=" + requestRegion; // request some tracks

                    URL serverUrl = new URL("http://cmrd-tracker.touring.city/api/cmrd" + requestRegionParam);
                    connection = (HttpURLConnection) serverUrl.openConnection();
                    connection.setRequestProperty("Accept", "application/json");
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
                    for (int i = 0; i < jsonTrackList.length(); i++) {
                        rawJsonResponse = jsonTrackList.getJSONObject(i);
                        trackId = rawJsonResponse.getInt("cmrdId");
                        trackName = rawJsonResponse.getString("name");
                        trackAddress = rawJsonResponse.getString("addr");
                        trackRegion = rawJsonResponse.getString("sido");

                        ItemTrackData track = new ItemTrackData(trackId, trackName, trackAddress, trackRegion);
                        if (recordManager != null) {
                            LinkedList<Track> courses = recordManager.getCourseListByTrackName(trackName);
                            boolean isTrackHasCourse = false;
                            // is this track has Course Record???
                            if (!courses.isEmpty()) {
                                for (Track course : courses) {
                                    // is this course has valid record???
                                    if (course.getDurationMoving() > 1.0f && course.getNumberOfLocations() > 1) {
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
                    if (connection != null) connection.disconnect();
                }
                return trackList;
            }

            @Override
            public void endTask(LinkedList<ItemTrackData> value) {
                listener.onRequestResponse(value, true);
            }

            @Override
            public void failTask(@NonNull Throwable throwable) {
                throwable.printStackTrace();
                listener.onRequestResponse(new LinkedList<>(), false);

            }
        };
        this.requestTask = new BackGroundAsyncTask<>(Dispatchers.getIO());
        this.requestTask.executeTask(responseReceiver);
    }
}
