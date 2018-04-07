/*
 * Created by Mystery0 on 18-2-10 下午4:00.
 * Copyright (c) 2018. All Rights reserved.
 *
 *                    =====================================================
 *                    =                                                   =
 *                    =                       _oo0oo_                     =
 *                    =                      o8888888o                    =
 *                    =                      88" . "88                    =
 *                    =                      (| -_- |)                    =
 *                    =                      0\  =  /0                    =
 *                    =                    ___/`---'\___                  =
 *                    =                  .' \\|     |# '.                 =
 *                    =                 / \\|||  :  |||# \                =
 *                    =                / _||||| -:- |||||- \              =
 *                    =               |   | \\\  -  #/ |   |              =
 *                    =               | \_|  ''\---/''  |_/ |             =
 *                    =               \  .-\__  '-'  ___/-. /             =
 *                    =             ___'. .'  /--.--\  `. .'___           =
 *                    =          ."" '<  `.___\_<|>_/___.' >' "".         =
 *                    =         | | :  `- \`.;`\ _ /`;.`/ - ` : | |       =
 *                    =         \  \ `_.   \_ __\ /__ _/   .-` /  /       =
 *                    =     =====`-.____`.___ \_____/___.-`___.-'=====    =
 *                    =                       `=---='                     =
 *                    =     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~   =
 *                    =                                                   =
 *                    =               佛祖保佑         永无BUG              =
 *                    =                                                   =
 *                    =====================================================
 *
 * Last modified 18-2-10 下午4:00
 */

package pw.janyo.janyoshare.util.drawable;

import android.graphics.Bitmap;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.support.graphics.drawable.VectorDrawableCompat;

import vip.mystery0.logs.Logs;

public class DrawableConvertContext {
    private static final String TAG = "DrawableConvertContext";
    private DrawableConvert drawableConvert;

    public Bitmap convert(Drawable drawable) {
        try {
            if (drawable instanceof BitmapDrawable)
                drawableConvert = new BitmapDrawableConvert();
            else if (drawable instanceof VectorDrawableCompat)
                drawableConvert = new VectorDrawableCompatConvert();
            else if (drawable instanceof VectorDrawable)
                drawableConvert = new VectorDrawableConvert();
            else if (Build.VERSION.SDK_INT >= 26)
                if (drawable instanceof AdaptiveIconDrawable)
                drawableConvert = new AdaptiveIconDrawableConvert();
            else
                return null;
            return drawableConvert.convert(drawable);
        } catch (Exception e) {
            Logs.wtf(TAG, "convert: ", e);
            return null;
        }
    }
}
