package com.example.permissionxdemo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.permissionx.guolindev.PermissionX

class PermissionManager(val activity: FragmentActivity) {


    companion object {
        @JvmStatic val camera = Manifest.permission.CAMERA;
        @JvmStatic val externalStorageRead = Manifest.permission.READ_EXTERNAL_STORAGE
        @JvmStatic val externalStorageWrite = Manifest.permission.WRITE_EXTERNAL_STORAGE
        @JvmStatic val coarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION
        @JvmStatic val fineLocation = Manifest.permission.ACCESS_FINE_LOCATION
        @JvmStatic val recordAudio = Manifest.permission.RECORD_AUDIO
        @JvmStatic val contactRead = Manifest.permission.READ_CONTACTS
        @JvmStatic val contactWrite = Manifest.permission.WRITE_CONTACTS
        @JvmStatic val calendarWrite = Manifest.permission.WRITE_CALENDAR
    }

    val permissionX = PermissionX.init(activity)
    val neededPermissions = populatePermissions()
    val requestPermissionBuilder = permissionX
        .permissions(neededPermissions)
    var grantedList: MutableSet<String> = requestPermissionBuilder.grantedPermissions
    var deniedList: MutableSet<String> = requestPermissionBuilder.deniedPermissions
    var allNeededPermissionGranted: Boolean = false
    get() {
        val flag = neededPermissions.map { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.checkSelfPermission(it)
        } else {
            0
        }
        }.fold(-1, { acc, i -> acc + i })
        return if(flag < 0) {
            false
        } else flag == 0
    }



    fun populatePermissions(): ArrayList<String> {
        PackageManager.PERMISSION_DENIED
        PackageManager.PERMISSION_GRANTED
        val permissionList = arrayListOf(
            camera,
            externalStorageRead,
            coarseLocation,
            fineLocation,
            recordAudio,
            contactRead,
            contactWrite
        )
        if (Build.VERSION.SDK_INT <= 28) {
            permissionList.add(externalStorageWrite)
        }
        return permissionList
    }

    fun requestAndHandlePermission(cb: () -> Unit = {}) {
        requestPermissionBuilder
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    "We care for your privacy and security,but core fundamental of this app are based on these permissions",
                    "OK",
                    "Cancel"
                )
            }.onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "You need to allow necessary permissions in Settings manually",
                    "OK",
                    "Cancel"
                )
            }
            .request { allGranted, grantedList, deniedList ->
                this.allNeededPermissionGranted = allGranted
                this.grantedList = grantedList.toMutableSet()
                this.deniedList = deniedList.toMutableSet()
                if (allGranted) {
                    Log.i(
                        "PermissionsGranted",
                        "All permissions are granted: $grantedList"
                    )
                } else {
                    Log.i(
                        "PermissionsDenied",
                        "These permissions are denied: $deniedList"
                    )
                }
                cb()
            }
    }

    fun requestPermissionFor(permissions: Array<String>, cb: () -> Unit) {
        permissionX
            .permissions(filterPermissionsForApiLevel(permissions.toList()))
            .explainReasonBeforeRequest()
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    "We care for your privacy and security,but core fundamental of this app are based on these permissions",
                    "OK",
                    "Cancel"
                )
            }.onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "You need to allow necessary permissions in Settings manually",
                    "OK",
                    "Cancel"
                )
            }
            .request { allGranted, grantedList, deniedList ->
                this.grantedList = this.grantedList.union(grantedList).toMutableSet()
                this.deniedList = this.deniedList.union(deniedList).toMutableSet()
                if (allGranted) {
                    Log.i(
                        "PermissionsGranted",
                        "These permissions are granted: $grantedList"
                    )
                } else {
                    Log.i(
                        "PermissionsDenied",
                        "These permissions are denied: $deniedList"
                    )
                }
                cb()
            }
    }

    fun filterPermissionsForApiLevel(permissions: List<String>): List<String> {
        val populatedPermissions = populatePermissions()
        return permissions.filter { populatedPermissions.contains(it) }
    }

    fun filterPermissionsForApiLevel(permission: String): String {
        val populatedPermissions = populatePermissions()
        return if(populatedPermissions.contains(permission)) permission else ""
    }

    fun hasPermissionFor(vararg permission: String): Boolean {
        return grantedList.containsAll(filterPermissionsForApiLevel(permission.toList()))
    }

    fun hasPermissionFor(permission: String): Boolean {
        return grantedList.contains(permission)
    }

}