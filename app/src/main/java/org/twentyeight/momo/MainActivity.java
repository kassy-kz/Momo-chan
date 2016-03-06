package org.twentyeight.momo;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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

    private static final int PERMISSION_TYPE_OVERLAY = 1;
    private static final int PERMISSION_TYPE_USAGE = 2;
    private static final int PERMISSION_TYPE_NOTIFICATION = 3;


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
                if (!MyNotificationListenerService.isRunning()) {
                    Log.d(TAG, "サービス起動");
                    startService(new Intent(MainActivity.this, ConciergeService.class));
                    Intent serviceIntent = new Intent(MainActivity.this, MyNotificationListenerService.class);
                    startService(serviceIntent);
                }
            }
        });

        Button btnStop = (Button) findViewById(R.id.btnStopService);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MyNotificationListenerService.isRunning()) {
                    // これは効かない
//                stopService(new Intent(MainActivity.this, MyNotificationListenerService.class));
                    // サービスを停止する
                    stopService(new Intent(MainActivity.this, ConciergeService.class));
                    MyNotificationListenerService.stopNotificationService();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        getAllPermissioins();
    }

    /**
     * 全パーミッションを尋ねる処理
     * 繰り返し呼んで問題ない
     */
    private void getAllPermissioins() {
        // オーバーレイの許可はAndroidM以上でのみ必要
        if (!isOverlayAllowed()) {
            Log.i(TAG, "get permission 1");
            showManualDialog(R.drawable.manual1, PERMISSION_TYPE_OVERLAY);
            return;
        }

        if (!isUsageStatsAllowed()) {
            Log.i(TAG, "get permission 2");
            showManualDialog(R.drawable.manual2, PERMISSION_TYPE_USAGE);
            return;
        }

        if (!isNotificationAllowed()) {
            Log.i(TAG, "get permission 3");
            showManualDialog(R.drawable.manual3, PERMISSION_TYPE_NOTIFICATION);
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
     * これが微妙に不安定というか、レス悪いというか...
     */
    private boolean isNotificationAllowed() {

        ContentResolver contentResolver = getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = getPackageName();
        Log.i(TAG, "noti allowed : "+ enabledNotificationListeners);
        Log.i(TAG, "contain : " + enabledNotificationListeners.contains(packageName));
        return !(enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName));
    }

    /**
     * パーミッション尋ねた結果
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        Log.i(TAG, "onResult");
        // Do nothing
    }

    /**
     * ユーザーにルート名の入力を促すダイアログを表示する
     */
    private void showManualDialog(int resId, final int permissionType) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_manual, null);
        ImageView manualImage = (ImageView) view.findViewById(R.id.imgManualDialog);
        manualImage.setImageResource(resId);
        TextView manualText = (TextView) view.findViewById(R.id.txtManualDialog);
        manualText.setText(R.string.manual_string);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String postStr = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            postStr = "（全部で3）";
        } else {
            postStr = "（全部で2）";
        }
        String title = getResources().getString(R.string.manual_title) + postStr;
        builder.setTitle(title);
        builder.setView(view);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (permissionType) {
                    case PERMISSION_TYPE_OVERLAY:
                        getOverlayPermission();
                        break;
                    case PERMISSION_TYPE_USAGE:
                        getUsagePermission();
                        break;
                    case PERMISSION_TYPE_NOTIFICATION:
                        getNotificationPermission();
                        break;
                }
            }
        }).show();
    }
}
