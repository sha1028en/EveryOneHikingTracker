package eu.basicairdata.graziano.gpslogger.tracklist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.LinkedList;

import eu.basicairdata.graziano.gpslogger.GPSApplication;
import eu.basicairdata.graziano.gpslogger.R;
import eu.basicairdata.graziano.gpslogger.databinding.ActivityTrackListBinding;
import eu.basicairdata.graziano.gpslogger.management.RequestTrackManager;
import eu.basicairdata.graziano.gpslogger.management.TrackRecordManager;
import eu.basicairdata.graziano.gpslogger.recording.enhanced.RecordEnhancedActivity;

public class TrackListActivity extends AppCompatActivity {
    private ActivityTrackListBinding bind; // View n Layout Instance
    private TrackRecordManager recordManager; // Track, Course, Placemark control Manager
    private RequestTrackManager requestTrackManager; // request and response Track Data from Server
    private TrackRecyclerAdapter trackListAdapter; // Track List View Adapter ( RecyclerView )

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

        this.recordManager = TrackRecordManager.createInstance(this);
        this.requestTrackManager = new RequestTrackManager();

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        EventBus.getDefault().register(this);
        this.initViewListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.requestTrackList(this.bind.selectRegion.getText().toString());
    }

    private void initViewListener() {
        this.bind.selectRegion.setOnClickListener(v -> {
            ChooseRegionListDialog selectRegionDialog = new ChooseRegionListDialog(this, (msg, viewId) -> {
                if(this.bind == null || this.requestTrackManager == null) return;
                this.bind.selectRegion.setText(msg);
                this.requestTrackList(msg);
            });
            selectRegionDialog.show();
        });

       LinearLayoutManager linearLayoutManager = new LinearLayoutManager(bind.getRoot().getContext());
       this.bind.trackList.setLayoutManager(linearLayoutManager);

       this.trackListAdapter = new TrackRecyclerAdapter((item, pos) -> {
           if(!item.isCompleteTrack) {
               Intent intent = new Intent(bind.getRoot().getContext(), RecordEnhancedActivity.class);
               intent.putExtra(GPSApplication.ATX_EXTRA_TRACK_TITLE, item.getTrackName());
               intent.putExtra(GPSApplication.ATV_EXTRA_TRACK_REGION, item.getTrackRegion());
               intent.putExtra(GPSApplication.ATV_EXTRA_TRACK_ID, (int) item.getTrackId());
               startActivity(intent);

           } else {
               Toast.makeText(this.bind.selectRegion.getContext(), "이미 기록이 완료된 무장애 나눔길 입니다.", Toast.LENGTH_SHORT).show();
           }
       });
       this.bind.trackList.setAdapter(trackListAdapter);
//       this.requestTrackList(TrackRegionType.SEOUL.getRegionName());
   }

   private void requestTrackList(@Nullable final String requestRegion) {
       if(this.requestTrackManager == null || this.bind == null || this.trackListAdapter == null) return;
       this.requestTrackManager.requestTrackList(requestRegion, new RequestTrackManager.OnRequestResponse<>() {
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
   protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        TrackRecordManager.destroyInstance();
        this.recordManager = null;

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
//        if (msg == EventBusMSG.UPDATE_FIX) {
//
//        }
   }
}
