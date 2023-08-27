package eu.basicairdata.graziano.gpslogger;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedList;

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
    private String currentCourseName = "";
    private String currentPoiType = "";
    private String currentTrackName;

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
                if (result.getResultCode() == RESULT_OK) {
                    String fileName = ImageManager.Companion.parseNameFromUri(bind.getRoot().getContext(), tmpFile);

                    if (!fileName.isBlank()) {
                        recordManager.addPlaceMark(fileName, "서울시 대모산 무장애 나눔길");

                    } else {
                        Toast.makeText(bind.getRoot().getContext(), "EMPTY FILE NAME", Toast.LENGTH_SHORT).show();
                    }

                } else if (result.getResultCode() == RESULT_CANCELED) {
                    ImageManager.Companion.removeLastImage(bind.getRoot().getContext(), "Parking");
                }
            }
        });

        LinearLayoutManager placeMarkLayoutManager = new LinearLayoutManager(this);
        this.bind.modifyPlacemarkTypeList.setLayoutManager(placeMarkLayoutManager);

        this.placeMarkListAdapter = new PlacemarkTypeRecyclerViewAdapter();
        this.bind.modifyPlacemarkTypeList.setAdapter(this.placeMarkListAdapter);

        LinearLayoutManager courseRecyclerLayoutManager = new LinearLayoutManager(this);
        courseRecyclerLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        this.bind.recordCourseList.setLayoutManager(courseRecyclerLayoutManager);

        this.courseRecyclerAdapter = new CourseNameRecyclerAdapter();
        this.bind.recordCourseList.setAdapter(this.courseRecyclerAdapter);

//        this.currentTrackName = "TEST TITLE";

        initViewEvent();
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
                        currentCourseName = receiveMessage;
                        courseRecyclerAdapter.addCourseItem(new ItemCourseData(currentTrackName, currentCourseName));
                    }
                }
            });
            inputCourseNameDialog.show(this.getSupportFragmentManager(), "Input POI Type Dialog");
        });

        this.bind.startRecordBtn.setOnClickListener(view -> {
            if(currentCourseName.isBlank()) {
                this.recordManager.startRecordTrack(currentTrackName, currentCourseName);

            } else {
                Toast.makeText(this.bind.getRoot().getContext(), "코스를 선택해 주세요", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
//        this.mBind.proofSatliteTxt.setText(String.format("%d/%d : %s", this.recordManager.getAvailableSatellitesCnt(), this.recordManager.getTotalSatellitesCnt(), this.getString(R.string.satellites)));
    }

    @Override
    protected void onDestroy() {
        this.recordManager = null;
        this.courseRecyclerAdapter = null;
        this.placeMarkListAdapter = null;
        this.bind = null;
        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }
}
