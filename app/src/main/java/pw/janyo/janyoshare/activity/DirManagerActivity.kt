/*
 * Created by Mystery0 on 18-2-10 下午4:44.
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
 * Last modified 18-2-10 下午4:44
 */

package pw.janyo.janyoshare.activity

import androidx.databinding.DataBindingUtil

import pw.janyo.janyoshare.R
import pw.janyo.janyoshare.databinding.ActivityDirManagerBinding
import pw.janyo.janyoshare.util.Settings
import vip.mystery0.tools.base.BaseActivity

class DirManagerActivity : BaseActivity(R.layout.activity_dir_manager) {
	private lateinit var binding: ActivityDirManagerBinding

	override fun inflateView(layoutId: Int) {
		binding = DataBindingUtil.setContentView(this, layoutId)
	}

	override fun initData() {
		super.initData()
		title = " "

		binding.dirManager.setCurrentPath(Settings.customExportDir)
	}

	override fun monitor() {
		binding.buttonOk.setOnClickListener {
			Settings.customExportDir = binding.dirManager.getCurrentPath()
			finish()
		}
		binding.buttonCancel.setOnClickListener { finish() }
	}
}
