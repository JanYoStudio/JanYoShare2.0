package pw.janyo.janyoshare.util.drawable;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.graphics.drawable.VectorDrawableCompat;

public class VectorDrawableCompatConvert extends DrawableConvert {
    @Override
    protected Bitmap convert(Drawable drawable) {
        VectorDrawableCompat vectorDrawableCompat = (VectorDrawableCompat) drawable;
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawableCompat.getIntrinsicWidth(), vectorDrawableCompat.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawableCompat.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawableCompat.draw(canvas);
        return bitmap;
    }
}
