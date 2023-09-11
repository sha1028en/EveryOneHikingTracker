package eu.basicairdata.graziano.gpslogger.management;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

import eu.basicairdata.graziano.gpslogger.GPSApplication;
import eu.basicairdata.graziano.gpslogger.Track;
import eu.basicairdata.graziano.gpslogger.tracklist.ItemTrackData;
import kotlinx.coroutines.Dispatchers;

public class RequestTrackManager {
    private BackGroundAsyncTask<LinkedList<ItemTrackData>> requestTrackListTask;

    private BackGroundAsyncTask<ItemTrackData> requestTrackUploadTask;

//    private WeakReference<Context> localContext;
    public interface OnRequestResponse<V> {
        void onRequestResponse(final V response, final boolean isSuccess);
    }

    public RequestTrackManager(/*@NonNull final Context context*/) {
//        this.localContext = new WeakReference<>(context);
    }

    public void release() {
//        if(this.localContext != null) {
//            this.localContext.clear();
//            this.localContext = null;
//        }

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
     * @param recordManager TrackRecordManager
     * @param listener Response CallBack Listener
     */
    public void requestTrackList(@Nullable final String requestRegion, @NonNull final TrackRecordManager recordManager, @NonNull final OnRequestResponse<LinkedList<ItemTrackData>> listener) {
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
        this.requestTrackListTask = new BackGroundAsyncTask<>(Dispatchers.getIO());
        this.requestTrackListTask.executeTask(responseReceiver);
    }

    public void requestUploadCourseFile(
            final int trackId,
            @NonNull final String courseName,
            @NonNull final String courseType,
            @NonNull final DocumentFile courseFile,
            @NonNull final OnRequestResponse<ItemTrackData> listener) {

        if(this.requestTrackUploadTask != null && this.requestTrackUploadTask.isTaskAlive()) this.requestTrackUploadTask.cancelTask();
        BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<ItemTrackData> responseReceiver = new BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<>() {

            @Override
            public void preTask() {}

            @Override
            public ItemTrackData doTask() {
                HttpURLConnection connection = null;

                final String boundary = Long.toHexString(System.currentTimeMillis());
                final String crlf = "\r\n";
                final String twoHyphens = "--";

                try {
                    URL serverUrl = new URL("http://cmrd-tracker.touring.city/api/upload/gpx/" + trackId + "/files");
                    connection = (HttpURLConnection) serverUrl.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Connection", "Keep-Alive");
                    connection.setRequestProperty("Authorization", "anwkddosksnarlf");
                    connection.setRequestProperty("Content-type", "multipart/form-data;boundary=" + boundary);
                    connection.setRequestProperty("Accept", "*/*");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(10000);
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setUseCaches(false);

                    try (DataOutputStream sendImageStream = new DataOutputStream(connection.getOutputStream())) {

                        // *.GPX Name
                        sendImageStream.writeBytes(twoHyphens + boundary + crlf);
                        final String courseName = "Content-Disposition: form-data; name=\"cmrdCourseDtos[0].courseName\"" + crlf;
                        sendImageStream.writeBytes(crlf);
                        sendImageStream.write(courseName.getBytes(StandardCharsets.UTF_8));
                        sendImageStream.writeBytes(crlf);

                        // *.GPX Course Type
                        sendImageStream.writeBytes(twoHyphens + boundary + crlf);
                        final String courseType = "Content-Disposition: form-data; name=\"cmrdCourseDtos[0].courseType\"" + crlf;
                        sendImageStream.writeBytes(crlf);
                        sendImageStream.write(courseType.getBytes(StandardCharsets.UTF_8));
                        sendImageStream.writeBytes(crlf);

                        // *.GPX BODY
                        sendImageStream.writeBytes(twoHyphens + boundary + crlf);
                        final String fileBody = "Content-Disposition: form-data; name=\"cmrdCourseDtos[0].courseGpx\";" + "filename=\"" + courseName + "\"" + crlf;
                        sendImageStream.write(fileBody.getBytes(StandardCharsets.UTF_8));
                        sendImageStream.writeBytes(crlf);


//                        byte[] toSendImageBuffer = new byte[4 * 1024];
                        String buffer;
                        try (BufferedReader imageReadStream = new BufferedReader(new InputStreamReader(GPSApplication.getInstance().getContentResolver().openInputStream(courseFile.getUri())))) {
                            while ((buffer = imageReadStream.readLine()) != null) {
                                sendImageStream.write(buffer.getBytes(StandardCharsets.UTF_8));
                            }
                        }
                        sendImageStream.writeBytes(crlf);
                        sendImageStream.writeBytes(twoHyphens + boundary + twoHyphens);
                        sendImageStream.flush();
                    }
                    Log.d("dspark", connection.getResponseMessage() + "send Gpx response Code " + connection.getResponseCode());

                    BufferedReader readStream = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                    String buffer;
                    StringBuilder response = new StringBuilder();
                    try (readStream) {
                        while ((buffer = readStream.readLine()) != null) {
                            response.append(buffer);
                        }
                    }
                    JSONObject responseJson = new JSONObject(response.toString());
                    final String isSuccess = responseJson.getString("message");
                    if (isSuccess.equals("OK")) Log.d("dspark", "course *.gpx upload success");

                } catch (IOException | JSONException | IndexOutOfBoundsException e) {
                    e.printStackTrace();

                } finally {
                    if (connection != null) connection.disconnect();
                }
                return null;
            }

            @Override
            public void endTask(ItemTrackData value) {
                listener.onRequestResponse(null, true);
            }

            @Override
            public void failTask(@NonNull Throwable throwable) {
                throwable.printStackTrace();
                listener.onRequestResponse(null, false);
            }

        };
        this.requestTrackUploadTask = new BackGroundAsyncTask<>(Dispatchers.getIO());
        this.requestTrackUploadTask.executeTask(responseReceiver);
    }
}
