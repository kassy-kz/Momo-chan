package org.twentyeight.concierge;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.util.Timer;
import java.util.TimerTask;

import java.lang.reflect.Array;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by YKEI on 2016/02/27.
 */
public class ConciergeService extends Service {
    WindowManager.LayoutParams prms;

    private static final String TAG = "ConciergeService";
    private final Context context = this;
    private View view;
    private WindowManager wm;
    WindowManager.LayoutParams params;
    Handler mHnadler;
    private int mWalkCounter;
    private static final int WALK_COUNT_MAX = 120;
    private Timer mWalkTimer;


    // 現在表示したいID(R.id.hoge)
    private int currentId = 0;

    // アニメーションの実行回数
    private int count = 0;

    // 現在実行すべきアニメーションの種類番号
    private int currentType = 0;

    // タイマー（定期実行関係）
    Timer   mTimer   = null;
    Handler mHandler = new Handler();

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
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

        startWalkCharacter();

        // 定期実行のタイマー設定
        this.currentId = R.id.characterImageView;
        setTimer();

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
                        chenge(currentType);
                    }
                });
            }
        }, 1000, 1000);
    }

    /**
     * 表情を切り替える
     *
     * type:
     *  0: 未定義（「1」を実行。）!!!!!未実装!!!!!!
     *  1: 立ってる。スタンバイ状態。
     *  2: 立ってる。歩いてる。!!!!!未実装!!!!!!
     *  3: 座ってる。首振り。口が空いてる状態。ｹﾗｹﾗｹﾗって感じ。若干激しい首振り。
     *  4: 座ってる。首振り。口が空いてる状態。ほほえみ顔。
     *  5: 座ってる。首振り。口が空いてる状態。普通な感じ。
     *  6: !!!!!未実装!!!!!!
     *  7: !!!!!未実装!!!!!!
     *
     * @param type ☝︎参照
     */
    public void chenge(int type){

        // typeに応じて呼び出すメソッドを変更する
        switch (type){
            case 0:
                this._00();
                break;
            case 1:
                this._01();
                break;
            case 2:
                this._02();
                break;
            case 3:
                this._03();
                break;
            case 4:
                this._04();
                break;
            case 5:
                this._05();
                break;
            default:
                Log.d(TAG, "ERROR : " + type);
                break;
        }

    }

    /**
     * 未定義につき、_01()を実行。
     */
    private void _00(){
        _01();
    }

    /**
     * 立ってる。アイドル状態に変更する
     */
    private void _01(){

        // 表示すべき項目の表示
        ImageView currentImage = (ImageView) view.findViewById(currentId);

        if(count % 2 == 0) {
            Log.d(TAG,"0");
            currentImage.setImageResource(R.drawable.idle_r1);
        }else{
            Log.d(TAG,"1");
            currentImage.setImageResource(R.drawable.idle_r2);
        }

        count++;
    }

    /**
     * 立ってる。歩いてる。
     */
    private void _02(){
        // TODO:変更
        _01();
    }

    /**
     * 座ってる。首振り。口が空いてる状態。ｹﾗｹﾗｹﾗって感じ。若干激しい首振り。
     */
    private void _03(){

        // 表示すべき項目の表示
        ImageView currentImage = (ImageView) view.findViewById(currentId);

        if(count % 3 == 0) {
            currentImage.setImageResource(R.drawable.smile_d_r1);
        }else if(count %3 == 1){
            currentImage.setImageResource(R.drawable.smile_d_r2);
        }else{
            currentImage.setImageResource(R.drawable.smile_d_r3);
        }

        count++;
    }

    /**
     * 座ってる。首振り。口が空いてる状態。ほほえみ顔。
     */
    private void _04(){

        // 表示すべき項目の表示
        ImageView currentImage = (ImageView) view.findViewById(currentId);

        if(count % 2 == 0) {
            currentImage.setImageResource(R.drawable.smile_b_r1);
        }else {
            currentImage.setImageResource(R.drawable.smile_b_r2);
        }

        count++;

    }

    /**
     * 座ってる。
     */
    private void _05(){

        // 表示すべき項目の表示
        ImageView currentImage = (ImageView) view.findViewById(currentId);

        if(count % 2 == 0) {
            currentImage.setImageResource(R.drawable.smile_c_r1);
        }else {
            currentImage.setImageResource(R.drawable.smile_c_r2);
        }

        count++;

    }

    /**
     * 座ってる。未定義につき、_03();を実行
     */
    private void _06(){
        //TODO:実装（取り敢えず画像待ち）
        _03();
    }

    /**
     * テストコード
     */
    public void testChange(){

        // Success
        this.chenge(0);
        this.chenge(1);
        this.chenge(2);
        this.chenge(3);
        this.chenge(4);

        // Faild
        this.chenge(100);

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
}
