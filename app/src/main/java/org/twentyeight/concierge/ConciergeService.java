package org.twentyeight.concierge;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by YKEI on 2016/02/27.
 */
public class ConciergeService extends Service {
    WindowManager.LayoutParams prms;

    private static final String TAG = "ConciergeService";
    private View view;
    private WindowManager wm;
    WindowManager.LayoutParams params;

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        Log.d(TAG, "onStart start");

        // Viewからインフレータを作成する
        LayoutInflater layoutInflater = LayoutInflater.from(this);

        Log.d(TAG, "onStart start 001");

        // 重ね合わせするViewの設定を行う
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        // WindowManagerを取得する
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        // レイアウトファイルから重ね合わせするViewを作成する
        view = layoutInflater.inflate(R.layout.overlay, null);

        view.setOnTouchListener(new DragViewListener(view));

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


    private class DragViewListener implements View.OnTouchListener {
        // ドラッグ対象のView
        private View dragView;
        // ドラッグ中に移動量を取得するための変数
        private int oldx = 0;
        private int oldy = 0;

        private int dragStartX = 0;
        private int dragStartY = 0;

        public DragViewListener(View dragView) {
            this.dragView = dragView;
            oldx = params.x;
            oldy = params.y;
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            // タッチしている位置取得
            int x = (int) event.getRawX();
            int y = (int) event.getRawY();
            Log.i(TAG, "onTouch " + event.getAction() +" x,y : " + x + "," + y);

            // 画像と重ならなければスルーする
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dragStartX = x;
                    dragStartY = y;
                    Log.i(TAG, "first params " + params.x + ","+params.y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    // 今回イベントでのView移動先の位置
                    int deltaX = x - dragStartX;
                    int deltaY = y - dragStartY;
                    params.x += deltaX;
                    params.y += deltaY;
                    wm.updateViewLayout(view, params);
                    dragStartX = x;
                    dragStartY = y;
                    Log.i(TAG, "update params " + params.x + ","+params.y);
                    break;
            }
            // 今回のタッチ位置を保持
            oldx = x;
            oldy = y;
            // イベント処理完了
            return true;
        }
    }
}
