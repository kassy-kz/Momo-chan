package org.twentyeight.momo;

import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
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
                // これは効かない
//                stopService(new Intent(MainActivity.this, MyNotificationListenerService.class));
                // サービスを停止する
                stopService(new Intent(MainActivity.this, ConciergeService.class));
                MyNotificationListenerService.stopNotificationService();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getAllPermissioins();
    }

    /**
     * 全パーミッションを尋ねる処理
     * 繰り返し呼んで問題ない
     */
    private void getAllPermissioins() {
        if (!isOverlayAllowed()) {
            getOverlayPermission();
            return;
        }

        if (!isUsageStatsAllowed()) {
            getUsagePermission();
            return;
        }

        if (!isNotificationAllowed()) {
            getNotificationPermission();
            return;
        }
    }

    /**
     * オーバーレイの許可を取りに行く
     */
    private void getOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_CODE_OVERLAY);
    }

    /**
     * Usageの許可を取りに行く
     */
    private void getUsagePermission() {
        // 4. usageの許可取りに行く
        startActivityForResult(new Intent("android.settings.USAGE_ACCESS_SETTINGS"), REQUEST_CODE_USAGE);
    }

    /**
     * Notificationの許可を取りに行く
     */
    private void getNotificationPermission() {
        startActivityForResult(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"), REQUEST_CODE_NOTI);
    }

    /**
     * 自分アプリがOverlay許可をもらってるかどうか
     */
    private boolean isOverlayAllowed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        } else {
            return true;
        }
    }

    /**
     * 自分アプリがUsage許可もらってるかどうか
     * @return
     */
    private boolean isUsageStatsAllowed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int uid = android.os.Process.myUid();
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, uid, getPackageName());
            return mode == AppOpsManager.MODE_ALLOWED;
        } else {
            return true;
        }
    }

    /**
     * Notificationの許可をもらってるかどうか
     */
    private boolean isNotificationAllowed() {
        ContentResolver contentResolver = getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = getPackageName();
        return !(enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName));
    }

    /**
     * パーミッション尋ねた結果
     * パーミッションを全部もらうまで繰り返し尋ねる感じ
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        getAllPermissioins();
    }
}
