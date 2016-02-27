package org.twentyeight.concierge;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by YKEI on 2016/02/27.
 */
public class ConciergeService extends Service {

    private static final String TAG = "ConciergeService";
    View view;
    WindowManager wm;

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        Log.d(TAG, "onStart start");

        // Viewからインフレータを作成する
        LayoutInflater layoutInflater = LayoutInflater.from(this);

        Log.d(TAG, "onStart start 001");

        // 重ね合わせするViewの設定を行う
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        Log.d(TAG, "onStart start 020");

        // WindowManagerを取得する
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        Log.d(TAG, "onStart start 030");

        // レイアウトファイルから重ね合わせするViewを作成する
        view = layoutInflater.inflate(R.layout.overlay, null);

        Log.d(TAG, "onStart start 040");

        // Viewを画面上に重ね合わせする
        wm.addView(view, params);

        Log.d(TAG, "onStart end");

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // サービスが破棄されるときには重ね合わせしていたViewを削除する
        wm.removeView(view);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

}
