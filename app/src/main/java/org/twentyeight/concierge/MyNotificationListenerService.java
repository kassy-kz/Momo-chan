package org.twentyeight.concierge;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.Locale;

public class MyNotificationListenerService extends NotificationListenerService implements TextToSpeech.OnInitListener {
    
    private String TAG = "Notification";
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;
    private static final int NOTIFICATION_ID = 0;
    private TextToSpeech mTts;
    private MyReceiver mReceiver;

    /**
     * onCreate
     */
    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();

        // TextToSpeechオブジェクトの生成
        mTts = new TextToSpeech(this, this);
    }

    /**
     * onStartCommand
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,flags,startId);

        // 通知マネージャー
        mNotificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);

        Log.i(TAG, "mynotificationlistener start");
        showNotification();

        speechText("通知を待ち受けます");

        mReceiver = new MyReceiver(this);
        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_POWER_CONNECTED));

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "mynotificationlistener stop");
        deleteNotification();
        unregisterReceiver(mReceiver);
    }

    /**
     *  ステータスバーに通知があった場合に呼ばれる
      */
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(TAG,"onNotificationPosted");
        // 通知内容をログに出力する
        showLog(sbn);
    }

    /**
     * ステータスバーから通知が消された場合
     * @param sbn
     */
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG, "onNotificationRemoved");
    }

    /**
     * 通知内容をログに出す
     * @param sbn
     */
    private void showLog( StatusBarNotification sbn ){

        // sbnから各種データを取り出し
        int id = sbn.getId();
        String packageName = sbn.getPackageName();
        String tag = sbn.getTag();
        long time = sbn.getPostTime();
        boolean clearable = sbn.isClearable();
        boolean ongoing = sbn.isOngoing();
        CharSequence text = sbn.getNotification().tickerText;

        // ログに表示してみる
        Log.i(TAG,"id:" + id + " time:" +time + " isClearable:" + clearable + " isOngoing:" + ongoing);
        Log.i(TAG,"packageName : " + packageName);
        Log.i(TAG,"tickerText  : " + text);
        Log.i(TAG,"tag         : " + tag);
        Log.i(TAG,"tostring:" + sbn.toString());


        // 受信した結果をBLEに投げてみる
        // いくつかのアプリは特別扱いしよう
        // Gmailの場合
        if("com.google.android.gm".equals(packageName)) {
            Log.i(TAG, "app: Gmail");
            String message1  = "メールが届きました";
            speechText(message1);
        }
        // Twitterの場合
        else if("com.twitter.android".equals(packageName)) {
            Log.i(TAG,"app: twitter");
            String message1  = "twitterのメンションです";
            speechText(message1);
        }
        // LINEの場合
        else if("jp.naver.line.android".equals(packageName)) {
            Log.i(TAG,"app: LINE");
            String message1  = "ラインのメッセージです";
            speechText(message1);
        }
        // その他
        else {
            Log.i(TAG, "app: other");
        }
    }

    /**
     * 通知バーにアイコンを出す
     */
    private void showNotification() {
        Log.i(TAG, "show notification");
        // ビルダーを経由してノーティフィケーションを作成
        mNotificationBuilder = new NotificationCompat.Builder(getApplicationContext());
        mNotificationBuilder.setSmallIcon(R.drawable.ic_get_app);

        // 大きなアイコンを設定
        mNotificationBuilder.setContentTitle("通知マネージャー起動中");
        Bitmap largeIcon = BitmapFactory.decodeResource(
                getResources(), R.mipmap.ic_launcher);
        mNotificationBuilder.setLargeIcon(largeIcon);

        // マネージャをつかって通知する
        mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    /**
     * 通知バーのアイコンを消す
     */
    private void deleteNotification() {
        NotificationManager manager = (NotificationManager)
                getSystemService(Service.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
    }

    /**
     * TextToSpeechのInitが完了した
     * @param status
     */
    @Override
    public void onInit(int status) {
        if (TextToSpeech.SUCCESS == status) {
            Locale locale = Locale.JAPANESE;
            if (mTts.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                mTts.setLanguage(locale);
            } else {
                Log.d("", "Error SetLocale");
            }
        } else {
            Log.d("", "Error Init");
        }
    }

    /**
     * TextToSpeechで喋らせる
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void speechText(String str) {
        if (0 < str.length()) {
            if (mTts.isSpeaking()) {
                // 読み上げ中なら止める
                mTts.stop();
            }

            // 読み上げ開始
            mTts.speak(str, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }
}