package org.twentyeight.momo;

import android.app.ActivityManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import java.util.List;

/**
 * Created by kashimoto on 2016/02/28.
 */
public class Utils {

    private static MediaPlayer sMediaPlayer;

    /**
     * トップに起動しているActivityのpackage nameを指定する
     * @param context
     * @return
     */
    public static String getTopApplicationPackage(Context context) {

        if (Build.VERSION.SDK_INT >= 22) {
            UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            UsageEvents events = usm.queryEvents(time - (1000 * 60 * 60), time);
            if (events != null && events.hasNextEvent()) {
                UsageEvents.Event app = new android.app.usage.UsageEvents.Event();
                long lastAppTime = 0;
                String packageName = null;
                while (events.hasNextEvent()) {
                    events.getNextEvent(app);
                    if (app.getTimeStamp() > lastAppTime && app.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                        packageName = app.getPackageName();
                        lastAppTime = app.getTimeStamp();
                    }
                }

                if (!StringUtil.isEmpty(packageName)) {
                    return packageName;
                }
            }
        } else {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo info : processes) {
                if (info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    if (info.importanceReasonComponent != null) {
                        return info.importanceReasonComponent.getPackageName();
                    } else {
                        return info.pkgList[0];
                    }
                }
            }
        }

        return context.getPackageName();
    }

    /**
     * ももちゃんにしゃべらせる
     */
    public static void speechVoice(Context context, int resId, MediaPlayer.OnCompletionListener listener) {
        if (sMediaPlayer != null && sMediaPlayer.isPlaying()) {
            sMediaPlayer.stop();
        }
        sMediaPlayer = MediaPlayer.create(context, resId);
        if (listener != null) {
            sMediaPlayer.setOnCompletionListener(listener);
        }
        sMediaPlayer.start();
    }
}
