package pw.janyo.janyoshare.utils.bitmap

import android.graphics.Bitmap
import pw.janyo.janyoshare.utils.Settings

/**
 * Created by mystery0.
 */
class BitmapCropContext {
	private lateinit var bitmapCrop: BitmapCrop

	companion object {
		const val DEFAULT = 0
		const val ROUND = 1
		const val RECTANGLE = 2
		const val ROUND_RECTANGLE = 3
	}

	private fun setCropType(type: Int) {
		bitmapCrop = when (type) {
			DEFAULT -> DefaultBitmapCrop()
			ROUND -> RoundBitmapCrop()
			RECTANGLE -> RectangleBitmapCrop()
			ROUND_RECTANGLE -> RoundRectangleCrop()
			else -> throw NullPointerException("the type is null")
		}
	}

	fun crop(bitmap: Bitmap?): Bitmap {
		if (!::bitmapCrop.isInitialized)
			setCropType(Settings.cropType)
		if (bitmap == null)
			throw NullPointerException("bitmap can not be null")
		return bitmapCrop.crop(bitmap)
	}
}