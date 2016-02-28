package org.twentyeight.concierge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;

public class MyReceiver extends BroadcastReceiver {
    private static final String TAG = "receiver";
    private final Context mContext;

    // 音声再生
    MediaPlayer mMediaPlayer = null;

    public MyReceiver(Context context) {
        mContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "action = " + intent.getAction());
        if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            speechVoice(R.raw.trg_sleep_on);
//        } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
//            speechVoice(R.raw.trg_sleep_off);
        } else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
            speechVoice(R.raw.trg_flightmode_on);
        } else if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
            speechVoice(R.raw.trg_battery_start_gohan);
        } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
            speechVoice(R.raw.trg_sleep_off);
        }
    }



    private void speechVoice(int resId) {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(mContext, resId);
            mMediaPlayer.start();
            return;
        }
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
//            mMediaPlayer.prepare();
        }
        Log.i(TAG, "speech voice");
        mMediaPlayer = MediaPlayer.create(mContext, resId);
        mMediaPlayer.start();
    }
}
