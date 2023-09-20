package eu.basicairdata.graziano.gpslogger.recording.enhanced;

import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_DRAGGING;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HALF_EXPANDED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_SETTLING;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.MenuItem;
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
import java.util.ArrayList;
import java.util.LinkedList;

import eu.basicairdata.graziano.gpslogger.BuildConfig;
import eu.basicairdata.graziano.gpslogger.EventBusMSG;
import eu.basicairdata.graziano.gpslogger.GPSApplication;
import eu.basicairdata.graziano.gpslogger.R;
import eu.basicairdata.graziano.gpslogger.Track;
import eu.basicairdata.graziano.gpslogger.databinding.ActivityRecordEnahnecdBinding;
import eu.basicairdata.graziano.gpslogger.management.CourseRoadType;
import eu.basicairdata.graziano.gpslogger.management.ExporterManager;
import eu.basicairdata.graziano.gpslogger.management.ImageManager;
import eu.basicairdata.graziano.gpslogger.management.ItemTrackRecord;
import eu.basicairdata.graziano.gpslogger.management.RequestRecordManager;
import eu.basicairdata.graziano.gpslogger.management.RequestTrackManager;
import eu.basicairdata.graziano.gpslogger.management.TrackRecordManager;
import eu.basicairdata.graziano.gpslogger.recording.AddCourseNameDialog;
import eu.basicairdata.graziano.gpslogger.recording.LoadingDialog;

public class RecordEnhancedActivity extends AppCompatActivity {
    private final String RESTORE_LATEST_IMG_URI = "LATEST_CAPTURE_IMG_URI";
    private final String RESTORE_LATEST_IMG_TYPE = "LATEST_CAPTURE_IMG_TYPE";
    private boolean isRestored = false;
//    private final String RESTORE_TRACK_RECORD = "LATEST_TRACK_RECORDS";

    private ActivityRecordEnahnecdBinding bind; // this View n Layout Instance
    private boolean isModifyTrackExpended = false; // id Behavior Bottom Sheet has Expended? ( state )
    private Toast toast; // using this activity's toast

    private RequestTrackManager requestTrackManager;
    private RequestRecordManager requestManager;
    private PlaceMarkEnhancedRecyclerAdapter placeMarkListAdapter; // POI list
    private CourseNameRecyclerAdapter courseRecyclerAdapter; // upside Course list

    private ActivityResultLauncher<Intent> requestCamera; // Camera
    private ActivityResultLauncher<Intent> requestExportDir; // RW Document;

    private ItemCourseEnhancedData removeCourseBeforeAddCourse = null;
    private Uri tmpFile; // taken a Picture's File Instance

    private TrackRecordManager recordManager = TrackRecordManager.getInstance(); // Track, Course, POI control Manager.
    private ExporterManager exporterManager; // Export Course, Placemark
//    private RequestPlaceMarkManager requestPlaceMarkManager; // Request and Send Placemark Info to Server

    private boolean isPauseCourseRecording = false; // this is FLAG course recording has paused ( state )
    private int currentTrackId = -1; // this course's parent track Id;
    private String currentTrackName = ""; // this course's Parents Track Name
    private String currentTrackRegion; // this course Region
//    private int currentPoiId = -1;
    private String currentPoiType = ""; // selected POI Type
    private String currentPoiName = ""; // selected POI Name
    private boolean currentPoiEnable = true; // is POI enabled( POI's checkbox )

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setTheme(R.style.MyMaterialTheme);
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            this.tmpFile = Uri.parse(savedInstanceState.getString(RESTORE_LATEST_IMG_URI, ""));
            this.currentPoiType = savedInstanceState.getString(RESTORE_LATEST_IMG_TYPE, "");
            this.isRestored = true;

            Log.w("RESTORE", "RESTORE LATEST IMG URI " + this.tmpFile);
            Log.w("RESTORE", "RESTORE LATEST IMG TYPE " + this.currentPoiType);
        }

        this.bind = ActivityRecordEnahnecdBinding.inflate(this.getLayoutInflater());
        setContentView(this.bind.getRoot());

        toast = new Toast(this.bind.getRoot().getContext());
        // FINAL VALUE, NEVER MODIFY THIS
        this.currentTrackId = this.getIntent().getIntExtra(GPSApplication.ATV_EXTRA_TRACK_ID, -1);
        this.currentTrackName = this.getIntent().getStringExtra(GPSApplication.ATX_EXTRA_TRACK_TITLE);
        this.currentTrackRegion = this.getIntent().getStringExtra(GPSApplication.ATV_EXTRA_TRACK_REGION);

        this.bind.toolbarTitle.setText(this.currentTrackName);
        this.setSupportActionBar(this.bind.idToolbar);
        this.getSupportActionBar().setDisplayShowTitleEnabled(false);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // register GPS Event
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        EventBus.getDefault().register(this);
        this.exporterManager = new ExporterManager(GPSApplication.getInstance(), this.bind.getRoot().getContext());
        this.requestManager = new RequestRecordManager();
        this.requestTrackManager = new RequestTrackManager();

        // Request Perm to write GPX File
        this.requestExportDir = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
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
                        this.requestManager.requestAddImg((int) currentTrackId, currentPoiType, ImageManager.Companion.getFileFromImageURI(this.bind.getRoot().getContext(), tmpFile), fileName, new RequestRecordManager.OnRequestResponse<>() {
                            @Override
                            public void onRequestResponse(ItemPlaceMarkImgData response, boolean isSuccess) {
                                runOnUiThread(() -> {
                                    // unusual, fail task...
                                    if (isSuccess) {
                                        response.setTrackId((int) currentTrackId);
                                        if (placeMarkListAdapter != null) placeMarkListAdapter.addPlaceMarkImg(response);

                                    } else {
                                        runOnUiThread(() -> {
                                            Toast.makeText(GPSApplication.getInstance(), "서버와 통신에 실패했습니다. 다시 시도해 주세요", Toast.LENGTH_SHORT).show();
                                        });
                                    }
                                });
                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                    this.recordManager.addPlaceMark(currentPoiName, currentPoiType, currentTrackRegion, currentTrackName, currentPoiEnable);

                } else {
                    Toast.makeText(bind.getRoot().getContext(), "사진 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }

            } else if (result.getResultCode() == RESULT_CANCELED) {
                try {
                    ImageManager.Companion.removeLastImage(bind.getRoot().getContext(), currentPoiType);

                } catch (CursorIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "사진 촬영에 실패했습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });
        this.initCourseList(true);
        this.initPlaceMarkList();
        this.requestTrackRecords(isRestored);
        this.initViewEvent();
        this.initModifyTrackBottomSheet();
        this.initWebView();
    }

    /**
     * Init Course RecyclerView
     * Not Load Data here!
     * @param createEmptyCourse create Empty Course if it empty
     */
    private void initCourseList(boolean createEmptyCourse) {
        if(this.recordManager == null || this.bind == null) return;

        LinkedList<Track> rawCourseList = recordManager.getCourseListByTrackName(this.currentTrackName);
        if(rawCourseList.isEmpty() && createEmptyCourse) {
            this.recordManager.createBlankTables(this.currentTrackId, this.currentTrackName, "코스 1", this.currentTrackRegion);
        }

        LinearLayoutManager courseRecyclerLayoutManager = new LinearLayoutManager(this);
        courseRecyclerLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        this.bind.recordCourseList.setLayoutManager(courseRecyclerLayoutManager);

        this.courseRecyclerAdapter = new CourseNameRecyclerAdapter(new CourseNameRecyclerAdapter.OnItemSelectListener() {
            @Override
            public void onItemSelected(String courseType, ItemCourseEnhancedData item) {
                // when course select, Check Box state has change
                bind.checkDeckCheckbox.setChecked(item.calcIsCheck(CourseRoadType.WOOD_DECK.name()));
                bind.checkDirtCheckbox.setChecked(item.calcIsCheck(CourseRoadType.DIRT.name()));
            }
        });
        this.bind.recordCourseList.setAdapter(this.courseRecyclerAdapter);
    }

    /**
     * Init PlaceMark RecyclerView
     * Not Load Data here!
     */
    private void initPlaceMarkList() {
        if(this.bind == null || this.recordManager == null) return;

        // init Placemark List
        LinearLayoutManager placeMarkLayoutManager = new LinearLayoutManager(this);
        this.bind.modifyPlacemarkTypeList.setLayoutManager(placeMarkLayoutManager);
        this.placeMarkListAdapter = new PlaceMarkEnhancedRecyclerAdapter((imgData, pos) -> {
            // show Image ( Placemark )
            ImageDetailDialog imgDialog = new ImageDetailDialog(bind.getRoot().getContext(), imgData, new ImageDetailDialog.OnRemoveBtnClickedListener() {
                @Override
                public void onRemoveBtnClicked(ItemPlaceMarkImgData removeImgData) {
                    // avoid NPE
                    if(requestManager != null && placeMarkListAdapter != null) {
                        // request REMOVE IMG to Server
                        requestManager.requestRemoveImg(removeImgData.getImgId(), null);
                        placeMarkListAdapter.removePlaceMarkImg(imgData);
                    }
                }
            });
            imgDialog.show();

        // Add Image( Placemark )
        }, (placemarkItem, pos) -> {
            if (!recordManager.isAvailableRecord() || recordManager.getLastObserveLat() == 0.0f || recordManager.getLastObserveLng() == 0.0f) {
                toast.cancel();
                toast = Toast.makeText(bind.getRoot().getContext(), "시설을 기록할수 없습니다. GPS 상태를 확인해 주세요", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            currentPoiName = placemarkItem.getPlaceMarkTitle();
            currentPoiType = placemarkItem.getPlaceMarkType();
            currentPoiEnable = placemarkItem.isPlaceMarkEnable();

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            ImageManager.Companion.createEmptyDirectory("Trekking/" + currentTrackName + "/" + placemarkItem.getPlaceMarkType(), placemarkItem.getPlaceMarkTitle());

            tmpFile = ImageManager.Companion.createTmpFile(
                    bind.getRoot().getContext(),
                    placemarkItem.getPlaceMarkType(),
                    "Trekking/" + currentTrackName + "/" + placemarkItem.getPlaceMarkType() + "/" + placemarkItem.getPlaceMarkTitle());

            intent.putExtra(MediaStore.EXTRA_OUTPUT, tmpFile);
            requestCamera.launch(intent);
        });
        this.bind.modifyPlacemarkTypeList.setAdapter(this.placeMarkListAdapter);
    }

    /**
     * request Course List to Server
     */
    private void requestCourseRecord(final boolean isDelay) {
        if(this.bind == null || this.recordManager == null || this.requestManager == null || this.courseRecyclerAdapter == null) return;
        this.requestManager.requestCourse((int) this.currentTrackId, isDelay? 1000L: 0L, new RequestRecordManager.OnRequestResponse<>() {
            @Override
            public void onRequestResponse(ItemTrackRecord response, boolean isSuccess) {
                runOnUiThread(() -> {
                    if (courseRecyclerAdapter != null && isSuccess) {
                        courseRecyclerAdapter.addCourseItems(response.getItemCourseList());
                    }
                });
            }
        });
    }

    /**
     * request Course Item to Server
     *
     * @param toRemoveCourse to Remove Course Item
     * @param listener CallBack Listener Nullable
     */
    private void removeCourseRecord(@NonNull final ItemCourseEnhancedData toRemoveCourse, @Nullable RequestRecordManager.OnRequestResponse<ItemCourseEnhancedData> listener) {
        if(this.bind == null || this.recordManager == null || this.requestManager == null || this.courseRecyclerAdapter == null) return;
        this.requestManager.requestRemoveCourse(toRemoveCourse, listener);
    }

    /**
     * request Track Record From Server
     * @param isRestored is it true, pause 3sec and request course List
     */
    private void requestTrackRecords(boolean isRestored) {
        if(this.bind == null || this.recordManager == null || this.requestManager == null) return;
        final long delayMillis = isRestored? 3000L: 0L;
        LoadingDialog loadingDialog;
        if (isRestored) {
            loadingDialog = new LoadingDialog(this);
            loadingDialog.show();

        } else {
            loadingDialog = null;
        }
        this.requestManager.requestTrackRecords((int) this.currentTrackId, delayMillis, new RequestRecordManager.OnRequestResponse<>() {
            @Override
            public void onRequestResponse(ItemTrackRecord response, boolean isSuccess) {
                runOnUiThread(() -> {
                    if (isSuccess) {
                        // SET Course Items
                        if (courseRecyclerAdapter != null) {
                            if (response.getItemCourseList() == null || response.getItemCourseList().isEmpty()) {
                                courseRecyclerAdapter.addCourseItem(new ItemCourseEnhancedData(currentTrackName, "코스 1", (int) currentTrackId, -1, 0.0f, CourseRoadType.WOOD_DECK.name()));

                            } else {
                                courseRecyclerAdapter.addCourseItems(response.getItemCourseList());
                            }
                        }
                        // SET Placemark Items
                        if (placeMarkListAdapter != null) placeMarkListAdapter.addPlaceMark(response.getItemPlaceMarkList());

                    } else {
                        Toast.makeText(bind.getRoot().getContext(), "데이터를 가져오는데 실패했습니다. 다시 시도해 주세요", Toast.LENGTH_SHORT).show();
                    }
                    if (loadingDialog != null && bind != null) loadingDialog.dismiss();
                });
            }
        });
    }

    /**
     * Recev Event when Recording, Exporting, GeoLocation
     * @param msg Event Msg
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Short msg) {
        if(msg == EventBusMSG.UPDATE_FIX) { // GPS SIGNAL RECEIVE
            Log.i("GPS_STATE", "UPDATE_FIX");
            this.updateUpsideControlPanelState();

        } else if (msg == EventBusMSG.UPDATE_TRACK) { // RECORDING COURSE
            Log.i("GPS_STATE", "UPDATE_TRACK");

        } else if (msg == EventBusMSG.NEW_TRACK) { // A NEW NEW TRACK INCOMING!!!
            if(this.courseRecyclerAdapter != null && this.recordManager != null) {
                LinkedList<Track> rawCourseList = this.recordManager.getCourseList(this.currentTrackName, this.courseRecyclerAdapter.getSelectedCourseName());

                if(!rawCourseList.isEmpty()) {
//                    Track rawCourse = rawCourseList.getLast();
                    // update course list's item
//                    ItemCourseEnhancedData toInsertCourse = new ItemCourseEnhancedData(this.currentTrackName, this.courseRecyclerAdapter.getSelectedCourseName(), (int) rawCourse.getPrimaryId(), -1, (int) rawCourse.getDurationMoving(), rawCourse.getCourseType());
//                    this.courseRecyclerAdapter.replaceCourseItem(toInsertCourse);

                    // when new TRACK has, create *.gpx File
                    this.removeCourseBeforeAddCourse = this.courseRecyclerAdapter.getSelectCourse();
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
            Log.i("GPS_STATE", "NEW_TRACK");

        } else if (msg == EventBusMSG.TRACK_EXPORTED) { // when TRACK EXPORT START!
            Log.i("GPS_STATE", "TRACK_EXPORTED");

        } else if (msg == EventBusMSG.TRACK_COURSE_SEND_SUCCESS) { // when TRACK EXPORT SUCCESSFULLY
            Log.i("GPS_STATE", "TRACK_COURSE_SEND_SUCCESS");

            // if already uploaded Course, remove First
            if(this.requestManager != null && this.removeCourseBeforeAddCourse != null && this.removeCourseBeforeAddCourse.getCourseId() > -1) {
                this.requestManager.requestRemoveCourse(this.removeCourseBeforeAddCourse, (response, isSuccess) -> {
                    if(!isSuccess && toast != null) {
                        runOnUiThread(() -> {
                            if(toast != null) toast.cancel();
                            toast = Toast.makeText(bind.getRoot().getContext(), "코스 덮어쓰기에 실패 했습니다.", Toast.LENGTH_SHORT);
                            toast.show();
                        });
                    }
                });
            }
            this.initCourseList(false);
            this.requestCourseRecord(true);
        }
    }

    /**
     * Init Others View's Event
     */
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
                        if(toast != null) toast.cancel();
                        toast = Toast.makeText(bind.getRoot().getContext(), "이미 존재하는 코스입니다.", Toast.LENGTH_SHORT);
                        toast.show();

                    } else {
                        courseRecyclerAdapter.addCourseItem(new ItemCourseEnhancedData(currentTrackName, receiveMessage, -1,-1, 0.0f, CourseRoadType.WOOD_DECK.name()));
                    }
                }
            });
            inputCourseNameDialog.show(this.getSupportFragmentManager(), "Input POI Type Dialog");
        });

        // upside "Remove CourseX" btn
        this.bind.courseRemoveBtn.setOnClickListener(view -> {
            if(this.courseRecyclerAdapter.getItemCount() > 1) {

                // Before Remove Course, Ask User Confirm
                final ItemCourseEnhancedData toRemoveCourse = this.courseRecyclerAdapter.getSelectCourse();

                // Avoid NPE
                if(toRemoveCourse == null) return;

                // is course already Uploaded? ask before remove course
                if(toRemoveCourse.getCourseId() > -1) {
                    ConfirmDialog confirmDialog = new ConfirmDialog(this.bind.getRoot().getContext(), "코스 기록을 삭제할까요?\n(삭제된 기록은 복구할 수 없습니다.)", new ConfirmDialog.OnDialogActionListener() {
                        @Override
                        public void onConfirmClick() {
                            final String toRemoveCourseName = courseRecyclerAdapter.getSelectedCourseName();
                            requestManager.requestRemoveCourse(courseRecyclerAdapter.getSelectCourse(), new RequestRecordManager.OnRequestResponse<ItemCourseEnhancedData>() {
                                @Override
                                public void onRequestResponse(ItemCourseEnhancedData response, boolean isSuccess) {
                                    if(isSuccess && isModifyTrackExpended) {
                                        runOnUiThread(RecordEnhancedActivity.this::loadMapFromWebView);
                                    }
                                }
                            });
                            recordManager.removeCourse(currentTrackName, toRemoveCourseName);
                            courseRecyclerAdapter.removeCourse(currentTrackName, toRemoveCourseName);
                        }

                        @Override
                        public void onCancelClick() {
                            // Do Nothing...
                        }
                    });
                    confirmDialog.show();

                // is this course not Uploaded? REMOVE NOW!
                } else {
                    final String toRemoveCourseName = courseRecyclerAdapter.getSelectedCourseName();
                    requestManager.requestRemoveCourse(courseRecyclerAdapter.getSelectCourse(), new RequestRecordManager.OnRequestResponse<ItemCourseEnhancedData>() {
                        @Override
                        public void onRequestResponse(ItemCourseEnhancedData response, boolean isSuccess) {
                            if(isSuccess && isModifyTrackExpended) {
                                runOnUiThread(RecordEnhancedActivity.this::loadMapFromWebView);
                            }
                        }
                    });
                    recordManager.removeCourse(currentTrackName, toRemoveCourseName);
                    courseRecyclerAdapter.removeCourse(currentTrackName, toRemoveCourseName);
                }

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
            final ItemCourseEnhancedData toRecordCourseItem = this.courseRecyclerAdapter.getSelectCourse();

            // AVOID NPE! if not select anything, will return NULL
            if (toRecordCourseItem == null) {
                if(toast != null) toast.cancel();
                toast = Toast.makeText(bind.getRoot().getContext(), "코스를 선택해 주세요.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            final String selectedCourseName = toRecordCourseItem.getCourseName();

            if(!selectedCourseName.isBlank()) {
                if(!this.recordManager.isRecordingCourse() && !this.isPauseCourseRecording) {
                    if(toRecordCourseItem.getCourseId() < 0) { // start NEW Record Course
                        this.recordManager.startRecordCourse(this.currentTrackId, currentTrackName, selectedCourseName, currentTrackRegion);
                        this.isPauseCourseRecording = false;

                    } else { // show warning Override Course Dialog
                        ConfirmDialog confirmDialog = new ConfirmDialog(this.bind.getRoot().getContext(), new ConfirmDialog.OnDialogActionListener() {
                            @Override
                            public void onConfirmClick() {
                                // override Course
                                recordManager.startRecordCourse(currentTrackId, currentTrackName, selectedCourseName, currentTrackRegion);
                                isPauseCourseRecording = false;
                            }

                            @Override
                            public void onCancelClick() {
                                // Do Nothing...
                            }
                        });
                        confirmDialog.show();
                    }

                } else if (isPauseCourseRecording) {
                    // resume Record Course
                    this.recordManager.resumeRecordCourse();
                    this.isPauseCourseRecording = false;

                } else {
                    // pause Record Course
                    this.recordManager.pauseRecordTrack();
                    this.isPauseCourseRecording = true;
                }
                this.updateUpsideControlPanelState();

            } else {
                if(toast != null) toast.cancel();
                toast = Toast.makeText(bind.getRoot().getContext(), "코스를 선택해 주세요.", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        // upside "Stop Record" btn
        this.bind.stopRecordBtn.setOnClickListener(v -> {
            if(this.bind == null || this.courseRecyclerAdapter == null) return;

            final ItemCourseEnhancedData toRecordCourse = this.courseRecyclerAdapter.getSelectCourse();
            final String selectedCourseName = toRecordCourse.getCourseName();

            if(!selectedCourseName.isBlank()) {
                this.recordManager.stopRecordTrack(this.currentTrackId, this.currentTrackName, selectedCourseName, this.currentTrackRegion, this.courseRecyclerAdapter.getSelectCourse().getCourseType());
                this.isPauseCourseRecording = false;
                this.updateUpsideControlPanelState();
            }
        });

        // upside "Deck" checkBox
        this.bind.checkDeckCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(this.recordManager == null || !this.recordManager.isRecordingCourse() || this.courseRecyclerAdapter == null) return;

            ItemCourseEnhancedData toUpdateCourse = this.courseRecyclerAdapter.getSelectCourse();
            if(toUpdateCourse != null) {

                toUpdateCourse.calcCourseType(isChecked, this.bind.checkDirtCheckbox.isChecked());
                this.courseRecyclerAdapter.updateCourse(toUpdateCourse);
            }
        });

        // upside "Dirt" checkBox
        this.bind.checkDirtCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(this.recordManager == null || !this.recordManager.isRecordingCourse() || this.courseRecyclerAdapter == null) return;

            ItemCourseEnhancedData toUpdateCourse = this.courseRecyclerAdapter.getSelectCourse();
            if(toUpdateCourse != null) {
                toUpdateCourse.calcCourseType(this.bind.checkDeckCheckbox.isChecked(), isChecked);
                this.courseRecyclerAdapter.updateCourse(toUpdateCourse);
            }
        });

        // downside "^" BottomSheet Control Button
        this.bind.modifySheetControlBtn.setOnClickListener(v -> {
            BottomSheetBehavior<LinearLayout> modifyTrackSheet = BottomSheetBehavior.from(this.bind.modifyTrackRoot);
            modifyTrackSheet.setState(this.isModifyTrackExpended? STATE_COLLAPSED: STATE_EXPANDED);
            modifyTrackSheet = null;
        });
    }

    /**
     * Init BottomSheet,
     * Not Init WebView here!
     */
    private void initModifyTrackBottomSheet() {
        BottomSheetBehavior<LinearLayout> modifyTrackSheet = BottomSheetBehavior.from(this.bind.modifyTrackRoot);
        modifyTrackSheet.setGestureInsetBottomIgnored(true);

        modifyTrackSheet.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case STATE_EXPANDED -> { // when BottomSheet Complete Appeared
                        modifyTrackSheet.setDraggable(false);

//                        placeMarkListAdapter.setPlaceMarksIsHidden(true);
                        bind.getRoot().setNestedScrollingEnabled(false);
                        bind.getRoot().setFocusable(false);
                        bind.getRoot().setClickable(false);
                        bind.getRoot().setEnabled(false);
                        isModifyTrackExpended = true;
                        loadMapFromWebView();
                    }

                    case STATE_COLLAPSED, STATE_HIDDEN, STATE_HALF_EXPANDED, STATE_SETTLING -> {
//                        placeMarkListAdapter.setPlaceMarksIsHidden(false);
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

    /**
     * Init BottomSheet's WebView
     */
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

    /**
     * Update BottomSheet's WebView
     * Course, Placemark
     */
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
                JSONObject recordCourse = parseCourseLocations();
                if(recordCourse != null) {
                    view.loadUrl("javascript:window.AndroidToWeb('course', '" + recordCourse +"')");
                }

                String dataString = parsePlaceMarkEnhanced().toString();
                view.loadUrl("javascript:window.AndroidToWeb('place', '" + dataString +"')");
            }
        });
        final String modifyTrackUrl = "http://cmrd-tracker.touring.city/";
        this.bind.modifyTrackWebview.loadUrl(modifyTrackUrl);
    }

    /**
     * JavaScript Interface for BottomSheet's Modify Map WebView
     */
    private class ModifyTrackInterface {

        /**
         * From WebView to Android Client
         *
         * @param key What kind of Doing ???
         * @param value Value
         */
        @JavascriptInterface
        public void WebToAndroid(final String key, final String value) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                if(recordManager == null || bind == null) return;
                Log.d("dspark", "key : " + key + " value : " + value);

                // My Current Place
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

                // moved Placemark Position to WebView
                } else if (key.equals("modifyPlace")) {
                    try {
                        if(requestManager == null ) return;

                        JSONObject receiveModifyPlaceMarkJson = new JSONObject(value);
                        final int photoId = receiveModifyPlaceMarkJson.getInt("photoId");
                        final double lat = receiveModifyPlaceMarkJson.getDouble("lat");
                        final double lng = receiveModifyPlaceMarkJson.getDouble("lng");

                        // Request Update Placemark Position to Server
                        requestManager.requestMoveImgPos(photoId, lat, lng, new RequestRecordManager.OnRequestResponse<>() {
                            @Override
                            public void onRequestResponse(ItemPlaceMarkImgData response, boolean isSuccess) {
                                runOnUiThread(() -> {
                                    if (bind == null || placeMarkListAdapter == null || !isSuccess) return;
                                    placeMarkListAdapter.setPlaceMarkImg(response);

                                    String toSendPlaceMarkList = parsePlaceMarkEnhanced().toString();
                                    bind.modifyTrackWebview.loadUrl("javascript:window.AndroidToWeb('place', '" + toSendPlaceMarkList + "')");
                                });
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * @return Json Object when Course Data Exists
     */
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
                ArrayList<Double> latList = courseData.getLatList();
                ArrayList<Double> lngList = courseData.getLngList();

                if (!latList.isEmpty() && !lngList.isEmpty()) {
                    try {
                        JSONArray toSendCourseLocations = new JSONArray();
                        JSONObject toSendCourseData = new JSONObject();
                        toSendCourseData.put("courseName", courseData.getCourseName());

                        for (int i = 0; i < latList.size(); i++) {
                            lat = latList.get(i);
                            lng = lngList.get(i);
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
            requestCourseList = new JSONObject();
        }
        return requestCourseList;
    }

    /**
     * @return Json Object when Placeamrk Data Exists
     */

    private JSONObject parsePlaceMarkEnhanced() {
        JSONObject requestPlaceMarkHeader = new JSONObject(); // PlaceMark Header
        try {
            LinkedList<ItemPlaceMarkEnhancedData> clonedPlaceMarkList = this.placeMarkListAdapter.getClonedList();
            if(clonedPlaceMarkList.isEmpty()) return requestPlaceMarkHeader;

            requestPlaceMarkHeader.put("trackName", this.currentTrackName);
            requestPlaceMarkHeader.put("cmrdId", this.currentTrackId);
            requestPlaceMarkHeader.put("sido", this.currentTrackRegion);
            JSONArray requestPlaceMarkBody = new JSONArray(); // PlaceMark Body
            int dummyPlaceMarkId = 0;

            for(ItemPlaceMarkEnhancedData itemPlaceMark : clonedPlaceMarkList) {
                final boolean isEnablePlaceMark = itemPlaceMark.isPlaceMarkEnable();
                final int placeMarkId = dummyPlaceMarkId++;
                final String placeMarkName = itemPlaceMark.getPlaceMarkTitle();
                final String placeMarkType = itemPlaceMark.getPlaceMarkType();
                final LinkedList<ItemPlaceMarkImgData> imgList = itemPlaceMark.getPlaceMarkImgItemList();

                if(placeMarkName.equals("label")) continue;
                JSONArray itemSendPlaceMarkImg = new JSONArray();

                if(!isEnablePlaceMark) {
                    JSONObject itemSendPlaceMarkImgBuffer = new JSONObject();
                    itemSendPlaceMarkImgBuffer.put("photoId", 0); // PlaceMark Image Id
                    itemSendPlaceMarkImgBuffer.put("imgUrl", ""); // PlaceMark Image Url
                    itemSendPlaceMarkImgBuffer.put("imgLat", 0.0f); // PlaceMark Image Lat
                    itemSendPlaceMarkImgBuffer.put("imgLng", 0.0f); // PlaceMark Image Lng
                    itemSendPlaceMarkImg.put(itemSendPlaceMarkImgBuffer);

                } else {
                    for(ItemPlaceMarkImgData itemImgBuffer : imgList) {
                        JSONObject itemSendPlaceMarkImgBuffer = new JSONObject();
                        itemSendPlaceMarkImgBuffer.put("photoId", itemImgBuffer.getImgId()); // PlaceMark Image Id
                        itemSendPlaceMarkImgBuffer.put("imgUrl", itemImgBuffer.getImageUrl()); // PlaceMark Image Url
                        itemSendPlaceMarkImgBuffer.put("imgLat", itemImgBuffer.getImgLat()); // PlaceMark Image Lat
                        itemSendPlaceMarkImgBuffer.put("imgLng", itemImgBuffer.getImgLng()); // PlaceMark Image Lng
                        itemSendPlaceMarkImg.put(itemSendPlaceMarkImgBuffer);
                    }
                }
                JSONObject itemSendPlaceMark = new JSONObject();

                // Essential Values
                itemSendPlaceMark.put("placeMarkId", placeMarkId); // PlaceMark Id
                itemSendPlaceMark.put("notExists", !isEnablePlaceMark); // is PlaceMark Enable?
                itemSendPlaceMark.put("placeMarkType", placeMarkType); // PlaceMark Type

                // Optional Values
                itemSendPlaceMark.put("placeMarkName", placeMarkName); // PlaceMark Name ( ex: 화장실 )
                itemSendPlaceMark.put("imgUrl", itemSendPlaceMarkImg); // a PlaceMark Image List
                requestPlaceMarkBody.put(itemSendPlaceMark);
            }
            requestPlaceMarkHeader.put("data", requestPlaceMarkBody);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("dspark", requestPlaceMarkHeader.toString());
        return requestPlaceMarkHeader;
    }

    /**
     * update upside panel's views visibility state
     */
    private void updateUpsideControlPanelState() {
        if (this.bind == null || this.recordManager == null) return;
        final boolean isRecording = this.recordManager.isRecordingCourse();

        this.bind.stopRecordBtn.setFocusable(isRecording);
        this.bind.stopRecordBtn.setClickable(isRecording);
        this.bind.stopRecordBtn.setEnabled(isRecording);

        SpannableString emphaticInformation = new SpannableString("종료");
        emphaticInformation.setSpan(new ForegroundColorSpan(isRecording? Color.parseColor("#ededed"): Color.parseColor("#0099d7")), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        emphaticInformation.setSpan(new StyleSpan(Typeface.BOLD), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        this.bind.stopRecordBtn.setText(emphaticInformation);
        this.bind.stopRecordBtn.setCompoundDrawablesWithIntrinsicBounds(isRecording ? R.drawable.ic_stop_enable_24 : R.drawable.ic_stop_disable_24, 0, 0, 0);

        // update Upside "DECK" Course Type CheckBox
        this.bind.checkDeckCheckbox.setEnabled(isRecording);
        this.bind.checkDeckCheckbox.setClickable(isRecording);
        this.bind.checkDeckCheckbox.setFocusable(isRecording);

        // update Upside "DIRT" Course Type CheckBox
        this.bind.checkDirtCheckbox.setEnabled(isRecording);
        this.bind.checkDirtCheckbox.setClickable(isRecording);
        this.bind.checkDirtCheckbox.setFocusable(isRecording);

        if(!isRecording && !this.isPauseCourseRecording) {
            this.setButtonState(this.bind.recordControlBtn, R.drawable.blue_round_border_32, R.drawable.ic_play_24, R.string.record);

        } else if(isRecording && !this.isPauseCourseRecording) {
            this.setButtonState(this.bind.recordControlBtn, R.drawable.red_round_border_32, R.drawable.ic_pause_24, R.string.pause);

        } else if (isRecording/* && this.isPauseCourseRecording*/) {
            this.setButtonState(this.bind.recordControlBtn, R.drawable.red_round_border_32, R.drawable.ic_play_24, R.string.record);
        }
    }

    /**
     * update Button's visiblity state
     *
     * @param button Button View Instance
     * @param backgroundId to show Img Id
     * @param iconId to show Img Id
     * @param stringId to show Text Id
     */
    private void setButtonState(final TextView button, final int backgroundId, final int iconId, final int stringId) {
        if(stringId != 0) button.setText(stringId);
        if(backgroundId != 0) button.setBackground(AppCompatResources.getDrawable(this.bind.getRoot().getContext(), backgroundId));
        if(iconId != 0) button.setCompoundDrawablesWithIntrinsicBounds(iconId, 0, 0, 0);
    }

    /**
     * release WebView instance
     */
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

        // Exit Recording Enhanced Activity
        } else {
            super.onBackPressed();
        }
    }

    // OFTEN GOTO CAPTURE IMG, CALLED onDestory()
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (this.tmpFile != null) {
            outState.putString(RESTORE_LATEST_IMG_URI, this.tmpFile.toString());
            Log.w("RECORDING", "SAVE LATEST IMG URI " + this.tmpFile.toString());
        }

        if(this.currentPoiType != null && !this.currentPoiType.isBlank()) {
            outState.putString(RESTORE_LATEST_IMG_TYPE, this.currentPoiType);
            Log.w("RECORDING", "SAVE LATEST POI TYPE " + this.currentPoiType);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // toolbar "<-" button
        if (item.getItemId() == android.R.id.home) {
            this.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        this.releaseWebView();

        // SEND Placemark STATE to Server
        LinkedList<ItemPlaceMarkEnhancedData> toCheckPlacemarkModify = this.placeMarkListAdapter.getClonedList();
        for(ItemPlaceMarkEnhancedData isModifyItem : toCheckPlacemarkModify) {
            if(isModifyItem.isPlaceMarkStateChange()) {
                this.requestManager.requestPlacemarkEnableChanged(this.currentTrackId, isModifyItem.getPlaceMarkType(), isModifyItem.isPlaceMarkEnable(), null);
            }
        }

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

        if(this.requestTrackManager != null) {
            this.requestTrackManager.release();
            this.requestTrackManager = null;
        }

        this.requestManager = null;
        this.recordManager = null;
        this.bind = null;
        super.onDestroy();
    }
}