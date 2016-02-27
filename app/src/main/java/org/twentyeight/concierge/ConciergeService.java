package org.twentyeight.concierge;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by YKEI on 2016/02/27.
 */
public class ConciergeService extends Service implements TextToSpeech.OnInitListener {
    WindowManager.LayoutParams prms;

    private static final String TAG = "ConciergeService";
    private View view;
    private WindowManager wm;
    WindowManager.LayoutParams params;
    Handler mHnadler;
    private int mWalkCounter;
    private static final int WALK_COUNT_MAX = 120;
    private Timer mWalkTimer;
    private Timer mAppUsageTimer;
    private TextToSpeech mTts;
    private String mBeforeApp = "";


    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        mHnadler = new Handler();

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
        startWalkCharacter();

        mAppUsageTimer = new Timer();
        mAppUsageTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                String app = Utils.getTopApplicationPackage(ConciergeService.this);
                if (!mBeforeApp.equals(app)) {
                    Log.i(TAG, "app : " +  app);
                    mBeforeApp = app;
                    if ("com.google.android.dialer".equals(app)) {
                        speechText("電話かけるの");
                    }
                }
            }
        }, 100, 100);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // TextToSpeechオブジェクトの生成
        mTts = new TextToSpeech(this, this);

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

    private void startWalkCharacter() {
        mWalkCounter = 0;
        mWalkTimer = new Timer();
        Random random = new Random();
        final double degrees = (double)random.nextInt(360);
        final double radian = Math.toRadians(degrees);
        Log.i(TAG, "degree:" + degrees);
        mWalkTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mWalkCounter < WALK_COUNT_MAX) {
                    walkCharacterOneStep(radian);
                    mWalkCounter++;
                } else {
                    mWalkTimer.cancel();
                }
            }
        }, 16, 16);
    }

    /**
     * キャラを歩かせる
     */
    private void walkCharacterOneStep(final double radian) {
        mHnadler.post(new Runnable() {
            @Override
            public void run() {
//                Log.i(TAG, "rad : " + radian);
//                Log.i(TAG, "x,y : " + params.x + "," + params.y);
                int deltaX = (int) (2 * Math.cos(radian));
                int deltaY = (int) (2 * Math.sin(radian));
                params.x += deltaX;
                params.y += deltaY;
                wm.updateViewLayout(view, params);
            }
        });
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
