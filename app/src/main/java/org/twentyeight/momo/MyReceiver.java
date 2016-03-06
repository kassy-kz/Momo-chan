package org.twentyeight.momo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.BatteryManager;
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

        // スクリーンオフ
        if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            speechVoice(R.raw.mm_114_sleep_chottoyasumuyo);
        }
        // 機内モード
        else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
            // 機内モードオン
            if (intent.getBooleanExtra("state", false)) {
                speechVoice(R.raw.mm_112_flightmode_hikoukinoruno);
            }
        }
        // 充電開始
        else if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
            speechVoice(R.raw.mm_76_battery_gohantaberune);
        }
        // 充電終了
        else if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
            IntentFilter intentfilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, intentfilter);
            int batteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

            // バッテリー残量で振り分け
            if (batteryLevel > 90) {
                speechVoice(R.raw.mm_74_battery_genkimantan);
            } else {
                speechVoice(R.raw.mm_113_battery_mottohoshiina);
            }
        }
        // ロック解除
        else if (Intent.ACTION_USER_PRESENT.equals(action)) {
            speechVoice(R.raw.mm_115_sleep_jajaaan);
        }
        // カメラボタン（出現条件不明）
        else if (Intent.ACTION_CAMERA_BUTTON.equals(action)) {
            speechVoice(R.raw.mm_10_camera_hicheeze);
        }
        // アラーム
        else if (Utils.ACTION_ALARM.equals(action)) {
            speechVoice(R.raw.mm_126_time_kisyounojikan);
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
