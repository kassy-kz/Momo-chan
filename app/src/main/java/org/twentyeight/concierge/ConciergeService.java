package org.twentyeight.concierge;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.util.Timer;
import java.util.TimerTask;

import java.lang.reflect.Array;

/**
 * Created by YKEI on 2016/02/27.
 */
public class ConciergeService extends Service {

    private static final String TAG = "ConciergeService";
    private final Context context = this;

    View view;
    WindowManager wm;

    // 現在表示したいID(R.id.hoge)
    private int currentId = 0;

    // 以前表示していたID(R.id.hoge)
    private int oldId = 0;

    Timer   mTimer   = null;
    Handler mHandler = new Handler();

    private int count = 0;


    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        // Viewからインフレータを作成する
        LayoutInflater layoutInflater = LayoutInflater.from(this);

        // 重ね合わせするViewの設定を行う
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        // WindowManagerを取得する
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        // レイアウトファイルから重ね合わせするViewを作成する
        view = layoutInflater.inflate(R.layout.overlay, null);

        // WindowManagerを取得する
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        // Viewを画面上に重ね合わせする
        wm.addView(view, params);


//        testChange();
//        addHandler(500, R.id.idle_r1);
//
//        addHandler(2500, R.id.idle_r1);
//        addHandler(3500, R.id.idle_r2);
//        addHandler(4500, R.id.idle_r1);

        this.currentId = R.id.characterImageView;
        setTimer();

    }


    public void setTimer(){

        mTimer = new Timer(true);
        mTimer.schedule( new TimerTask(){
            @Override
            public void run() {

                mHandler.post( new Runnable() {
                    public void run() {

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
                });

            }
        }, 1000, 1000);

    }
    
    /**
     * 表情を切り替える
     * @param type 0: １: 2:
     */
    private void chenge(int type){

        // typeに応じて呼び出すメソッドを変更する
        switch (type){
            case 0:

                this._01();

                break;
            case 1:
                Log.d(TAG, "1");
                break;
            case 2:
                Log.d(TAG, "2");
                break;
            case 3:
                Log.d(TAG, "3");
                break;
            case 4:
                Log.d(TAG, "4");
                break;
            default:
                Log.d(TAG, "ERROR : " + type);
                break;
        }

    }

    /**
     * 立ってる。アイドル状態に変更する
     */
    private void _01(){

    }

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
































}
