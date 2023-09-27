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

    /**
     * init
     * @param gpsApplication Android Application
     */
    public ExporterManager(@NonNull final GPSApplication gpsApplication) {
        this.gpsApp = gpsApplication;
    }

    /**
     * release this
     */
    public void release() {
        this.gpsApp = null;
    }

    /**
     * convert File Path to Uri
     *
     * @param path to convert File Path
     * @return Uri or NULL
     */
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

    public static File pathToFile(@NonNull final String directoryPath, @NonNull final String fileName) {
        File file = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM).getPath() + "/" + directoryPath + "/" + fileName);
        return file;
    }

    /**
     * @param path set path to export
     */
    public void setExportDir(@NonNull final Uri path) {
        if(this.gpsApp != null) gpsApp.setPrefExportFolder(path.toString());
    }

    /**
     * @return is export path has?
     */
    public boolean isExportDirHas() {
        if(this.gpsApp == null) return false;
        return gpsApp.isExportFolderWritable();
    }

    /**
     * export track to local course record
     * @param trackName to export course's track Name
     * @param courseName to export course Name
     */
    public void export(@NonNull final String trackName, @NonNull final String courseName) {
        if(this.gpsApp != null) {
            this.gpsApp.toExportLoadJob(GPSApplication.JOB_TYPE_EXPORT, trackName, courseName);
            this.gpsApp.executeJob();
            this.gpsApp.deselectAllTracks();
        }
    }
}
