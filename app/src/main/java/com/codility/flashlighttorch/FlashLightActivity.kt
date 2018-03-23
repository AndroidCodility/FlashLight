@file:Suppress("DEPRECATION")

package com.codility.flashlighttorch

import android.Manifest
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.hardware.Camera
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast


/**
 * Created by Govind on 3/22/2018.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class FlashLightActivity : AppCompatActivity() {

    private var REQUEST_CAMERA_CODE = 101
    private var imgPower: ImageView? = null
    private var camera: Camera? = null
    private var parameter: Camera.Parameters? = null
    private var isFlashLightOn: Boolean? = false
    private var deviceHasFlash: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Check If device has FlashLight or Not.
        deviceHasFlash = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                //Camera Permission already granted
                initializeCamera()
            } else {
                //Request Camera Permission
                checkCameraPermission()
            }
        } else {
            initializeCamera()
        }
    }

    private fun initializeCamera() {
        imgPower = findViewById<ImageButton>(R.id.imgPower)
        if (!deviceHasFlash!!) {
            val alert = android.app.AlertDialog.Builder(this).create()
            alert.setTitle(getString(R.string.app_name))
            alert.setMessage(getString(R.string.msg_error))
            alert.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok)) { _, _ -> finish() }
            alert.show()
            return
        } else {
            this.camera = Camera.open(0)
            parameter = this.camera!!.parameters
        }

        //Click Event On Button Click
        imgPower!!.setOnClickListener(View.OnClickListener {
            if (!isFlashLightOn!!) {
                turnOnFlashLight()
            } else {
                turnOffFlashLight()
            }
        })
    }

    // Method for Turning FlashLight ON
    private fun turnOnFlashLight() {
        try {
            if (this.camera != null) {
                parameter = this.camera!!.parameters
                parameter!!.flashMode = Camera.Parameters.FLASH_MODE_TORCH
                this.camera!!.parameters = parameter
                this.camera!!.startPreview()
                isFlashLightOn = true
                imgPower!!.setImageResource(R.drawable.ic_power)
                DrawableCompat.setTint(imgPower!!.drawable, ContextCompat.getColor(this, R.color.off_light))
                playOnOffSound()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Method for Turning FlashLight OFF
    private fun turnOffFlashLight() {
        try {
            if (camera != null) {
                parameter!!.flashMode = Camera.Parameters.FLASH_MODE_OFF
                this.camera!!.parameters = parameter
                this.camera!!.stopPreview()
            }
            isFlashLightOn = false
            imgPower!!.setImageResource(R.drawable.ic_power)
            DrawableCompat.setTint(imgPower!!.drawable, ContextCompat.getColor(this, R.color.on_light))
            playOnOffSound()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getCamera() {
        if (camera == null) {
            try {
                camera = Camera.open()
                parameter = camera!!.parameters
            } catch (e: RuntimeException) {
                e.printStackTrace()
            }
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                AlertDialog.Builder(this)
                        .setTitle("Camera Permission Needed")
                        .setMessage("This app needs the Camera permission, please accept to use Camera Flash Feature")
                        .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_CODE)
                        })
                        .create()
                        .show()

            } else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CAMERA_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the CAMERA-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission Granted..!!", Toast.LENGTH_LONG).show()
                        initializeCamera()
                    }
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    Toast.makeText(this, "Permission Denied..!!", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // on Pause turn off the flash
        if (isFlashLightOn!!)
            turnOffFlashLight()
    }

    override fun onResume() {
        super.onResume()
        // on Resume turn on the flash
        if (deviceHasFlash!!) {
            turnOnFlashLight()
        }
    }

    override fun onStart() {
        super.onStart()
        // on starting the app get the camera params
        getCamera()
    }

    override fun onStop() {
        super.onStop()
        // on stop release the camera
        if (camera != null) {
            camera!!.release()
            camera = null
        }
    }

    private fun playOnOffSound() {
        val mediaPlayer = MediaPlayer.create(this, R.raw.flash_sound)
        mediaPlayer.setOnCompletionListener { mp -> mp.release() }
        mediaPlayer.start()
    }
}