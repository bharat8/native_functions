package com.example.native_functions

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import java.lang.Exception
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit
import kotlin.concurrent.fixedRateTimer

class MainActivity : FlutterActivity() {
    private val nativeFunctionChannel = "NativeFunctionsChannel"

    private val nativeFunctionStreamChannel = "NativeFunctionsStreamChannel"

    private val locationPermissionRequest = 1800

    private lateinit var result: MethodChannel.Result

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var locationRequest: LocationRequest

    private lateinit var locationCallback: LocationCallback

    private var currentLocation: Location? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            nativeFunctionChannel
        ).setMethodCallHandler { call, r1 ->
            when {
                call.method.equals("locationData") -> {
                    result = r1
                    requestLocationPermission();
                }
            }
        }

        EventChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            nativeFunctionStreamChannel
        ).setStreamHandler(
            object : EventChannel.StreamHandler {
                override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                    Timer().scheduleAtFixedRate(object : TimerTask() {
                        override fun run() {
                            var data = hashMapOf(
                                "long" to currentLocation?.longitude,
                                "lat" to currentLocation?.latitude
                            )
                            runOnUiThread {
                                events?.success(data)
                            }
                        }
                    }, 0, 2000)
                }

                override fun onCancel(arguments: Any?) {
                }
            }
        )
    }

    private fun requestLocationPermission(): Unit {
        try {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    locationPermissionRequest
                )
            } else {
                getLocationCoordinates()
            }
        } catch (e: Exception) {
            result.error("101", "Could not get location!", "")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            locationPermissionRequest -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    getLocationCoordinates()
                } else {
                    result.error("101", "Location Permissions denied!", "")
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun getLocationCoordinates(): Unit {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest().apply {
            interval = TimeUnit.SECONDS.toMillis(60)

            fastestInterval = TimeUnit.SECONDS.toMillis(30)

            maxWaitTime = TimeUnit.SECONDS.toMillis(2)

            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0.lastLocation.let {
                    currentLocation = it
                }
                super.onLocationResult(p0)
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            result.error("101", "Location Permissions denied!", "")
            return
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }
}
