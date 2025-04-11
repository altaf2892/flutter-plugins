package dk.cachet.activity_recognition_flutter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

public class ActivityRecognizedService extends JobIntentService {

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, ActivityRecognizedService.class, 1, work);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    protected void onHandleWork(@Nullable Intent intent) {
        onHandleIntent(intent);
    }

    private void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            Log.w("ActivityRecognizedService", "Intent is null");
            return;
        }

        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        if (result == null) {
            Log.w("ActivityRecognizedService", "No ActivityRecognitionResult found in intent.");
            return;
        }

        List<DetectedActivity> activities = result.getProbableActivities();
        if (activities == null || activities.isEmpty()) {
            Log.w("ActivityRecognizedService", "No detected activities.");
            return;
        }

        DetectedActivity mostLikely = activities.get(0);
        for (DetectedActivity a : activities) {
            if (a.getConfidence() > mostLikely.getConfidence()) {
                mostLikely = a;
            }
        }

        String type = getActivityString(mostLikely.getType());
        int confidence = mostLikely.getConfidence();
        String data = type + "," + confidence;

        Log.d("ActivityRecognizedService", "Detected: " + data);

        SharedPreferences preferences =
                getApplicationContext().getSharedPreferences(
                        ActivityRecognitionFlutterPlugin.ACTIVITY_RECOGNITION, MODE_PRIVATE);

        preferences.edit()
                .putString(ActivityRecognitionFlutterPlugin.DETECTED_ACTIVITY, data)
                .apply();
    }

    public static String getActivityString(int type) {
        switch (type) {
            case DetectedActivity.IN_VEHICLE: return "IN_VEHICLE";
            case DetectedActivity.ON_BICYCLE: return "ON_BICYCLE";
            case DetectedActivity.ON_FOOT: return "ON_FOOT";
            case DetectedActivity.RUNNING: return "RUNNING";
            case DetectedActivity.STILL: return "STILL";
            case DetectedActivity.TILTING: return "TILTING";
            case DetectedActivity.WALKING: return "WALKING";
            default: return "UNKNOWN";
        }
    }
}
