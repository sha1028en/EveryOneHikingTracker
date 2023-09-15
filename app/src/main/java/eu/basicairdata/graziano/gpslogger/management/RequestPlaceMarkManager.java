package eu.basicairdata.graziano.gpslogger.management;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

import eu.basicairdata.graziano.gpslogger.recording.ItemPlaceMarkData;
import eu.basicairdata.graziano.gpslogger.recording.enhanced.ItemPlaceMarkEnhancedData;
import eu.basicairdata.graziano.gpslogger.recording.enhanced.ItemPlaceMarkImgData;
import kotlinx.coroutines.Dispatchers;

/**
 * @deprecated new manager {@link RequestRecordManager}
 */
public class RequestPlaceMarkManager {
    private BackGroundAsyncTask<LinkedList<ItemPlaceMarkData>> requestTask; // LEGACY
    private BackGroundAsyncTask<LinkedList<ItemPlaceMarkEnhancedData>> requestEnhancedTask;

    private BackGroundAsyncTask<ItemPlaceMarkImgData> requestAddImgTask;

    private WeakReference<Context> localContext;
    public interface OnRequestResponse<V> {
        void onRequestResponse(final V response, final boolean isSuccess);
    }

    public RequestPlaceMarkManager(@NonNull final Context context) {
        this.localContext = new WeakReference<>(context);
        this.requestTask = new BackGroundAsyncTask<>(Dispatchers.getIO());
        this.requestEnhancedTask = new BackGroundAsyncTask<>(Dispatchers.getIO());
        this.requestAddImgTask = new BackGroundAsyncTask<>(Dispatchers.getIO());
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

        if(this.requestEnhancedTask != null) {
            this.requestEnhancedTask.cancelTask();
            this.requestEnhancedTask = null;
        }

        if(this.requestAddImgTask != null) {
            this.requestAddImgTask.cancelTask();
            this.requestAddImgTask = null;
        }
    }

    /**
     * reuqest Placemark List if server has
     *
     * @param trackId placemark's TrackId
     * @param trackName placemark's trackName
     * @param listener Response CallBack Listener
     *
     * @deprecated not used function
     */
    public void requestPlaceMarkList(final int trackId,
                                     @NonNull final String trackName,
                                     @NonNull final RequestPlaceMarkManager.OnRequestResponse<LinkedList<ItemPlaceMarkData>> listener) {
        if(this.requestTask != null && this.requestTask.isTaskAlive()) this.requestTask.cancelTask();
        BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<LinkedList<ItemPlaceMarkData>> responseReceiver = new BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<>() {
            @Override
            public void preTask() {}

            @Override
            public LinkedList<ItemPlaceMarkData> doTask() {
                HttpURLConnection connection = null;
                LinkedList<ItemPlaceMarkData> placeMarkList = new LinkedList<>();

                try {
                    URL serverUrl = new URL("http://cmrd-tracker.touring.city/api/cmrd/" + trackId + "/poi");
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
                    JSONArray rawPlaceMarkJsonArray = rawJsonResponse.getJSONArray("data");
                    JSONArray rawPlaceMarkImgJsonArray;
                    int placeMarkId;
                    String placeMarkType;
                    String imageUrl;

                    // Parse Data
                    for(int i = 0; i < rawPlaceMarkJsonArray.length(); i++) {
                        LinkedList<String> imageUrlList = new LinkedList<>();
                        rawJsonResponse = rawPlaceMarkJsonArray.getJSONObject(i);
                        placeMarkId = rawJsonResponse.getInt("poiId");
                        placeMarkType = rawJsonResponse.getString("poiType");

                        rawPlaceMarkImgJsonArray = rawJsonResponse.getJSONArray("poiPhotos");
                        final int photoFront;

                        if(rawPlaceMarkImgJsonArray.length() > 3) {
                            photoFront = rawPlaceMarkImgJsonArray.length() -3;

                        } else {
                            photoFront = 0;
                        }

                        // parse Image
                        for(int j = photoFront; j < rawPlaceMarkImgJsonArray.length(); j++) {
                            rawJsonResponse = rawPlaceMarkImgJsonArray.getJSONObject(j);
                            imageUrl = rawJsonResponse.getString("imgUrl");
                            imageUrlList.add(imageUrl);
                        }

                        ItemPlaceMarkData rawPlaceMarkItem = new ItemPlaceMarkData(trackName, "", placeMarkType, "", true);
                        rawPlaceMarkItem.setToServerId(placeMarkId);
                        rawPlaceMarkItem.setPlaceMarkImgUrl(imageUrlList);
                        placeMarkList.add(rawPlaceMarkItem);
                    }

                } catch (IOException | JSONException | IndexOutOfBoundsException e) {
                    e.printStackTrace();

                } finally {
                    if (connection != null) connection.disconnect();
                }
                return placeMarkList;
            }

            @Override
            public void endTask(LinkedList<ItemPlaceMarkData> value) {
                listener.onRequestResponse(value, true);
            }

            @Override
            public void failTask(@NonNull Throwable throwable) {
                throwable.printStackTrace();
                listener.onRequestResponse(new LinkedList<>(), false);

            }
        };
        this.requestTask.executeTask(responseReceiver);
    }

//    public void requestPlaceMarkListEnhanced(
//            final int trackId,
//            @NonNull final String trackName,
//            @NonNull final RequestPlaceMarkManager.OnRequestResponse<LinkedList<ItemPlaceMarkEnhancedData>> listener) {
//
//        if(this.requestEnhancedTask != null && this.requestEnhancedTask.isTaskAlive()) this.requestEnhancedTask.cancelTask();
//        BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<LinkedList<ItemPlaceMarkEnhancedData>> responseReceiver = new BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<>() {
//            @Override
//            public void preTask() {}
//
//            @Override
//            public LinkedList<ItemPlaceMarkEnhancedData> doTask() {
//                HttpURLConnection connection = null;
//                LinkedList<ItemPlaceMarkEnhancedData> placeMarkList = new LinkedList<>();
//
//                try {
//                    URL serverUrl = new URL("http://cmrd-tracker.touring.city/api/cmrd/" + trackId + "/poi");
//                    connection = (HttpURLConnection) serverUrl.openConnection();
//                    connection.setRequestProperty("Accept", "application/json");
//                    connection.setRequestProperty("Authorization", "anwkddosksnarlf");
//                    connection.setRequestMethod("GET");
//                    connection.connect();
//
//                    BufferedReader readStream = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
//                    String buffer;
//                    StringBuilder response = new StringBuilder();
//                    try (readStream) {
//                        while ((buffer = readStream.readLine()) != null) {
//                            response.append(buffer);
//                        }
//                    }
//                    JSONObject rawJsonResponse = new JSONObject(response.toString());
//                    JSONArray rawPlaceMarkJsonArray = rawJsonResponse.getJSONArray("data");
//                    JSONArray rawPlaceMarkImgJsonArray;
//
//                    int placeMarkId; // placemark UNIQUE id
//                    String placeMarkType; // placemark facility Type
//
//                    // Parse Data
//                    for(int i = 0; i < rawPlaceMarkJsonArray.length(); i++) {
//                        LinkedList<ItemPlaceMarkImgData> imgDataList = new LinkedList<>();
//                        rawJsonResponse = rawPlaceMarkJsonArray.getJSONObject(i);
//                        placeMarkId = rawJsonResponse.getInt("poiId");
//                        placeMarkType = rawJsonResponse.getString("poiType");
//                        rawPlaceMarkImgJsonArray = rawJsonResponse.getJSONArray("poiPhotos");
//
//                        // parse Images
//                        for(int j = 0; j < rawPlaceMarkImgJsonArray.length(); j++) {
//                            rawJsonResponse = rawPlaceMarkImgJsonArray.getJSONObject(j);
//                            final int imageId = rawJsonResponse.getInt("photoId");
//                            final String imageUrl = rawJsonResponse.optString("imgUrl", "");
//                            final double imageLat = rawJsonResponse.optDouble("imgLat", 0.0f);
//                            final double imageLng = rawJsonResponse.optDouble("imgLng", 0.0f);
//
//                            // when not valid placemark record? ignore them!
//                            if(imageLat == 0.0f && imageLng == 0.0f) continue;
//                            ItemPlaceMarkImgData imgData = new ItemPlaceMarkImgData(placeMarkId, imageId, placeMarkType, imageUrl);
//                            imgData.setImgLat(imageLat);
//                            imgData.setImgLng(imageLng);
//                            imgDataList.add(imgData);
//                        }
//
//                        ItemPlaceMarkEnhancedData rawPlaceMarkItem = new ItemPlaceMarkEnhancedData(trackName, getNameByPlacemarkTypeEnhanced(placeMarkType), placeMarkType, "", true);
//                        rawPlaceMarkItem.setPlaceMarkId(placeMarkId);
//                        rawPlaceMarkItem.setPlaceMarkImgItemList(imgDataList);
//                        placeMarkList.add(rawPlaceMarkItem);
//                    }
//
//                } catch (IOException | JSONException | IndexOutOfBoundsException e) {
//                    e.printStackTrace();
//
//                } finally {
//                    if (connection != null) connection.disconnect();
//                }
//                return placeMarkList;
//            }
//
//            @Override
//            public void endTask(LinkedList<ItemPlaceMarkEnhancedData> value) {
//                listener.onRequestResponse(value, true);
//            }
//
//            @Override
//            public void failTask(@NonNull Throwable throwable) {
//                throwable.printStackTrace();
//                listener.onRequestResponse(new LinkedList<>(), false);
//
//            }
//        };
//        this.requestEnhancedTask.executeTask(responseReceiver);
//    }

    private String getNameByPlacemarkType(PlaceMarkType type) {
        String placemarkType = "기타 시설물 1";

        switch (type) {
            case ENTRANCE -> placemarkType = "나눔길 입구";
            case PARKING -> placemarkType = "주차장";
            case TOILET -> placemarkType = "화장실 01";
            case REST_AREA -> placemarkType = "쉼터 01";
            case BUS_STOP -> placemarkType = "버스";
            case OBSERVATION_DECK -> placemarkType = "전망대 01";
            case ETC -> placemarkType = "기타 시설물 01";
//            default -> placemarkType = "기타 시설물 1";
        }
        return placemarkType;
    }

    private String getNameByPlacemarkTypeEnhanced(PlaceMarkType type) {
        String placemarkType = "기타 시설물";

        switch (type) {
            case ENTRANCE -> placemarkType = "나눔길 입구";
            case PARKING -> placemarkType = "주차장";
            case TOILET -> placemarkType = "화장실";
            case REST_AREA -> placemarkType = "쉼터";
            case BUS_STOP -> placemarkType = "버스";
            case OBSERVATION_DECK -> placemarkType = "전망대";
            case ETC -> placemarkType = "기타 시설물";
//            default -> placemarkType = "기타 시설물";
        }
        return placemarkType;
    }

    private String getNameByPlacemarkTypeEnhanced(String type) {
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


    /**
     * request EMPTY Placemark Item from Server
     * @deprecated
     *
     * @param trackId this placemark's trackId
     * @param trackName this placemark's trackName
     * @param placeMarKTypeList what kind of types request
     * @param listener Response CallBack Listener
     */
    public void requestAddEmptyPlaceMarkList(final int trackId, @NonNull final String trackName, @NonNull final LinkedList<PlaceMarkType> placeMarKTypeList, @NonNull final RequestPlaceMarkManager.OnRequestResponse<LinkedList<ItemPlaceMarkData>> listener) {
        if(this.requestTask != null && this.requestTask.isTaskAlive()) this.requestTask.cancelTask();
        BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<LinkedList<ItemPlaceMarkData>> responseReceiver = new BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<>() {
            @Override
            public void preTask() {}

            @Override
            public LinkedList<ItemPlaceMarkData> doTask() {
                HttpURLConnection connection = null;
                LinkedList<ItemPlaceMarkData> placeMarkList = new LinkedList<>();

                for(PlaceMarkType type : placeMarKTypeList) {
                    try {
                        JSONObject toSendEmptyPlaceMark = new JSONObject();
                        toSendEmptyPlaceMark.put("brief", "string");
                        toSendEmptyPlaceMark.put("contents", new JSONObject());
                        toSendEmptyPlaceMark.put("poiType", type);

                        URL serverUrl = new URL("http://cmrd-tracker.touring.city/api/cmrd/" + trackId + "/poi");
                        connection = (HttpURLConnection) serverUrl.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Authorization", "anwkddosksnarlf");
                        connection.setRequestProperty("Content-Type", "application/json;");
                        connection.setRequestProperty("accept", "*/*");
                        connection.setDoOutput(true);
                        connection.setDoInput(true);

                        try(OutputStream requestStream = connection.getOutputStream()) {
                            requestStream.write(toSendEmptyPlaceMark.toString().getBytes(StandardCharsets.UTF_8));
                            requestStream.flush();
                        }
                        Log.d("dspark", connection.getResponseMessage() + " response Code " + connection.getResponseCode());

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
                        final int poiId = responseJson.getInt("poiId");
                        final String placeMarkTitle = getNameByPlacemarkType(type);

                        ItemPlaceMarkData rawPlaceMarkItem = new ItemPlaceMarkData(trackName, placeMarkTitle, type.name(), "", true);
                        rawPlaceMarkItem.setToServerId(poiId);
                        placeMarkList.add(rawPlaceMarkItem);

                    } catch (IOException | JSONException | IndexOutOfBoundsException e) {
                        e.printStackTrace();

                    } finally {
                        if (connection != null) connection.disconnect();
                    }
                }
                return placeMarkList;
            }

            @Override
            public void endTask(LinkedList<ItemPlaceMarkData> value) {
                if (!value.isEmpty()) listener.onRequestResponse(value, true);
                else listener.onRequestResponse(new LinkedList<>(), false);
            }

            @Override
            public void failTask(@NonNull Throwable throwable) {

            }
        };
        this.requestTask.executeTask(responseReceiver);
    }

//    /**
//     * @deprecated
//     * @param trackId
//     * @param trackName
//     * @param placeMarKTypeList
//     * @param listener
//     */
//    public void requestAddEmptyPlaceMarkListEnhanced(
//            final int trackId,
//            @NonNull final String trackName,
//            @NonNull final LinkedList<PlaceMarkType> placeMarKTypeList,
//            @NonNull final RequestPlaceMarkManager.OnRequestResponse<LinkedList<ItemPlaceMarkEnhancedData>> listener) {
//
//        if(this.requestEnhancedTask != null && this.requestEnhancedTask.isTaskAlive()) this.requestEnhancedTask.cancelTask();
//        BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<LinkedList<ItemPlaceMarkEnhancedData>> responseReceiver = new BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<>() {
//            @Override
//            public void preTask() {}
//
//            @Override
//            public LinkedList<ItemPlaceMarkEnhancedData> doTask() {
//                HttpURLConnection connection = null;
//                LinkedList<ItemPlaceMarkEnhancedData> placeMarkList = new LinkedList<>();
//
//                for(PlaceMarkType type : placeMarKTypeList) {
//                    try {
//                        JSONObject toSendEmptyPlaceMark = new JSONObject();
//                        toSendEmptyPlaceMark.put("brief", "string");
//                        toSendEmptyPlaceMark.put("contents", new JSONObject());
//                        toSendEmptyPlaceMark.put("poiType", type);
//
//                        URL serverUrl = new URL("http://cmrd-tracker.touring.city/api/cmrd/" + trackId + "/poi");
//                        connection = (HttpURLConnection) serverUrl.openConnection();
//                        connection.setRequestMethod("POST");
//                        connection.setRequestProperty("Authorization", "anwkddosksnarlf");
//                        connection.setRequestProperty("Content-Type", "application/json;");
//                        connection.setRequestProperty("accept", "*/*");
//                        connection.setDoOutput(true);
//                        connection.setDoInput(true);
//
//                        try(OutputStream requestStream = connection.getOutputStream()) {
//                            requestStream.write(toSendEmptyPlaceMark.toString().getBytes(StandardCharsets.UTF_8));
//                            requestStream.flush();
//                        }
//                        Log.d("dspark", connection.getResponseMessage() + " response Code " + connection.getResponseCode());
//
//                        BufferedReader readStream = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
//                        String buffer;
//                        StringBuilder response = new StringBuilder();
//                        try (readStream) {
//                            while ((buffer = readStream.readLine()) != null) {
//                                response.append(buffer);
//                            }
//                        }
//                        JSONObject responseJson = new JSONObject(response.toString());
//                        responseJson = responseJson.getJSONObject("data");
//                        final int poiId = responseJson.getInt("poiId");
//                        final String placeMarkTitle = getNameByPlacemarkTypeEnhanced(type);
//
//                        ItemPlaceMarkEnhancedData rawPlaceMarkItem = new ItemPlaceMarkEnhancedData(trackName, placeMarkTitle, type.name(), "", true);
//                        rawPlaceMarkItem.setPlaceMarkId(poiId);
//                        placeMarkList.add(rawPlaceMarkItem);
//
//                    } catch (IOException | JSONException | IndexOutOfBoundsException e) {
//                        e.printStackTrace();
//
//                    } finally {
//                        if (connection != null) connection.disconnect();
//                    }
//                }
//                return placeMarkList;
//            }
//
//            @Override
//            public void endTask(LinkedList<ItemPlaceMarkEnhancedData> value) {
//                if (!value.isEmpty()) listener.onRequestResponse(value, true);
//                else listener.onRequestResponse(new LinkedList<>(), false);
//            }
//
//            @Override
//            public void failTask(@NonNull Throwable throwable) {
//
//            }
//        };
//        this.requestEnhancedTask.executeTask(responseReceiver);
//    }


    /**
     * upload Placemark Image to Server
     *
     * @param placeMarkId who is parent this image
     * @param imageFile to Upload Image File
     * @param fileName to Upload Image Name
     * @param listener Response CallBack Listener
     */
    public void requestAddPicturePlaceMark(final int placeMarkId, @NonNull final File imageFile, @NonNull final String fileName, @NonNull final RequestPlaceMarkManager.OnRequestResponse<Integer> listener) {
        if(this.requestTask != null && this.requestTask.isTaskAlive()) this.requestTask.cancelTask();
        BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<LinkedList<ItemPlaceMarkData>> responseReceiver = new BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<>() {
            @Override
            public void preTask() {}

            @Override
            public LinkedList<ItemPlaceMarkData> doTask() {
                HttpURLConnection connection = null;
                LinkedList<ItemPlaceMarkData> placeMarkList = new LinkedList<>();

                final String boundary = Long.toHexString(System.currentTimeMillis());
                final String crlf = "\r\n";
                final String twoHyphens = "--";

                try {
                    URL serverUrl = new URL("http://cmrd-tracker.touring.city/api/upload/poi/" + placeMarkId + "/files");
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
                        final String fileHeader = "Content-Disposition: form-data; name=\"cmrdCourseDtos[0].courseGpx\";" + "filename=\"" + fileName + "\"" + crlf;
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
                    Log.d("dspark", connection.getResponseMessage() + " response Code " + connection.getResponseCode());

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

                    if(isSuccess.equals("OK")) {
                        ItemPlaceMarkData rawPlaceMarkItem = new ItemPlaceMarkData("", "", "", "", true);
                        placeMarkList.add(rawPlaceMarkItem);
                    }

                } catch (IOException | JSONException | IndexOutOfBoundsException e) {
                    e.printStackTrace();

                } finally {
                    if (connection != null) connection.disconnect();
                }
                return placeMarkList;
            }

            @Override
            public void endTask(LinkedList<ItemPlaceMarkData> value) {
                if (!value.isEmpty()) listener.onRequestResponse(0, true);
                else listener.onRequestResponse(-1, false);
            }

            @Override
            public void failTask(@NonNull Throwable throwable) {

            }
        };
        this.requestTask.executeTask(responseReceiver);
    }

    public void requestAddPicturePlaceMarkEnhanced(final int placeMarkId,
                                                   @NonNull final String placemarkType,
                                                   @NonNull final File imageFile,
                                                   @NonNull final String fileName,
                                                   @NonNull final RequestPlaceMarkManager.OnRequestResponse<ItemPlaceMarkImgData> listener) {

        if(this.requestAddImgTask != null && this.requestAddImgTask.isTaskAlive()) this.requestEnhancedTask.cancelTask();
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
                    URL serverUrl = new URL("http://cmrd-tracker.touring.city/api/upload/poi/" + placeMarkId + "/files");
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
                        final String fileHeader = "Content-Disposition: form-data; name=\"files\";" + "filename=\"" + fileName + "\"" + crlf;
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
//                    Log.d("dspark", connection.getResponseMessage() + " response Code " + connection.getResponseCode());

                    BufferedReader readStream = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                    String buffer;
                    StringBuilder response = new StringBuilder();
                    try (readStream) {
                        while ((buffer = readStream.readLine()) != null) {
                            response.append(buffer);
                        }
                    }
                    JSONObject responseJson = new JSONObject(response.toString());
                    JSONArray responseJsonArray = responseJson.optJSONArray("data");
                    responseJson = responseJsonArray.getJSONObject(0);

                    final double imgLat = responseJson.optDouble("imgLat", 0.0f);
                    final double imgLng = responseJson.optDouble("imgLng", 0.0f);
                    final String imgUrl = responseJson.optString("imgUrl", "");
                    final int photoId = responseJson.optInt("photoId", -1);

                    if(photoId != -1 && (imgLat != 0.0 || imgLng != 0.0f)) {
                        imgData = new ItemPlaceMarkImgData(-1, photoId, placemarkType, imgUrl);
                        // TODO PARSE RAW TO IMAGE DATA CLASS
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

    public void requestRemovePicture(final int photoId, @NonNull final RequestTrackManager.OnRequestResponse<Boolean> listener) {
        if(this.requestAddImgTask != null && this.requestAddImgTask.isTaskAlive()) this.requestEnhancedTask.cancelTask();
        BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<ItemPlaceMarkImgData> responseReceiver = new BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<>() {
            @Override
            public void preTask() {}

            @Override
            public ItemPlaceMarkImgData doTask() {
                HttpURLConnection connection = null;
                ItemPlaceMarkImgData imgData = null;
                try {
                    URL serverUrl = new URL("http://cmrd-tracker.touring.city/api/cmrd/poi/files/" + photoId);
                    connection = (HttpURLConnection) serverUrl.openConnection();
                    connection.setRequestMethod("DELETE");
                    connection.setRequestProperty("Authorization", "anwkddosksnarlf");
                    connection.setRequestProperty("Content-type", "application/json");
                    connection.setRequestProperty("Accept", "*/*");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(10000);

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
                if (value != null) listener.onRequestResponse(true, true);
                else listener.onRequestResponse(false, false);
            }

            @Override
            public void failTask(@NonNull Throwable throwable) {
                throwable.printStackTrace();
                listener.onRequestResponse(false, false);
            }
        };
        this.requestAddImgTask.executeTask(responseReceiver);
    }
}