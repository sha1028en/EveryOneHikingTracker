package eu.basicairdata.graziano.gpslogger.recording.enhanced;

import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_DRAGGING;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HALF_EXPANDED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_SETTLING;

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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
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

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;

import eu.basicairdata.graziano.gpslogger.BuildConfig;
import eu.basicairdata.graziano.gpslogger.EventBusMSG;
import eu.basicairdata.graziano.gpslogger.GPSApplication;
import eu.basicairdata.graziano.gpslogger.LocationExtended;
import eu.basicairdata.graziano.gpslogger.R;
import eu.basicairdata.graziano.gpslogger.Track;
import eu.basicairdata.graziano.gpslogger.databinding.ActivityRecordEnahnecdBinding;
import eu.basicairdata.graziano.gpslogger.management.ExporterManager;
import eu.basicairdata.graziano.gpslogger.management.ImageManager;
import eu.basicairdata.graziano.gpslogger.management.ItemTrackRecord;
import eu.basicairdata.graziano.gpslogger.management.RequestPlaceMarkManager;
import eu.basicairdata.graziano.gpslogger.management.RequestRecordManager;
import eu.basicairdata.graziano.gpslogger.management.TrackRecordManager;
import eu.basicairdata.graziano.gpslogger.recording.AddCourseNameDialog;
import eu.basicairdata.graziano.gpslogger.recording.CourseNameRecyclerAdapter;

public class RecordEnhancedActivity extends AppCompatActivity {
    private ActivityRecordEnahnecdBinding bind; // this View n Layout Instance
    private boolean isModifyTrackExpended = false; // id Behavior Bottom Sheet has Expended? ( state )
    private Toast toast; // using this activity's toast

    private RequestRecordManager requestManager;
    private PlaceMarkEnhancedRecyclerAdapter placeMarkListAdapter; // POI list
    private CourseNameRecyclerAdapter courseRecyclerAdapter; // upside Course list

    private ActivityResultLauncher<Intent> requestCamera; // Camera
    private ActivityResultLauncher<Intent> requestExportDir; // RW Document;
    private Uri tmpFile; // taken a Picture's File Instance

    private TrackRecordManager recordManager = TrackRecordManager.getInstance(); // Track, Course, POI control Manager.
    private ExporterManager exporterManager; // Export Course, Placemark
//    private RequestPlaceMarkManager requestPlaceMarkManager; // Request and Send Placemark Info to Server

    private boolean isPauseCourseRecording = false; // this is FLAG course recording has paused ( state )
    private long currentTrackId = -1; // this course's parent track Id;
    private String currentTrackName = ""; // this course's Parents Track Name
    private String currentTrackRegion; // this course Region
    private int currentPoiId = -1;
    private String currentPoiType = ""; // selected POI Type
    private String currentPoiName = ""; // selected POI Name
    private boolean currentPoiEnable = true; // is POI enabled( POI's checkbox )

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setTheme(R.style.MyMaterialTheme);
        super.onCreate(savedInstanceState);

        this.bind = ActivityRecordEnahnecdBinding.inflate(this.getLayoutInflater());
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
//        this.requestPlaceMarkManager = new RequestPlaceMarkManager(this.bind.getRoot().getContext());
        this.requestManager = new RequestRecordManager();

        // Request Perm to write GPX File
        this.requestExportDir = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result != null && bind != null && exporterManager != null) {
                    Intent resultData = result.getData();

                    if (resultData != null) {
                        Uri treeUri = resultData.getData();
                        grantUriPermission(getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                        GPSApplication.getInstance().getContentResolver().takePersistableUriPermission(treeUri, Intent
                                .FLAG_GRANT_READ_URI_PERMISSION | Intent
                                .FLAG_GRANT_WRITE_URI_PERMISSION);

                        Log.w("myApp", "[#] GPSActivity.java - onActivityResult URI: " + treeUri);
                        Log.w("myApp", "[#] GPSActivity.java - onActivityResult URI: " + treeUri.getPath());
                        Log.w("myApp", "[#] GPSActivity.java - onActivityResult URI: " + treeUri.getEncodedPath());
                        exporterManager.setExportDir(treeUri);
                        exporterManager.export(currentTrackName, courseRecyclerAdapter.getSelectedCourseName());
                    }
                }
            }
        });

        // Request to Camera Action
        // when Placemark list clicked, took Picture
        // and register Placemark with taken Picture
        this.requestCamera = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(bind == null || placeMarkListAdapter == null || recordManager == null) return;
            if (result.getResultCode() == RESULT_OK) {
                String fileName = ImageManager.Companion.parseNameFromUri(bind.getRoot().getContext(), tmpFile);

                if (!fileName.isBlank()) {
                    try {
                        ImageManager.Companion.addLocationIntoImage(ImageManager.Companion.getFileFromImageURI(bind.getRoot().getContext(), tmpFile), recordManager.getLastObserveLat(), recordManager.getLastObserveLng());

                        this.requestManager.requestAddImg((int) currentTrackId, currentPoiType, ImageManager.Companion.getFileFromImageURI(this.bind.getRoot().getContext(), tmpFile), fileName, new RequestPlaceMarkManager.OnRequestResponse<ItemPlaceMarkImgData>() {
                            @Override
                            public void onRequestResponse(ItemPlaceMarkImgData response, boolean isSuccess) {
                                runOnUiThread(() -> {
                                    response.setTrackId(currentPoiId);
                                    placeMarkListAdapter.addPlaceMarkImg(response);
                                });
                            }
                        });

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    this.recordManager.addPlaceMark(currentPoiName, currentPoiType, currentTrackRegion, currentTrackName, currentPoiEnable);

                } else {
                    Toast.makeText(bind.getRoot().getContext(), "사진 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }

            } else if (result.getResultCode() == RESULT_CANCELED) {
                ImageManager.Companion.removeLastImage(bind.getRoot().getContext(), currentPoiType);
            }
        });

        this.initCourseList();
        this.initPlaceMarkList();
        this.requestTrackRecords();
        this.initViewEvent();
        this.initModifyTrackBottomSheet();
        this.initWebView();
    }

    private void initCourseList() {
        if(this.recordManager == null || this.bind == null) return;

        LinkedList<Track> rawCourseList = recordManager.getCourseListByTrackName(this.currentTrackName);
        if(rawCourseList.isEmpty()) {
            this.recordManager.createBlankTables(this.currentTrackId, this.currentTrackName, "코스 1", this.currentTrackRegion);
        }

        LinearLayoutManager courseRecyclerLayoutManager = new LinearLayoutManager(this);
        courseRecyclerLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        this.bind.recordCourseList.setLayoutManager(courseRecyclerLayoutManager);

        this.courseRecyclerAdapter = new CourseNameRecyclerAdapter(new CourseNameRecyclerAdapter.OnItemSelectListener() {
            @Override
            public void onItemSelected(boolean isDeck, ItemCourseEnhancedData item) {
                // when course select, Check Box state has change
                bind.checkDeckCheckbox.setChecked(isDeck);
            }
        });
        this.bind.recordCourseList.setAdapter(this.courseRecyclerAdapter);
    }

    private void initPlaceMarkList() {
        if(this.bind == null || this.recordManager == null) return;

        // init Placemark List
        LinearLayoutManager placeMarkLayoutManager = new LinearLayoutManager(this);
        this.bind.modifyPlacemarkTypeList.setLayoutManager(placeMarkLayoutManager);
        this.placeMarkListAdapter = new PlaceMarkEnhancedRecyclerAdapter(new PlaceMarkEnhancedRecyclerAdapter.PlacemarkTypeViewHolder.OnImageSelectedListener() {
            @Override
            public void onImageSelect(ItemPlaceMarkImgData imgData, int pos) {
                ImageDetailDialog imgDialog = new ImageDetailDialog(bind.getRoot().getContext(), imgData, new ImageDetailDialog.OnRemoveBtnClickedListener() {
                    @Override
                    public void onRemoveBtnClicked(ItemPlaceMarkImgData removeImgData) {

                        // avoid NPE
                        if(requestManager != null && placeMarkListAdapter != null) {

                            // REMOVE IMG
                            requestManager.requestRemoveImg(removeImgData.getImgId(), null);
                            placeMarkListAdapter.removePlaceMarkImg(imgData);
                        }
                    }
                });
                imgDialog.show();
            }

        }, new PlaceMarkEnhancedRecyclerAdapter.OnAddImageClickListener() {
            @Override
            public void onAddImageClick(ItemPlaceMarkEnhancedData placemarkItem, int pos) {
                if (!recordManager.isAvailableRecord() || recordManager.getLastObserveLat() == 0.0f || recordManager.getLastObserveLng() == 0.0f) {
                    toast.cancel();
                    toast = Toast.makeText(bind.getRoot().getContext(), "시설을 기록할수 없습니다. GPS 상태를 확인해 주세요", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                currentPoiName = placemarkItem.getPlaceMarkTitle();
                currentPoiType = placemarkItem.getPlaceMarkType();
                currentPoiEnable = placemarkItem.isPlaceMarkEnable();
                currentPoiId = placemarkItem.getPlaceMarkId();

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                tmpFile = ImageManager.Companion.createTmpFile(
                        bind.getRoot().getContext(),
                        placemarkItem.getPlaceMarkType(),
                        "Trekking/" + currentTrackName + "/" + placemarkItem.getPlaceMarkType() + "/" + placemarkItem.getPlaceMarkTitle());
                intent.putExtra(MediaStore.EXTRA_OUTPUT, tmpFile);

                requestCamera.launch(intent);
            }
        });
        this.bind.modifyPlacemarkTypeList.setAdapter(this.placeMarkListAdapter);

        LinkedList<LocationExtended> rawPlaceMarkList = recordManager.getPlaceMarkByTrackName(this.currentTrackName);
        LinkedList<ItemPlaceMarkEnhancedData> toSortPlacemarkList = new LinkedList<>();

        for(LocationExtended buffer : rawPlaceMarkList) {
            final String trackName = buffer.getTrackName();
            final String placeMarkType = buffer.getType();
            final String placeMarkName = buffer.getName();
            final String placeMarkDesc = "";
            final boolean placeMarkEnable = true;

            ItemPlaceMarkEnhancedData placeMark = new ItemPlaceMarkEnhancedData(trackName, placeMarkName, placeMarkType, placeMarkDesc, placeMarkEnable);
            toSortPlacemarkList.add(placeMark);
        }
        Collections.sort(toSortPlacemarkList);
        this.placeMarkListAdapter.addPlaceMark(toSortPlacemarkList);
    }

    private void requestTrackRecords() {
        if(this.bind == null || this.recordManager == null || this.requestManager == null) return;
        this.requestManager.requestTrackRecords((int) this.currentTrackId, new RequestRecordManager.OnRequestResponse<ItemTrackRecord>() {
            @Override
            public void onRequestResponse(ItemTrackRecord response, boolean isSuccess) {
                if (isSuccess) {
                    runOnUiThread(() -> {
                        // SET Course Items
                        courseRecyclerAdapter.addCourseItems(response.getItemCourseList());

                        // SET Placemark Img
                        placeMarkListAdapter.addPlaceMarkImg(response.getItemPlacemarkImgList());
                    });
                }
            }
        });
    }

    private void updatePlaceMarkList(@NonNull final LinkedList<ItemPlaceMarkEnhancedData> placemarkList) {
        if(this.bind == null || this.recordManager == null || this.placeMarkListAdapter == null) return;
        Collections.sort(placemarkList);
        this.placeMarkListAdapter.addPlaceMark(placemarkList);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Short msg) {
        if(msg == EventBusMSG.UPDATE_FIX) { // GPS SIGNAL RECEIVE
            Log.i("dspark", "Recording: UPDATE_FIX");
            this.updateUpsideControlButtonState();

        } else if (msg == EventBusMSG.UPDATE_TRACK) { // RECORDING COURSE
            Log.i("dspark", "Recording: UPDATE_TRACK");

        } else if (msg == EventBusMSG.NEW_TRACK) { // A NEW NEW TRACK INCOMING!!!
            if(this.courseRecyclerAdapter != null && this.recordManager != null) {
                LinkedList<Track> rawCourseList = this.recordManager.getCourseList(this.currentTrackName, this.courseRecyclerAdapter.getSelectedCourseName());

                if(!rawCourseList.isEmpty()) {
                    Track rawCourse = rawCourseList.getLast();
                    // update course list's item
                    ItemCourseEnhancedData toUpdateCourse = new ItemCourseEnhancedData(this.currentTrackName, this.courseRecyclerAdapter.getSelectedCourseName(), (int) rawCourse.getPrimaryId(), -1, (int) rawCourse.getDurationMoving(), rawCourse.getCourseType().equals("wood_deck"));
                    this.courseRecyclerAdapter.replaceCourseItem(toUpdateCourse);

                    // when new TRACK has, create *.gpx File
                    if(this.exporterManager.isExportDirHas()) {

                        // if already has Perm? Export NOW!
                        this.exporterManager.export(this.currentTrackName, this.courseRecyclerAdapter.getSelectedCourseName());

                    } else {
                        // request Perm With File Picker by Android System
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
                        intent.putExtra("android.content.extra.FANCY", true);
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

                        // where *.gpx saved? where!
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, exporterManager.pathToUri("Trekking"));
                        }
                        this.requestExportDir.launch(intent);
                    }
                }
            }
            Log.i("dspark", "Recording: NEW_TRACK");

        } else if (msg == EventBusMSG.TRACK_EXPORTED) {  // when TRACK EXPORT SUCCESSFULLY BRANCH HERE!
            Log.i("dspark", "Recording: TRACK_EXPORTED");
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
                    LinkedList<ItemCourseEnhancedData> courseList = courseRecyclerAdapter.getCloneCourseList();

                    boolean alreadyHas = false;
                    for(ItemCourseEnhancedData buffer : courseList) {
                        if(buffer.getCourseName().equals(receiveMessage)) {
                            alreadyHas = true;
                            break;
                        }
                    }

                    if(alreadyHas) {
                        Toast.makeText(bind.getRoot().getContext(), "이미 존재하는 코스입니다", Toast.LENGTH_SHORT).show();

                    } else {
                        courseRecyclerAdapter.addCourseItem(new ItemCourseEnhancedData(currentTrackName, receiveMessage, -1,-1, 0.0f, true));
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
                this.requestManager.requestRemoveCourse(this.courseRecyclerAdapter.getSelectCourse(), null);
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
                    this.recordManager.startRecordCourse(this.currentTrackId, currentTrackName, selectedCourseName, currentTrackRegion);
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
                this.updateUpsideControlButtonState();

            } else {
                Toast.makeText(this.bind.getRoot().getContext(), "코스를 선택해 주세요", Toast.LENGTH_SHORT).show();
            }
        });

        // upside "Stop Record" btn
        this.bind.stopRecordBtn.setOnClickListener(v -> {
            if(this.bind == null || this.courseRecyclerAdapter == null) return;
            final String selectedCourseName = this.courseRecyclerAdapter.getSelectedCourseName();

            if(!selectedCourseName.isBlank()) {
                this.recordManager.stopRecordTrack(this.currentTrackId, this.currentTrackName, selectedCourseName, this.currentTrackRegion, this.courseRecyclerAdapter.getSelectCourse().isWoodDeck());
                this.isPauseCourseRecording = false;
                this.updateUpsideControlButtonState();
            }
        });

        // upside "is Deck" checkBox
        this.bind.checkDeckCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ItemCourseEnhancedData toUpdateCourse = this.courseRecyclerAdapter.getSelectCourse();
            if(toUpdateCourse != null) {
                toUpdateCourse.setWoodDeck(isChecked);
                this.courseRecyclerAdapter.updateCourse(toUpdateCourse);
                this.recordManager.updateCourseType(toUpdateCourse.getTrackName(), toUpdateCourse.getCourseName(), toUpdateCourse.isWoodDeck());
            }
        });

        // downside "^" BottomSheet Control Button
        this.bind.modifySheetControlBtn.setOnClickListener(v -> {
            BottomSheetBehavior<LinearLayout> modifyTrackSheet = BottomSheetBehavior.from(this.bind.modifyTrackRoot);
            modifyTrackSheet.setState(this.isModifyTrackExpended? STATE_COLLAPSED: STATE_EXPANDED);
            modifyTrackSheet = null;
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
                    }

                    case STATE_COLLAPSED, STATE_HIDDEN, STATE_HALF_EXPANDED, STATE_SETTLING -> {
                        bind.rootDisableLayout.setVisibility(View.GONE);
                        placeMarkListAdapter.setPlaceMarksIsHidden(false);
                        bind.getRoot().setFocusable(true);
                        bind.getRoot().setClickable(true);
                        bind.getRoot().setEnabled(true);
                        isModifyTrackExpended = false;
                    }
                    case STATE_DRAGGING -> {}
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
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

        final String modifyTrackUrl = "http://cmrd-tracker.touring.city/";
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

                dataString = parsePlaceMarkEnhanced().toString();
//                dataString = parsePlacemark().toString();
                view.loadUrl("javascript:window.AndroidToWeb('place', '" + dataString +"')");
            }
        });
        final String modifyTrackUrl = "http://cmrd-tracker.touring.city/index.html";
        this.bind.modifyTrackWebview.loadUrl(modifyTrackUrl);
    }

    private class ModifyTrackInterface {

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

                        String toSendPlaceMarkList =  parsePlaceMarkEnhanced().toString(); //parsePlacemark().toString();
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
        try {
            LinkedList<ItemCourseEnhancedData> clonedCourseList = this.courseRecyclerAdapter.getCloneCourseList();
            if(clonedCourseList.isEmpty()) return null;

            requestCourseList = new JSONObject(); // Header
            JSONArray requestCourseBody = new JSONArray(); // Body

            // Header
            requestCourseList.put("trackName", this.currentTrackName);
            requestCourseList.put("cmrdId", this.currentTrackId);
            requestCourseList.put("sido", this.currentTrackRegion);
            double lat;
            double lng;
            int index = 0;

            // Body
            for (ItemCourseEnhancedData courseData : clonedCourseList) {
                LinkedList<LocationExtended> courseList = this.recordManager.getCourseLocationList(courseData.getTrackName(), courseData.getCourseName());

                if (!courseList.isEmpty()) {
                    try {
                        JSONArray toSendCourseLocations = new JSONArray();
                        JSONObject toSendCourseData = new JSONObject();
                        toSendCourseData.put("courseName", courseData.getCourseName());

                        for (LocationExtended courseLocation : courseList) {
                            lat = courseLocation.getLatitude();
                            lng = courseLocation.getLongitude();
                            JSONObject courseLocationBuffer = new JSONObject();

                            courseLocationBuffer.put("lat", lat);
                            courseLocationBuffer.put("lng", lng);
                            toSendCourseLocations.put(courseLocationBuffer);
                        }
                        toSendCourseData.put("courseLocations", toSendCourseLocations);
                        requestCourseBody.put(index++, toSendCourseData);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            requestCourseList.put("data", requestCourseBody);
            Log.d("dspark", requestCourseList.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return requestCourseList;
    }

    private JSONObject parsePlaceMarkEnhanced() {
        JSONObject requestPlaceMarkHeader = new JSONObject(); // PlaceMark Header
        try {
            LinkedList<ItemPlaceMarkEnhancedData> clonedPlaceMarkList = this.placeMarkListAdapter.getClonedList();
            if(clonedPlaceMarkList.isEmpty()) return requestPlaceMarkHeader;

            requestPlaceMarkHeader.put("trackName", this.currentTrackName);
            requestPlaceMarkHeader.put("cmrdId", this.currentTrackId);
            requestPlaceMarkHeader.put("sido", this.currentTrackRegion);
            JSONArray requestPlaceMarkBody = new JSONArray(); // PlaceMark Body

            for(ItemPlaceMarkEnhancedData itemPlaceMark : clonedPlaceMarkList) {
                final boolean isEnablePlaceMark = itemPlaceMark.isPlaceMarkEnable();
                final int placeMarkId = itemPlaceMark.getPlaceMarkId();
                final String placeMarkName = itemPlaceMark.getPlaceMarkTitle();
                final String placeMarkType = itemPlaceMark.getPlaceMarkType();
                final LinkedList<ItemPlaceMarkImgData> imgList = itemPlaceMark.getPlaceMarkImgItemList();

                if(placeMarkName.equals("label")) continue;
                if(imgList.isEmpty()) continue;
                JSONArray itemSendPlaceMarkImg = new JSONArray();
                int i = 0;

                for(ItemPlaceMarkImgData itemImgBuffer : imgList) {
                    if(i++ > 1) break;

                    JSONObject itemSendPlaceMarkImgBuffer = new JSONObject();
                    itemSendPlaceMarkImgBuffer.put("photoId", itemImgBuffer.getImgId()); // PlaceMark Image Id
                    itemSendPlaceMarkImgBuffer.put("imgUrl", itemImgBuffer.getImageUrl()); // PlaceMark Image Url
                    itemSendPlaceMarkImgBuffer.put("imgLat", itemImgBuffer.getImgLat()); // PlaceMark Image Lat
                    itemSendPlaceMarkImgBuffer.put("imgLng", itemImgBuffer.getImgLng()); // PlaceMark Image Lng
                    itemSendPlaceMarkImg.put(itemSendPlaceMarkImgBuffer);
                }
                JSONObject itemSendPlaceMark = new JSONObject();

                // Essential Values
                itemSendPlaceMark.put("placeMarkId", placeMarkId); // PlaceMark Id
                itemSendPlaceMark.put("notExists", isEnablePlaceMark); // is PlaceMark Enable?
                itemSendPlaceMark.put("placeMarkType", placeMarkType); // PlaceMark Type

                // Optional Values
                itemSendPlaceMark.put("placeMarkName", isEnablePlaceMark? placeMarkName: ""); // PlaceMark Name ( ex: 화장실 )
                itemSendPlaceMark.put("imgUrl", isEnablePlaceMark? itemSendPlaceMarkImg: new JSONArray()); // a PlaceMark Image List
                requestPlaceMarkBody.put(itemSendPlaceMark);
            }
            requestPlaceMarkHeader.put("data", requestPlaceMarkBody);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return requestPlaceMarkHeader;
    }

    private void updateUpsideControlButtonState() {
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

    private void verifyTrackCourseValid() {
        LinkedList<ItemCourseEnhancedData> courseList = this.courseRecyclerAdapter.getCloneCourseList();
        boolean hasValidCourse = false;

        for(ItemCourseEnhancedData item : courseList) {
            if(item.getCourseDistance() > 0.0f && !item.getCourseName().isBlank()) {
                hasValidCourse = true;
                break;
            }
        }

        if(hasValidCourse) {
//            this.recordManager.updateTt
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
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

//        if(this.requestPlaceMarkManager != null) {
//            this.requestPlaceMarkManager.release();
//            this.requestPlaceMarkManager = null;
//        }

//        if(this.requestTrackManager != null) {
//            this.requestTrackManager.release();
//            this.requestTrackManager = null;
//        }
        this.recordManager = null;
        this.bind = null;
        super.onDestroy();
    }
}