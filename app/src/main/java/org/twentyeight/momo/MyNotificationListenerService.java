package org.twentyeight.momo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

/**
 * 通知を受け取るのと、BroadcastReceiverの管理を行うサービス
 * 最初のあいさつもここでしゃべる
 */
public class MyNotificationListenerService extends NotificationListenerService {
    
    private static final String TAG = "Notification";
    private NotificationManager mNotificationManager;
    private static final int NOTIFICATION_ID = 0;
    private MyReceiver mReceiver;

    // 音声再生
    private static MyNotificationListenerService sSelf;

    // このサービスは停止できないので、稼働中フラグを持つ
    private boolean mIsRunning = false;

    /**
     * onCreate
     */
    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();
        sSelf = this;
    }

    /**
     * NotificationListenerServiceはSystemにbindされるため、
     * 特殊な殺し方をしないといけない
     */
    public static void stopNotificationService() {
        Log.i(TAG, "virtual stop notification service");
        sSelf.deleteRunningNotification();
        sSelf.unregisterReceiver(sSelf.mReceiver);
        sSelf.mIsRunning = false;
    }

    /**
     * サービスが今稼働中か否か調べる
     * @return
     */
    public static boolean isRunning() {
        return sSelf.mIsRunning;
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

        mIsRunning = true;

        // 通知マネージャー
        mNotificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);

        Log.i(TAG, "mynotificationlistener start : " + startId);
        showRunningNotification();

//        speechText("通知を待ち受けます");
        // 最初のあいさつ
        Utils.speechVoice(this, R.raw.mm_21_random_himomoseyuri, null);

        mReceiver = new MyReceiver(this);
        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_POWER_CONNECTED));
        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));
        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_CAMERA_BUTTON));
        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_USER_PRESENT));

        return START_NOT_STICKY;
    }

    /**
     * これは絶対に呼ばれないので注意
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "mynotificationlistener stop ====================================");
    }

    /**
     *  ステータスバーに通知があった場合に呼ばれる
      */
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(TAG, "onNotificationPosted");
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
        Log.i(TAG, "tag         : " + tag);
        Log.i(TAG, "tostring:" + sbn.toString());

        // 受信した結果をBLEに投げてみる
        // いくつかのアプリは特別扱いしよう
        // Gmailの場合
        if ("com.google.android.gm".equals(packageName)) {
            Log.i(TAG, "app: Gmail");
            Utils.speechVoice(this, R.raw.mm_119_mailapp_todoitayo, null);
        }
        // Twitterの場合
        else if ("com.twitter.android".equals(packageName)) {
            Log.i(TAG, "app: twitter");
            Utils.speechVoice(this, R.raw.mm_121_twitter_todoitayo, null);
        }
        // LINEの場合
        else if ("jp.naver.line.android".equals(packageName)) {
            Log.i(TAG, "app: LINE");
            Utils.speechVoice(this, R.raw.mm_123_line_todoitayo, null);
        }
        // 本アプリの場合
        else if (getPackageName().equals(packageName)) {
            // do nothing
        }
        // その他
        else {
            Log.i(TAG, "app: other");
            // do nothing
            // その他でしゃべらせるのは危険過ぎる
//            Utils.speechVoice(this, R.raw.mm_145_mailapp_nankakiteruyo, null);
        }
    }

    /**
     * 通知バーにアイコンを出す
     */
    private void showRunningNotification() {
        Log.i(TAG, "show notification");

        // ビルダーを立てる
        NotificationCompat.Builder notificationBuilder;

        // ビルダーを経由してノーティフィケーションを作成
        notificationBuilder = new NotificationCompat.Builder(getApplicationContext());
        notificationBuilder.setSmallIcon(R.drawable.ic_get_app);

        // 大きなアイコンを設定
        notificationBuilder.setContentTitle("通知マネージャー起動中");
        Bitmap largeIcon = BitmapFactory.decodeResource(
                getResources(), R.mipmap.ic_launcher);
        notificationBuilder.setLargeIcon(largeIcon);

        // 通知を押したらアプリが起動するようにしたい
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(contentIntent);

        // DELボタンを追加する
        Intent stopIntent = new Intent("orz.kassy.momo.stop");
        PendingIntent pIntent = PendingIntent.getBroadcast(this, 10, stopIntent, PendingIntent.FLAG_ONE_SHOT);
        notificationBuilder.addAction(android.R.drawable.ic_input_delete, "終了", pIntent);

        // 横フリックで消されないようにしたい
        Notification noti = notificationBuilder.build();
        noti.flags |= Notification.FLAG_NO_CLEAR;

        // マネージャをつかって通知する
        mNotificationManager.notify(NOTIFICATION_ID, noti);
    }

    /**
     * 通知バーのアイコンを消す
     */
    private void deleteRunningNotification() {
        NotificationManager manager = (NotificationManager)
                getSystemService(Service.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
    }
}