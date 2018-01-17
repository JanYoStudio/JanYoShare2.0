package pw.janyo.janyoshare.util.drawable;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class BitmapDrawableConvert extends DrawableConvert {
    @Override
    protected Bitmap convert(Drawable drawable) {
        return ((BitmapDrawable) drawable).getBitmap();
    }
}
