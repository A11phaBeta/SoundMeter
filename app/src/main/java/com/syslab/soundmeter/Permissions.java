package com.syslab.soundmeter;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Permissions {

    public static final String TAG = "RecordSoundModule";
    public static final int REQ_RECORD_AUDIO = 1000;
    public static final int REQ_WRITE_EXTENAL_STORAGE = 1001;

    public static void getRecordAudioPermission(Activity _activity){
        int permissionRecordAudio = ContextCompat.checkSelfPermission(_activity.getApplicationContext(), Manifest.permission.RECORD_AUDIO);

        if(permissionRecordAudio == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(_activity, new String[]{Manifest.permission.RECORD_AUDIO}, REQ_RECORD_AUDIO);
        }
        else{
            getWriteExternalStoragePermission(_activity);
            Log.d(TAG, "Granted Permission");
        }
    }

    public static boolean checkRecordAudioPermissionResponse(int _grantedResult){
        if(_grantedResult == PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "Granted Permission");
            return true;
        }
        else{
            Log.d(TAG, "Denied Permission");
            return false;
        }
    }

    public static void getWriteExternalStoragePermission(Activity _activity){
        int permissionRecordAudio = ContextCompat.checkSelfPermission(_activity.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(permissionRecordAudio == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(_activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_WRITE_EXTENAL_STORAGE);
        }
        else{
            Log.d(TAG, "Granted Permission");
        }
    }

    public static boolean checkWriteExternalStoragePermissionResponse(int _grantedResult){
        if(_grantedResult == PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "Granted Permission");
            return true;
        }
        else{
            Log.d(TAG, "Denied Permission");
            return false;
        }
    }


}
