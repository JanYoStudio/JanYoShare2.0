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

package pw.janyo.janyoshare.config

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import pw.janyo.janyoshare.BuildConfig
import pw.janyo.janyoshare.repository.local.db.DBHelper
import vip.mystery0.crashhandler.CrashConfig
import vip.mystery0.crashhandler.CrashHandler
import vip.mystery0.logs.Logs

class APP : Application() {

	override fun onCreate() {
		super.onCreate()
		context = applicationContext
		DBHelper.init(this)
		CrashHandler.getInstance(this)
				.setConfig(CrashConfig()
						.setAutoClean(false))
				.init()
		Logs.setConfig {
			it.setShowLog(BuildConfig.DEBUG)
		}
	}

	companion object {
		@SuppressLint("StaticFieldLeak")
		lateinit var context: Context
			private set
	}
}
