package uk.co.markormesher.quicktimer

import android.content.Context
import android.preference.PreferenceManager

object Preferences {

	fun getPrefs(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)!!

	fun shouldVibrateOnTimerEnd(context: Context) = getPrefs(context).getBoolean(context.getString(R.string.setting_key_vibrate_on_timer_end), true)

	fun shouldPlaySoundOnTimerEnd(context: Context) = getPrefs(context).getBoolean(context.getString(R.string.setting_key_sound_on_timer_end), false)

}
