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

import eu.basicairdata.graziano.gpslogger.BuildConfig;
import eu.basicairdata.graziano.gpslogger.recording.ItemPlaceMarkData;
import eu.basicairdata.graziano.gpslogger.recording.enhanced.ItemPlaceMarkEnhanced;
import eu.basicairdata.graziano.gpslogger.recording.enhanced.ItemPlaceMarkImg;
import kotlinx.coroutines.Dispatchers;

/**
 * @deprecated new manager {@link RequestRecordManager}
 */
public class RequestPlaceMarkManager {
    private BackGroundAsyncTask<LinkedList<ItemPlaceMarkData>> requestTask; // LEGACY
    private BackGroundAsyncTask<LinkedList<ItemPlaceMarkEnhanced>> requestEnhancedTask;

    private BackGroundAsyncTask<ItemPlaceMarkImg> requestAddImgTask;

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
     * @deprecated not work now...
     * upload Placemark Image to Server
     *
     * @param placeMarkId who is parent this image
     * @param imageFile to Upload Image File
     * @param fileName to Upload Image Name
     * @param listener Response CallBack Listener
     */
    public void requestAddPicturePlaceMark(final int placeMarkId, @NonNull final File imageFile, @NonNull final String fileName, @NonNull final RequestPlaceMarkManager.OnRequestResponse<Integer> listener) {
        throw new RuntimeException("THIS METHOD DIDNT WORK NOW!!!");
//        if(this.requestTask != null && this.requestTask.isTaskAlive()) this.requestTask.cancelTask();
//        BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<LinkedList<ItemPlaceMarkData>> responseReceiver = new BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<>() {
//            @Override
//            public void preTask() {}
//
//            @Override
//            public LinkedList<ItemPlaceMarkData> doTask() {
//                HttpURLConnection connection = null;
//                LinkedList<ItemPlaceMarkData> placeMarkList = new LinkedList<>();
//
//                final String boundary = Long.toHexString(System.currentTimeMillis());
//                final String crlf = "\r\n";
//                final String twoHyphens = "--";
//
//                try {
//                    URL serverUrl = new URL("http://cmrd-tracker.touring.city/api/upload/poi/" + placeMarkId + "/files");
//                    connection = (HttpURLConnection) serverUrl.openConnection();
//                    connection.setRequestMethod("POST");
//                    connection.setRequestProperty("Connection", "Keep-Alive");
//                    connection.setRequestProperty("Authorization", BuildConfig.API_AUTH_KEY);
//                    connection.setRequestProperty("Content-type", "multipart/form-data;boundary=" + boundary);
//                    connection.setRequestProperty("Accept", "*/*");
//                    connection.setConnectTimeout(5000);
//                    connection.setReadTimeout(10000);
//                    connection.setDoOutput(true);
//                    connection.setDoInput(true);
//                    connection.setUseCaches(false);
//
//                    try (DataOutputStream sendImageStream = new DataOutputStream(connection.getOutputStream())) {
//                        sendImageStream.writeBytes(twoHyphens + boundary + crlf);
//                        final String fileHeader = "Content-Disposition: form-data; name=\"cmrdCourseDtos[0].courseGpx\";" + "filename=\"" + fileName + "\"" + crlf;
//                        sendImageStream.write(fileHeader.getBytes(StandardCharsets.UTF_8));
//                        sendImageStream.writeBytes(crlf);
//
//                        byte[] toSendImageBuffer = new byte[4 * 1024];
//                        int i;
//                        try (FileInputStream imageReadStream = new FileInputStream(imageFile)) {
//                            while ((i = imageReadStream.read(toSendImageBuffer)) != -1) {
//                                sendImageStream.write(toSendImageBuffer, 0, i);
//                            }
//                        }
//                        sendImageStream.writeBytes(crlf);
//                        sendImageStream.writeBytes(twoHyphens + boundary + twoHyphens);
//                        sendImageStream.flush();
//                    }
//                    Log.d("dspark", connection.getResponseMessage() + " response Code " + connection.getResponseCode());
//
//                    BufferedReader readStream = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
//                    String buffer;
//                    StringBuilder response = new StringBuilder();
//                    try (readStream) {
//                        while ((buffer = readStream.readLine()) != null) {
//                            response.append(buffer);
//                        }
//                    }
//                    JSONObject responseJson = new JSONObject(response.toString());
//                    final String isSuccess = responseJson.getString("message");
//
//                    if(isSuccess.equals("OK")) {
//                        ItemPlaceMarkData rawPlaceMarkItem = new ItemPlaceMarkData("", "", "", "", true);
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
//            public void endTask(LinkedList<ItemPlaceMarkData> value) {
//                if (!value.isEmpty()) listener.onRequestResponse(0, true);
//                else listener.onRequestResponse(-1, false);
//            }
//
//            @Override
//            public void failTask(@NonNull Throwable throwable) {
//
//            }
//        };
//        this.requestTask.executeTask(responseReceiver);
    }
}