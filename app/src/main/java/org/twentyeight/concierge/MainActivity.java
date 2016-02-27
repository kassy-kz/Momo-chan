package org.twentyeight.concierge;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ConciergeMainActivity";
    private static final int RC_HANDLE_PERMISSION = 1;
    private static final int REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        // NotificationListenerServiceを立ち上げる
//        Intent serviceIntent = new Intent(this, MyNotificationListenerService.class);
//        startService(serviceIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 一番最初にパーミッションたずねてしまおう
        // まだパーミッションを得ていない
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkPermission()) {
            Log.i(TAG, "not granted yet");
//            // ダイアログを出してパーミッション許可を取りに行く
            checkDrawOverlayPermission();
        }
        // 既にパーミッションを得ている or M未満だから関係ない場合
        else {
            Log.i(TAG, "already granted");
            // ConciergeService 起動
            Log.d(TAG,"起動");
            startService(new Intent(MainActivity.this, ConciergeService.class));
        }
    }

    /**
     * オーバーレイの許可を取りに行く
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void checkDrawOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    /**
     * パーミッションを尋ねた結果
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequestPermissionsResult");
        Log.d(TAG, "起動");
        startService(new Intent(MainActivity.this, ConciergeService.class));
    }

    /**
     * アプリに必要なパーミッションを尋ねる
     * @return true if yet granted, false if not granted
     */
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG,"permission ng");
                return false;
            }
        }
        Log.i(TAG,"permission ok");
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
