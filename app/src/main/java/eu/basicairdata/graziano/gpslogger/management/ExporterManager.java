package eu.basicairdata.graziano.gpslogger.management;

import static android.os.Environment.DIRECTORY_DCIM;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;

import java.io.File;
import java.lang.ref.WeakReference;

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

    public Uri pathToUri(@NonNull final String directoryPath, @NonNull final String directoryName) {
        File dir;
        dir = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM).getAbsolutePath() + "/" + directoryPath + "/" + directoryName + "/");

        if (!dir.exists()) {
            dir.mkdirs();
        }
        return Uri.fromFile(dir);
    }

    public void requestExport(@NonNull final Uri path, @NonNull final String currentTrackName) {


    }

    public void export(@NonNull final Uri path, @NonNull final String currentTrackName) {
        gpsApp.setPrefExportFolder(path.getPath());
        gpsApp.toExportLoadJob(GPSApplication.JOB_TYPE_EXPORT, currentTrackName);
        gpsApp.executeJob();
        gpsApp.deselectAllTracks();
    }
}
