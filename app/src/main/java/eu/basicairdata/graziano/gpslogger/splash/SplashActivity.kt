package eu.basicairdata.graziano.gpslogger.splash


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import eu.basicairdata.graziano.gpslogger.GPSApplication
import eu.basicairdata.graziano.gpslogger.R
import eu.basicairdata.graziano.gpslogger.databinding.ActivitySplashBinding
import eu.basicairdata.graziano.gpslogger.tracklist.TrackListActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val bind by lazy { ActivitySplashBinding.inflate(this.layoutInflater) }
    private val REQUEST_ID_MULTIPLE_PERMISSIONS = 1
    private val gpsApp = GPSApplication.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setTheme(R.style.MyMaterialTheme)
        super.onCreate(savedInstanceState)
        setContentView(this.bind.root)

        // show this layout during 2 sec
        this.bind.root.postDelayed({
            this.checkPermissions(Build.VERSION.SDK_INT)

        }, 2000L)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        var allGrantPerm = true

        if (requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS) {
            val perms: MutableMap<String, Int> = HashMap()
            if (grantResults.isNotEmpty()) {
                // Fill with actual results from user
                for (i in permissions.indices) perms[permissions[i]] = grantResults[i]
                // Check for permissions


                if (perms.containsKey(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == PackageManager.PERMISSION_GRANTED) {
                        Log.w("myApp", "[#] SplashActivity.java - ACCESS_FINE_LOCATION = PERMISSION_GRANTED; setGPSLocationUpdates!")
                        gpsApp.setGPSLocationUpdates(false)
                        gpsApp.setGPSLocationUpdates(true)
                        gpsApp.updateGPSLocationFrequency()

                    } else {
                        Toast.makeText(this.bind.root.context, "위치 권한을 허용해 주셔야\n앱을 사용 하실수 있습니다.", Toast.LENGTH_SHORT).show()
                        Log.w("myApp", "[#] SplashActivity.java - ACCESS_FINE_LOCATION = PERMISSION_DENIED")
                        allGrantPerm = false
                    }
                }

                if (perms.containsKey(Manifest.permission.INTERNET)) {
                    if (perms[Manifest.permission.INTERNET] == PackageManager.PERMISSION_GRANTED) {
                        Log.w("myApp", "[#] SplashActivity.java - INTERNET = PERMISSION_GRANTED")


                    } else {
                        Log.w("myApp", "[#] SplashActivity.java - INTERNET = PERMISSION_DENIED")
                        allGrantPerm = false
                    }
                }

                if(perms.containsKey(Manifest.permission.CAMERA)) {
                    if(perms[Manifest.permission.CAMERA] == PackageManager.PERMISSION_GRANTED) {
                        Log.w("myApp", "[#] SplashActivity.java - CAMERA = PERMISSION_GRANTED")

                    } else {
                        Toast.makeText(this.bind.root.context, "카메라 권한을 허용해 주셔야\n앱을 사용 하실수 있습니다.", Toast.LENGTH_SHORT).show()
                        Log.w("myApp", "[#] SplashActivity.java - CAMERA = PERMISSION_DENIED")
                        allGrantPerm = false
                    }
                }

                // TO READ IMAGES
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && perms.containsKey(Manifest.permission.READ_MEDIA_IMAGES)) {
                    if(perms[Manifest.permission.READ_MEDIA_IMAGES] == PackageManager.PERMISSION_GRANTED) {
                        Log.w("myApp", "[#] SplashActivity.java - READ_MEDIA_IMG = PERMISSION_GRANTED")

                    } else {
                        Toast.makeText(this.bind.root.context, "사진 및 동영상 권한을\n허용해 주셔야 앱을 사용 하실수 있습니다.", Toast.LENGTH_SHORT).show()
                        Log.w("myApp", "[#] SplashActivity.java - READ_MEDIA_IMG = PERMISSION_DENIED")
                        allGrantPerm = false
                    }

                    // this PERM is not MUST GRANT perm
                    // if it isnt GRANT PERM, didnt post Notification to show GPS_STATE
                    if(perms[Manifest.permission.POST_NOTIFICATIONS] == PackageManager.PERMISSION_GRANTED) {
                        Log.w("myApp", "[#] SplashActivity.java - POST_NOTI = PERMISSION_DENIED")

                    } else {
                        Log.w("myApp", "[#] SplashActivity.java - POST_NOTI = PERMISSION_DENIED")
                    }

                // TO READ EXTERNAL STORAGE IMAGES
                // UNDER ANDROID OS VERSION : TIRAMISU, 33
                } else {
                    if(perms[Manifest.permission.READ_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED) {
                        Log.w("myApp", "[#] SplashActivity.java - READ_EXTERNAL_STORAGE = PERMISSION_GRANTED")

                    } else {
                        Toast.makeText(this.bind.root.context, "외부저장소 읽기 권한을 허용해 주셔야\n앱을 사용 하실수 있습니다.", Toast.LENGTH_SHORT).show()
                        Log.w("myApp", "[#] SplashActivity.java - READ_EXTERNAL_STORAGE = PERMISSION_DENIED")
                        allGrantPerm = false
                    }
                }

                // TO WRITE EXTERNAL STORAGE IMAGES
                // UNDER ANDROID OS VERSION : OREO, 28
                // UPPER VERSION IS NOT NEED TO REQ THIS PERM
                if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
                    if(perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED) {
                        Log.w("myApp", "[#] SplashActivity.java - WRITE_EXTERNAL_STORAGE = PERMISSION_GRANTED")

                    } else {
                        Toast.makeText(this.bind.root.context, "외부저장소 쓰기 권한을 허용해 주셔야\n앱을 사용 하실수 있습니다.", Toast.LENGTH_SHORT).show()
                        Log.w("myApp", "[#] SplashActivity.java - WRITE_EXTERNAL_STORAGE = PERMISSION_DENIED")
                        allGrantPerm = false
                    }
                }

                if(allGrantPerm) { // goto trackListAct.
                    val intent = Intent(this, TrackListActivity::class.java)
                    this.startActivity(intent)
                    this.finish()

                } else { // goto this App Setting
                    val gotoSetting = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${this.packageName}"))
                    gotoSetting.addCategory(Intent.CATEGORY_DEFAULT)
                    gotoSetting.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    this.startActivity(gotoSetting)
                    this.finish()
                }
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    /**
     * check Permission by param: osVersion
     * @param osVersion baseline to check Permissions
     */
    @SuppressLint("NewApi")
    private fun checkPermissions(osVersion : Int) {
        Log.w("myApp", "[#] GPSActivity.java - Check Permission...")

        if(osVersion >= Build.VERSION_CODES.TIRAMISU) { // SDK 33 or High ( VER : 13 )
           this.checkPermissions()

        } else {  // FOR LOW VERSION ANDROID OS
            this.checkLegacyPermissions()
        }
    }

    /**
     * check Permissions
     *
     * ACCESS_FINE_LOCATION
     * ACCESS_COARSE_LOCATION<br>
     * READ_MEDIA_IMAGES<br>
     * CAMERA
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED  &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {

            // All Grant
            Log.w("myApp", "[#] GPSActivity.java - Precise ALL Permission granted")
            val intent = Intent(this, TrackListActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            this.startActivity(intent)
            this.finish()

        } else {
            // Somethings Denined
            Log.w("myApp", "[#] GPSActivity.java - Precise Permission denied")
            val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)

            if (showRationale || !gpsApp.isLocationPermissionChecked ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {

                Log.w("myApp", "[#] GPSActivity.java - Precise Permission denied, need new check")

                val listPermissionsNeeded: MutableList<String> = ArrayList()
                listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
                listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
                listPermissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES)
                listPermissionsNeeded.add(Manifest.permission.INTERNET)
                listPermissionsNeeded.add(Manifest.permission.CAMERA)
                listPermissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS)

                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), REQUEST_ID_MULTIPLE_PERMISSIONS)
            }
        }
    }


    /**
     * check LEGACY Permissions
     *
     * ACCESS_FINE_LOCATION
     * ACCESS_COARSE_LOCATION
     * READ_EXTERNAL_STORAGE
     * WRITE_EXTERNAL_STORAGE
     * CAMERA
     */
    private fun checkLegacyPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED  &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.w("myApp", "[#] GPSActivity.java - Precise ALL Permission granted")

            val intent = Intent(this, TrackListActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            this.startActivity(intent)
            this.finish()

        } else {
            Log.w("myApp", "[#] GPSActivity.java - Precise Permission denied")
            val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
            if (showRationale || !gpsApp.isLocationPermissionChecked ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED  &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                Log.w("myApp", "[#] GPSActivity.java - Precise Permission denied, need new check")

                val listPermissionsNeeded: MutableList<String> = ArrayList()
                listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
                listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
                listPermissionsNeeded.add(Manifest.permission.INTERNET)
                listPermissionsNeeded.add(Manifest.permission.CAMERA)
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), REQUEST_ID_MULTIPLE_PERMISSIONS)
            }
        }
    }
}