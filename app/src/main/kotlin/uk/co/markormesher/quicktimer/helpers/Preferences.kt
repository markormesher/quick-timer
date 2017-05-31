package uk.co.markormesher.quicktimer.helpers

import android.content.Context
import android.preference.PreferenceManager
import uk.co.markormesher.quicktimer.R

object Preferences {

	fun getPrefs(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)!!

	fun shouldKeepScreenOn(context: Context) = getPrefs(context).getBoolean(context.getString(R.string.setting_key_keep_screen_on), true)

	fun shouldVibrateOnTimerEnd(context: Context) = getPrefs(context).getBoolean(context.getString(R.string.setting_key_vibrate_on_timer_end), true)

	fun shouldPlaySoundOnTimerEnd(context: Context) = getPrefs(context).getBoolean(context.getString(R.string.setting_key_sound_on_timer_end), false)

}
