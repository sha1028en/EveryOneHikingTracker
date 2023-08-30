package eu.basicairdata.graziano.gpslogger.recording;

import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HALF_EXPANDED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN;

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
import java.util.LinkedList;

import eu.basicairdata.graziano.gpslogger.EventBusMSG;
import eu.basicairdata.graziano.gpslogger.GPSApplication;
import eu.basicairdata.graziano.gpslogger.LocationExtended;
import eu.basicairdata.graziano.gpslogger.R;
import eu.basicairdata.graziano.gpslogger.Track;
import eu.basicairdata.graziano.gpslogger.databinding.ActivityRecordingBinding;
import eu.basicairdata.graziano.gpslogger.management.ImageManager;
import eu.basicairdata.graziano.gpslogger.management.PlaceMarkType;
import eu.basicairdata.graziano.gpslogger.management.TrackRecordManager;

public class RecordingActivity extends AppCompatActivity {
    private ActivityRecordingBinding bind;
    private PlacemarkTypeRecyclerViewAdapter placeMarkListAdapter;
    private CourseNameRecyclerAdapter courseRecyclerAdapter;

    private ActivityResultLauncher<Intent> requestCamera;
    private TrackRecordManager recordManager = TrackRecordManager.getInstance();
    private Uri tmpFile;

    private String currentTrackName = "";
    private String currentPoiType = "";

    private ItemPlaceMarkData currentSelectedPlaceMarkItem;
    private int currentPoiPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setTheme(R.style.MyMaterialTheme);
        super.onCreate(savedInstanceState);

        this.bind = ActivityRecordingBinding.inflate(this.getLayoutInflater());
        setContentView(this.bind.getRoot());

        // FINAL VALUE, NEVER MODIFY VALUE!
        this.currentTrackName = this.getIntent().getStringExtra(GPSApplication.ATX_EXTRA_TRACK_TITLE);

        // register GPS Event
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        EventBus.getDefault().register(this);

        String path = ImageManager.Companion.createEmptyDirectory("Trekking/" + this.currentTrackName + "/" + PlaceMarkType.PARKING.name() + "/", PlaceMarkType.PARKING.name());

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
                        recordManager.addPlaceMark(currentPoiType, currentTrackName);
                        try {
                            LinkedList<Uri> imgUriList = ImageManager.Companion.loadImageUriList(bind.getRoot().getContext(), fileName.replaceAll(".png", ""), "Trekking/" + currentTrackName + "/" + currentPoiType);
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

        // init Placemark List
        LinearLayoutManager placeMarkLayoutManager = new LinearLayoutManager(this);
        this.bind.modifyPlacemarkTypeList.setLayoutManager(placeMarkLayoutManager);

        this.placeMarkListAdapter = new PlacemarkTypeRecyclerViewAdapter(this.currentTrackName, (placeMarkData, pos) -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            tmpFile = ImageManager.Companion.createTmpFile(bind.getRoot().getContext(), placeMarkData.getPlaceMarkType(), "Trekking/" + this.currentTrackName + "/" + placeMarkData.getPlaceMarkType());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, tmpFile);
            this.currentPoiType = placeMarkData.getPlaceMarkType();
            this.currentPoiPosition = pos;
            this.currentSelectedPlaceMarkItem = placeMarkData;

            requestCamera.launch(intent);
        });
        this.bind.modifyPlacemarkTypeList.setAdapter(this.placeMarkListAdapter);

        // init course list
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

        LinkedList<Track> rawCourseList = recordManager.getCourseListByTrackName(this.currentTrackName);
        LinkedList<LocationExtended> rawPlaceMarkList = recordManager.getPlaceMarkByTrackName(this.currentTrackName);

        for(Track item : rawCourseList) {
            final String trackName = item.getName();
            final String courseName = item.getDescription();
            final int distance = (int) item.getDurationMoving();
            final boolean isWoodDeck = item.getCourseType().equals("wood_deck");

            ItemCourseData course = new ItemCourseData(trackName, courseName, distance, isWoodDeck);
            this.courseRecyclerAdapter.addCourseItem(course);
        }

        // set data into placemark list
        for(LocationExtended buffer : rawPlaceMarkList) {
            final String trackName = buffer.getName();
            final String placeMarkType = buffer.getDescription();
            final String placeMarkDesc = "";
            final boolean placeMarkEnable = true;
            final double lat = buffer.getLatitude();
            final double lng = buffer.getLongitude();

            ItemPlaceMarkData placeMark = new ItemPlaceMarkData(trackName, TrackRecordManager.typeToTitle(placeMarkType), placeMarkType, placeMarkDesc, placeMarkEnable);
            placeMark.setPlaceMarkLat(lat);
            placeMark.setPlaceMarkLng(lng);
            LinkedList<Uri> placeMarkImgList = null;

            try {
                placeMarkImgList = ImageManager.Companion.loadImageUriList(this.bind.getRoot().getContext(), placeMarkDesc, "Trekking/" + this.currentTrackName + "/" + placeMarkType);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            placeMarkListAdapter.updatePlaceMark(placeMark);

            if(placeMarkImgList != null) {
                this.imageLoadTask(placeMarkImgList, new OnCompressImageListener() {
                    @Override
                    public void onCompressImage(LinkedList<Bitmap> compressedImg, boolean isSuccess) {
                        int index = 0;
                        for(Bitmap img : compressedImg) {
                            placeMark.setPlaceMarkImg(img, index);
                            ++index;
                            if(index > 2) break;
                        }
                        placeMarkListAdapter.updatePlaceMark(placeMark);
                    }
                });
            }
        }
        this.initViewEvent();
        this.initModifyTrackBottomSheet();
    }

    private interface OnCompressImageListener {
        void onCompressImage(LinkedList<Bitmap> compressedImg, boolean isSuccess);
    }

    // Load image TASK
    private void imageLoadTask(@NonNull LinkedList<Uri> sourceImages, @NonNull final OnCompressImageListener listener) {
        AsyncTask imgLoadTask = new AsyncTask() {
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


    private void imageLoadTask(@NonNull LinkedList<Bitmap> sourceImages, final int compressPercent, @NonNull final OnCompressImageListener listener) {
        AsyncTask imgLoadTask = new AsyncTask() {
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
                    for (Bitmap source : sourceImages) {
                        Bitmap compressedImg = ImageManager.Companion.compressBitmapAggressive(source, compressPercent, 100, 200);
                        compressedImgList.add(compressedImg);

                        if(!source.isRecycled()) source.recycle();
                        source = null;
                    }

                } catch (IllegalArgumentException e) {
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

        this.bind.courseRemoveBtn.setOnClickListener(view -> {
            final String toRemoveCourseName = this.courseRecyclerAdapter.getSelectedCourseName();
            this.recordManager.removeCourse(this.currentTrackName, toRemoveCourseName);
            this.courseRecyclerAdapter.removeCourse(this.currentTrackName, toRemoveCourseName);
        });

        this.bind.recordControlBtn.setOnClickListener(view -> {
            if(this.bind == null || this.courseRecyclerAdapter == null) return;
            final String selectedCourseName = this.courseRecyclerAdapter.getSelectedCourseName();

            if(!selectedCourseName.isBlank()) {
                this.recordManager.startRecordTrack(currentTrackName, selectedCourseName);

            } else {
                Toast.makeText(this.bind.getRoot().getContext(), "코스를 선택해 주세요", Toast.LENGTH_SHORT).show();
            }
        });

        this.bind.stopRecordBtn.setOnClickListener(v -> {
            if(this.bind == null || this.courseRecyclerAdapter == null) return;
            final String selectedCourseName = this.courseRecyclerAdapter.getSelectedCourseName();

            if(!selectedCourseName.isBlank()) {
                this.recordManager.stopRecordTrack(true, this.currentTrackName, selectedCourseName, this.courseRecyclerAdapter.getSelectCourse().isWoodDeck());
            }
        });

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
                    case STATE_EXPANDED -> {
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
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });
    }

    private void updateUI() {
        if(this.bind == null || this.recordManager == null) return;
//        this.mBind.proofSatliteTxt.setText(String.format("%d/%d : %s", this.recordManager.getAvailableSatellitesCnt(), this.recordManager.getTotalSatellitesCnt(), this.getString(R.string.satellites)));

        boolean isRecording = this.recordManager.isRecordingCourse();
        this.bind.stopRecordBtn.setFocusable(isRecording);
        this.bind.stopRecordBtn.setClickable(isRecording);
        this.bind.stopRecordBtn.setEnabled(isRecording);
        this.bind.stopRecordBtn.setText(isRecording? "종료": "            ");
        this.bind.stopRecordBtn.setCompoundDrawablesWithIntrinsicBounds(isRecording? R.drawable.ic_stop_24: 0, 0 ,0 ,0);

        if(isRecording) {
            this.setButtonState(this.bind.recordControlBtn, R.drawable.red_round_border_32, R.drawable.ic_pause_24, R.string.pause);

        } else {
            this.setButtonState(this.bind.recordControlBtn, R.drawable.blue_round_border_32, R.drawable.ic_play_24, R.string.record);
        }
    }

    private void setButtonState(final TextView button, final int backgroundId, final int iconId, final int stringId) {
        if(stringId != 0) button.setText(stringId);
        if(backgroundId != 0) button.setBackground(AppCompatResources.getDrawable(this.bind.getRoot().getContext(), backgroundId));
        if(iconId != 0) button.setCompoundDrawablesWithIntrinsicBounds(iconId, 0, 0, 0);
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
        this.bind = null;
        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }
}
