package com.videomaster.app.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/** Permission request helpers for Android 10+. */
public final class PermissionUtils {
    private PermissionUtils() {}

    public static final int REQUEST_STORAGE = 100;

    /**
     * Returns all storage-related permissions that are needed but not yet granted.
     */
    public static String[] getMissingStoragePermissions(Activity activity) {
        List<String> missing = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasPermission(activity, Manifest.permission.READ_MEDIA_VIDEO)) {
                missing.add(Manifest.permission.READ_MEDIA_VIDEO);
            }
            if (!hasPermission(activity, Manifest.permission.READ_MEDIA_AUDIO)) {
                missing.add(Manifest.permission.READ_MEDIA_AUDIO);
            }
        } else {
            if (!hasPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                missing.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
        return missing.toArray(new String[0]);
    }

    /** Returns true if the given permission is already granted. */
    public static boolean hasPermission(Activity activity, String permission) {
        return ContextCompat.checkSelfPermission(activity, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Requests all missing storage permissions.
     * @return true if all permissions are already granted (no dialog needed)
     */
    public static boolean ensureStoragePermissions(Activity activity) {
        String[] missing = getMissingStoragePermissions(activity);
        if (missing.length == 0) return true;
        ActivityCompat.requestPermissions(activity, missing, REQUEST_STORAGE);
        return false;
    }

    /**
     * Checks if the storage permission result grants all requested permissions.
     */
    public static boolean isGranted(int[] grantResults) {
        if (grantResults.length == 0) return false;
        for (int r : grantResults) {
            if (r != PackageManager.PERMISSION_GRANTED) return false;
        }
        return true;
    }
}
