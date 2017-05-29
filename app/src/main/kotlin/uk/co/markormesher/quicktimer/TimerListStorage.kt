package uk.co.markormesher.quicktimer

import android.content.Context
import uk.co.markormesher.quicktimer.Preferences.getPrefs

private val TIMER_LIST_KEY = "timer_list"
private val DEFAULT_TIMER_LIST = "30,60,90"

fun Context.getTimerList(): List<Int> = getPrefs(this)
		.getString(TIMER_LIST_KEY, DEFAULT_TIMER_LIST)
		.split(",")
		.filter { it.isNotEmpty() }
		.map { it.toInt() }

fun Context.addTimer(duration: Int) {
	val list = ArrayList<Int>(getTimerList())
	list.add(duration)
	list.sort()
	getPrefs(this).edit()
			.putString(TIMER_LIST_KEY, list.joinToString(","))
			.apply()
}

fun Context.removeTimer(duration: Int) {
	val list = ArrayList<Int>(getTimerList())
	list.remove(duration)
	getPrefs(this).edit()
			.putString(TIMER_LIST_KEY, list.joinToString(","))
			.apply()
}
