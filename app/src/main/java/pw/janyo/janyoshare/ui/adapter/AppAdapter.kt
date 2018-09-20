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

package pw.janyo.janyoshare.ui.adapter

import android.content.Context
import androidx.coordinatorlayout.widget.CoordinatorLayout
import android.view.View
import kotlinx.android.synthetic.main.app_bar_main.*

import pw.janyo.janyoshare.R
import pw.janyo.janyoshare.ui.activity.MainActivity
import pw.janyo.janyoshare.model.InstallAPP
import pw.janyo.janyoshare.databinding.ItemAppBinding
import pw.janyo.janyoshare.ui.fragment.AppFragment
import pw.janyo.janyoshare.handler.ItemAppHelper
import pw.janyo.janyoshare.viewModel.MainViewModel
import vip.mystery0.tools.base.binding.BaseBindingRecyclerViewAdapter

class AppAdapter(private val context: Context,
				 private val fragment: AppFragment,
				 private val mainViewModel: MainViewModel) : BaseBindingRecyclerViewAdapter<InstallAPP, ItemAppBinding>(R.layout.item_app) {
	private val coordinatorLayout: CoordinatorLayout = (context as MainActivity).coordinatorLayout

	override fun setItemView(binding: ItemAppBinding, position: Int, data: InstallAPP) {
		val handler = ItemAppHelper(coordinatorLayout, context, this, fragment, mainViewModel)
		val checked = fragment.isChecked(data)
		binding.isChecked = checked
		binding.handler = handler
		binding.installAPP = data
		binding.appDisable.visibility = if (data.isDisable) View.VISIBLE else View.GONE
		binding.checkBox.isChecked = checked
		binding.checkBox.alpha = if (checked) 1f else 0f
		binding.appIcon.alpha = if (checked) 0f else 1f
		binding.root.setOnLongClickListener {
			handler.longClick(data)
			true
		}
		binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
			if (isChecked) {
				fragment.mark(data)
				handler.showAnimation(binding.checkBox, binding.appIcon, true)
			} else {
				fragment.unMark(data)
				handler.showAnimation(binding.checkBox, binding.appIcon, false)
			}
		}
	}

	fun removeList(appList: List<InstallAPP>) {
		appList.forEach { installAPP ->
			val index = items.indexOfFirst { it.packageName == installAPP.packageName }
			if (index != -1) {
				items.removeAt(index)
				notifyItemRemoved(index)
			}
		}
	}

	fun updateList(appList: List<InstallAPP>) {
		appList.forEach { installAPP ->
			val index = items.indexOfFirst { it.packageName == installAPP.packageName }
			if (index != -1) {
				items[index] = installAPP
				notifyItemChanged(index)
			}
		}
	}
}
