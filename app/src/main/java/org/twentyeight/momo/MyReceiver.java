package org.twentyeight.momo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.util.Log;

import java.util.Calendar;

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
        // 時間経過
        else if (Intent.ACTION_TIME_TICK.equals(action)) {
            Calendar cal = Calendar.getInstance();
            if (cal.get(Calendar.MINUTE) == 0) {
                speechJihou(cal.get(Calendar.HOUR));
            }
        }
    }

    /**
     * 時報をしゃべる
     * @param hour
     */
    private void speechJihou(int hour) {
        switch (hour) {
            case 1:
            case 13:
                speechVoice(R.raw.mm_82_jihou_1ji);
                break;
            case 2:
            case 14:
                speechVoice(R.raw.mm_83_jihou_2ji);
                break;
            case 3:
            case 15:
                speechVoice(R.raw.mm_84_jihou_3ji);
                break;
            case 4:
            case 16:
                speechVoice(R.raw.mm_85_jihou_4ji);
                break;
            case 5:
            case 17:
                speechVoice(R.raw.mm_86_jihou_5ji);
                break;
            case 6:
            case 18:
                speechVoice(R.raw.mm_87_jihou_6ji);
                break;
            case 7:
            case 19:
                speechVoice(R.raw.mm_88_jihou_7ji);
                break;
            case 8:
            case 20:
                speechVoice(R.raw.mm_89_jihou_8ji);
                break;
            case 9:
            case 21:
                speechVoice(R.raw.mm_102_jihou_9ji);
                break;
            case 10:
            case 22:
                speechVoice(R.raw.mm_103_jihou_10ji);
                break;
            case 11:
            case 23:
                speechVoice(R.raw.mm_104_jihou_11ji);
                break;
            case 12:
            case 24:
            case 0:
                speechVoice(R.raw.mm_93_jihou_12ji);
                break;
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
