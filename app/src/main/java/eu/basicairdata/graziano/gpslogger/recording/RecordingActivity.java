package eu.basicairdata.graziano.gpslogger.recording;

import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_DRAGGING;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HALF_EXPANDED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_SETTLING;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;

import eu.basicairdata.graziano.gpslogger.BuildConfig;
import eu.basicairdata.graziano.gpslogger.GPSApplication;
import eu.basicairdata.graziano.gpslogger.LocationExtended;
import eu.basicairdata.graziano.gpslogger.R;
import eu.basicairdata.graziano.gpslogger.databinding.ActivityRecordingBinding;
import eu.basicairdata.graziano.gpslogger.management.ExporterManager;
import eu.basicairdata.graziano.gpslogger.management.ImageManager;
import eu.basicairdata.graziano.gpslogger.management.RequestPlaceMarkManager;
import eu.basicairdata.graziano.gpslogger.management.TrackRecordManager;
import eu.basicairdata.graziano.gpslogger.recording.enhanced.CourseNameRecyclerAdapter;

/**
 * @deprecated {@link eu.basicairdata.graziano.gpslogger.recording.enhanced.RecordEnhancedActivity}
 */
public class RecordingActivity extends AppCompatActivity {
//    private static final int REQUEST_ACTION_OPEN_DOCUMENT_TREE = 2;

    private ActivityRecordingBinding bind; // this View n Layout Instance
    private boolean isModifyTrackExpended = false; // id Behavior Bottom Sheet has Expended? ( state )
    private Toast toast; // using this activity's toast


    private PlacemarkTypeRecyclerViewAdapter placeMarkListAdapter; // POI list
    private CourseNameRecyclerAdapter courseRecyclerAdapter; // upside Course list

    private ActivityResultLauncher<Intent> requestCamera; // Camera
    private Uri tmpFile; // taken a Picture's File Instance

    private TrackRecordManager recordManager = TrackRecordManager.getInstance(); // Track, Course, POI control Manager.
    private ExporterManager exporterManager; // Export Course, Placemark
    private RequestPlaceMarkManager requestManager; // Request and Send Placemark Info to Server


    private boolean isPauseCourseRecording = false; // this is FLAG course recording has paused ( state )
    private long currentTrackId = -1; // this course's parent track Id;
    private String currentTrackName = ""; // this course's Parents Track Name
    private String currentTrackRegion; // this course Region
    private String currentPoiType = ""; // selected POI Type
    private String currentPoiName = ""; // selected POI Name
    private boolean currentPoiEnable = true; // is POI enabled( POI's checkbox )
    private int currentPoiPosition = 0; // this poi position that selected picture
    private ItemPlaceMarkData currentSelectedPlaceMarkItem; // selected POI's DataClass


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setTheme(R.style.MyMaterialTheme);
        super.onCreate(savedInstanceState);

        this.bind = ActivityRecordingBinding.inflate(this.getLayoutInflater());
        setContentView(this.bind.getRoot());

        toast = new Toast(this.bind.getRoot().getContext());
        // FINAL VALUE, NEVER MODIFY THIS
        this.currentTrackId = this.getIntent().getLongExtra(GPSApplication.ATV_EXTRA_TRACK_ID, -1);
        this.currentTrackName = this.getIntent().getStringExtra(GPSApplication.ATX_EXTRA_TRACK_TITLE);
        this.currentTrackRegion = this.getIntent().getStringExtra(GPSApplication.ATV_EXTRA_TRACK_REGION);

        // register GPS Event
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        EventBus.getDefault().register(this);
        this.exporterManager = new ExporterManager(GPSApplication.getInstance(), this.bind.getRoot().getContext());
        this.requestManager = new RequestPlaceMarkManager(this.bind.getRoot().getContext());

        // Camera Action
        // when Placemark list clicked, took Picture
        // and register Placemark with taken Picture
        this.requestCamera = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(bind == null || placeMarkListAdapter == null || recordManager == null) return;
            if (result.getResultCode() == RESULT_OK) {
                String fileName = ImageManager.Companion.parseNameFromUri(bind.getRoot().getContext(), tmpFile);

                if (!fileName.isBlank()) {
                    try {
                        ImageManager.Companion.addLocationIntoImage(ImageManager.Companion.getFileFromImageURI(bind.getRoot().getContext(), tmpFile), recordManager.getLastObserveLat(), recordManager.getLastObserveLng());
                        this.requestManager.requestAddPicturePlaceMark(3, ImageManager.Companion.getFileFromImageURI(this.bind.getRoot().getContext(), tmpFile), fileName, new RequestPlaceMarkManager.OnRequestResponse<>() {
                            @Override
                            public void onRequestResponse(Integer response, boolean isSuccess) {
                                Log.d("RequestPlaceMarkMgr", "success?" + isSuccess);
                            }
                        });

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    this.recordManager.addPlaceMark(currentPoiName, currentPoiType, currentTrackRegion, currentTrackName, currentPoiEnable);

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

                    } catch (IndexOutOfBoundsException | IOException e) {
                        throw new RuntimeException(e);
                    }

                } else {
                    Toast.makeText(bind.getRoot().getContext(), "사진 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }

            } else if (result.getResultCode() == RESULT_CANCELED) {
                ImageManager.Companion.removeLastImage(bind.getRoot().getContext(), currentPoiType);
            }
        });
//        this.initCourseList();
        this.initPlaceMarkList();
//        this.initViewEvent();
        this.initModifyTrackBottomSheet();
        this.initWebView();
    }

//    /**
//     * Executes the local exportation into the selected folder.
//     * For Android >= LOLLYPOP
//     */
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
//        if (requestCode == REQUEST_ACTION_OPEN_DOCUMENT_TREE && resultCode == Activity.RESULT_OK) {
//            // The result data contains a URI for the document or directory that
//            // the user selected.
//
//            if (resultData != null) {
//                Uri treeUri = resultData.getData();
//                grantUriPermission(getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//
//
//
//                // Perform operations on the document using its URI.
//            }
//        }
//        super.onActivityResult(resultCode, resultCode, resultData);
//    }

//    private void initCourseList(/*ItemPlaceMarkData toRequestPlaceMarkType*/) {
//        if(this.recordManager == null || this.bind == null) return;
//        this.requestManager.requestPlaceMarkList((int) this.currentTrackId, this.currentTrackName, new RequestPlaceMarkManager.OnRequestResponse<>() {
//            @Override
//            public void onRequestResponse(LinkedList<ItemPlaceMarkData> response, boolean isSuccess) {
//                if(isSuccess) {
//                    for (ItemPlaceMarkData item : response) {
//                        Log.d("RequestPlaceMarkMgr", item.toString());
//                    }
//                }
//
//                // if never
//                if(response.isEmpty()) {
//                    RecordingActivity.this.runOnUiThread(() -> {
//                        LinkedList<PlaceMarkType> requestEmptyPoiList = new LinkedList<>(Arrays.asList(PlaceMarkType.values()));
//                        requestManager.requestAddEmptyPlaceMarkList((int) currentTrackId, currentTrackName, requestEmptyPoiList, new RequestPlaceMarkManager.OnRequestResponse<>() {
//                            @Override
//                            public void onRequestResponse(LinkedList<ItemPlaceMarkData> response, boolean isSuccess) {
//                                if (isSuccess) {
//                                    for (ItemPlaceMarkData item : response) {
//                                        Log.d("RequestPlaceMarkMgr", "response placemark id : " + item.toString());
//                                    }
//                                }
//                            }
//                        });
//                    });
//                }
//            }
//        });
//
//        LinkedList<Track> rawCourseList = recordManager.getCourseListByTrackName(this.currentTrackName);
//        if(rawCourseList.isEmpty()) {
//            this.recordManager.createBlankTables(this.currentTrackId, this.currentTrackName, "코스 1", this.currentTrackRegion);
//            rawCourseList = recordManager.getCourseListByTrackName(this.currentTrackName);
//        }
//
//        LinearLayoutManager courseRecyclerLayoutManager = new LinearLayoutManager(this);
//        courseRecyclerLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
//        this.bind.recordCourseList.setLayoutManager(courseRecyclerLayoutManager);
//
//        // set data into course list
//        this.courseRecyclerAdapter = new CourseNameRecyclerAdapter(new CourseNameRecyclerAdapter.OnItemSelectListener() {
//            @Override
//            public void onItemSelected(boolean isDeck, ItemCourseData item) {
//                // when course select, Check Box state has change
//                bind.checkDeckCheckbox.setChecked(isDeck);
//            }
//        });
//        this.bind.recordCourseList.setAdapter(this.courseRecyclerAdapter);
//
//        for(Track item : rawCourseList) {
//            final int coursePrimaryId = (int) item.getPrimaryId();
//            final String trackName = item.getName();
//            final String courseName = item.getDescription();
//            final int distance = (int) item.getDurationMoving();
//            final boolean isWoodDeck = item.getCourseType().equals("wood_deck");
//
//            ItemCourseData course = new ItemCourseData(trackName, courseName, coursePrimaryId, distance, isWoodDeck);
//            this.courseRecyclerAdapter.addCourseItem(course);
//        }
//    }

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
                                placeMark.setPlaceMarkImg(img, index++);
                                if(index > 2) break;
                            }
                            if(placeMarkListAdapter != null) placeMarkListAdapter.updatePlaceMark(placeMark);
                        }
                    }
                });
            }
        }
    }

//    @Subscribe (threadMode = ThreadMode.MAIN)
//    public void onEvent(Short msg) {
//        if(msg == EventBusMSG.UPDATE_FIX) { // GPS SIGNAL RECEIVE
//            Log.d("dspark", "Recording: UPDATE_FIX");
//            this.updateUpsideControlButtonState();
//
//        } else if (msg == EventBusMSG.UPDATE_TRACK) { // RECORDING COURSE
//            Log.d("dspark", "Recording: UPDATE_TRACK");
//
//        } else if (msg == EventBusMSG.NEW_TRACK) { // A NEW NEW TRACK INCOMING!!!
//            if(this.courseRecyclerAdapter != null && this.recordManager != null) {
//                LinkedList<Track> rawCourseList = this.recordManager.getCourseList(this.currentTrackName, this.courseRecyclerAdapter.getSelectedCourseName());
//
//                if(!rawCourseList.isEmpty()) {
//                    Track rawCourse = rawCourseList.getLast();
//                    // update course list's item
//                    ItemCourseData toUpdateCourse = new ItemCourseData(this.currentTrackName, this.courseRecyclerAdapter.getSelectedCourseName(), (int) rawCourse.getPrimaryId(), (int) rawCourse.getDurationMoving(), rawCourse.getCourseType().equals("wood_deck"));
//                    this.courseRecyclerAdapter.replaceCourseItem(toUpdateCourse);
//                }
//            }
//            Log.d("dspark", "Recording: NEW_TRACK");
//        }
//    }

//    private void initViewEvent() {
//        // upside "Add Course+" btn
//        this.bind.courseAddBtn.setOnClickListener(view -> {
//            AddCourseNameDialog inputCourseNameDialog = new AddCourseNameDialog();
//            inputCourseNameDialog.setOnReceiveMessage(new AddCourseNameDialog.MessageReceiveListener() {
//                @Override
//                public void onReceiveMessage(String receiveMessage) {
//                    if(receiveMessage.isBlank()) return;
//                    LinkedList<ItemCourseData> courseList = courseRecyclerAdapter.getCloneCourseList();
//
//                    boolean alreadyHas = false;
//                    for(ItemCourseData buffer : courseList) {
//                        if(buffer.getCourseName().equals(receiveMessage)) {
//                            alreadyHas = true;
//                            break;
//                        }
//                    }
//
//                    if(alreadyHas) {
//                        Toast.makeText(bind.getRoot().getContext(), "이미 존재하는 코스입니다", Toast.LENGTH_SHORT).show();
//
//                    } else {
//                        courseRecyclerAdapter.addCourseItem(new ItemCourseData(currentTrackName, receiveMessage, -1,0, true));
//                    }
//                }
//            });
//            inputCourseNameDialog.show(this.getSupportFragmentManager(), "Input POI Type Dialog");
//        });
//
//        // upside "Remove CourseX" btn
//        this.bind.courseRemoveBtn.setOnClickListener(view -> {
//            if(this.courseRecyclerAdapter.getItemCount() > 1) {
//                final String toRemoveCourseName = this.courseRecyclerAdapter.getSelectedCourseName();
//                this.recordManager.removeCourse(this.currentTrackName, toRemoveCourseName);
//                this.courseRecyclerAdapter.removeCourse(this.currentTrackName, toRemoveCourseName);
//
//            } else {
//                if(toast != null) toast.cancel();
//                toast = Toast.makeText(this.bind.getRoot().getContext(), "최소 1개 이상의 코스가 있어야 합니다.", Toast.LENGTH_SHORT);
//                toast.show();
//            }
//        });
//
//        // upside "Start Record / Pause Record" btn
//        this.bind.recordControlBtn.setOnClickListener(view -> {
//            if(this.bind == null || this.courseRecyclerAdapter == null || this.recordManager == null) return;
//            if(!this.recordManager.isAvailableRecord()) {
//                this.toast.cancel();
//                this.toast = Toast.makeText(this, "코스를 기록할수 없습니다. GPS 상태를 확인해 주세요", Toast.LENGTH_SHORT);
//                this.toast.show();
//                return;
//            }
//
//            final String selectedCourseName = this.courseRecyclerAdapter.getSelectedCourseName();
//            if(!selectedCourseName.isBlank()) {
//                if(!this.recordManager.isRecordingCourse() && !this.isPauseCourseRecording) {
//                    // start Record Course
//                    this.recordManager.startRecordCourse(this.currentTrackId, currentTrackName, selectedCourseName, currentTrackRegion);
//                    this.isPauseCourseRecording = false;
//
//                } else if (isPauseCourseRecording) {
//                    // resume Record Course
//                    this.recordManager.resumeRecordCourse();
//                    this.isPauseCourseRecording = false;
//
//                } else {
//                    // pause Record Course
//                    this.recordManager.pauseRecordTrack();
//                    this.isPauseCourseRecording = true;
//                }
//                this.updateUpsideControlButtonState();
//
//            } else {
//                Toast.makeText(this.bind.getRoot().getContext(), "코스를 선택해 주세요", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        // upside "Stop Record" btn
//        this.bind.stopRecordBtn.setOnClickListener(v -> {
//            if(this.bind == null || this.courseRecyclerAdapter == null) return;
//            final String selectedCourseName = this.courseRecyclerAdapter.getSelectedCourseName();
//
//            if(!selectedCourseName.isBlank()) {
//                this.recordManager.stopRecordTrack(this.currentTrackId, this.currentTrackName, selectedCourseName, this.currentTrackRegion, this.courseRecyclerAdapter.getSelectCourse().isWoodDeck());
//                this.isPauseCourseRecording = false;
//                this.updateUpsideControlButtonState();
//            }
//        });
//
//        // upside "is Deck" checkBox
//        this.bind.checkDeckCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            ItemCourseData toUpdateCourse = this.courseRecyclerAdapter.getSelectCourse();
//            if(toUpdateCourse != null) {
//                toUpdateCourse.setWoodDeck(isChecked);
//                this.courseRecyclerAdapter.updateCourse(toUpdateCourse);
//                this.recordManager.updateCourseType(toUpdateCourse.getTrackName(), toUpdateCourse.getCourseName(), toUpdateCourse.isWoodDeck());
//            }
//        });
//
//        // downside "^" BottomSheet Control Button
//        this.bind.modifySheetControlBtn.setOnClickListener(v -> {
//            BottomSheetBehavior<LinearLayout> modifyTrackSheet = BottomSheetBehavior.from(this.bind.modifyTrackRoot);
//            modifyTrackSheet.setState(this.isModifyTrackExpended? STATE_COLLAPSED: STATE_EXPANDED);
//            modifyTrackSheet = null;
//        });
//    }

    private void initModifyTrackBottomSheet() {
        BottomSheetBehavior<LinearLayout> modifyTrackSheet = BottomSheetBehavior.from(this.bind.modifyTrackRoot);
        modifyTrackSheet.setGestureInsetBottomIgnored(true);

        modifyTrackSheet.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case STATE_EXPANDED -> { // when BottomSheet Complete Appeared
                        modifyTrackSheet.setDraggable(false);

                        bind.rootDisableLayout.bringToFront();
                        bind.rootDisableLayout.setVisibility(View.VISIBLE);
                        placeMarkListAdapter.setPlaceMarksIsHidden(true);
                        bind.getRoot().setNestedScrollingEnabled(false);
                        bind.getRoot().setFocusable(false);
                        bind.getRoot().setClickable(false);
                        bind.getRoot().setEnabled(false);
                        isModifyTrackExpended = true;
                        loadMapFromWebView();

//                        initWebView();
//                        parseCourseLocations();
//                        parsePlacemark();
                    }

                    case STATE_COLLAPSED, STATE_HIDDEN, STATE_HALF_EXPANDED, STATE_SETTLING -> {
                        bind.rootDisableLayout.setVisibility(View.GONE);
                        placeMarkListAdapter.setPlaceMarksIsHidden(false);
                        bind.getRoot().setFocusable(true);
                        bind.getRoot().setClickable(true);
                        bind.getRoot().setEnabled(true);
                        isModifyTrackExpended = false;

                        // JUST FUNC TEST
//                        exporterManager.export(exporterManager.pathToUri( "Trekking", currentTrackName), currentTrackName);
                    }
                    case STATE_DRAGGING -> {}
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        WebView.setWebContentsDebuggingEnabled(BuildConfig.BUILD_TYPE.equals("debug"));

        this.bind.modifyTrackWebview.getSettings().setAllowContentAccess(true);
        this.bind.modifyTrackWebview.getSettings().setAllowFileAccessFromFileURLs(true);
        this.bind.modifyTrackWebview.getSettings().setSupportZoom(true);

        this.bind.modifyTrackWebview.getSettings().setJavaScriptEnabled(true);
        this.bind.modifyTrackWebview.getSettings().setGeolocationEnabled(true);

        this.bind.modifyTrackWebview.addJavascriptInterface(new ModifyTrackInterface(), "HybridApp");
        this.bind.modifyTrackWebview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        this.bind.modifyTrackWebview.getSettings().setSupportMultipleWindows(true);
        this.bind.modifyTrackWebview.getSettings().setDomStorageEnabled(true);

        final String modifyTrackUrl = "http://cmrd-tracker.touring.city/index.html";
        this.bind.modifyTrackWebview.loadUrl(modifyTrackUrl);
    }

    private void loadMapFromWebView() {
        if(this.bind == null || this.recordManager == null) return;
        this.bind.modifyTrackWebview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                super.onGeolocationPermissionsShowPrompt(origin, callback);
                callback.invoke(origin, true, false);
//                modifyTrackWebViewPermListener = callback;
            }
        });

        this.bind.modifyTrackWebview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // 페이지 로드 시, 데이터 전달
                String dataString = parseCourseLocations().toString();
                view.loadUrl("javascript:window.AndroidToWeb('course', '" + dataString +"')");

                dataString = parsePlacemark().toString();
                view.loadUrl("javascript:window.AndroidToWeb('place', '" + dataString +"')");
            }
        });
        final String modifyTrackUrl = "http://cmrd-tracker.touring.city/index.html";
        this.bind.modifyTrackWebview.loadUrl(modifyTrackUrl);
    }

    private class ModifyTrackInterface {

//        @JavascriptInterface
//        public void AndroidToWeb(final String type, final String json) {
//            // might be not used
//        }

        // From WebView to Android Client
        @JavascriptInterface
        public void WebToAndroid(final String key, final String value) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                if(recordManager == null || bind == null) return;
                Log.d("dspark", "key : " + key + " value : " + value);
                if(key.equals("geolocation")) {
                    final double lat = recordManager.getLastObserveLat();
                    final double lng = recordManager.getLastObserveLng();

                    if(lat > 0.0f && lng > 0.0f) {
                        try {
                            JSONObject toSendCurrentLocationJson = new JSONObject();
                            toSendCurrentLocationJson.put("lat", lat);
                            toSendCurrentLocationJson.put("lng", lng);

                            Log.d("dspark", toSendCurrentLocationJson.toString());
                            bind.modifyTrackWebview.loadUrl("javascript:window.AndroidToWeb('geolocation', '" + toSendCurrentLocationJson + "')");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                } else if (key.equals("modifyPlace")) {
                    try {
                        JSONObject receiveModifyPlaceMarkJson = new JSONObject(value);
                        final int placeMarkId = receiveModifyPlaceMarkJson.getInt("placeMarkId");
                        final double lat = receiveModifyPlaceMarkJson.getDouble("lat");
                        final double lng = receiveModifyPlaceMarkJson.getDouble("lng");
                        recordManager.updatePlaceMark(placeMarkId, lat, lng);

                        String toSendPlaceMarkList = parsePlacemark().toString();
                        bind.modifyTrackWebview.loadUrl("javascript:window.AndroidToWeb('place', '" + toSendPlaceMarkList +"')");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private JSONObject parseCourseLocations() {
        JSONObject requestCourseList = new JSONObject();
//        try {
//            LinkedList<ItemCourseData> clonedCourseList = this.courseRecyclerAdapter.getCloneCourseList();
//            if(clonedCourseList.isEmpty()) return null;
//
//            requestCourseList = new JSONObject(); // Header
//            JSONArray requestCourseBody = new JSONArray(); // Body
//
//            // Header
//            requestCourseList.put("trackName", this.currentTrackName);
//            requestCourseList.put("cmrdId", this.currentTrackId);
//            requestCourseList.put("sido", this.currentTrackRegion);
//            double lat;
//            double lng;
//            int index = 0;
//
//            // Body
//            for (ItemCourseData courseData : clonedCourseList) {
//                LinkedList<LocationExtended> courseList = this.recordManager.getCourseLocationList(courseData.getTrackName(), courseData.getCourseName());
//
//                if (!courseList.isEmpty()) {
//                    try {
//                        JSONArray toSendCourseLocations = new JSONArray();
//                        JSONObject toSendCourseData = new JSONObject();
//                        toSendCourseData.put("courseName", courseData.getCourseName());
//
//                        for (LocationExtended courseLocation : courseList) {
//                            lat = courseLocation.getLatitude();
//                            lng = courseLocation.getLongitude();
//                            JSONObject courseLocationBuffer = new JSONObject();
//
//                            courseLocationBuffer.put("lat", lat);
//                            courseLocationBuffer.put("lng", lng);
//                            toSendCourseLocations.put(courseLocationBuffer);
//                        }
//                        toSendCourseData.put("courseLocations", toSendCourseLocations);
//                        requestCourseBody.put(index++, toSendCourseData);
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//            requestCourseList.put("data", requestCourseBody);
//            Log.d("dspark", requestCourseList.toString());
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        return requestCourseList;
    }

    private JSONObject parsePlacemark() {
        JSONObject requestPlaceMarkList = new JSONObject();

//        try {
//            LinkedList<ItemCourseData> clonedCourseList = this.courseRecyclerAdapter.getCloneCourseList();
//            if(clonedCourseList.isEmpty()) return null;
//
//            requestPlaceMarkList = new JSONObject(); // Header
//            JSONArray requestPlaceMarkBody = new JSONArray(); // Body
//
//            // Header
//            requestPlaceMarkList.put("trackName", this.currentTrackName);
//            requestPlaceMarkList.put("cmrdId", this.currentTrackId);
//            requestPlaceMarkList.put("sido", this.currentTrackRegion);
//            int placeMarkId;
//            double lat;
//            double lng;
//            boolean isEnablePlaceMark;
//            int index = 0;
//
//            LinkedList<LocationExtended> placemarkList = this.recordManager.getPlaceMarkByTrackName(this.currentTrackName);
//            for(LocationExtended placemarkData : placemarkList) {
//                JSONObject placeMarkItem = new JSONObject();
//                placeMarkId = placemarkData.getPrimaryId();
//                isEnablePlaceMark = placemarkData.isEnable();
//                lat = placemarkData.getLatitude();
//                lng = placemarkData.getLongitude();
//                String placeMarkName = placemarkData.getName();
//                String placeMarkType = placemarkData.getType();
//
//
//                // this Data is not valid
//                if(!isEnablePlaceMark || lat <= 0.0f || lng <= 0.0f) continue;
//
//                placeMarkItem.put("placeMarkId", placeMarkId);
//                placeMarkItem.put("placeMarkName", placeMarkName);
//                placeMarkItem.put("placeMarkType", placeMarkType);
//                placeMarkItem.put("lat", lat);
//                placeMarkItem.put("lng", lng);
//                requestPlaceMarkBody.put(index++, placeMarkItem);
//            }
//            requestPlaceMarkList.put("data", requestPlaceMarkBody);
//            Log.d("dspark", requestPlaceMarkList.toString());
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        return requestPlaceMarkList;
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

    private void updateUpsideControlButtonState() {
        if (this.bind == null || this.recordManager == null) return;
//        this.mBind.proofSatliteTxt.setText(String.format("%d/%d : %s", this.recordManager.getAvailableSatellitesCnt(), this.recordManager.getTotalSatellitesCnt(), this.getString(R.string.satellites)));

        final boolean isRecording = this.recordManager.isRecordingCourse();

        this.bind.stopRecordBtn.setFocusable(isRecording);
        this.bind.stopRecordBtn.setClickable(isRecording);
        this.bind.stopRecordBtn.setEnabled(isRecording);
        this.bind.stopRecordBtn.setText(isRecording ? "종료" : "            ");
        this.bind.stopRecordBtn.setCompoundDrawablesWithIntrinsicBounds(isRecording ? R.drawable.ic_stop_enable_24 : 0, 0, 0, 0);

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

        // when BottomSheet has Expended and press Back,
        // BottomSheet Close
        } else if (this.isModifyTrackExpended) {
            BottomSheetBehavior<LinearLayout> modifyTrackSheet = BottomSheetBehavior.from(this.bind.modifyTrackRoot);
            modifyTrackSheet.setState(STATE_COLLAPSED);
            this.isModifyTrackExpended = false;
            modifyTrackSheet = null;

        // Exit Recording Activity
        } else {
            super.onBackPressed();
        }
    }

    private void releaseWebView() {
        if(this.bind != null) {
            this.bind.modifyTrackWebview.removeJavascriptInterface("HybridApp");
            this.bind.modifyTrackWebview.setWebViewClient(null);
            this.bind.modifyTrackWebview.setWebChromeClient(null);
            this.bind.modifyTrackWebview.clearCache(false);
            this.bind.modifyTrackWebview.removeAllViews();
            this.bind.modifyTrackWebview.destroyDrawingCache();
            this.bind.modifyTrackWebview.destroy();
        }
    }

    @Override
    protected void onDestroy() {
        this.releaseWebView();

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

        if(this.exporterManager != null) {
            this.exporterManager.release();
            this.exporterManager = null;
        }

        if(this.requestManager != null) {
            this.requestManager.release();
            this.requestManager = null;
        }
        this.recordManager = null;

        this.bind = null;
        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }
}
