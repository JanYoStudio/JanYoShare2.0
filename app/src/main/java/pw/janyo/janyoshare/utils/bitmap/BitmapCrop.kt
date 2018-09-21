package pw.janyo.janyoshare.utils.bitmap

import android.graphics.Bitmap

abstract class BitmapCrop {
	abstract fun crop(bitmap: Bitmap): Bitmap
}