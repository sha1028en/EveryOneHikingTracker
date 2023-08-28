package eu.basicairdata.graziano.gpslogger.recording;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.FileNotFoundException;
import java.util.LinkedList;

import eu.basicairdata.graziano.gpslogger.AddCourseNameDialog;
import eu.basicairdata.graziano.gpslogger.EventBusMSG;
import eu.basicairdata.graziano.gpslogger.GPSApplication;
import eu.basicairdata.graziano.gpslogger.LocationExtended;
import eu.basicairdata.graziano.gpslogger.R;
import eu.basicairdata.graziano.gpslogger.Track;
import eu.basicairdata.graziano.gpslogger.databinding.ActivityRecordingBinding;
import eu.basicairdata.graziano.gpslogger.management.ImageManager;
import eu.basicairdata.graziano.gpslogger.management.TrackRecordManager;

public class RecordingActivity extends AppCompatActivity {
    private ActivityRecordingBinding bind;
    private PlacemarkTypeRecyclerViewAdapter placeMarkListAdapter;
    private CourseNameRecyclerAdapter courseRecyclerAdapter;

    private ActivityResultLauncher<Intent> requestCamera;
    private TrackRecordManager recordManager = TrackRecordManager.getInstance();
    private Uri tmpFile;

    // private String currentCourseName = "";
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

        this.currentTrackName = this.getIntent().getStringExtra(GPSApplication.ATX_EXTRA_TRACK_TITLE);

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        EventBus.getDefault().register(this);

        this.requestCamera = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(bind == null || placeMarkListAdapter == null || recordManager == null) return;

                if (result.getResultCode() == RESULT_OK) {
                    String fileName = ImageManager.Companion.parseNameFromUri(bind.getRoot().getContext(), tmpFile);
                    if (!fileName.isBlank()) {
                        recordManager.addPlaceMark(currentPoiType, currentTrackName);
                        try {
                            Bitmap img = ImageManager.Companion.loadImage(bind.getRoot().getContext(), fileName.replaceAll(".png", ""), "Trekking/" + currentTrackName + "/" + currentPoiType, "png");
                            LinkedList<Bitmap> toCompressImgList  = new LinkedList<>();
                            toCompressImgList.add(img);

                            imageLoadTask(toCompressImgList, 90, (compressedImg, isSuccess) -> {
                                currentSelectedPlaceMarkItem.setPlaceMarkImg(compressedImg.get(0), currentPoiPosition);
                                placeMarkListAdapter.updatePlaceMark(currentSelectedPlaceMarkItem);
                            });
                            // Bitmap compressedImg = ImageManager.Companion.compressBitmap(img, 70);
                            // img = null;

                        } catch (FileNotFoundException e) {
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

        LinearLayoutManager courseRecyclerLayoutManager = new LinearLayoutManager(this);
        courseRecyclerLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        this.bind.recordCourseList.setLayoutManager(courseRecyclerLayoutManager);

        this.courseRecyclerAdapter = new CourseNameRecyclerAdapter();
        this.bind.recordCourseList.setAdapter(this.courseRecyclerAdapter);

        LinkedList<Track> rawCourseList = recordManager.getCourseListByTrackName(this.currentTrackName);
        LinkedList<LocationExtended> rawPlaceMarkList = recordManager.getPlaceMarkByTrackName(this.currentTrackName);


        // LinkedList<ItemCourseData> courseList = new LinkedList<>();
        for(Track item : rawCourseList) {
            final String trackName = item.getName();
            final String courseName = item.getDescription();
            final int distance = (int) item.getDurationMoving();
            final boolean isWoodDeck = item.getCourseType().equals("wood_deck");

            ItemCourseData course = new ItemCourseData(trackName, courseName, distance, isWoodDeck);
            this.courseRecyclerAdapter.addCourseItem(course);
        }

        for(LocationExtended buffer : rawPlaceMarkList) {
            final String trackName = buffer.getName();
            final String placeMarkType = buffer.getDescription();
            final String placeMarkDesc = "";
            final boolean placeMarkEnable = true;
            ItemPlaceMarkData placeMark = new ItemPlaceMarkData(trackName, TrackRecordManager.typeToTitle(placeMarkType), placeMarkType, placeMarkDesc, placeMarkEnable);
            LinkedList<Bitmap> placeMarkImgList = null;
            try {
                placeMarkImgList = ImageManager.Companion.loadImageList(this.bind.getRoot().getContext(), placeMarkDesc, "Trekking/" + this.currentTrackName + "/" + placeMarkType);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                // throw new RuntimeException(e);
            }
            placeMarkListAdapter.updatePlaceMark(placeMark);

            if(placeMarkImgList != null) {
                this.imageLoadTask(placeMarkImgList, 90, new OnCompressImageListener() {
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
        initViewEvent();

//        this.currentTrackName = "TEST TITLE";
//        this.mBind.proofReqCamera.setOnClickListener(v -> {
//            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, ImageManager.Companion.createTmpFile(this.mBind.getRoot().getContext(), "Parking", "Trekking/서울 강남구 대모산 무장애 나눔길"));
//
//            this.requestCamera.launch(intent);
//        });
//
//        this.mBind.proofLoadImg.setOnClickListener(v -> {
//            AddCourseNameDialog inputCourseNameDialog = new AddCourseNameDialog();
//            inputCourseNameDialog.setOnReceiveMessage(new AddCourseNameDialog.MessageReceiveListener() {
//                @Override
//                public void onReceiveMessage(String receiveMessage) {
//                    if(receiveMessage != null && !receiveMessage.isBlank()) {
//                        Bitmap image;
//                        LinkedList<Bitmap> imageList;
//                        try {
//                            imageList = ImageManager.Companion.loadImageList(mBind.getRoot().getContext(), receiveMessage, "Trekking/서울 강남구 대모산 무장애 나눔길/" + receiveMessage);
//                            image = imageList.getLast();
//
//                        } catch (FileNotFoundException | NullPointerException | IndexOutOfBoundsException e) {
//                            e.printStackTrace();
//                            image = null;
//                        }
//
//                        if(image != null) {
//                            mBind.proofLoadImgView.setImageBitmap(image);
//                        }
//                    }
//                }
//            });
//            inputCourseNameDialog.show(this.getSupportFragmentManager(), "Input to load POI image Dialog");
//        });
//
//        this.mBind.proofRemoveImg.setOnClickListener( v -> {
//            Toast.makeText(this.mBind.getRoot().getContext(), "NOT YET IMPL", Toast.LENGTH_SHORT).show();
//        });
//
//        this.mBind.proofAddAnnotation.setOnClickListener(v -> {
//            AddCourseNameDialog inputCourseNameDialog = new AddCourseNameDialog();
//            inputCourseNameDialog.setOnReceiveMessage(new AddCourseNameDialog.MessageReceiveListener() {
//                @Override
//                public void onReceiveMessage(String receiveMessage) {
//                    if(receiveMessage != null && !receiveMessage.isBlank()) {
//                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                        tmpFile = ImageManager.Companion.createTmpFile(mBind.getRoot().getContext(), receiveMessage, "Trekking/서울 강남구 대모산 무장애 나눔길/" + receiveMessage);
//                        intent.putExtra(MediaStore.EXTRA_OUTPUT, tmpFile);
//
//                        requestCamera.launch(intent);
//                    }
//                }
//            });
//            inputCourseNameDialog.show(this.getSupportFragmentManager(), "Input POI Type Dialog");
//        });
//
//        this.mBind.proofStartRecord.setOnClickListener( v -> {
//            AddCourseNameDialog inputCourseNameDialog = new AddCourseNameDialog();
//            inputCourseNameDialog.setOnReceiveMessage(new AddCourseNameDialog.MessageReceiveListener() {
//                @Override
//                public void onReceiveMessage(String receiveMessage) {
//                    if(receiveMessage != null) {
//                        currentCourseName = receiveMessage;
//                        recordManager.startRecordTrack("서울 강남구 대모산 무장애 나눔길", currentCourseName);
//                    }
//                }
//            });
//            inputCourseNameDialog.show(this.getSupportFragmentManager(), "Input CourseName Dialog");
//        });
//
//        this.mBind.proofStopRecord.setOnClickListener(v -> {
//            this.recordManager.stopRecordTrack(true, "서울시 대모산 무장애 나눔길", currentCourseName);
//        });
    }

    private interface OnCompressImageListener {
        void onCompressImage(LinkedList<Bitmap> compressedImg, boolean isSuccess);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Short msg) {
        if(msg == EventBusMSG.UPDATE_FIX) {
            this.updateUI();
        }
    }

    private void initViewEvent() {
        this.bind.courseAddBtn.setOnClickListener(view -> {
            AddCourseNameDialog inputCourseNameDialog = new AddCourseNameDialog();
            inputCourseNameDialog.setOnReceiveMessage(new AddCourseNameDialog.MessageReceiveListener() {
                @Override
                public void onReceiveMessage(String receiveMessage) {
                    if(receiveMessage.isBlank()) return; // currentCourseName = receiveMessage;
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
                        // currentCourseName = receiveMessage;
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

        this.bind.startRecordBtn.setOnClickListener(view -> {
            if(this.bind == null || this.courseRecyclerAdapter == null) return;
            final String selectedCourseName = this.courseRecyclerAdapter.getSelectedCourseName();

            if(!selectedCourseName.isBlank()) {
                this.recordManager.startRecordTrack(currentTrackName, selectedCourseName);

            } else {
                Toast.makeText(this.bind.getRoot().getContext(), "코스를 선택해 주세요", Toast.LENGTH_SHORT).show();
            }
        });

        this.bind.pauseRecordBtn.setOnClickListener(v -> {
            if(this.bind == null || this.courseRecyclerAdapter == null) return;
            final String selectedCourseName = this.courseRecyclerAdapter.getSelectedCourseName();

            if(!selectedCourseName.isBlank()) {
                this.recordManager.stopRecordTrack(true, this.currentTrackName, selectedCourseName, this.courseRecyclerAdapter.getSelectCourse().isWoodDeck());
            }
        });
    }

    private void updateUI() {
//        this.mBind.proofSatliteTxt.setText(String.format("%d/%d : %s", this.recordManager.getAvailableSatellitesCnt(), this.recordManager.getTotalSatellitesCnt(), this.getString(R.string.satellites)));
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
