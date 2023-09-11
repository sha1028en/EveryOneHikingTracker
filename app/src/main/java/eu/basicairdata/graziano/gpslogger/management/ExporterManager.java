package eu.basicairdata.graziano.gpslogger.management;

import static android.os.Environment.DIRECTORY_DCIM;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import eu.basicairdata.graziano.gpslogger.GPSApplication;

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
}
