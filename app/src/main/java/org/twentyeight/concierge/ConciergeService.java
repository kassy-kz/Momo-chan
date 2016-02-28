package org.twentyeight.concierge;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
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
import android.widget.ImageView;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;
import java.util.HashMap;

/**
 * Created by YKEI on 2016/02/27.
 */
public class ConciergeService extends Service implements TextToSpeech.OnInitListener {
    WindowManager.LayoutParams prms;

    private static final String TAG = "ConciergeService";
    private final Context context = this;
    private View view;
    private WindowManager wm;
    WindowManager.LayoutParams params;
    Handler mHnadler;
    private int mWalkCounter;
    private static final int WALK_COUNT_MAX = 120;
    private Timer mWalkStartTimer;
    private Timer mWalkTimer;
    private Timer mAppUsageTimer;
    private TextToSpeech mTts;
    private String mBeforeApp = "";


    // 現在表示したいID(R.id.hoge)
    private int currentImageViewId = 0;

    // アニメーションの実行回数
    private int count = 0;

    // ↓ これを変えるとキャラがかわります！！
    // 現在実行すべきアニメーションの種類番号
    private String currentType = "1"; // 1〜17にすると変わります！
    private int currentTypeInt = 1; // 1〜17にすると変わります！

    // タイマー（定期実行関係）
    Timer   mTimer   = null;
    Handler mHandler = new Handler();

    // アニメデータのサイクル
    private final int MS = 500;

    // アニメデータ
    AnimationData anime = new AnimationData();

    // 音声再生
    MediaPlayer mMediaPlayer = null;

    /**
     * アニメデータ保持クラス
     */
    public static class AnimationData{

        private HashMap<String,String[][]> animationData = new HashMap<String,String[][]>();
        private final String[] INIT_D = {"", "", ""};
        private final String[][] INIT_DATA = {{"", "", ""}};

        AnimationData(){

            // 立っている
            this.animationData.put("1", new String[][]{{String.valueOf(R.drawable.idle_r1),"700","900"}, {String.valueOf(R.drawable.idle_r2),"700","900"}});

            // 右
            this.animationData.put("2", new String[][]{{String.valueOf(R.drawable.walkright_r1),"700","900"}, {String.valueOf(R.drawable.walkright_r2),"700","900"}, {String.valueOf(R.drawable.walkright_r3),"700","900"}});

            // 後ろ
            this.animationData.put("3", new String[][]{{String.valueOf(R.drawable.walkback_r1),"700","900"}, {String.valueOf(R.drawable.walkback_r2),"700","900"}, {String.valueOf(R.drawable.walkback_r3),"700","900"}});

            // 左
            this.animationData.put("4", new String[][]{{String.valueOf(R.drawable.walkleft_r1),"700","900"}, {String.valueOf(R.drawable.walkleft_r2),"700","900"}, {String.valueOf(R.drawable.walkleft_r3),"700","900"}});

            // 正面
            this.animationData.put("5", new String[][]{{String.valueOf(R.drawable.walkfront_r1),"700","900"}, {String.valueOf(R.drawable.walkfront_r2),"700","900"}, {String.valueOf(R.drawable.walkfront_r3),"700","900"}});

            // スマイルA(座り)
            this.animationData.put("6", new String[][]{{String.valueOf(R.drawable.smile_a_r1),"700","700"}, {String.valueOf(R.drawable.smile_a_r2),"700","700"}, {String.valueOf(R.drawable.smile_a_r3),"700","700"}});

            // スマイルB(座り)
            this.animationData.put("7", new String[][]{{String.valueOf(R.drawable.smile_b_r1),"700","700"}, {String.valueOf(R.drawable.smile_b_r2),"700","700"}});

            // スマイルC(座り)
            this.animationData.put("8", new String[][]{{String.valueOf(R.drawable.smile_c_r1),"700","700"}, {String.valueOf(R.drawable.smile_c_r2),"700","700"}});

            // スマイルD(座り)
            this.animationData.put("9", new String[][]{{String.valueOf(R.drawable.smile_d_r1),"700","700"}, {String.valueOf(R.drawable.smile_d_r2),"700","700"},{String.valueOf(R.drawable.smile_d_r3),"700","700"}});

            // びっくり
            this.animationData.put("10", new String[][]{{String.valueOf(R.drawable.surprise_r1),"700","700"}, {String.valueOf(R.drawable.surprise_r2),"700","700"},{String.valueOf(R.drawable.surprise_r3),"700","700"}});

            // はてな
            this.animationData.put("11", new String[][]{{String.valueOf(R.drawable.question_r),"700","700"}});

            // 困りA
            this.animationData.put("12", new String[][]{{String.valueOf(R.drawable.trouble_a_r),"700","700"}});

            // 困りB
            this.animationData.put("13", new String[][]{{String.valueOf(R.drawable.trouble_b_r1),"700","700"}, {String.valueOf(R.drawable.trouble_b_r2),"700","700"},{String.valueOf(R.drawable.trouble_b_r3),"700","700"}});

            // 困りC
            this.animationData.put("14", new String[][]{{String.valueOf(R.drawable.trouble_c_r1),"700","700"}, {String.valueOf(R.drawable.trouble_c_r2),"700","700"},{String.valueOf(R.drawable.trouble_c_r3),"700","700"}});

            // 渋い顔
            this.animationData.put("15", new String[][]{{String.valueOf(R.drawable.bitter_r1),"700","700"}, {String.valueOf(R.drawable.bitter_r2),"700","700"},{String.valueOf(R.drawable.bitter_r3),"700","700"}});

            // 睡眠
            this.animationData.put("16", new String[][]{{String.valueOf(R.drawable.sleep_r2),"700","700"}, {String.valueOf(R.drawable.sleep_r2),"700","700"}});

            // 目覚め
            this.animationData.put("17", new String[][]{{String.valueOf(R.drawable.awake_r1),"700","700"}, {String.valueOf(R.drawable.awake_r2),"700","700"}});

            // 口パク
            this.animationData.put("18", new String[][]{{String.valueOf(R.drawable.smile_d_r1),"700","700"}, {String.valueOf(R.drawable.smile_a_r2),"700","700"}});
            
        }

        /**
         *
         * @param key
         * @return String[]
         */
        public String[][] get(String key){
            if(this.animationData.get(key) != null) {
                return this.animationData.get(key);
            }else{
                return INIT_DATA;
            }
        }

        // 現在有効なanimation(keyで指定)のnumber番目の配列を取得する
        public String[] getCurrentChild(String key, int number){
            if(this.animationData.get(key) != null) {
                return this.animationData.get(key)[number];
            }else{
                return INIT_D;
            }
        }

    }


    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        mWalkStartTimer = new Timer(true);
        mWalkStartTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                startWalkCharacter();
            }
        }, 5000, 5000);
    }

    /**
     * タイマー
     * アニメーションをする。
     * 本メソッドでは、定期実行のみおこなう。
     * 実処理自体は、別メソッドでおこなっている。
     */
    public void setTimer(){
        mTimer = new Timer(true);
        mTimer.schedule( new TimerTask(){
            @Override
            public void run() {
                mHandler.post( new Runnable() {
                    public void run() {
                        // 設定されている種類に応じた画面遷移(アニメーション(=画像差し替え))
                        update();
                    }
                });
            }
        }, 1000, MS);
    }

    private void update(){

        // 表示すべき項目の表示
        ImageView currentImage = (ImageView) view.findViewById(currentImageViewId);

        // コマ数別
        if(anime.get(currentType).length == 2){
            currentImage.setImageResource(Integer.parseInt(
                    anime.getCurrentChild(currentType, count % 2)[0]));
        }else if(anime.get(currentType).length == 3){
            currentImage.setImageResource(Integer.parseInt(
                    anime.getCurrentChild(currentType, count % 3)[0]));
        }

        count++;

    }

    @Override
    public void onCreate() {
        super.onCreate();

        mHnadler = new Handler();

        // Viewからインフレータを作成する
        LayoutInflater layoutInflater = LayoutInflater.from(this);

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

        mAppUsageTimer = new Timer();
        mAppUsageTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                String app = Utils.getTopApplicationPackage(ConciergeService.this);
                if (!mBeforeApp.equals(app)) {
                    Log.i(TAG, "app : " +  app);
                    mBeforeApp = app;
                    if ("com.google.android.dialer".equals(app)) {
                        speechVoice(R.raw.trg_phoneappstart);
                    } else if ("com.google.android.gm".equals(app)) {
                        speechVoice(R.raw.trg_mail_app_start);
                    } else if ("com.amazon.kindle".equals(app)) {
                        speechVoice(R.raw.trg_kindle);
                    } else if ("com.google.android.music".equals(app)) {
                        speechVoice(R.raw.trg_musicappstart);
                    }
                }
            }
        }, 100, 100);

        // TextToSpeechオブジェクトの生成
        mTts = new TextToSpeech(this, this);

        // 定期実行のタイマー設定
        this.currentImageViewId = R.id.characterImageView;
        setTimer();

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
        private int downX = 0;
        private int downY = 0;

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
//            Log.i(TAG, "onTouch " + event.getAction() +" x,y : " + x + "," + y);

            // 画像と重ならなければスルーする
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = x;
                    downY = y;
                    dragStartX = x;
                    dragStartY = y;
//                    Log.i(TAG, "first params " + params.x + ","+params.y);
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
//                    Log.i(TAG, "update params " + params.x + ","+params.y);
                    break;
                case MotionEvent.ACTION_UP:
                    if (Math.abs(downX - x) < 8 && Math.abs(downY - y) < 8) {
                        speechVoice(R.raw.trg_sleep_off);
                    }
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
        mWalkTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mWalkCounter < WALK_COUNT_MAX) {
                    walkCharacterOneStep(radian);
                    mWalkCounter++;
                } else {
                    mWalkTimer.cancel();
                    if(currentTypeInt < 18) {
                        currentTypeInt++;
                    } else {
                        currentTypeInt = 1;
                    }
                    currentType = currentTypeInt + "";
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

    private void speechVoice(String str) {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.sample1);
            mMediaPlayer.start();
            return;
        }
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
//            mMediaPlayer.prepare();
        }
        Log.i(TAG, "speech voice");
        mMediaPlayer = MediaPlayer.create(this, R.raw.sample1);
        currentTypeInt = 18;
        currentType = "18";
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                currentTypeInt = 0;
                currentType = "0";
            }
        });
        mMediaPlayer.start();
    }

    private void speechVoice(int resId) {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, resId);
            mMediaPlayer.start();
            return;
        }
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
//            mMediaPlayer.prepare();
        }
        Log.i(TAG, "speech voice");
        mMediaPlayer = MediaPlayer.create(this, resId);
        mMediaPlayer.start();
    }
}
