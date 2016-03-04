package org.twentyeight.momo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServiceStopReceiver extends BroadcastReceiver {
    private static final String TAG = "ServiceStop";

    public ServiceStopReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive");
        // サービスを停止する
        context.stopService(new Intent(context, ConciergeService.class));
        MyNotificationListenerService.stopNotificationService();
    }
}
