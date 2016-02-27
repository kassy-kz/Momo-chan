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

/**
 * Created by YKEI on 2016/02/27.
 */
public class ConciergeService extends Service {

    private static final String TAG = "ConciergeService";
    View view;
    WindowManager wm;
    private final Context context = this;

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

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ImageView image = (ImageView) view.findViewById(R.id.unity_chan_walk);
                image.setVisibility(View.VISIBLE);
            }
        }, 500);


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run");
                ImageView image = (ImageView) view.findViewById(R.id.unity_chan_walk);
                image.setVisibility(View.INVISIBLE);
            }
        }, 1500);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run");
                ImageView image = (ImageView) view.findViewById(R.id.unity_chan_walk);
                image.setVisibility(View.VISIBLE);
            }
        }, 2000);

    }

    private void addView(View view){


    }

    private void removeView(){
        wm.removeView(view);
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

        // MEMO:サンプル実装
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.overlay, null);
        ImageView image = (ImageView) view.findViewById(R.id.unity_chan_walk);
        image.setVisibility(View.INVISIBLE);

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
