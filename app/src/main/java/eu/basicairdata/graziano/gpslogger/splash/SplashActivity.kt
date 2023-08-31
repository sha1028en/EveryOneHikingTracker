package eu.basicairdata.graziano.gpslogger.splash


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import eu.basicairdata.graziano.gpslogger.GPSApplication
import eu.basicairdata.graziano.gpslogger.R
import eu.basicairdata.graziano.gpslogger.databinding.ActivitySplashBinding
import eu.basicairdata.graziano.gpslogger.tracklist.TrackListActivity

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
            this.checkLocationPermission()

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
                        Log.w("myApp", "[#] SplashActivity.java - ACCESS_FINE_LOCATION = PERMISSION_DENIED")
                        allGrantPerm = false;
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
                        Log.w("myApp", "[#] SplashActivity.java - CAMERA = PERMISSION_DENIED")
                        allGrantPerm = false
                    }
                }

                // TO READ IMAGES
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && perms.containsKey(Manifest.permission.READ_MEDIA_IMAGES)) {
                    if(perms[Manifest.permission.READ_MEDIA_IMAGES] == PackageManager.PERMISSION_GRANTED) {
                        Log.w("myApp", "[#] SplashActivity.java - READ_MEDIA_IMG = PERMISSION_GRANTED")

                    } else {
                        Log.w("myApp", "[#] SplashActivity.java - READ_MEDIA_IMG = PERMISSION_DENIED")
                        allGrantPerm = false
                    }

                // TO READ EXTERNAL STORAGE IMAGES
                // UNDER ANDROID OS VERSION : TIRAMISU, 33
                } else {
                    if(perms[Manifest.permission.READ_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED) {
                        Log.w("myApp", "[#] SplashActivity.java - READ_EXTERNAL_STORAGE = PERMISSION_GRANTED")

                    } else {
                        Log.w("myApp", "[#] SplashActivity.java - READ_EXTERNAL_STORAGE = PERMISSION_DENIED")
                        allGrantPerm = false;
                    }
                }

                // TO WRITE EXTERNAL STORAGE IMAGES
                // UNDER ANDROID OS VERSION : OREO, 28
                // UPPER VERSION IS NOT NEED TO REQ THIS PERM
                if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
                    if(perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED) {
                        Log.w("myApp", "[#] SplashActivity.java - WRITE_EXTERNAL_STORAGE = PERMISSION_GRANTED")

                    } else {
                        Log.w("myApp", "[#] SplashActivity.java - WRITE_EXTERNAL_STORAGE = PERMISSION_DENIED")
                        allGrantPerm = false;
                    }
                }

                if(allGrantPerm) {
                    val intent = Intent(this, TrackListActivity::class.java)
                    this.startActivity(intent)
                    this.finish()
                }
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun checkLocationPermission() {
        Log.w("myApp", "[#] GPSActivity.java - Check Location Permission...")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Log.w("myApp", "[#] GPSActivity.java - Precise Location Permission granted")

            // Permission Granted
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(this, TrackListActivity::class.java)
                    this.startActivity(intent)
                    this.finish()

                } else {
                    val listPermissionsNeeded: MutableList<String> = ArrayList()
                    listPermissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES)
                    ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), REQUEST_ID_MULTIPLE_PERMISSIONS)
                    return
                }
            }
            val intent = Intent(this, TrackListActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            this.startActivity(intent)
            this.finish()

        } else {
            Log.w("myApp", "[#] GPSActivity.java - Precise Location Permission denied")
            val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
            if (showRationale || !gpsApp.isLocationPermissionChecked ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                Log.w("myApp", "[#] GPSActivity.java - Precise Location Permission denied, need new check")

                val listPermissionsNeeded: MutableList<String> = ArrayList()
                listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
                listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
                listPermissionsNeeded.add(Manifest.permission.CAMERA)

                // TO READ MEDIA_IMG, AUDIO, VIDEO
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    listPermissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES)
                }
                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), REQUEST_ID_MULTIPLE_PERMISSIONS)
            }
        }
    }
}