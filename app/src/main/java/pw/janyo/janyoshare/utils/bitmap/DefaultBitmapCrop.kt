package pw.janyo.janyoshare.utils.bitmap

import android.graphics.Bitmap

class DefaultBitmapCrop : BitmapCrop() {
	override fun crop(bitmap: Bitmap): Bitmap = bitmap
}