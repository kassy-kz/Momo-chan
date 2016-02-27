package org.twentyeight.concierge;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Created by YKEI on 2016/02/27.
 */
public class ConciergeService extends Service {
    WindowManager.LayoutParams prms;

    private static final String TAG = "ConciergeService";
    private View view;
    private WindowManager wm;
    private View touchView;
    int recButtonLastX;
    int recButtonLastY;
    int recButtonFirstX;
    int recButtonFirstY;
    boolean touchconsumedbyMove = false;
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

        WindowManager.LayoutParams paramsMatchParent = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        // WindowManagerを取得する
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        // レイアウトファイルから重ね合わせするViewを作成する
        view = layoutInflater.inflate(R.layout.overlay, null);

        // タッチ専用View（全画面）
//        touchView = layoutInflater.inflate(R.layout.overlay_touch, null);

        // ドラッグイベント
//        ImageView characterView = (ImageView) view.findViewById(R.id.unity_chan_walk);
//        characterView.setOnTouchListener(new DragViewListener(characterView));
//        view.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                return false;
//            }
//        });
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
//            if ( oldx < x  && x < oldx + dragView.getWidth() || oldy < y && y < oldy + dragView.getHeight()) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dragStartX = x;
                        dragStartY = y;
                        Log.i(TAG, "first params " + params.x + ","+params.y);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // 今回イベントでのView移動先の位置
//                        int left = dragView.getLeft() + (x - oldx);
//                        int top = dragView.getTop() + (y - oldy);
//                        Log.i(TAG, "   drag " + " x,y : " + left + "," + top);
//
//                        // Viewを移動する
//                        dragView.layout(left, top, left + dragView.getWidth(), top + dragView.getHeight());
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
//            } else {
//                Log.i(TAG, "return false");
//                return false;
//            }
        }
    }

    View.OnTouchListener recButtonOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            WindowManager.LayoutParams prm = getRecbuttonLayout();
            int totalDeltaX = recButtonLastX - recButtonFirstX;
            int totalDeltaY = recButtonLastY - recButtonFirstY;

            switch(event.getActionMasked())
            {
                case MotionEvent.ACTION_DOWN:
                    recButtonLastX = (int) event.getRawX();
                    recButtonLastY = (int) event.getRawY();
                    recButtonFirstX = recButtonLastX;
                    recButtonFirstY = recButtonLastY;
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                case MotionEvent.ACTION_MOVE:
                    int deltaX = (int) event.getRawX() - recButtonLastX;
                    int deltaY = (int) event.getRawY() - recButtonLastY;
                    recButtonLastX = (int) event.getRawX();
                    recButtonLastY = (int) event.getRawY();
                    if (Math.abs(totalDeltaX) >= 5  || Math.abs(totalDeltaY) >= 5) {
                        if (event.getPointerCount() == 1) {
                            prm.x += deltaX;
                            prm.y += deltaY;
                            touchconsumedbyMove = true;
                            wm.updateViewLayout(view, prm);
                        }
                        else{
                            touchconsumedbyMove = false;
                        }
                    }else{
                        touchconsumedbyMove = false;
                    }
                    break;
                default:
                    break;
            }
            return touchconsumedbyMove;
        }
    };

    private WindowManager.LayoutParams getRecbuttonLayout() {
        if (prms != null) {
            return prms;
        }
        prms = new WindowManager.LayoutParams();
        prms.format = PixelFormat.TRANSLUCENT;
        prms.flags = WindowManager.LayoutParams.FORMAT_CHANGED; // 8
        prms.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        prms.gravity = Gravity.TOP | Gravity.CENTER;
        prms.width = WindowManager.LayoutParams.WRAP_CONTENT;
        prms.height = WindowManager.LayoutParams.WRAP_CONTENT;
        // Tools.Log("getRecbuttonLayout", "return getRecbuttonLayout()");
        return prms;
    }


}
