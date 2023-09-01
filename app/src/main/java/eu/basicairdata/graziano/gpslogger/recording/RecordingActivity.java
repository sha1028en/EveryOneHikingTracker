package eu.basicairdata.graziano.gpslogger.recording;

import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_DRAGGING;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HALF_EXPANDED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_SETTLING;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.LinkedList;

import eu.basicairdata.graziano.gpslogger.EventBusMSG;
import eu.basicairdata.graziano.gpslogger.GPSApplication;
import eu.basicairdata.graziano.gpslogger.LocationExtended;
import eu.basicairdata.graziano.gpslogger.R;
import eu.basicairdata.graziano.gpslogger.Track;
import eu.basicairdata.graziano.gpslogger.databinding.ActivityRecordingBinding;
import eu.basicairdata.graziano.gpslogger.management.ImageManager;
import eu.basicairdata.graziano.gpslogger.management.TrackRecordManager;

public class RecordingActivity extends AppCompatActivity {
    private ActivityRecordingBinding bind; // this View n Layout Instance
    private PlacemarkTypeRecyclerViewAdapter placeMarkListAdapter; // POI list
    private CourseNameRecyclerAdapter courseRecyclerAdapter; // upside Course list

    private ActivityResultLauncher<Intent> requestCamera; // Camera
    private Uri tmpFile; // taken a Picture's File Instance

    private TrackRecordManager recordManager = TrackRecordManager.getInstance(); // Track, Course, POI control Manager.
    private boolean isPauseCourseRecording = false; // this is FLAG course recording has paused

    private String currentTrackName = ""; // this course's Parents Track Name
    private String currentTrackRegion; // this course Region
    private String currentPoiType = ""; // selected POI Type
    private String currentPoiName = ""; // selected POI Name
    private boolean currentPoiEnable = true; // is POI enabled( POI's checkbox )
    private int currentPoiPosition = 0; // this poi position that selected picture
    private ItemPlaceMarkData currentSelectedPlaceMarkItem; // selected POI's DataClass

    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setTheme(R.style.MyMaterialTheme);
        super.onCreate(savedInstanceState);

        this.bind = ActivityRecordingBinding.inflate(this.getLayoutInflater());
        setContentView(this.bind.getRoot());

        toast = new Toast(this.bind.getRoot().getContext());
        // FINAL VALUE, NEVER MODIFY THIS
        this.currentTrackName = this.getIntent().getStringExtra(GPSApplication.ATX_EXTRA_TRACK_TITLE);
        this.currentTrackRegion = this.getIntent().getStringExtra(GPSApplication.ATV_EXTRA_TRACK_REGION);

        // register GPS Event
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        EventBus.getDefault().register(this);


        // Camera Action
        // when Placemark list clicked, took Picture
        // and register Placemark with taken Picture
        this.requestCamera = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(bind == null || placeMarkListAdapter == null || recordManager == null) return;
                if (result.getResultCode() == RESULT_OK) {
                    String fileName = ImageManager.Companion.parseNameFromUri(bind.getRoot().getContext(), tmpFile);
                    if (!fileName.isBlank()) {
                        recordManager.addPlaceMark(currentPoiName, currentPoiType, currentTrackRegion, currentTrackName, currentPoiEnable);
                        try {
                            LinkedList<Uri> imgUriList = ImageManager.Companion.loadImageUriList(
                                    bind.getRoot().getContext(),
                                    fileName.replaceAll(".png", ""),
                                    "Trekking/" + currentTrackName + "/" + currentPoiType + "/" + currentPoiName);

                            LinkedList<Uri> imgUri = new LinkedList<>();
                            imgUri.add(imgUriList.getLast());
                            imgUriList.clear();
                            imgUriList = null;

                            imageLoadTask(imgUri, (compressedImg, isSuccess) -> {
                                currentSelectedPlaceMarkItem.setPlaceMarkImg(compressedImg.get(0), currentPoiPosition);
                                placeMarkListAdapter.updatePlaceMark(currentSelectedPlaceMarkItem);
                            });

                        } catch (FileNotFoundException | IndexOutOfBoundsException e) {
                            throw new RuntimeException(e);
                        }

                    } else {
                        Toast.makeText(bind.getRoot().getContext(), "사진 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }

                } else if (result.getResultCode() == RESULT_CANCELED) {
                    ImageManager.Companion.removeLastImage(bind.getRoot().getContext(), currentPoiType);
                }
            }
        });
        this.initCourseList();
        this.initPlaceMarkList();
        this.initViewEvent();
        this.initModifyTrackBottomSheet();
    }

    private void initCourseList() {
        if(this.recordManager == null || this.bind == null) return;
        LinkedList<Track> rawCourseList = recordManager.getCourseListByTrackName(this.currentTrackName);

        if(rawCourseList.isEmpty()) {
            this.recordManager.createBlankTables(this.currentTrackName, "기본 코스", this.currentTrackRegion);
            rawCourseList = recordManager.getCourseListByTrackName(this.currentTrackName);
        }

        LinearLayoutManager courseRecyclerLayoutManager = new LinearLayoutManager(this);
        courseRecyclerLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        this.bind.recordCourseList.setLayoutManager(courseRecyclerLayoutManager);

        // set data into course list
        this.courseRecyclerAdapter = new CourseNameRecyclerAdapter(new CourseNameRecyclerAdapter.OnItemSelectListener() {
            @Override
            public void onItemSelected(boolean isDeck, ItemCourseData item) {
                // when course select, Check Box state has change
                bind.checkDeckCheckbox.setChecked(isDeck);
            }
        });
        this.bind.recordCourseList.setAdapter(this.courseRecyclerAdapter);

        for(Track item : rawCourseList) {
            final String trackName = item.getName();
            final String courseName = item.getDescription();
            final int distance = (int) item.getDurationMoving();
            final boolean isWoodDeck = item.getCourseType().equals("wood_deck");

            ItemCourseData course = new ItemCourseData(trackName, courseName, distance, isWoodDeck);
            this.courseRecyclerAdapter.addCourseItem(course);
        }
    }

    private void initPlaceMarkList() {
        if(this.bind == null || this.recordManager == null) return;

        // init Placemark List
        LinearLayoutManager placeMarkLayoutManager = new LinearLayoutManager(this);
        this.bind.modifyPlacemarkTypeList.setLayoutManager(placeMarkLayoutManager);
        this.placeMarkListAdapter = new PlacemarkTypeRecyclerViewAdapter((placeMarkData, pos) -> {

            if(!this.recordManager.isAvailableRecord()) {
                this.toast.cancel();
                this.toast = Toast.makeText(this, "시설을 기록할수 없습니다. GPS 상태를 확인해 주세요", Toast.LENGTH_SHORT);
                this.toast.show();
                return;
            }

            this.currentPoiName = placeMarkData.getPlaceMarkTitle();
            this.currentPoiType = placeMarkData.getPlaceMarkType();
            this.currentPoiEnable = placeMarkData.isPlaceMarkEnable();
            this.currentPoiPosition = pos;
            this.currentSelectedPlaceMarkItem = placeMarkData;

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            this.tmpFile = ImageManager.Companion.createTmpFile(
                    this.bind.getRoot().getContext(),
                    placeMarkData.getPlaceMarkType(),
                    "Trekking/" + this.currentTrackName + "/" + placeMarkData.getPlaceMarkType() + "/" + placeMarkData.getPlaceMarkTitle());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, this.tmpFile);

            this.requestCamera.launch(intent);
        });
        this.bind.modifyPlacemarkTypeList.setAdapter(this.placeMarkListAdapter);

        // update data
        LinkedList<LocationExtended> rawPlaceMarkList = recordManager.getPlaceMarkByTrackName(this.currentTrackName);
        LinkedList<ItemPlaceMarkData> toSortPlacemarkList = new LinkedList<>();

        for(LocationExtended buffer : rawPlaceMarkList) {
            final String trackName = buffer.getTrackName();
            final String placeMarkType = buffer.getType();
            final String placeMarkName = buffer.getName();
            final String placeMarkDesc = "";
            final boolean placeMarkEnable = true;
            final double lat = buffer.getLatitude();
            final double lng = buffer.getLongitude();

            ItemPlaceMarkData placeMark = new ItemPlaceMarkData(trackName, placeMarkName, placeMarkType, placeMarkDesc, placeMarkEnable);
            placeMark.setPlaceMarkLat(lat);
            placeMark.setPlaceMarkLng(lng);
            toSortPlacemarkList.add(placeMark);
        }
        Collections.sort(toSortPlacemarkList);

        for (ItemPlaceMarkData placeMark : toSortPlacemarkList) {
            placeMarkListAdapter.addPlaceMark(placeMark);

            // load Image Later
            LinkedList<Uri> placeMarkImgList = null;
            try {
                placeMarkImgList = ImageManager.Companion.loadImageUriList(
                        this.bind.getRoot().getContext(),
                        "", "Trekking/" + this.currentTrackName + "/" + placeMark.getPlaceMarkType() + "/" + placeMark.getPlaceMarkTitle() + "/");

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if(placeMarkImgList != null) {
                this.imageLoadTask(placeMarkImgList, new OnCompressImageListener() {
                    @Override
                    public void onCompressImage(LinkedList<Bitmap> compressedImg, boolean isSuccess) {
                        if(isSuccess) {
                            int index = 0;
                            for(Bitmap img : compressedImg) {
                                placeMark.setPlaceMarkImg(img, index);
                                ++index;
                                if(index > 2) break;
                            }
                            if(placeMarkListAdapter != null) placeMarkListAdapter.updatePlaceMark(placeMark);
                        }
                    }
                });
            }
        }
    }

    // when Compressed Images Ready to show, notify
    private interface OnCompressImageListener {
        void onCompressImage(LinkedList<Bitmap> compressedImg, boolean isSuccess);
    }

    // Load image TASK
    private void imageLoadTask(@NonNull LinkedList<Uri> sourceImages, @NonNull final OnCompressImageListener listener) {
        AsyncTask<Object, Integer, Object> imgLoadTask = new AsyncTask<>() {
            LinkedList<Bitmap> compressedImgList;
            boolean isSuccess = true;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                compressedImgList = new LinkedList<>();
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                try {
                    for (Uri sourceUri : sourceImages) {
                        Bitmap compressedImg = ImageManager.Companion.loadBitmapWithCompressAggressive(bind.getRoot().getContext(), sourceUri, 200, 100);
                        compressedImgList.add(compressedImg);
//                        if(!source.isRecycled()) source.recycle();
//                        source = null;
                    }

                } catch (IllegalArgumentException | FileNotFoundException e) {
                    e.printStackTrace();
                    isSuccess = false;
                }
                sourceImages.clear();
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                if(listener != null) listener.onCompressImage(compressedImgList, isSuccess);
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                if(listener != null) listener.onCompressImage(new LinkedList<>(), false);
            }
        };
        imgLoadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 8);
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onEvent(Short msg) {
        if(msg == EventBusMSG.UPDATE_FIX) { // GPS SIGNAL RECEIVE
            Log.d("dspark", "Recording: UPDATE_FIX");
            this.updateUI();

        } else if (msg == EventBusMSG.UPDATE_TRACK) { // RECORDING COURSE
            Log.d("dspark", "Recording: UPDATE_TRACK");

        } else if (msg == EventBusMSG.NEW_TRACK) { // MIGHT BE CREATE NEW TRACK?
            Log.d("dspark", "Recording: NEW_TRACK");
        }
    }

    private void initViewEvent() {
        // upside "Add Course+" btn
        this.bind.courseAddBtn.setOnClickListener(view -> {
            AddCourseNameDialog inputCourseNameDialog = new AddCourseNameDialog();
            inputCourseNameDialog.setOnReceiveMessage(new AddCourseNameDialog.MessageReceiveListener() {
                @Override
                public void onReceiveMessage(String receiveMessage) {
                    if(receiveMessage.isBlank()) return;
                    LinkedList<ItemCourseData> courseList = courseRecyclerAdapter.getCloneCourseList();

                    boolean alreadyHas = false;
                    for(ItemCourseData buffer : courseList) {
                        if(buffer.getCourseName().equals(receiveMessage)) {
                            alreadyHas = true;
                            break;
                        }
                    }

                    if(alreadyHas) {
                        Toast.makeText(bind.getRoot().getContext(), "이미 존재하는 코스입니다", Toast.LENGTH_SHORT).show();

                    } else {
                        courseRecyclerAdapter.addCourseItem(new ItemCourseData(currentTrackName, receiveMessage, 0, true));
                    }
                }
            });
            inputCourseNameDialog.show(this.getSupportFragmentManager(), "Input POI Type Dialog");
        });

        // upside "Remove CourseX" btn
        this.bind.courseRemoveBtn.setOnClickListener(view -> {
            if(this.courseRecyclerAdapter.getItemCount() > 1) {
                final String toRemoveCourseName = this.courseRecyclerAdapter.getSelectedCourseName();
                this.recordManager.removeCourse(this.currentTrackName, toRemoveCourseName);
                this.courseRecyclerAdapter.removeCourse(this.currentTrackName, toRemoveCourseName);

            } else {
                if(toast != null) toast.cancel();
                toast = Toast.makeText(this.bind.getRoot().getContext(), "최소 1개 이상의 코스가 있어야 합니다.", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        // upside "Start Record / Pause Record" btn
        this.bind.recordControlBtn.setOnClickListener(view -> {
            if(this.bind == null || this.courseRecyclerAdapter == null || this.recordManager == null) return;
            if(!this.recordManager.isAvailableRecord()) {
                this.toast.cancel();
                this.toast = Toast.makeText(this, "코스를 기록할수 없습니다. GPS 상태를 확인해 주세요", Toast.LENGTH_SHORT);
                this.toast.show();
                return;
            }

            final String selectedCourseName = this.courseRecyclerAdapter.getSelectedCourseName();
            if(!selectedCourseName.isBlank()) {
                if(!this.recordManager.isRecordingCourse() && !this.isPauseCourseRecording) {
                    // start Record Course
                    this.recordManager.startRecordCourse(currentTrackName, selectedCourseName, currentTrackRegion);
                    this.isPauseCourseRecording = false;

                } else if (isPauseCourseRecording) {
                    // resume Record Course
                    this.recordManager.resumeRecordCourse();
                    this.isPauseCourseRecording = false;

                } else {
                    // pause Record Course
                    this.recordManager.pauseRecordTrack();
                    this.isPauseCourseRecording = true;
                }
                this.updateUI();

            } else {
                Toast.makeText(this.bind.getRoot().getContext(), "코스를 선택해 주세요", Toast.LENGTH_SHORT).show();
            }
        });

        // upside "Stop Record" btn
        this.bind.stopRecordBtn.setOnClickListener(v -> {
            if(this.bind == null || this.courseRecyclerAdapter == null) return;
            final String selectedCourseName = this.courseRecyclerAdapter.getSelectedCourseName();

            if(!selectedCourseName.isBlank()) {
                this.recordManager.stopRecordTrack(this.currentTrackName, selectedCourseName, this.currentTrackRegion, this.courseRecyclerAdapter.getSelectCourse().isWoodDeck());
                this.isPauseCourseRecording = false;
                this.updateUI();
            }
        });

        // upside "is Deck" checkBox
        this.bind.checkDeckCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ItemCourseData toUpdateCourse = this.courseRecyclerAdapter.getSelectCourse();
            if(toUpdateCourse != null) {
                toUpdateCourse.setWoodDeck(isChecked);
                this.courseRecyclerAdapter.updateCourse(toUpdateCourse);
                this.recordManager.updateCourseType(toUpdateCourse.getTrackName(), toUpdateCourse.getCourseName(), toUpdateCourse.isWoodDeck());
            }
        });
    }

    private void initModifyTrackBottomSheet() {
        BottomSheetBehavior<LinearLayout> modifyTrackSheet = BottomSheetBehavior.from(this.bind.modifyTrackRoot);
        modifyTrackSheet.setGestureInsetBottomIgnored(true);

        modifyTrackSheet.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case STATE_EXPANDED -> { // when BottomSheet Complete Appeared
                        bind.rootDisableLayout.bringToFront();
                        bind.rootDisableLayout.setVisibility(View.VISIBLE);
                        placeMarkListAdapter.setPlaceMarksIsHidden(true);
                        bind.getRoot().setFocusable(false);
                        bind.getRoot().setClickable(false);
                        bind.getRoot().setEnabled(false);
                    }

                    case STATE_COLLAPSED, STATE_HIDDEN, STATE_HALF_EXPANDED -> {
                        bind.rootDisableLayout.setVisibility(View.GONE);
                        placeMarkListAdapter.setPlaceMarksIsHidden(false);
                        bind.getRoot().setFocusable(true);
                        bind.getRoot().setClickable(true);
                        bind.getRoot().setEnabled(true);
                    }
                    case STATE_SETTLING, STATE_DRAGGING -> {}
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });
    }

    private void updateUI() {
        if (this.bind == null || this.recordManager == null) return;
//        this.mBind.proofSatliteTxt.setText(String.format("%d/%d : %s", this.recordManager.getAvailableSatellitesCnt(), this.recordManager.getTotalSatellitesCnt(), this.getString(R.string.satellites)));

        final boolean isRecording = this.recordManager.isRecordingCourse();

        this.bind.stopRecordBtn.setFocusable(isRecording);
        this.bind.stopRecordBtn.setClickable(isRecording);
        this.bind.stopRecordBtn.setEnabled(isRecording);
        this.bind.stopRecordBtn.setText(isRecording ? "종료" : "            ");
        this.bind.stopRecordBtn.setCompoundDrawablesWithIntrinsicBounds(isRecording ? R.drawable.ic_stop_24 : 0, 0, 0, 0);

        if(!isRecording && !this.isPauseCourseRecording) {
            this.setButtonState(this.bind.recordControlBtn, R.drawable.blue_round_border_32, R.drawable.ic_play_24, R.string.record);

        } else if(isRecording && !this.isPauseCourseRecording) {
            this.setButtonState(this.bind.recordControlBtn, R.drawable.red_round_border_32, R.drawable.ic_pause_24, R.string.pause);

        } else if (isRecording/* && this.isPauseCourseRecording*/) {
            this.setButtonState(this.bind.recordControlBtn, R.drawable.red_round_border_32, R.drawable.ic_play_24, R.string.record);
        }
    }

    private void setButtonState(final TextView button, final int backgroundId, final int iconId, final int stringId) {
        if(stringId != 0) button.setText(stringId);
        if(backgroundId != 0) button.setBackground(AppCompatResources.getDrawable(this.bind.getRoot().getContext(), backgroundId));
        if(iconId != 0) button.setCompoundDrawablesWithIntrinsicBounds(iconId, 0, 0, 0);
    }

    @Override
    public void onBackPressed() {
        if(this.bind == null || this.recordManager == null || toast == null) {
            super.onBackPressed();
        }

        // when recording course, CANT run away.
        // press "stop record" to stop recording, you can back to previous activity
        if(this.recordManager.isRecordingCourse()) {
            toast.cancel();
            toast = Toast.makeText(this.bind.getRoot().getContext(), "코스 기록중에는 뒤로 돌아갈수 없습니다.", Toast.LENGTH_SHORT);
            toast.show();

        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        this.recordManager = null;
        if(this.courseRecyclerAdapter != null) {
            this.courseRecyclerAdapter.release();
            this.courseRecyclerAdapter = null;
        }

        if(this.placeMarkListAdapter != null) {
            this.placeMarkListAdapter.release();
            this.placeMarkListAdapter = null;
        }

        if(toast != null) {
            this.toast.cancel();
            this.toast = null;
        }

        this.bind = null;
        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }
}
