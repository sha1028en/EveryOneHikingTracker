package eu.basicairdata.graziano.gpslogger.management;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

import eu.basicairdata.graziano.gpslogger.recording.enhanced.ItemCourseEnhancedData;
import eu.basicairdata.graziano.gpslogger.recording.enhanced.ItemPlaceMarkImgData;
import kotlinx.coroutines.Dispatchers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RequestRecordManager {
    private BackGroundAsyncTask<ItemTrackRecord> requestTrackRecordTask;
    private BackGroundAsyncTask<ItemCourseEnhancedData> requestCourseRemoveTask;
    private BackGroundAsyncTask<ItemCourseEnhancedData> requestCourseAddTask;
    private BackGroundAsyncTask<ItemPlaceMarkImgData> requestAddImgTask;
    private BackGroundAsyncTask<ItemPlaceMarkImgData> requestRemoveImgTask;
    public interface OnRequestResponse<V> {
        void onRequestResponse(final V response, final boolean isSuccess);
    }

    public RequestRecordManager() {
        this.requestTrackRecordTask = new BackGroundAsyncTask<>(Dispatchers.getIO());
        this.requestCourseRemoveTask = new BackGroundAsyncTask<>(Dispatchers.getIO());
        this.requestAddImgTask = new BackGroundAsyncTask<>(Dispatchers.getIO());
        this.requestRemoveImgTask = new BackGroundAsyncTask<>(Dispatchers.getIO());
        this.requestCourseAddTask = new BackGroundAsyncTask<>(Dispatchers.getIO());
    }

    public void release() {
        if(this.requestTrackRecordTask != null) {
            this.requestTrackRecordTask.cancelTask();
            this.requestTrackRecordTask = null;
        }

        if(this.requestCourseRemoveTask != null) {
            this.requestCourseRemoveTask.cancelTask();
            this.requestCourseRemoveTask = null;
        }

        if (this.requestAddImgTask != null) {
            this.requestAddImgTask.cancelTask();
            this.requestAddImgTask = null;
        }

        if(this.requestRemoveImgTask != null) {
            this.requestRemoveImgTask.cancelTask();
            this.requestRemoveImgTask = null;
        }

        if(this.requestCourseAddTask != null) {
            this.requestCourseAddTask.cancelTask();
            this.requestCourseAddTask = null;
        }
    }
    // REQUEST ADD IMG
    public void requestAddImg(final int trackId,
                              @NonNull final String placemarkType,
                              @NonNull final File imageFile,
                              @NonNull final String fileName,
                              @NonNull final RequestPlaceMarkManager.OnRequestResponse<ItemPlaceMarkImgData> listener) {

        if(this.requestAddImgTask == null) return;
        if(this.requestAddImgTask.isTaskAlive()) this.requestAddImgTask.cancelTask();
        BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<ItemPlaceMarkImgData> responseReceiver = new BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<>() {
            @Override
            public void preTask() {}

            @Override
            public ItemPlaceMarkImgData doTask() {
                HttpURLConnection connection = null;
                ItemPlaceMarkImgData imgData = null;

                final String boundary = Long.toHexString(System.currentTimeMillis());
                final String crlf = "\r\n";
                final String twoHyphens = "--";

                try {
                    URL serverUrl = new URL("http://cmrd-tracker.touring.city/api/cmrd/poi/");
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

                        sendImageStream.writeBytes(twoHyphens + boundary + crlf);
                        String header = "Content-Disposition: form-data; name=\"cmrdId\"" + crlf;
                        sendImageStream.write(header.getBytes(StandardCharsets.UTF_8));
                        sendImageStream.writeBytes(crlf);
                        sendImageStream.write(String.valueOf(trackId).getBytes(StandardCharsets.UTF_8));
                        sendImageStream.writeBytes(crlf);

                        sendImageStream.writeBytes(twoHyphens + boundary + crlf);
                        header = "Content-Disposition: form-data; name=\"poiType\"" + crlf;
                        sendImageStream.write(header.getBytes(StandardCharsets.UTF_8));
                        sendImageStream.writeBytes(crlf);
                        sendImageStream.write(placemarkType.getBytes(StandardCharsets.UTF_8));
                        sendImageStream.writeBytes(crlf);

                        sendImageStream.writeBytes(twoHyphens + boundary + crlf);
                        final String fileHeader = "Content-Disposition: form-data; name=\"file\";" + "filename=\"" + fileName + "\"" + crlf;
                        sendImageStream.write(fileHeader.getBytes(StandardCharsets.UTF_8));
                        sendImageStream.writeBytes(crlf);


                        byte[] toSendImageBuffer = new byte[4 * 1024];
                        int i;
                        try (FileInputStream imageReadStream = new FileInputStream(imageFile)) {
                            while ((i = imageReadStream.read(toSendImageBuffer)) != -1) {
                                sendImageStream.write(toSendImageBuffer, 0, i);
                            }
                        }
                        sendImageStream.writeBytes(crlf);
                        sendImageStream.writeBytes(twoHyphens + boundary + twoHyphens);
                        sendImageStream.flush();
                    }
                    Log.w("dspark", connection.getResponseMessage() + "ADD IMG response Code " + connection.getResponseCode());

                    BufferedReader readStream = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                    String buffer;
                    StringBuilder response = new StringBuilder();
                    try (readStream) {
                        while ((buffer = readStream.readLine()) != null) {
                            response.append(buffer);
                        }
                    }
                    JSONObject responseJson = new JSONObject(response.toString());
                    responseJson = responseJson.getJSONObject("data");

                    final double imgLat = responseJson.optDouble("imgLat", 0.0f);
                    final double imgLng = responseJson.optDouble("imgLng", 0.0f);
                    final String imgUrl = responseJson.optString("imgUrl", "");
                    final int photoId = responseJson.optInt("poiId", -1);

                    if(photoId != -1 && (imgLat != 0.0 || imgLng != 0.0f)) {
                        imgData = new ItemPlaceMarkImgData(trackId, photoId, placemarkType, imgUrl);

                    } else {
                        // avoid NPE!!!
                        imgData = new ItemPlaceMarkImgData(-1, -1, "NONE", "");
                    }

                } catch (IOException | JSONException | IndexOutOfBoundsException e) {
                    e.printStackTrace();

                } finally {
                    if (connection != null) connection.disconnect();
                }
                return imgData;
            }

            @Override
            public void endTask(ItemPlaceMarkImgData value) {
                if (value != null) listener.onRequestResponse(value, true);
                else listener.onRequestResponse(null, false);
            }

            @Override
            public void failTask(@NonNull Throwable throwable) {
                throwable.printStackTrace();
                listener.onRequestResponse(null, false);
            }
        };
        this.requestAddImgTask.executeTask(responseReceiver);
    }

    // REQUEST REMOVE IMG
    public void requestRemoveImg(final int photoId, @Nullable RequestTrackManager.OnRequestResponse<Boolean> listener) {
        if(this.requestRemoveImgTask == null) return;
        if(this.requestRemoveImgTask.isTaskAlive()) this.requestRemoveImgTask.cancelTask();
        BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<ItemPlaceMarkImgData> responseReceiver = new BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<>() {
            @Override
            public void preTask() {}

            @Override
            public ItemPlaceMarkImgData doTask() {
                HttpURLConnection connection = null;
                ItemPlaceMarkImgData imgData = null;
                try {
                    URL serverUrl = new URL("http://cmrd-tracker.touring.city/api/cmrd/poi/" + photoId);
                    connection = (HttpURLConnection) serverUrl.openConnection();
                    connection.setRequestMethod("DELETE");
                    connection.setRequestProperty("Authorization", "anwkddosksnarlf");
                    connection.setRequestProperty("Content-type", "application/json");
                    connection.setRequestProperty("Accept", "*/*");

                    BufferedReader readStream = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                    String buffer;
                    StringBuilder response = new StringBuilder();
                    try (readStream) {
                        while ((buffer = readStream.readLine()) != null) {
                            response.append(buffer);
                        }
                    }
                    JSONObject responseJson = new JSONObject(response.toString());
                    final String responseMsg = responseJson.optString("message", "fail");

                    if(responseMsg.equals("OK")) {
                        imgData = new ItemPlaceMarkImgData(-1, -1, "", "");
                    }

                } catch (IOException | JSONException | IndexOutOfBoundsException e) {
                    e.printStackTrace();

                } finally {
                    if (connection != null) connection.disconnect();
                }
                return imgData;
            }

            @Override
            public void endTask(ItemPlaceMarkImgData value) {
                if (value != null && listener != null) listener.onRequestResponse(true, true);
                else if (listener != null) listener.onRequestResponse(false, false);
            }

            @Override
            public void failTask(@NonNull Throwable throwable) {
                throwable.printStackTrace();
                if(listener != null) listener.onRequestResponse(false, false);
            }
        };
        this.requestRemoveImgTask.executeTask(responseReceiver);
    }


    // REQUEST REMOVE COURSE
    public void requestRemoveCourse(@NonNull final ItemCourseEnhancedData item, @Nullable final OnRequestResponse<ItemCourseEnhancedData> listener) {
        if(this.requestCourseRemoveTask == null) return;
        if(this.requestCourseRemoveTask.isTaskAlive()) this.requestCourseRemoveTask.cancelTask();

        BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<ItemCourseEnhancedData> responseReceiver = new BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<ItemCourseEnhancedData>() {
            @Override
            public void preTask() {}

            @Override
            public ItemCourseEnhancedData doTask() {

                HttpURLConnection connection;

                try {
                    URL serverUrl = new URL("http://cmrd-tracker.touring.city/api/cmrd/gpx/" + item.getCourseId());
                    connection = (HttpURLConnection) serverUrl.openConnection();
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Authorization", "anwkddosksnarlf");
                    connection.setRequestMethod("DELETE");
                    connection.connect();

                    BufferedReader readStream = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                    String buffer;
                    StringBuilder response = new StringBuilder();

                    try (readStream) {
                        while ((buffer = readStream.readLine()) != null) {
                            response.append(buffer);
                        }
                    }
                    JSONObject rawJsonResponse = new JSONObject(response.toString());
                    Log.w("remove course", rawJsonResponse.toString());

                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
                return item;
            }

            @Override
            public void endTask(ItemCourseEnhancedData value) {
                if(listener != null) listener.onRequestResponse(value, true);
            }

            @Override
            public void failTask(@NonNull Throwable throwable) {
                throwable.printStackTrace();
                if(listener != null) listener.onRequestResponse(null, false);
            }
        };
        this.requestCourseRemoveTask.executeTask(responseReceiver);
    }

    // REQUEST TRACK RECORDS LIST
    private String convertPlacemarkTypeToName(String type) {
        String placemarkType = "기타 시설물";

        switch (type) {
            case "ENTRANCE" -> placemarkType = "나눔길 입구";
            case "PARKING" -> placemarkType = "주차장";
            case "TOILET" -> placemarkType = "화장실";
            case "REST_AREA" -> placemarkType = "쉼터";
            case "BUS_STOP" -> placemarkType = "버스";
            case "OBSERVATION_DECK" -> placemarkType = "전망대";
            case "ETC" -> placemarkType = "기타 시설물";
//            default -> placemarkType = "기타 시설물";
        }
        return placemarkType;
    }

    public void requestTrackRecords(final int trackId, @NonNull final OnRequestResponse<ItemTrackRecord> listener) {
        if(this.requestTrackRecordTask == null) return;
        if(this.requestTrackRecordTask.isTaskAlive()) this.requestTrackRecordTask.cancelTask();
        BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<ItemTrackRecord> responseReceiver = new BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<>() {
            @Override
            public void preTask() {}

            @Override
            public ItemTrackRecord doTask() {
                ItemTrackRecord trackRecord = new ItemTrackRecord();
                HttpURLConnection connection = null;

                LinkedList<ItemPlaceMarkImgData> placeMarkImgList;
                LinkedList<ItemCourseEnhancedData> courseList;

                try {
                    URL serverUrl = new URL("http://cmrd-tracker.touring.city/api/cmrd/" + trackId);
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
                    JSONObject rawJsonResponse = new JSONObject(response.toString());
                    rawJsonResponse = rawJsonResponse.getJSONObject("data");

                    final String trackName = rawJsonResponse.getString("name");
                    final int trackId = rawJsonResponse.getInt("cmrdId");

                    placeMarkImgList = parsePlacemarkList(trackName, trackId, rawJsonResponse);
                    courseList = parseCourseList(trackName, trackId, rawJsonResponse);

                    trackRecord.setItemPlacemarkImgList(placeMarkImgList);
                    trackRecord.setItemCourseList(courseList);

                } catch (IOException | JSONException | IndexOutOfBoundsException e) {
                    e.printStackTrace();

                } finally {
                    if (connection != null) connection.disconnect();
                }
                return trackRecord;
            }

            @Override
            public void endTask(ItemTrackRecord value) {
                listener.onRequestResponse(value, true);
            }

            @Override
            public void failTask(@NonNull Throwable throwable) {
                throwable.printStackTrace();
                listener.onRequestResponse(new ItemTrackRecord(), false);
            }
        };
        this.requestTrackRecordTask.executeTask(responseReceiver);
    }

    private LinkedList<ItemPlaceMarkImgData> parsePlacemarkList(final String trackName, final int trackId, @NonNull final JSONObject rawRecordJson) {
        LinkedList<ItemPlaceMarkImgData> placeMarkList = new LinkedList<>();
        try {
            JSONArray rawPlaceMarkList = rawRecordJson.getJSONArray("poiList");
            int photoId;
            String poiType;
            double imgLat;
            double imgLng;
            String imgUrl;

            for (int i = 0; i < rawPlaceMarkList.length(); i++) {
                JSONObject placemarkBuffer = rawPlaceMarkList.getJSONObject(i);
                photoId = placemarkBuffer.getInt("poiId");
                poiType = placemarkBuffer.getString("poiType");
                imgUrl = placemarkBuffer.getString("imgUrl");
                imgLat = placemarkBuffer.optDouble("imgLat", 0.0f);
                imgLng = placemarkBuffer.optDouble("imgLng", 0.0f);

                if(imgLat == 0.0f && imgLng == 0.0f) continue;

                ItemPlaceMarkImgData placemarkImg = new ItemPlaceMarkImgData(trackId, photoId, poiType, imgUrl);
                placemarkImg.setImgLat(imgLat);
                placemarkImg.setImgLng(imgLng);
                placeMarkList.add(placemarkImg);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return placeMarkList;
    }

    private LinkedList<ItemCourseEnhancedData> parseCourseList(final String trackName, final int trackId, @NonNull final JSONObject rawRecordJson) {
        LinkedList<ItemCourseEnhancedData> courseList = new LinkedList<>();
        try {
            JSONArray rawCourseList = rawRecordJson.getJSONArray("courseList");
            int courseId;
            String courseName;
            double courseDistance;
            boolean isWoodDeck;

            for (int i = 0; i < rawCourseList.length(); i++) {
                JSONObject courseBuffer = rawCourseList.getJSONObject(i);
                courseId = courseBuffer.getInt("courseId");
                courseName = courseBuffer.getString("courseName");
                courseDistance = courseBuffer.getDouble("courseLength");
                isWoodDeck = courseBuffer.getString("courseType").equals("wood_deck");
                ItemCourseEnhancedData course = new ItemCourseEnhancedData(trackName, courseName, trackId, courseId, courseDistance, isWoodDeck);
                courseList.add(course);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return courseList;
    }
}