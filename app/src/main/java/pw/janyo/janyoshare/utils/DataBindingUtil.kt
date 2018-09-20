/*
 * Created by Mystery0 on 6/14/18 10:19 PM.
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
 * Last modified 6/14/18 10:19 PM
 */

package pw.janyo.janyoshare.utils

import androidx.databinding.BindingAdapter
import androidx.databinding.BindingConversion
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import vip.mystery0.tools.utils.FileTools

object DataBindingUtil {
	private val options = RequestOptions()
			.diskCacheStrategy(DiskCacheStrategy.NONE)

	@JvmStatic
	@BindingAdapter("bind:icon", "bind:path")
	fun iconLoader(imageView: ImageView, icon: Drawable?, path: String?) {
		if (path != null)
			Glide.with(imageView.context).load(path).apply(options).into(imageView)
		else
			imageView.setImageDrawable(icon)
	}

	@JvmStatic
	@BindingConversion
	fun convertAppSize(size: Long): String = FileTools.formatFileSize(size, 2)
}