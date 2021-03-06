package pw.janyo.janyoshare.utils.bitmap

import android.graphics.*

class RoundRectangleCrop : BitmapCrop() {
	override fun crop(bitmap: Bitmap): Bitmap {
		val width = bitmap.width
		val height = bitmap.height
		val round = if (width <= height) width * 12 / 192f else height * 12 / 192f

		val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
		val canvas = Canvas(output)
		val paint = Paint()
		val rectF = RectF(width * 20 / 192f, height * 20 / 192f, width * 172 / 192f, height * 172 / 192f)
		paint.isAntiAlias = true
		canvas.drawARGB(0, 0, 0, 0)
		canvas.drawRoundRect(rectF, round, round, paint)
		paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)//设置图像重叠时的处理方式
		canvas.drawBitmap(bitmap, 0f, 0f, paint)
		return output
	}
}