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
import androidx.databinding.DataBindingUtil
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup

import pw.janyo.janyoshare.R
import pw.janyo.janyoshare.activity.MainActivity
import pw.janyo.janyoshare.classes.InstallAPP
import pw.janyo.janyoshare.databinding.ItemAppBinding
import pw.janyo.janyoshare.fragment.AppFragment
import pw.janyo.janyoshare.handler.ItemAppHelper
import vip.mystery0.tools.base.BaseRecyclerViewAdapter

class AppAdapter(private val context: Context,
				 private val installAPPList: ArrayList<InstallAPP>,
				 private val fragment: AppFragment) : BaseRecyclerViewAdapter<AppAdapter.ViewHolder, InstallAPP>(context, R.layout.item_app, installAPPList) {
	private val coordinatorLayout: CoordinatorLayout = (context as MainActivity).coordinatorLayout

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return ViewHolder(createView(parent))
	}

	override fun setItemView(holder: ViewHolder, position: Int, data: InstallAPP) {
		val handler = ItemAppHelper(coordinatorLayout, context, fragment, installAPPList)
		val checked = fragment.isChecked(data)
		holder.binding.isChecked = checked
		holder.binding.handler = handler
		holder.binding.installAPP = data
		holder.binding.appDisable.visibility = if (data.isDisable) View.VISIBLE else View.GONE
		holder.binding.checkBox.isChecked = checked
		holder.binding.checkBox.alpha = if (checked) 1f else 0f
		holder.binding.appIcon.alpha = if (checked) 0f else 1f
		holder.binding.root.setOnLongClickListener {
			handler.longClick(data)
			true
		}
		holder.binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
			if (holder.adapterPosition != position)
				return@setOnCheckedChangeListener
			if (isChecked) {
				fragment.mark(data)
				handler.showAnimation(holder.binding.checkBox, holder.binding.appIcon, true)
			} else {
				fragment.unMark(data)
				handler.showAnimation(holder.binding.checkBox, holder.binding.appIcon, false)
			}
		}
	}

	inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val binding = DataBindingUtil.bind<ItemAppBinding>(itemView)!!
	}
}
