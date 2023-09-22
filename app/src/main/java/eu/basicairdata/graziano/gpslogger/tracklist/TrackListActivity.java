package eu.basicairdata.graziano.gpslogger.tracklist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedList;

import eu.basicairdata.graziano.gpslogger.EventBusMSG;
import eu.basicairdata.graziano.gpslogger.GPSApplication;
import eu.basicairdata.graziano.gpslogger.R;
import eu.basicairdata.graziano.gpslogger.databinding.ActivityTrackListBinding;
import eu.basicairdata.graziano.gpslogger.management.ExporterManager;
import eu.basicairdata.graziano.gpslogger.management.data.ItemCourseUploadQueue;
import eu.basicairdata.graziano.gpslogger.management.OnRequestResponse;
import eu.basicairdata.graziano.gpslogger.management.RequestTrackManager;
import eu.basicairdata.graziano.gpslogger.management.TrackRecordManager;
import eu.basicairdata.graziano.gpslogger.management.define.TrackRegionType;
import eu.basicairdata.graziano.gpslogger.recording.RecordEnhancedActivity;

public class TrackListActivity extends AppCompatActivity {
    private ActivityTrackListBinding bind; // View n Layout Instance

    private TrackRecordManager recordManager; // Track, Course, Placemark control Manager
    private RequestTrackManager requestTrackManager; // request and response Track Data from Server
    private TrackRecyclerAdapter trackListAdapter; // Track List View Adapter ( RecyclerView )
    private ExporterManager exporterManager;

    private ActivityResultLauncher<Intent> requestExportDir; // Write Document;
    private LinkedList<ItemCourseUploadQueue> toFirstUploadCourseList;
    private String selectedRegion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setTheme(R.style.MyMaterialTheme);
        super.onCreate(savedInstanceState);

        this.bind = ActivityTrackListBinding.inflate(this.getLayoutInflater());
        setContentView(this.bind.getRoot());

//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if(ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
//
//            }
//        }
        this.toFirstUploadCourseList = new LinkedList<>();

        // Request Perm to write GPX File
        this.requestExportDir = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result == null || this.bind == null || this.exporterManager == null || this.recordManager == null) return;

            Intent resultData = result.getData();
            if (resultData != null) {
                Uri treeUri = resultData.getData();
                grantUriPermission(getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                GPSApplication.getInstance().getContentResolver().takePersistableUriPermission(treeUri, Intent
                        .FLAG_GRANT_READ_URI_PERMISSION | Intent
                        .FLAG_GRANT_WRITE_URI_PERMISSION);
                // set Path where save *.gpx file
                this.exporterManager.setExportDir(treeUri);

                for (ItemCourseUploadQueue toUploadCourse : this.toFirstUploadCourseList) {
                    this.exporterManager.export(toUploadCourse.getTrackName(), toUploadCourse.getCourseName());
                }
            }
        });

        // init last selected region
        this.loadLastRegion();
        this.bind.selectRegion.setText(this.selectedRegion);

        this.recordManager = TrackRecordManager.createInstance(this);
        this.requestTrackManager = new RequestTrackManager();
        this.exporterManager = new ExporterManager(GPSApplication.getInstance(), this.bind.getRoot().getContext());
        this.initViewListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        EventBus.getDefault().register(this);
        this.requestTrackList(this.selectedRegion);
    }

    private void loadLastRegion() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        this.selectedRegion = preferences.getString(GPSApplication.ATV_EXTRA_TRACK_REGION, TrackRegionType.SEOUL.getRegionName());
    }

    private void putLastRegion(@NonNull final String lastSelectRegion) {
        SharedPreferences.Editor lastRegionEditor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        lastRegionEditor.putString(GPSApplication.ATV_EXTRA_TRACK_REGION, lastSelectRegion);
        lastRegionEditor.apply();
    }

    private void initViewListener() {
        this.bind.selectRegion.setOnClickListener(v -> {
            ChooseRegionListDialog selectRegionDialog = new ChooseRegionListDialog(this, (msg, viewId) -> {
                if(this.bind == null || this.requestTrackManager == null) return;
                this.bind.selectRegion.setText(msg);
                this.requestTrackList(msg);
                this.selectedRegion = msg;
            });
            selectRegionDialog.show();
        });

       LinearLayoutManager linearLayoutManager = new LinearLayoutManager(bind.getRoot().getContext());
       this.bind.trackList.setLayoutManager(linearLayoutManager);

       this.trackListAdapter = new TrackRecyclerAdapter((item, pos, actionType) -> {
           switch (actionType) {
               case 0 -> { // Goto record Enhanced Activity
                   if (!item.isCompleteTrack()) {
                       Intent intent = new Intent(bind.getRoot().getContext(), RecordEnhancedActivity.class);
                       intent.putExtra(GPSApplication.ATX_EXTRA_TRACK_TITLE, item.getTrackName());
                       intent.putExtra(GPSApplication.ATV_EXTRA_TRACK_REGION, item.getTrackRegion());
                       intent.putExtra(GPSApplication.ATV_EXTRA_TRACK_ID, (int) item.getTrackId());
                       startActivity(intent);

                   } else {
                       Toast.makeText(this.bind.selectRegion.getContext(), "이미 기록이 완료된 무장애 나눔길 입니다.", Toast.LENGTH_SHORT).show();
                   }
               }

               case 1 -> { // upload left Course
                   if(item.getUploadCourseList().isEmpty() || this.exporterManager == null) return;

                   if(this.exporterManager.isExportDirHas()) { // if already has Perm? Export NOW!
                       for (ItemCourseUploadQueue toUploadCourse : item.getUploadCourseList()) {
                           this.exporterManager.export(toUploadCourse.getTrackName(), toUploadCourse.getCourseName());
                       }

                   } else { // request Perm With File Picker by Android System
                       Intent intent = new Intent();
                       intent.setAction(Intent.ACTION_OPEN_DOCUMENT_TREE);
                       intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
                       intent.putExtra("android.content.extra.FANCY", true);
                       intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                               | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                               | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

                       // where *.gpx file save? where!
                       if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                           intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, exporterManager.pathToUri("Trekking"));
                       }
                       this.toFirstUploadCourseList = item.getUploadCourseList();
                       this.requestExportDir.launch(intent);
                   }
               }
           }
       });
       this.bind.trackList.setAdapter(trackListAdapter);
   }

   private void requestTrackList(@Nullable final String requestRegion) {
       if(this.requestTrackManager == null || this.bind == null || this.trackListAdapter == null) return;
       this.requestTrackManager.requestTrackList(requestRegion, new OnRequestResponse<>() {
           @Override
           public void onRequestResponse(LinkedList<ItemTrackData> response, boolean isSuccess) {
               if (isSuccess) {
                   runOnUiThread(() -> {
                       if (bind == null || trackListAdapter == null) return;
                       trackListAdapter.clearItems();
                       trackListAdapter.addItems(response);
                   });

               } else {
                   runOnUiThread(() -> {
                       Toast.makeText(TrackListActivity.this, "서버와 연결이 실패했습니다. 다시 시도해 보세요.", Toast.LENGTH_SHORT).show();
                   });
               }
           }
       });
   }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
   protected void onDestroy() {
        super.onDestroy();
        TrackRecordManager.destroyInstance();
        this.recordManager = null;

        this.putLastRegion(this.selectedRegion);

        if(this.trackListAdapter != null) {
            this.trackListAdapter.release();
            this.trackListAdapter = null;
        }

        if(this.requestTrackManager != null) {
            this.requestTrackManager.release();
            this.requestTrackManager = null;
        }
        this.bind = null;
   }

   @Subscribe(threadMode = ThreadMode.BACKGROUND)
   public void onEvent(Short msg) {

        if (msg == EventBusMSG.TRACK_COURSE_SEND_SUCCESS) {
            this.requestTrackList(this.selectedRegion);
        }
   }
}
