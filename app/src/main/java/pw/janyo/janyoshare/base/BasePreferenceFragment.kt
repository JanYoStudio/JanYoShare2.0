package pw.janyo.janyoshare.base

import android.os.Bundle
import android.widget.Toast
import androidx.annotation.XmlRes
import androidx.annotation.StringRes
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

abstract class BasePreferenceFragment(@XmlRes private val preferencesResId: Int) : PreferenceFragmentCompat() {
	private var toast: Toast? = null

	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) =setPreferencesFromResource(preferencesResId, rootKey)

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
		initPreference()
		monitor()
	}

	open fun initPreference() {}

	open fun monitor() {}

	fun toastMessage(@StringRes stringRes: Int, isShowLong: Boolean = false) = toastMessage(getString(stringRes), isShowLong)

	fun toastMessage(message: String?, isShowLong: Boolean = false) {
		toast?.cancel()
		toast = Toast.makeText(activity!!, message, if (isShowLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT)
		toast?.show()
	}

	 fun <T : Preference> findPreferenceById(@StringRes id: Int): T {
		@Suppress("UNCHECKED_CAST")
		return findPreference(getString(id)) as T
	}
}