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

package pw.janyo.janyoshare.adapter

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.design.widget.CoordinatorLayout
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup

import pw.janyo.janyoshare.R
import pw.janyo.janyoshare.activity.MainActivity
import pw.janyo.janyoshare.classes.InstallAPP
import pw.janyo.janyoshare.databinding.ItemAppBinding
import pw.janyo.janyoshare.fragment.AppFragment
import pw.janyo.janyoshare.handler.ItemAppClickHandler
import vip.mystery0.tools.base.BaseRecyclerViewAdapter

class AppAdapter(private val context: Context,
				 private val installAPPList: ArrayList<InstallAPP>, private val fragment: AppFragment) : BaseRecyclerViewAdapter<AppAdapter.ViewHolder, InstallAPP>(context, R.layout.item_app, installAPPList) {
	private val coordinatorLayout: CoordinatorLayout = (context as MainActivity).coordinatorLayout

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return ViewHolder(createView(parent))
	}

	override fun setItemView(holder: ViewHolder, position: Int, data: InstallAPP) {
		val handler = ItemAppClickHandler(coordinatorLayout, context, fragment, installAPPList)
		holder.binding.handler = handler
		holder.binding.installAPP = data
	}


	inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val binding = DataBindingUtil.bind<ItemAppBinding>(itemView)!!
	}
}
