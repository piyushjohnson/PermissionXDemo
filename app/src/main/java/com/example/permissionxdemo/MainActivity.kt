package com.example.permissionxdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

import android.os.Build
import android.os.Environment
import android.util.Log
import com.example.permissionxdemo.PermissionManager.Companion.externalStorageRead
import com.example.permissionxdemo.PermissionManager.Companion.externalStorageWrite
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private var isSDPresent: Boolean = false
    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionManager = PermissionManager(this)
        if (!permissionManager.allNeededPermissionGranted) {
            permissionManager.requestAndHandlePermission(this::createMLeadsFolder);
        }
    }

    private fun createMLeadsFolder() {
        val directoryPath: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            "/sdcard/Android/media/com.example.permissionxdemo/Mleads"
        } else {
            Environment.getExternalStorageDirectory().absolutePath + File.separator.toString() + "Mleads/"
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionManager.hasPermissionFor(externalStorageRead, externalStorageWrite)) {
                isSDPresent =
                    Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                if (isSDPresent) {
                    val direct = File(directoryPath)
                    if (direct.exists()) {
                        try {
                            direct.createNewFile()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        val result: Boolean = direct.setExecutable(true, false)
                        // evaluate the result
                        if (result) {
                            println("Operation succeeded")
                        } else {
                            println("Operation failed")
                        }
                    } else {
                        println("File does not exist")
                    }
                    if (direct.exists()) {
                        val result: Boolean = direct.setExecutable(true)
                        val read: Boolean = direct.setReadable(true)
                        val write: Boolean = direct.setWritable(true)

                        Log.e("exits", "trpb67, RESULT IS $result  $read  $write")
                    }
                    var success = true
                    if (!direct.exists()) {
                        direct.mkdirs()
                        success = direct.mkdirs()
                    }
                } else {
                    Toast.makeText(this, "SD card not found", Toast.LENGTH_LONG)
                        .show()
                }

                //do your work
            } else {
                permissionManager.requestPermissionFor(arrayOf(externalStorageRead, externalStorageWrite),this::createMLeadsFolder)
            }
        } else {
            isSDPresent = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
            if (isSDPresent) {
                val direct = File(directoryPath)
                if (direct.exists()) {
                    val result: Boolean = direct.setExecutable(true)
                    Log.e("exits", "trpb67, RESULT IS $result")
                }
                if (!direct.exists()) {
                    direct.mkdirs()
                }
            } else {
                Toast.makeText(this, "SD card not found", Toast.LENGTH_LONG).show()
            }
        }
    }
}