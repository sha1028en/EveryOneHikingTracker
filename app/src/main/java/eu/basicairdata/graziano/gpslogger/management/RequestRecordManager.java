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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.basicairdata.graziano.gpslogger.BuildConfig;
import eu.basicairdata.graziano.gpslogger.recording.enhanced.ItemCourseEnhanced;
import eu.basicairdata.graziano.gpslogger.recording.enhanced.ItemPlaceMarkEnhanced;
import eu.basicairdata.graziano.gpslogger.recording.enhanced.ItemPlaceMarkImg;
import eu.basicairdata.graziano.gpslogger.recording.enhanced.ItemPlaceMarkType;
import kotlinx.coroutines.Dispatchers;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RequestRecordManager {
    private BackGroundAsyncTask<ItemTrackRecord> requestTrackRecordTask;
    private BackGroundAsyncTask<ItemCourseEnhanced> requestCourseRemoveTask;
    private BackGroundAsyncTask<ItemTrackRecord> requestCourseTask;
    private BackGroundAsyncTask<ItemTrackRecord> requestPlaceMarkTask;
    private BackGroundAsyncTask<ItemPlaceMarkImg> requestMoveImgPosTask;
    private BackGroundAsyncTask<ItemPlaceMarkImg> requestRemoveImgTask;

    // callback Listener
    public interface OnRequestResponse<V> {
        void onRequestResponse(final V response, final boolean isSuccess);
    }

    public RequestRecordManager() {
        this.requestTrackRecordTask = new BackGroundAsyncTask<>(Dispatchers.getIO());
        this.requestCourseRemoveTask = new BackGroundAsyncTask<>(Dispatchers.getIO());
        this.requestRemoveImgTask = new BackGroundAsyncTask<>(Dispatchers.getIO());
        this.requestCourseTask = new BackGroundAsyncTask<>(Dispatchers.getIO());
        this.requestMoveImgPosTask = new BackGroundAsyncTask<>(Dispatchers.getIO());
        this.requestPlaceMarkTask = new BackGroundAsyncTask<>(Dispatchers.getIO());
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

//        if (this.requestAddImgTask != null) {
//            this.requestAddImgTask.cancelTask();
//            this.requestAddImgTask = null;
//        }

        if(this.requestRemoveImgTask != null) {
            this.requestRemoveImgTask.cancelTask();
            this.requestRemoveImgTask = null;
        }

        if(this.requestCourseTask != null) {
            this.requestCourseTask.cancelTask();
            this.requestCourseTask = null;
        }

        if(this.requestMoveImgPosTask != null) {
            this.requestMoveImgPosTask.cancelTask();
            this.requestMoveImgPosTask = null;
        }

        if(this.requestPlaceMarkTask != null) {
            this.requestPlaceMarkTask.cancelTask();
            this.requestPlaceMarkTask = null;
        }
    }

    /**
     * REQUEST ADD IMG
     *
     * @param trackId to Add TrackId
     * @param placemarkType to Add PlaceMarkType
     * @param imageFile to Add Image File
     * @param fileName to Add Image Name
     * @param listener callBackListener
     */
    public void requestAddImg(final int trackId,
                              @NonNull final String placemarkType,
                              @NonNull final File imageFile,
                              @NonNull final String fileName,
                              @NonNull final OnRequestResponse<ItemPlaceMarkImg> listener) {
        BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<ItemPlaceMarkImg> responseReceiver = new BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<>() {
            @Override
            public void preTask() {}

            @Override
            public ItemPlaceMarkImg doTask() {
                HttpURLConnection connection = null;
                ItemPlaceMarkImg imgData = null;

                final String boundary = Long.toHexString(System.currentTimeMillis());
                final String crlf = "\r\n";
                final String twoHyphens = "--";

                try {
                    URL serverUrl = new URL(BuildConfig.API_URL + "poi/");
                    connection = (HttpURLConnection) serverUrl.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Connection", "Keep-Alive");
                    connection.setRequestProperty("Authorization", BuildConfig.API_AUTH_KEY);
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
                    Log.w("SEND_IMG", connection.getResponseMessage() + "SEND IMG response Code " + connection.getResponseCode());

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
                        imgData = new ItemPlaceMarkImg(trackId, photoId, placemarkType, imgUrl);
                        imgData.setImgLat(imgLat);
                        imgData.setImgLng(imgLng);

                    }
                } catch (IOException | JSONException | IndexOutOfBoundsException e) {
                    e.printStackTrace();

                } finally {
                    if (connection != null) connection.disconnect();
                }
                return imgData;
            }

            @Override
            public void endTask(ItemPlaceMarkImg value) {
                if (value != null) listener.onRequestResponse(value, true);
                else listener.onRequestResponse(null, false);
            }

            @Override
            public void failTask(@NonNull Throwable throwable) {
                throwable.printStackTrace();
                listener.onRequestResponse(null, false);
            }
        };
        BackGroundAsyncTask<ItemPlaceMarkImg> requestAddImgTask = new BackGroundAsyncTask<>(Dispatchers.getIO());
        requestAddImgTask.executeTask(responseReceiver);
    }

    //

    /**
     * REQUEST REMOVE IMG
     *
     * @param photoId to Remove Placemark Img Id
     * @param listener CallBackListener Nullable
     */
    public void requestRemoveImg(final int photoId, @Nullable RequestTrackManager.OnRequestResponse<Boolean> listener) {
        if(this.requestRemoveImgTask == null) return;
        if(this.requestRemoveImgTask.isTaskAlive()) this.requestRemoveImgTask.cancelTask();
        BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<ItemPlaceMarkImg> responseReceiver = new BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<>() {
            @Override
            public void preTask() {}

            @Override
            public ItemPlaceMarkImg doTask() {
                HttpURLConnection connection = null;
                ItemPlaceMarkImg imgData = null;
                try {
                    URL serverUrl = new URL(BuildConfig.API_URL + "poi/" + photoId);
                    connection = (HttpURLConnection) serverUrl.openConnection();
                    connection.setRequestMethod("DELETE");
                    connection.setRequestProperty("Authorization", BuildConfig.API_AUTH_KEY);
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
                        imgData = new ItemPlaceMarkImg(-1, -1, "", "");
                    }

                } catch (IOException | JSONException | IndexOutOfBoundsException e) {
                    e.printStackTrace();

                } finally {
                    if (connection != null) connection.disconnect();
                }
                return imgData;
            }

            @Override
            public void endTask(ItemPlaceMarkImg value) {
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

    /**
     * request update Placemark's img Position to Server
     *
     * @param photoId to Update Placemark Img Id
     * @param imgLat to Update Placemark Img Lat
     * @param imgLng to Update Placemark Img Lng
     * @param listener CallBackListener
     */
    public void requestMoveImgPos(final int photoId, final double imgLat, final double imgLng, @NonNull RequestRecordManager.OnRequestResponse<ItemPlaceMarkImg> listener) {
        if(this.requestMoveImgPosTask == null) return;
        if(this.requestMoveImgPosTask.isTaskAlive()) this.requestMoveImgPosTask.cancelTask();

        BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<ItemPlaceMarkImg> responseReceiver = new BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<>() {
            @Override
            public void preTask() {
                // NOTHING TO DO
            }

            @Override
            public ItemPlaceMarkImg doTask() {
                OkHttpClient connection = new OkHttpClient();
                final String serverUrl = BuildConfig.API_URL + "poi/" + photoId;
                final JSONObject toSendPhotoLocation = new JSONObject();
                ItemPlaceMarkImg resultImg = null;

                try {
                    toSendPhotoLocation.put("lat", imgLat);
                    toSendPhotoLocation.put("lng", imgLng);

                    Request request = new Request.Builder()
                            .url(serverUrl)
                            .addHeader("Authorization", BuildConfig.API_AUTH_KEY)
                            .post(RequestBody.create(MediaType.parse("application/json"), toSendPhotoLocation.toString()))
                            .build();

                    Response response = connection.newCall(request).execute();
                    JSONObject rawResponse = new JSONObject(response.body().string());
                    rawResponse = rawResponse.getJSONObject("data");

                    final int trackId = rawResponse.getInt("cmrdId");
                    final int photoId = rawResponse.getInt("poiId");
                    final String poiType = rawResponse.getString("poiType");
                    final String imgUrl = rawResponse.getString("imgUrl");
                    final double modifyImgLat = rawResponse.getDouble("imgLat");
                    final double modifyImgLng = rawResponse.getDouble("imgLng");

                    resultImg = new ItemPlaceMarkImg(trackId, photoId, poiType, imgUrl);
                    resultImg.setImgLat(modifyImgLat);
                    resultImg.setImgLng(modifyImgLng);

                } catch (JSONException | IOException | NullPointerException e) {
                    e.printStackTrace();
                }
                return resultImg;
            }

            @Override
            public void endTask(ItemPlaceMarkImg value) {
                if(value == null) listener.onRequestResponse(null, false);
                else listener.onRequestResponse(value, true);
            }

            @Override
            public void failTask(@NonNull Throwable throwable) {
                throwable.printStackTrace();
                listener.onRequestResponse(null, false);
            }
        };
        this.requestMoveImgPosTask.executeTask(responseReceiver);
    }

    /**
     * REQUEST REMOVE COURSE to Server
     *
     * @param item to Remove Course Data Class
     * @param listener CallBackListener Nullable
     */
    public void requestRemoveCourse(@NonNull final ItemCourseEnhanced item, @Nullable final OnRequestResponse<ItemCourseEnhanced> listener) {
        if(this.requestCourseRemoveTask == null) return;
        if(this.requestCourseRemoveTask.isTaskAlive()) this.requestCourseRemoveTask.cancelTask();

        BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<ItemCourseEnhanced> responseReceiver = new BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<>() {
            @Override
            public void preTask() {}

            @Override
            public ItemCourseEnhanced doTask() {

                HttpURLConnection connection;

                try {
                    URL serverUrl = new URL(BuildConfig.API_URL + "gpx/" + item.getCourseId());
                    connection = (HttpURLConnection) serverUrl.openConnection();
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Authorization", BuildConfig.API_AUTH_KEY);
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
            public void endTask(ItemCourseEnhanced value) {
                if (listener != null) listener.onRequestResponse(value, true);
            }

            @Override
            public void failTask(@NonNull Throwable throwable) {
                throwable.printStackTrace();
                if (listener != null) listener.onRequestResponse(null, false);
            }
        };
        this.requestCourseRemoveTask.executeTask(responseReceiver);
    }

    /**
     * request to Update PlaceMark ENABLED State
     *
     * @param trackId to update TrackId
     * @param placeMarkType to Update Placemark Type
     * @param isEnable to Update ENABLED state
     * @param listener CallBackListener Nullable
     */
    public void requestPlacemarkEnableChanged(final int trackId, @NonNull final String placeMarkType, final boolean isEnable, @Nullable OnRequestResponse<Boolean> listener) {

        final BackGroundAsyncTask<Boolean> requestPlacemarkEnableChangeTask = new BackGroundAsyncTask<>(Dispatchers.getIO());
        final BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<Boolean> responseReceiver = new BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<>() {
            @Override
            public void preTask() {
            }

            @Override
            public Boolean doTask() {
                boolean result = false;
                try {
                    OkHttpClient connection = new OkHttpClient();

                    Request requestHeader = new Request.Builder()
                            .url(BuildConfig.API_URL + trackId + "/" + placeMarkType + "/" + (isEnable? "Y": "N"))
                            .method("PUT", new FormBody.Builder().build())
                            .addHeader("Authorization", BuildConfig.API_AUTH_KEY)
                            .addHeader("accept", "*/*")
                            .build();

                    Response response = connection.newCall(requestHeader).execute();
                    JSONObject message = new JSONObject(response.body().string());

                    Log.w("dspark", message.getString("message"));
                    if(message.getString("message").equalsIgnoreCase("OK")) {
                        result = true;
                    }

                } catch (NullPointerException | IOException | JSONException e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            public void endTask(Boolean value) {
                if(listener != null) listener.onRequestResponse(value, value);
            }

            @Override
            public void failTask(@NonNull Throwable throwable) {
                throwable.printStackTrace();
                if(listener != null) listener.onRequestResponse(false, false);
            }
        };
        requestPlacemarkEnableChangeTask.executeTask(responseReceiver);
    }

    /**
     * REQUEST COURSE List
     * @param trackId to Request Track Id
     * @param listener CallBackListener
     */
    public void requestCourse(final int trackId, final long delayMilliSecond, @NonNull final OnRequestResponse<ItemTrackRecord> listener) {
        if(this.requestCourseTask == null) return;
        if(this.requestCourseTask.isTaskAlive()) this.requestCourseTask.cancelTask();

        BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<ItemTrackRecord> responseReceiver = new BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<>() {
            private boolean isSuccess = true;
            @Override
            public void preTask() {}

            @Override
            public ItemTrackRecord doTask() {
                ItemTrackRecord trackRecord = new ItemTrackRecord();
                HttpURLConnection connection = null;

                LinkedList<ItemCourseEnhanced> courseList;

                try {
                    URL serverUrl = new URL(BuildConfig.API_URL + trackId);
                    connection = (HttpURLConnection) serverUrl.openConnection();
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Authorization", BuildConfig.API_AUTH_KEY);
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

                    courseList = parseCourseList(trackName, trackId, rawJsonResponse);

                    trackRecord.setItemCourseList(courseList);

                } catch (IOException | JSONException | IndexOutOfBoundsException e) {
                    e.printStackTrace();
                    this.isSuccess = false;

                } finally {
                    if (connection != null) connection.disconnect();
                }
                return trackRecord;
            }

            @Override
            public void endTask(ItemTrackRecord value) {
                listener.onRequestResponse(value, this.isSuccess);
            }

            @Override
            public void failTask(@NonNull Throwable throwable) {
                throwable.printStackTrace();
                listener.onRequestResponse(null, false);
            }
        };
        this.requestCourseTask.setDelay(delayMilliSecond);
        this.requestCourseTask.executeTask(responseReceiver);
    }


    /**
     * REQUEST TRACKS ALL RECORDS
     *
     * @param trackId to request Track Id
     * @param delayMillis to delay MilliSeconds
     * @param listener CallBackListener
     */
    public void requestTrackRecords(final int trackId, final long delayMillis, @NonNull final OnRequestResponse<ItemTrackRecord> listener) {
        if(this.requestTrackRecordTask == null) return;
        if(this.requestTrackRecordTask.isTaskAlive()) this.requestTrackRecordTask.cancelTask();
        BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<ItemTrackRecord> responseReceiver = new BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<>() {
            @Override
            public void preTask() {}

            @Override
            public ItemTrackRecord doTask() {
                ItemTrackRecord trackRecord = new ItemTrackRecord();
                HttpURLConnection connection = null;

                LinkedList<ItemPlaceMarkEnhanced> placeMarkDataList = new LinkedList<>();
                LinkedList<ItemPlaceMarkType> placeMarkTypeList;
                LinkedList<ItemPlaceMarkImg> placeMarkImgList;
                LinkedList<ItemCourseEnhanced> courseList;

                try {
                    URL serverUrl = new URL(BuildConfig.API_URL + trackId);
                    connection = (HttpURLConnection) serverUrl.openConnection();
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Authorization", BuildConfig.API_AUTH_KEY);
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

                    placeMarkTypeList = parsePlacemarkTypeList(trackId, rawJsonResponse);
                    placeMarkImgList = parsePlacemarkList(trackId, rawJsonResponse);
                    courseList = parseCourseList(trackName, trackId, rawJsonResponse);

                    for (ItemPlaceMarkType placeMarkType : placeMarkTypeList) {
                        ItemPlaceMarkEnhanced placeMark = new ItemPlaceMarkEnhanced(trackName, convertPlacemarkTypeToName(placeMarkType.getPlaceMarkType()), placeMarkType.getPlaceMarkType(), "", placeMarkType.isEnable());

                        for (ItemPlaceMarkImg img : placeMarkImgList) {
                            if(placeMark.getPlaceMarkType().equals(img.getPlaceMarkType())) {
                                placeMark.addPlaceMarkImgItemList(img);
                            }
                        }
                        placeMarkDataList.add(placeMark);
                    }
                    trackRecord.setItemPlaceMarkList(placeMarkDataList);
                    trackRecord.setItemPlaceMarkTypeList(placeMarkTypeList);
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
        this.requestTrackRecordTask.setDelay(delayMillis);
        this.requestTrackRecordTask.executeTask(responseReceiver);
    }

    /**
     * PLACEMARK TYPE TO KOREAN TEXT
     * @param type TO CONVERT Placemark Type
     *
     * @return Converted Korean Text
     */
    private String convertPlacemarkTypeToName(String type) {
        if(type == null) return "기타 시설물";
        final String placemarkType;

        switch (type) {
            case "ENTRANCE" -> placemarkType = "나눔길 입구";
            case "PARKING" -> placemarkType = "주차장";
            case "TOILET" -> placemarkType = "화장실";
            case "REST_AREA" -> placemarkType = "쉼터";
            case "BUS_STOP" -> placemarkType = "버스";
            case "OBSERVATION_DECK" -> placemarkType = "전망대";
            case "ETC" -> placemarkType = "기타 시설물";
            default -> placemarkType = "기타 시설물";
        }
        return placemarkType;
    }

    /**
     * parse Placemark List from RAW Record
     * @param trackId track Id to Record DataClass
     * @param rawRecordJson Raw Record to parse placemark
     *
     * @return Placemark List
     */
    private LinkedList<ItemPlaceMarkImg> parsePlacemarkList(final int trackId, @NonNull final JSONObject rawRecordJson) {
        LinkedList<ItemPlaceMarkImg> placeMarkList = new LinkedList<>();
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

                ItemPlaceMarkImg placemarkImg = new ItemPlaceMarkImg(trackId, photoId, poiType, imgUrl);
                placemarkImg.setImgLat(imgLat);
                placemarkImg.setImgLng(imgLng);
                placeMarkList.add(placemarkImg);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return placeMarkList;
    }

    /**
     * parse Placemark List from RAW Record
     * @param trackId track Id to Record DataClass
     * @param rawRecordJson Raw Record to parse Placemark
     *
     * @return PlaceMark Type List
     */
    private LinkedList<ItemPlaceMarkType> parsePlacemarkTypeList(final int trackId, @NonNull final JSONObject rawRecordJson) {
        LinkedList<ItemPlaceMarkType> placeMarkItemTypeList = new LinkedList<>();
        try {
            JSONArray rawPlaceMarkItemTypeArray = rawRecordJson.getJSONArray("categoryList");
            for(int i = 0; i < rawPlaceMarkItemTypeArray.length(); i++) {

                JSONObject rawPlaceMarkItemTypeBuffer = rawPlaceMarkItemTypeArray.getJSONObject(i);
                final String placeMarkType = rawPlaceMarkItemTypeBuffer.getString("poiType");
                final boolean isEnable = rawPlaceMarkItemTypeBuffer.optString("enabled", "Y").equals("Y");

                final ItemPlaceMarkType placeMarkTypeData = new ItemPlaceMarkType(trackId, placeMarkType, isEnable);
                placeMarkItemTypeList.add(placeMarkTypeData);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            placeMarkItemTypeList.clear();
        }
        return placeMarkItemTypeList;
    }

    /**
     * parse Course List from Raw Record
     *
     * @param trackName TrackName to Record DataClass
     * @param trackId TrackId to Record DataClass
     * @param rawRecordJson to Parse Raw Data
     *
     * @return Placemark list
     */
    private LinkedList<ItemCourseEnhanced> parseCourseList(final String trackName, final int trackId, @NonNull final JSONObject rawRecordJson) {
        LinkedList<ItemCourseEnhanced> courseList = new LinkedList<>();
        try {
            JSONArray rawCourseList = rawRecordJson.getJSONArray("courseList");
            int courseId;
            String courseName;
            String courseType;
            String courseFileUrl;
            double courseDistance;

            Pattern toParseLatPattern = Pattern.compile("<trkpt lat=\"-?[0-9]*(.)[0-9]*\"");
            Pattern toParseLngPattern = Pattern.compile("lon=\"-?[0-9]*(.)[0-9]*\">");

            for (int i = 0; i < rawCourseList.length(); i++) {
                JSONObject courseBuffer = rawCourseList.getJSONObject(i);
                courseId = courseBuffer.getInt("courseId");
                courseName = courseBuffer.getString("courseName");
                courseDistance = courseBuffer.getDouble("courseLength");
                courseType = courseBuffer.getString("courseType");
                courseFileUrl = courseBuffer.getString("courseFileUrl");

                ItemCourseEnhanced course = new ItemCourseEnhanced(trackName, courseName, trackId, courseId, courseDistance, courseType);
                course.setCourseFileUrl(courseFileUrl);

                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .addHeader("accept", "*/*")
                            .url(courseFileUrl)
                            .build();

                    Response response = client.newCall(request).execute();
                    String result = response.body().string();

                    Matcher latMatcher = toParseLatPattern.matcher(result);
                    Matcher lngMatcher = toParseLngPattern.matcher(result);

                    ArrayList<Double> parseLatList = new ArrayList<>();
                    ArrayList<Double> parseLngList = new ArrayList<>();

                    while (latMatcher.find()) {
                        String buf = latMatcher.group().replaceAll("\"", "").replaceAll("<trkpt lat=", "");
                        parseLatList.add(Double.parseDouble(buf)); //floatMatcher.group()));
                    }

                    while (lngMatcher.find()) {
                        String buf = lngMatcher.group().replaceAll("\"", "").replaceAll(">", "").replaceAll("lon=", "");
                        parseLngList.add(Double.parseDouble(buf)); //floatMatcher.group()));
                    }
                    course.setLatList(parseLatList);
                    course.setLngList(parseLngList);

                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                }

                courseList.add(course);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return courseList;
    }
}