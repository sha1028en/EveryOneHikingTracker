package eu.basicairdata.graziano.gpslogger.tracklist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import eu.basicairdata.graziano.gpslogger.EventBusMSG;
import eu.basicairdata.graziano.gpslogger.GPSApplication;
import eu.basicairdata.graziano.gpslogger.R;
import eu.basicairdata.graziano.gpslogger.databinding.ActivityTrackListBinding;
import eu.basicairdata.graziano.gpslogger.recording.RecordingActivity;
import eu.basicairdata.graziano.gpslogger.management.TrackRecordManager;

public class TrackListActivity extends AppCompatActivity {
    private ActivityTrackListBinding mBind;
    private TrackRecordManager recordManager;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setTheme(R.style.MyMaterialTheme);
        super.onCreate(savedInstanceState);

        this.mBind = ActivityTrackListBinding.inflate(this.getLayoutInflater());
        setContentView(this.mBind.getRoot());
        this.recordManager = TrackRecordManager.createInstance(this);

        this.initViewListener();

        this.mBind.gotoRecordBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, RecordingActivity.class);
            intent.putExtra(GPSApplication.ATX_EXTRA_TRACK_TITLE, "서울시 대모산 무장애 나눔길");
            this.startActivity(intent);
        });

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        EventBus.getDefault().register(this);

   }

   private void initViewListener() {
        this.mBind.selectRegion.setOnClickListener(v -> {
            ChooseRegionListDialog selectRegionDialog = new ChooseRegionListDialog(this, (msg, viewId) -> {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            });
            selectRegionDialog.show();
        });
   }


   @Override
   protected void onDestroy() {
        super.onDestroy();
        TrackRecordManager.destroyInstance();
        this.recordManager = null;

        EventBus.getDefault().unregister(this);

        this.mBind = null;
   }

   @Subscribe
   public void onEvent(Short msg) {
        if (msg == EventBusMSG.UPDATE_FIX) {
            updateUI();
//            Log.w("dspark", "record_list: UPDATE_FIX");
        }
   }

    private void updateUI() {
        // this.mBind.satelliteCntTxt.setText(String.format("%d/%d %s", this.recordManager.getAvailableSatellitesCnt(), this.recordManager.getTotalSatellitesCnt(), this.getString(R.string.satellites)));
        if(this.mBind == null || this.recordManager == null) return;

   }
}
