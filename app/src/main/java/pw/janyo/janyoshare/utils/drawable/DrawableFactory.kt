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

package pw.janyo.janyoshare.utils.drawable

import android.graphics.Bitmap
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import pw.janyo.janyoshare.utils.bitmap.BitmapCropContext

import java.io.File
import java.io.FileOutputStream

import vip.mystery0.logs.Logs

class DrawableFactory {
	private val drawableConvertContext = DrawableConvertContext()
	private val bitmapCropContext = BitmapCropContext()

	fun save(drawable: Drawable, path: String): Boolean {
		val file = File(path)
		if (!file.parentFile.exists()) {
			val mkdirs = file.parentFile.mkdirs()
			if (!mkdirs) {
				Logs.e("save: 创建文件夹失败")
				return false
			}
		}
		var fileOutputStream: FileOutputStream? = null
		try {
			fileOutputStream = FileOutputStream(file)
			var bitmap = drawableConvertContext.convert(drawable) ?: return false
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && drawable is AdaptiveIconDrawable)
				bitmap = bitmapCropContext.crop(bitmap)
			//图片裁剪在这里调用
			bitmap.compress(Bitmap.CompressFormat.PNG, 1, fileOutputStream)
		} catch (e: Exception) {
			e.printStackTrace()
			return false
		} finally {
			fileOutputStream?.close()
		}
		return true
	}
}
