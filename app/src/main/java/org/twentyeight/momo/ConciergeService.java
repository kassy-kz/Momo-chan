package org.twentyeight.momo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.Random;
import android.widget.ImageView;
import java.util.Timer;
import java.util.TimerTask;
import java.util.HashMap;

/**
 * ももちゃんのOverlay表示と Usage変化による応答（アプリ起動をチェック）するサービス
 */
public class ConciergeService extends Service {

    private static final String TAG = "ConciergeService";

    /**
     * ももちゃんのアニメーションの種別を表す定数
     */
    private static final Integer MOMO_STAND = 0;
    private static final Integer MOMO_WALK_L = 1;
    private static final Integer MOMO_WALK_R = 2;
    private static final Integer MOMO_WALK_U = 3;
    private static final Integer MOMO_WALK_D = 4;
    private static final Integer MOMO_SMILE_A = 5;
    private static final Integer MOMO_SMILE_B = 6;
    private static final Integer MOMO_SMILE_C = 7;
    private static final Integer MOMO_SMILE_D = 8;
    private static final Integer MOMO_SURPRISE = 9;
    private static final Integer MOMO_QUESTION = 10;
    private static final Integer MOMO_TROUBLE_A = 11;
    private static final Integer MOMO_TROUBLE_B = 12;
    private static final Integer MOMO_BITTER = 13;
    private static final Integer MOMO_SLEEP = 14;
    private static final Integer MOMO_CRY = 15;
    private static final Integer MOMO_TALK = 16;

    /**
     * ももちゃんのアニメに関する定数
      */
    // 基本のアニメ間隔
    private static final long PERIOD_ANIME = 500;
    // 歩き始める間隔
    private static final int PERIOD_WALK_START = 10000;
    // 歩き始めてから歩き終わるまで
    private static final int WALK_COUNT_MAX = 180; // 60fps x 3秒
    // ドラッグした時に目を回す距離
    private static final int DRAG_LENGTH_GURUGURU = 2000;

    private View mOverlayView;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;
    private Handler mHandler;
    private String mBeforeApp = "";

    // タイマー（定期実行関係）
    private Timer mImageChangeTimer;
    private Timer mWalkStartTimer;
    private Timer mWalkTimer;
    private Timer mAppUsageTimer;

    // ももちゃんのImageView
    ImageView mMainImageView;

    // アニメに関する変数とフラグ
    private int mWalkCounter;
    private int mAnimeType = 0;
    private int mAnimeCount = 0;
    private boolean mDraggedFlag = false;
    private boolean mTalkingFlag = false;
    private static Context sContext;
    private boolean mMomoAtWall = false;
    private boolean mGuruguruFlag = false;

    /**
     * アニメーションのパターンの定義
     */
    private HashMap<Integer, int[]> mAnimeImageMap = new HashMap<>();

    private void setImageMap() {
        // 素立ち
        mAnimeImageMap.put(MOMO_STAND, new int[]{R.drawable.idle_r1, R.drawable.idle_r2});
        // 歩き左
        mAnimeImageMap.put(MOMO_WALK_L, new int[]{R.drawable.walkright_r1, R.drawable.walkright_r2, R.drawable.walkright_r1, R.drawable.walkright_r3});
        // 歩き右
        mAnimeImageMap.put(MOMO_WALK_R, new int[]{R.drawable.walkleft_r1, R.drawable.walkleft_r2, R.drawable.walkleft_r1, R.drawable.walkleft_r3});
        // 歩き上
        mAnimeImageMap.put(MOMO_WALK_U, new int[]{R.drawable.walkback_r1, R.drawable.walkback_r2, R.drawable.walkback_r1, R.drawable.walkback_r3});
        // 歩き下
        mAnimeImageMap.put(MOMO_WALK_D, new int[]{R.drawable.walkfront_r1, R.drawable.walkfront_r2, R.drawable.walkfront_r1, R.drawable.walkfront_r3});
        // スマイルA座り
        mAnimeImageMap.put(MOMO_SMILE_A, new int[]{R.drawable.smile_a_r1, R.drawable.smile_a_r2, R.drawable.smile_a_r1, R.drawable.smile_a_r3});
        // スマイルB座り
        mAnimeImageMap.put(MOMO_SMILE_B, new int[]{R.drawable.smile_b_r1, R.drawable.smile_b_r2});
        // スマイルC座り
        mAnimeImageMap.put(MOMO_SMILE_C, new int[]{R.drawable.smile_c_r1, R.drawable.smile_c_r2});
        // スマイルD座り
        mAnimeImageMap.put(MOMO_SMILE_D, new int[]{R.drawable.smile_d_r1, R.drawable.smile_d_r2, R.drawable.smile_d_r1, R.drawable.smile_d_r3});
        // びっくり
        mAnimeImageMap.put(MOMO_SURPRISE, new int[]{R.drawable.surprise_r1, R.drawable.surprise_r2, R.drawable.surprise_r1, R.drawable.surprise_r3});
        // はてな
        mAnimeImageMap.put(MOMO_QUESTION, new int[]{R.drawable.question_r});
        // 困りA
        mAnimeImageMap.put(MOMO_TROUBLE_A, new int[]{R.drawable.trouble_b_r1, R.drawable.trouble_b_r2, R.drawable.trouble_b_r1, R.drawable.trouble_b_r3});
        // 困りB
        mAnimeImageMap.put(MOMO_TROUBLE_B, new int[]{R.drawable.trouble_c_r1, R.drawable.trouble_c_r2, R.drawable.trouble_c_r1, R.drawable.trouble_c_r3});
        // 渋い顔
        mAnimeImageMap.put(MOMO_BITTER, new int[]{R.drawable.bitter_r1, R.drawable.bitter_r2, R.drawable.bitter_r1, R.drawable.bitter_r3});
        // 眠り
        mAnimeImageMap.put(MOMO_SLEEP, new int[]{R.drawable.sleep_r1, R.drawable.sleep_r2});
        // なき
        mAnimeImageMap.put(MOMO_CRY, new int[]{R.drawable.cry_r1, R.drawable.cry_r2});
        // 口パク
        mAnimeImageMap.put(MOMO_TALK, new int[]{R.drawable.smile_d_r1, R.drawable.smile_a_r1});

    }

    /**
     * onStartCommand
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        mWalkStartTimer = new Timer(true);
        mWalkStartTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                startWalkCharacter();
            }
        }, PERIOD_WALK_START, PERIOD_WALK_START);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mHandler = new Handler();
        sContext = this;

        // Viewからインフレータを作成する
        LayoutInflater layoutInflater = LayoutInflater.from(this);

        // 重ね合わせするViewの設定を行う
        mParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        // WindowManagerを取得する
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        // レイアウトファイルから重ね合わせするViewを作成する
        mOverlayView = layoutInflater.inflate(R.layout.overlay, null);
        mOverlayView.setOnTouchListener(new DragViewListener(mOverlayView));

        // Viewを画面上に重ね合わせする
        mWindowManager.addView(mOverlayView, mParams);

        Log.d(TAG, "onStart end");

        mAppUsageTimer = new Timer();
        mAppUsageTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                checkLaunchingApp();
            }
        }, 100, 100);

        // 定期実行のタイマー設定
        setImageMap();
        mMainImageView = (ImageView) mOverlayView.findViewById(R.id.characterImageView);
        setImageChangeTimer();
    }

    /**
     * どのアプリが起動してるか調べる
     */
    private void checkLaunchingApp() {
        String app = Utils.getTopApplicationPackage(ConciergeService.this);
        if (!mBeforeApp.equals(app)) {
            Log.i(TAG, "app : " + app);
            mBeforeApp = app;
            if ("com.google.android.dialer".equals(app)) {
                speechMomo(R.raw.mm_130_phone_denwakakemasune);
            } else if ("com.google.android.gm".equals(app)) {
                speechMomo(R.raw.mm_15_mailapp_daijinayou);
            } else if ("com.amazon.kindle".equals(app)) {
                speechMomo(R.raw.mm_1_kindle_honyomuno);
            } else if ("com.google.android.music".equals(app)) {
                speechMomo(R.raw.mm_131_music_ongakukikuno);
            } else if ("com.google.android.GoogleCamera".equals(app)) {
                speechMomo(R.raw.mm_9_camera_makasete);
            } else if ("com.android.chrome".equals(app)) {
                speechMomo(R.raw.mm_13_browser_shirabemono);
            } else if ("jp.naver.line.android".equals(app)) {
                speechMomo(R.raw.mm_201_line_surunone);
            } else if ("com.android.providers.downloads".equals(app)) {
                speechMomo(R.raw.mm_204_search_sagasunone);
            } else if ("jp.co.rakuten.kobo".equals(app)) {
                speechMomo(R.raw.mm_206_kobo_kobodane);
            } else if ("jp.co.rakuten.appmarket".equals(app)) {
                speechMomo(R.raw.mm_205_rakuten_tokubetsunakanji);
            } else if ("com.google.android.apps.maps".equals(app)) {
                speechMomo(R.raw.mm_207_map_dokoikuno);
            } else if ("jp.co.yahoo.android.apps.transit".equals(app)) {
                speechMomo(R.raw.mm_199_norikae_tuginonorikae);
            } else if ("jp.co.jorudan.nrkj".equals(app)) {
                speechMomo(R.raw.mm_199_norikae_tuginonorikae);
            } else if ("com.google.android.apps.photos".equals(app)) {
                speechMomo(R.raw.mm_198_picture_kireinashashin);
            } else if ("com.android.vending".equals(app)) {
                speechMomo(R.raw.mm_204_search_sagasunone);
            }
        }
    }

    /**
     * onDestroy
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy *********************************");

        // サービスが破棄されるときには重ね合わせしていたViewを削除する
        mWindowManager.removeView(mOverlayView);

        // タイマー全部破棄
        if (mWalkStartTimer != null) {
            mWalkStartTimer.cancel();
        }
        if (mWalkTimer != null) {
            mWalkTimer.cancel();
        }
        if (mAppUsageTimer != null) {
            mAppUsageTimer.cancel();
        }
        if (mImageChangeTimer != null) {
            mImageChangeTimer.cancel();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // do nothing
        return null;
    }

    /**
     * アニメーション用のタイマーをセットする
     */
    private void setImageChangeTimer() {
        if (mImageChangeTimer != null) {
            mImageChangeTimer.cancel();
        }
        mImageChangeTimer = new Timer(true);
        mImageChangeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        changeMainImage();
                    }
                });
            }
        }, 0, PERIOD_ANIME);
    }

    /**
     * ももちゃんの画像を差し替える
     */
    private void changeMainImage() {
        int[] animeImages = mAnimeImageMap.get(mAnimeType);
        int imageId = animeImages[mAnimeCount % animeImages.length];
        mMainImageView.setImageResource(imageId);
        mAnimeCount++;
    }

    /**
     * アニメの種別を変更する
     * @param type
     */
    private void changeAnimeType(int type) {
        // 変化がないならそのまま
        if (type == mAnimeType) {
            return;
        }
        mAnimeCount = 0;
        mAnimeType = type;
        setImageChangeTimer();
    }

    /**
     * ももちゃんをタッチした時のリスナー
     */
    private class DragViewListener implements View.OnTouchListener {
        private int dragStartX = 0;
        private int dragStartY = 0;
        private int downX = 0;
        private int downY = 0;
        private int totalDrag = 0;

        public DragViewListener(View dragView) {
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            // タッチしている位置取得
            int x = (int) event.getRawX();
            int y = (int) event.getRawY();
//            Log.i(TAG, "onTouch " + event.getAction() +" x,y : " + x + "," + y);
            // 画像と重ならなければスルーする
            switch (event.getAction()) {
                // おした時
                case MotionEvent.ACTION_DOWN:
                    downX = x;
                    downY = y;
                    dragStartX = x;
                    dragStartY = y;
                    totalDrag = 0;
//                    Log.i(TAG, "first mParams " + mParams.x + ","+mParams.y);
                    break;

                // ドラッグしたとき
                case MotionEvent.ACTION_MOVE:
                    mDraggedFlag = true;
                    // 今回イベントでのView移動先の位置
                    int deltaX = x - dragStartX;
                    int deltaY = y - dragStartY;
                    mParams.x += deltaX;
                    mParams.y += deltaY;
                    // 画面外に出ないかチェック
                    checkMomoAtWall(mParams);
                    if (!mMomoAtWall) {
                        mWindowManager.updateViewLayout(view, mParams);
                        totalDrag += Math.abs(deltaX) + Math.abs(deltaY);
                    } else {
                        mParams.x -= deltaX;
                        mParams.y -= deltaY;
                    }

                    // ドラッグ距離に応じてももちゃんの表情をかえる
                    if (totalDrag < DRAG_LENGTH_GURUGURU) {
                        changeAnimeType(MOMO_SURPRISE);
                    } else {
                        changeAnimeType(MOMO_TROUBLE_B);
                    }
                    dragStartX = x;
                    dragStartY = y;
//                    Log.i(TAG, "update mParams " + mParams.x + ","+mParams.y);
                    break;

                // 離した時
                case MotionEvent.ACTION_UP:
                    // ドラッグをしていない時 => なんか喋らせる
                    if (Math.abs(downX - x) < 8 && Math.abs(downY - y) < 8) {
                        Random random = new Random();
                        int rand = random.nextInt(5);
                        switch (rand) {
                            case 0:
                                speechMomo(R.raw.mm_22_random_makasete);
                                break;
                            case 1:
                                speechMomo(R.raw.mm_36_random_nodogakawaite);
                                break;
                            case 2:
                                speechMomo(R.raw.mm_42_random_zuttosobani);
                                break;
                            case 3:
                                speechMomo(R.raw.mm_20_random_kyoumoganbaru);
                                break;
                            case 4:
                                speechMomo(R.raw.mm_21_random_himomoseyuri);
                                break;
                        }
                    }
                    // ドラッグをしてたとき
                    else {
                        if (totalDrag > DRAG_LENGTH_GURUGURU) {
                            changeAnimeType(MOMO_TROUBLE_B);
                            mGuruguruFlag = true;
                            speechMomo(R.raw.mm_4_error_huee, false);
                        } else {
                            changeAnimeType(MOMO_TROUBLE_A);
                        }
                    }
                    break;
            }
            // イベント処理完了
            return true;
        }
    }

    /**
     * キャラを歩かせる
     */
    private void startWalkCharacter() {

        // ドラッグ中 or 喋り中なら歩かない
        if (mGuruguruFlag) {
            mGuruguruFlag = false;
            return;
        }
        if (mTalkingFlag) {
            return;
        }
        if (mDraggedFlag) {
            mDraggedFlag = false;
            return;
        }
        mWalkCounter = 0;
        mWalkTimer = new Timer();
        Random random = new Random();
        final double degrees = (double)random.nextInt(360);
        final double radian = Math.toRadians(degrees);

        if (degrees < 45) {
            changeAnimeType(MOMO_WALK_R);
        } else if (degrees < 135) {
            changeAnimeType(MOMO_WALK_D);
        } else if (degrees < 225) {
            changeAnimeType(MOMO_WALK_L);
        } else if (degrees < 315) {
            changeAnimeType(MOMO_WALK_U);
        } else {
            changeAnimeType(MOMO_WALK_R);
        }

        mWalkTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mWalkCounter < WALK_COUNT_MAX) {
                    walkCharacterOneStep(radian);
                    mWalkCounter++;
                } else {
                    mWalkTimer.cancel();
                    changeAnimeType(MOMO_STAND);
                }
            }
        }, 16, 16);
    }

    /**
     * キャラを歩かせる
     * @param radian 歩かせる方向のみ指定（ラジアン）
     */
    private void walkCharacterOneStep(final double radian) {
        // ドラッグ中もしくはしゃべり中ならあるかない
        if (mDraggedFlag || mTalkingFlag) {
            return;
        }

        mHandler.post(new Runnable() {
            // 歩く
            @Override
            public void run() {
                try {
                    int deltaX = (int) (2 * Math.cos(radian));
                    int deltaY = (int) (2 * Math.sin(radian));
                    mParams.x += deltaX;
                    mParams.y += deltaY;
                    checkMomoAtWall(mParams);
                    if (!mMomoAtWall) {
                        mWindowManager.updateViewLayout(mOverlayView, mParams);
                    }
                    // 歩いて壁にぶつかった
                    else {
                        mParams.x -= deltaX;
                        mParams.y -= deltaY;
                        changeAnimeType(MOMO_QUESTION);
                    }
                }
                // タイミングによっては例外はくこともあるのでキャッチしておく
                catch (IllegalArgumentException e) {
                    Log.i(TAG, "IllegalArgumentException");
                }
            }
        });
    }

    /**
     * ももちゃんに喋らせる
     */
    private void speechMomo(int resId) {
        speechMomo(resId, true);
    }

    /**
     * ももちゃんにしゃべらせる
     * @param resId
     * @param isTalk おしゃべりか否か（表情に関わる）
     */
    private void speechMomo(int resId, boolean isTalk) {
        mTalkingFlag = true;
        // おしゃべり
        if (isTalk) {
            changeAnimeType(MOMO_TALK);
            Utils.speechVoice(sContext, resId, new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mTalkingFlag = false;
                    changeAnimeType(MOMO_SMILE_A);
                }
            });
        }
        // それ以外
        else {
            Utils.speechVoice(sContext, resId, null);
        }
    }

    /**
     * ももちゃんが壁際にいるか（これ以上歩けないか）調査する
     * @return
     */
    private void checkMomoAtWall(WindowManager.LayoutParams params) {
        int momoWidth = mMainImageView.getWidth();
        int momoHeight = mMainImageView.getHeight();
        Point point = new Point();
        mWindowManager.getDefaultDisplay().getSize(point);
        int dispWidth = point.x;
        int dispHeight = point.y;

//        Log.i(TAG, "params " + params.x + "," + params.y);

        if ( (params.x - momoWidth/2) < -dispWidth/2 ) {
            mMomoAtWall = true;
            Log.i(TAG, "  wall");
        } else if (dispWidth/2 < (params.x + momoWidth/2)) {
            mMomoAtWall = true;
            Log.i(TAG, "  wall");
        } else if ((params.y - momoHeight/2) < -dispHeight/2 ) {
            mMomoAtWall = true;
            Log.i(TAG, "  wall");
        } else if (dispHeight/2 < (params.y + momoHeight/2)) {
            mMomoAtWall = true;
            Log.i(TAG, "  wall");
        } else {
            mMomoAtWall = false;
        }
    }
}
