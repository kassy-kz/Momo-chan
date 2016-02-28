package org.twentyeight.concierge;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

/**
 * 大前提！
 * パーミッション取りに行く順番は overlay -> usage -> notification
 *
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ConciergeMainActivity";
    private static final int RC_HANDLE_PERMISSION = 1;
    private static final int REQUEST_CODE_OVERLAY = 2;
    private static final int REQUEST_CODE_USAGE = 3;
    private static final int REQUEST_CODE_NOTI = 4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button btnStart = (Button) findViewById(R.id.btnStartService);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "サービス起動");
                startService(new Intent(MainActivity.this, ConciergeService.class));
                Intent serviceIntent = new Intent(MainActivity.this, MyNotificationListenerService.class);
                startService(serviceIntent);
            }
        });

        Button btnStop = (Button) findViewById(R.id.btnStopService);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this, ConciergeService.class));
                stopService(new Intent(MainActivity.this, MyNotificationListenerService.class));
            }
        });

        // 一番最初にパーミッションたずねてしまおう
        // M以上かつまだパーミッションを得ていない
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getApplicationContext())) {
            Log.i(TAG, "not granted yet");
//            // ダイアログを出してパーミッション許可を取りに行く
            checkDrawOverlayPermission();
        }
        // M未満だがパーミッションはまだ
        else if (checkNotificationSetting()) {
            Log.i(TAG, "partially granted");
            // 6. Notificationの許可貰いに行く。
            startActivityForResult(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"), REQUEST_CODE_NOTI);
        }
        // 既に全パーミッション問題ない
        else {
            Log.i(TAG, "already granted");
            // ConciergeService 起動
            if (isUsageStatsAllowed()) {
//                Log.d(TAG, "サービス起動");
//                startService(new Intent(MainActivity.this, ConciergeService.class));
//                Intent serviceIntent = new Intent(this, MyNotificationListenerService.class);
//                startService(serviceIntent);
            } else {
                checkAppUsagePermission();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Notificationの許可有無を確認
     * @return
     */
    private boolean checkNotificationSetting() {

        ContentResolver contentResolver = getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = getPackageName();

        return !(enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName));
    }

    /**
     * 1. オーバーレイの許可を取りに行く
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void checkDrawOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_OVERLAY);
        }
    }

    /**
     * アプリUsageの許可を取りに行く
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void checkAppUsagePermission() {
        // 4. usageの許可取りに行く
        startActivityForResult(new Intent("android.settings.USAGE_ACCESS_SETTINGS"), REQUEST_CODE_USAGE);
    }

    /**
     * 自分アプリがUsage許可もらってるかどうか
     * @return
     */
    private boolean isUsageStatsAllowed() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int uid = android.os.Process.myUid();
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, uid, getPackageName());
        return  mode == AppOpsManager.MODE_ALLOWED;
    }

    /**
     * パーミッション尋ねた結果
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        // 2. Overlayの許可をもらった
        if (requestCode == REQUEST_CODE_OVERLAY) {
            Log.i(TAG, "response overlay");
            if (Settings.canDrawOverlays(this)) {
                // 3. usageの許可貰いに行く
                checkAppUsagePermission();
            }
        }
        // 5. usageの許可もらった
        else if (requestCode == REQUEST_CODE_USAGE) {
            Log.i(TAG, "response usage");
            // 6. Notificationの許可貰いに行く。
            startActivityForResult(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"), REQUEST_CODE_NOTI);
        }
        // 7. notiの許可もらった
        else if (requestCode == REQUEST_CODE_NOTI) {
//            Log.d(TAG,"サービス起動");
//            startService(new Intent(MainActivity.this, ConciergeService.class));
//            Intent serviceIntent = new Intent(this, MyNotificationListenerService.class);
//            startService(serviceIntent);
        }
    }
}
