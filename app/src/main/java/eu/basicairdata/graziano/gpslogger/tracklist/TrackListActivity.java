package eu.basicairdata.graziano.gpslogger.tracklist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import eu.basicairdata.graziano.gpslogger.EventBusMSG;
import eu.basicairdata.graziano.gpslogger.GPSApplication;
import eu.basicairdata.graziano.gpslogger.R;
import eu.basicairdata.graziano.gpslogger.databinding.ActivityTrackListBinding;
import eu.basicairdata.graziano.gpslogger.management.TrackRegionType;
import eu.basicairdata.graziano.gpslogger.recording.RecordingActivity;
import eu.basicairdata.graziano.gpslogger.management.TrackRecordManager;

public class TrackListActivity extends AppCompatActivity {
    private ActivityTrackListBinding mBind;
    private TrackRecyclerAdapter trackListAdapter;
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

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.mBind.getRoot().getContext());
        this.mBind.trackList.setLayoutManager(linearLayoutManager);

        this.trackListAdapter = new TrackRecyclerAdapter((item, pos) -> {
            Intent intent = new Intent(this, RecordingActivity.class);
            intent.putExtra(GPSApplication.ATX_EXTRA_TRACK_TITLE, item.getTrackName());
            intent.putExtra(GPSApplication.ATV_EXTRA_TRACK_REGION, item.getTrackRegion());
            this.startActivity(intent);
        });
        this.mBind.trackList.setAdapter(this.trackListAdapter);

        ItemTrackData itemTrackData = new ItemTrackData("서울시 대모산 무장애 나눔길", "서울 강남구 일원동 산52-14 일대", TrackRegionType.SEOUL.name());
        this.trackListAdapter.addItem(itemTrackData);

        itemTrackData = new ItemTrackData("곡성 치유의숲 무장애 나눔길", "전남 곡성군 곡성읍 신기리 1177-6", TrackRegionType.JEOLLA_SOUTH.name());
        this.trackListAdapter.addItem(itemTrackData);

//        this.mBind.gotoRecordBtn.setOnClickListener(v -> {
//            Intent intent = new Intent(this, RecordingActivity.class);
//            intent.putExtra(GPSApplication.ATX_EXTRA_TRACK_TITLE, "서울시 대모산 무장애 나눔길");
//            this.startActivity(intent);
//        });
//
//        this.mBind.button.setOnClickListener(v -> {
//            Intent intent = new Intent(this, RecordingActivity.class);
//            intent.putExtra(GPSApplication.ATX_EXTRA_TRACK_TITLE, "곡성 치유의숲 무장애 나눔길");
//            this.startActivity(intent);
//        });

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

        if(this.trackListAdapter != null) {
            this.trackListAdapter.release();
            this.trackListAdapter = null;
        }
        EventBus.getDefault().unregister(this);

        this.mBind = null;
   }

   @Subscribe
   public void onEvent(Short msg) {
        if (msg == EventBusMSG.UPDATE_FIX) {
//            updateUI();
//            Log.w("dspark", "record_list: UPDATE_FIX");
        }
   }

//    private void updateUI() {
//         this.mBind.satelliteCntTxt.setText(String.format("%d/%d %s", this.recordManager.getAvailableSatellitesCnt(), this.recordManager.getTotalSatellitesCnt(), this.getString(R.string.satellites)));
//        if(this.mBind == null || this.recordManager == null) return;
//
//   }
}
