package pw.janyo.janyoshare.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import pw.janyo.janyoshare.utils.Settings
import vip.mystery0.tools.base.BaseActivity

abstract class JanYoBaseActivity(layoutId: Int?) : BaseActivity(layoutId) {
	override fun onCreate(savedInstanceState: Bundle?) {
		when (Settings.nightMode) {
			0 -> delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
			1 -> delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES)
			2 -> delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO)
			3 -> delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
		}
		super.onCreate(savedInstanceState)
	}
}