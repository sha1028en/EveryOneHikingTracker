package eu.basicairdata.graziano.gpslogger


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

class SplashActivity : AppCompatActivity() {

    private val REQUEST_ID_MULTIPLE_PERMISSIONS = 1
    private val gpsApp = GPSApplication.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setTheme(R.style.MyMaterialTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        this.checkLocationPermission()
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

                // TO READ MEDIA_IMG, AUDIO, VIDEO
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && perms.containsKey(Manifest.permission.READ_MEDIA_IMAGES)) {
                    if(perms[Manifest.permission.CAMERA] == PackageManager.PERMISSION_GRANTED) {
                        Log.w("myApp", "[#] SplashActivity.java - READ_MEDIA_IMG = PERMISSION_GRANTED")

                    } else {
                        Log.w("myApp", "[#] SplashActivity.java - READ_MEDIA_IMG = PERMISSION_DENIED")
                        allGrantPerm = false
                    }
                }

                if(allGrantPerm) {
                    val intent = Intent(this, RecordListActivity::class.java)
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
                    val intent = Intent(this, RecordListActivity::class.java)
                    this.startActivity(intent)
                    this.finish()

                } else {
                    val listPermissionsNeeded: MutableList<String> = ArrayList()
                    listPermissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES)
                    ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), REQUEST_ID_MULTIPLE_PERMISSIONS)
                    return
                }
            }
            val intent = Intent(this, RecordListActivity::class.java)
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