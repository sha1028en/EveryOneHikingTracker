package eu.basicairdata.graziano.gpslogger.management;

import static android.os.Environment.DIRECTORY_DCIM;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import eu.basicairdata.graziano.gpslogger.GPSApplication;
import kotlinx.coroutines.Dispatchers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ExporterManager {
    private GPSApplication gpsApp;
    private WeakReference<Context> localContext;

    public ExporterManager(@NonNull final GPSApplication gpsApplication, @NonNull final Context context) {
        this.gpsApp = gpsApplication;
        this.localContext = new WeakReference<>(context);
    }

    public void release() {
        this.gpsApp = null;
        if(this.localContext != null) {
            this.localContext.clear();
            this.localContext = null;
        }
    }

    public Uri pathToUri(@NonNull final String path) {
        File dir;
        dir = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM).getAbsolutePath() + "/" + path);

        if (!dir.exists()) {
            dir.mkdirs();
        }
        return Uri.fromFile(dir);
    }

    public static Uri pathToUri(@NonNull final String directoryPath, @NonNull final String directoryName) {
        File dir;
        dir = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM).getAbsolutePath() + "/" + directoryPath + "/" + directoryName + "/");

        if (!dir.exists()) {
            dir.mkdirs();
        }
        return Uri.fromFile(dir);
    }

    public static File pathToFile(@NonNull Context context, @NonNull final String directoryPath, @NonNull final String fileName) {
        File file = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM).getPath() + "/" + directoryPath + "/" + fileName);
        return file;
    }


    public void setExportDir(@NonNull final Uri path) {
        if(this.gpsApp != null) gpsApp.setPrefExportFolder(path.toString());
    }

    public boolean isExportDirHas() {
        if(this.gpsApp == null) return false;
        return gpsApp.isExportFolderWritable();
    }

    public void export(@NonNull final String trackName, @NonNull final String courseName) {
        if(this.gpsApp != null) {
            this.gpsApp.toExportLoadJob(GPSApplication.JOB_TYPE_EXPORT, trackName, courseName);
            this.gpsApp.executeJob();
            this.gpsApp.deselectAllTracks();
        }
    }

//    public void parseGpx(@NonNull final String gpxFileUrl) {
//        BackGroundAsyncTask<JSONArray> requestGpxFileParse = new BackGroundAsyncTask<>(Dispatchers.getIO());
//        BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<JSONArray> responseListener = new BackGroundAsyncTask.Companion.BackGroundAsyncTaskListener<>() {
//            @Override
//            public void preTask() {
//
//            }
//
//            @Override
//            public JSONArray doTask() {
//                JSONArray trackPoint = new JSONArray();
//                try {
//                    OkHttpClient client = new OkHttpClient();
//                    Request request = new Request.Builder()
//                            .addHeader("accept", "*/*")
//                            .url(gpxFileUrl)
//                            .build();
//
//                    Response response = client.newCall(request).execute();
//                    String result = response.body().string();
//                    String[] rawPoints = result.split("^<trkpt lat=\"[0-9]+\\.[0-9]+\" lon=\"[0-9]+\\.[0-9]+\">$");
//                    Log.d("dspark", rawPoints.toString());
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                return trackPoint;
//            }
//
//            @Override
//            public void failTask(@NonNull Throwable throwable) {
//
//            }
//
//            @Override
//            public void endTask(JSONArray value) {
//
//            }
//        };
//        requestGpxFileParse.executeTask(responseListener);
//    }
}
