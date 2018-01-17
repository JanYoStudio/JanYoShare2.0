package pw.janyo.janyoshare.util.drawable;

import android.graphics.Bitmap;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.support.graphics.drawable.VectorDrawableCompat;

public class DrawableConvertContext {
    private DrawableConvert drawableConvert;

    public Bitmap convert(Drawable drawable) {
        if (drawable instanceof BitmapDrawable)
            drawableConvert = new BitmapDrawableConvert();
        else if (drawable instanceof VectorDrawableCompat)
            drawableConvert = new VectorDrawableCompatConvert();
        else if (drawable instanceof VectorDrawable)
            drawableConvert = new VectorDrawableConvert();
        else if (drawable instanceof AdaptiveIconDrawable)
            drawableConvert = new AdaptiveIconDrawableConvert();
        else
            return null;
        return drawableConvert.convert(drawable);
    }
}
