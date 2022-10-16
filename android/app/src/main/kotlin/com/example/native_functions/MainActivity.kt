package com.example.native_functions

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.lang.Exception

class MainActivity : FlutterActivity() {
    private val nativeFunctionChannel = "NativeFunctionsChannel"

    //Camera request
    private val cameraRequest = 1888

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            nativeFunctionChannel
        ).setMethodCallHandler { call, result ->
            when {
                call.method.equals("launchBrowser") -> {
                    browserOpen(call, result);
                }

                call.method.equals("openCamera") -> {
                    openCamera(result);
                }

                call.method.equals("getBatteryLevel") -> {
                    getBatteryLevel(result);
                }
            }
        }
        super.configureFlutterEngine(flutterEngine)
    }

    private fun browserOpen(call: MethodCall, result: MethodChannel.Result): Unit {
        try {
            val openURL = Intent(android.content.Intent.ACTION_VIEW)
            openURL.data = Uri.parse(call.argument<String>("url"))
            startActivity(openURL)
        } catch (e: Exception) {
            result.error("101", "Could not open browser!", "")
        }
    }

    private fun openCamera(result: MethodChannel.Result): Unit {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                cameraRequest
            )
        } else {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, cameraRequest)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            cameraRequest -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, cameraRequest)
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun getBatteryLevel(result: MethodChannel.Result): Unit {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val bm = applicationContext.getSystemService(BATTERY_SERVICE) as BatteryManager
                val batLevel: Int = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                result.success(batLevel);
            };
        } catch (e: Exception) {
            result.error("101", "Could not fetch!", "")
        }
    }
}
