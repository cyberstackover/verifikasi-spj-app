package com.zxing.client.android.camera.open;

import java.util.ArrayList;
import java.util.Collection;

import android.content.Context;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;

import com.zxing.client.android.common.executor.AsyncTaskExecInterface;
import com.zxing.client.android.common.executor.AsyncTaskExecManager;

final class AutoFocusManager implements Camera.AutoFocusCallback {

  private static final String TAG = AutoFocusManager.class.getSimpleName();

  private static final long AUTO_FOCUS_INTERVAL_MS = 5000L;
  private static final Collection<String> FOCUS_MODES_CALLING_AF;
  static {
    FOCUS_MODES_CALLING_AF = new ArrayList<String>(2);
    FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_AUTO);
    FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_MACRO);
  }

  private boolean active;
  private final boolean useAutoFocus;
  private final Camera camera;
  private AutoFocusTask outstandingTask;
  private final AsyncTaskExecInterface taskExec;

  AutoFocusManager(Context context, Camera camera) {
    this.camera = camera;
    taskExec = new AsyncTaskExecManager().build();
    //SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context); // No need to get SharedPreferences
    String currentFocusMode = camera.getParameters().getFocusMode();
    useAutoFocus =
        /*sharedPrefs.getBoolean(PreferencesActivity.KEY_AUTO_FOCUS, true) &&*/ // No need to get SharedPreferences
        FOCUS_MODES_CALLING_AF.contains(currentFocusMode);
    Log.i(TAG, "Current focus mode '" + currentFocusMode + "'; use auto focus? " + useAutoFocus);
    start();
  }

 // @Override
  public synchronized void onAutoFocus(boolean success, Camera theCamera) {
    if (active) {
      outstandingTask = new AutoFocusTask();
      taskExec.execute(outstandingTask);
    }
  }

  synchronized void start() {
    if (useAutoFocus) {
      active = true;
      try {
        camera.autoFocus(this);
      } catch (RuntimeException re) {
        // Have heard RuntimeException reported in Android 4.0.x+; continue?
        Log.w(TAG, "Unexpected exception while focusing", re);
      }
    }
  }

  synchronized void stop() {
    if (useAutoFocus) {
      try {
        camera.cancelAutoFocus();
      } catch (RuntimeException re) {
        // Have heard RuntimeException reported in Android 4.0.x+; continue?
        Log.w(TAG, "Unexpected exception while cancelling focusing", re);
      }
    }
    if (outstandingTask != null) {
      outstandingTask.cancel(true);
      outstandingTask = null;
    }
    active = false;
  }

  private final class AutoFocusTask extends AsyncTask<Object,Object,Object> {
    @Override
    protected Object doInBackground(Object... voids) {
      try {
        Thread.sleep(AUTO_FOCUS_INTERVAL_MS);
      } catch (InterruptedException e) {
        // continue
      }
      synchronized (AutoFocusManager.this) {
        if (active) {
          start();
        }
      }
      return null;
    }
  }

}
