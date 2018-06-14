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

package pw.janyo.janyoshare.activity

import android.os.Bundle
import android.preference.PreferenceActivity
import android.support.design.widget.CoordinatorLayout
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout

import pw.janyo.janyoshare.R
import pw.janyo.janyoshare.fragment.SettingsPreferenceFragment

class SettingsActivity : PreferenceActivity() {
	private var toolbar: Toolbar? = null
	var coordinatorLayout: CoordinatorLayout? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		fragmentManager.beginTransaction().replace(R.id.content_wrapper, SettingsPreferenceFragment()).commit()
		toolbar!!.title = title
	}

	override fun setContentView(layoutResID: Int) {
		val contentView = LayoutInflater.from(this).inflate(R.layout.activity_settings, LinearLayout(this), false) as ViewGroup
		toolbar = contentView.findViewById(R.id.toolbar)
		toolbar!!.setNavigationOnClickListener { finish() }
		coordinatorLayout = contentView.findViewById(R.id.coordinatorLayout)
		val contentWrapper = contentView.findViewById<ViewGroup>(R.id.content_wrapper)
		LayoutInflater.from(this).inflate(layoutResID, contentWrapper, true)
		window.setContentView(contentView)
	}
}
