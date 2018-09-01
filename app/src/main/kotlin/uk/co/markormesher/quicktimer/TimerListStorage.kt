package uk.co.markormesher.quicktimer

import android.content.Context
import uk.co.markormesher.quicktimer.helpers.Preferences.getPrefs

object TimerListStorage {

	private const val TIMER_LIST_KEY = "timer_list"
	private const val DEFAULT_TIMER_LIST = "30,60,90"

	fun getTimerList(context: Context): List<Int> = getPrefs(context)
			.getString(TIMER_LIST_KEY, DEFAULT_TIMER_LIST)
			.split(",")
			.filter { it.isNotEmpty() }
			.map { it.toInt() }

	fun addTimer(context: Context, duration: Int) {
		val list = ArrayList<Int>(getTimerList(context))
		list.add(duration)
		list.sort()
		getPrefs(context).edit()
				.putString(TIMER_LIST_KEY, list.joinToString(","))
				.apply()
	}

	fun removeTimer(context: Context, duration: Int) {
		val list = ArrayList<Int>(getTimerList(context))
		list.remove(duration)
		getPrefs(context).edit()
				.putString(TIMER_LIST_KEY, list.joinToString(","))
				.apply()
	}

}
