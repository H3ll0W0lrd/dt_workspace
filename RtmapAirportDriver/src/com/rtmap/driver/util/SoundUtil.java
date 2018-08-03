package com.rtmap.driver.util;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * Created by liyan on 15/11/16.
 */
public class SoundUtil {
    private static MediaPlayer mp;

    /***
     * @param context
     * @param resId 音乐文件ID
     * @param loop  // 循环次数，0无不循环，-1无永远循环
     * @return
     */
    public static void playSound(Context context, int resId, boolean loop) {

        if (mp == null) {
            mp = MediaPlayer.create(context, resId);
        }
        mp.start();
        mp.setLooping(loop);
    }

    public static void stopSound() {
        if (mp != null) {
            mp.release();
            mp = null;
        }
    }
}
