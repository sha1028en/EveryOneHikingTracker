package eu.basicairdata.graziano.gpslogger.management;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

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
import eu.basicairdata.graziano.gpslogger.recording.enhanced.ItemCourseEnhancedData;
import eu.basicairdata.graziano.gpslogger.tracklist.ItemTrackData;
import kotlinx.coroutines.Dispatchers;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RequestTrackManager {
    private BackGroundAsyncTask<LinkedList<ItemTrackData>> requestTrackListTask;
    private BackGroundAsyncTask<ItemTrackData> requestTrackUploadTask;
    private BackGroundAsyncTask<ItemCourseEnhancedData> requestCourseAddTask;

    public interface OnRequestResponse<V> {
        void onRequestResponse(final V response, final boolean isSuccess);
    }

    public RequestTrackManager() {
        this.requestCourseAddTask = new BackGroundAsyncTask<>(Dispatchers.getIO());
    }

    public void release() {
        if(this.requestCourseAddTask != null) {
            this.requestCourseAddTask.cancelTask();
            this.requestCourseAddTask = null;
        }

        if(this.requestTrackListTask != null) {
            this.requestTrackListTask.cancelTask();
            this.requestTrackListTask = null;
        }

        if(this.requestTrackUploadTask != null) {
            this.requestTrackUploadTask.cancelTask();
            this.requestTrackUploadTask = null;
        }
    }

    /**
     * request Track List to Server
     *
     * @param requestRegion what kind of Track Type? ( if it null, request All Tracks )
     * @param listener Response CallBack Listener
     */
    public void requestTrackList(@Nullable final String requestRegion, @NonNull final OnRequestResponse<LinkedList<ItemTrackData>> listener) {
        if(this.requestTrackListTask != null && this.requestTrackListTask.isTaskAlive()) this.requestTrackListTask.cancelTask();
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
                    int courseCount;
                    int placeMarkCount;
                    boolean isCompleteTrack;
                    for (int i = 0; i < jsonTrackList.length(); i++) {
                        rawJsonResponse = jsonTrackList.getJSONObject(i);
                        trackId = rawJsonResponse.getInt("cmrdId");
                        trackName = rawJsonResponse.getString("name");
                        trackAddress = rawJsonResponse.getString("addr");
                        trackRegion = rawJsonResponse.getString("sido");
                        isCompleteTrack = rawJsonResponse.optString("completed", "Y").equals("Y");
                        courseCount = rawJsonResponse.optInt("courseCnt", 0);
                        placeMarkCount = rawJsonResponse.optInt("poiCnt", 0);

                        ItemTrackData track = new ItemTrackData(trackId, trackName, trackAddress, trackRegion, isCompleteTrack);
                        track.setDoneCourseInfo(courseCount > 0);
                        track.setDonePlacemarkPic(placeMarkCount > 0);
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
        this.requestTrackListTask = new BackGroundAsyncTask<>(Dispatchers.getIO());
        this.requestTrackListTask.executeTask(responseReceiver);
    }

    // REQUEST ADD COURSE
    // for Exporter.
    public void requestAddCourse(
            final int trackId,
            @NonNull final String courseName,
            @NonNull final String courseType,
            @NonNull final DocumentFile courseFile,
            @NonNull final String fileName,
            @NonNull OnRequestResponse<ItemCourseEnhancedData> listener) {
        if(this.requestCourseAddTask == null) return;
        if(this.requestCourseAddTask.isTaskAlive()) this.requestCourseAddTask.cancelTask();

        BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<ItemCourseEnhancedData> responseReceiver = new BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<>() {
            @Override
            public void preTask() {}

            @Override
            public ItemCourseEnhancedData doTask() {
                OkHttpClient connection = new OkHttpClient();
                final String url = "http://cmrd-tracker.touring.city/api/cmrd/gpx/" + trackId + "/files";

                try (BufferedReader readStream = new BufferedReader(new InputStreamReader(GPSApplication.getInstance().getContentResolver().openInputStream(courseFile.getUri())))) {
                    StringBuilder courseFileBuffer = new StringBuilder();
                    String buffer;
                    while ((buffer = readStream.readLine()) != null) {
                        courseFileBuffer.append(buffer);
                    }

                    RequestBody body = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("cmrdCourseDtos[0].courseName", courseName)
                            .addFormDataPart("cmrdCourseDtos[0].courseType", courseType)
                            .addFormDataPart("cmrdCourseDtos[0].courseGpx", fileName + ".gpx", RequestBody.create(courseFileBuffer.toString().getBytes(StandardCharsets.UTF_8), MultipartBody.FORM))
                            .build();

                    Request request = new Request.Builder()
                            .url(url)
                            .addHeader("Authorization", "anwkddosksnarlf")
                            .addHeader("accept", "*/*")
                            .post(body)
                            .build();

                    Response response = connection.newCall(request).execute();
                    response.close();

                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
                return null;
            }

            @Override
            public void endTask(ItemCourseEnhancedData value) {
                listener.onRequestResponse(null, true);
            }

            @Override
            public void failTask(@NonNull Throwable throwable) {
                throwable.printStackTrace();
                listener.onRequestResponse(null, false);
            }
        };
        this.requestCourseAddTask.executeTask(responseReceiver);
    }
}
