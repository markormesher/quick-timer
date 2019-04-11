package uk.co.markormesher.quicktimer

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.dialog_duration_picker.view.*
import uk.co.markormesher.quicktimer.helpers.toast

@SuppressLint("InflateParams")
fun Context.makeDialogToCreateTimer(onTimerListUpdate: () -> Unit) {
	with(AlertDialog.Builder(this)) {
		val view = LayoutInflater.from(this@makeDialogToCreateTimer).inflate(R.layout.dialog_duration_picker, null)
		with(view.hour_picker) {
			minValue = 0
			maxValue = 9
			value = 0
		}
		with(view.min_picker) {
			minValue = 0
			maxValue = 59
			value = 0
		}
		with(view.sec_picker) {
			minValue = 0
			maxValue = 59
			value = 30
			requestFocus()
		}

		setTitle(R.string.select_timer_duration)
		setView(view)
		setPositiveButton(R.string.ok) { _, _ ->
			view.hour_picker.clearFocus()
			view.min_picker.clearFocus()
			view.sec_picker.clearFocus()
			val duration = (view.hour_picker.value * 60 * 60) + (view.min_picker.value * 60) + view.sec_picker.value
			if (TimerListStorage.getTimerList(this@makeDialogToCreateTimer).contains(duration)) {
				toast(R.string.duplicate_timer)
			} else {
				TimerListStorage.addTimer(this@makeDialogToCreateTimer, duration)
				onTimerListUpdate()
			}
		}
		setNegativeButton(R.string.cancel, null)
		setCancelable(true)

		with(create()) {
			setCanceledOnTouchOutside(true)
			show()
		}
	}
}

fun Context.makeDialogToDeleteTimer(duration: Int, onTimerListUpdate: () -> Unit) {
	with(AlertDialog.Builder(this)) {
		setMessage(R.string.confirm_timer_delete)
		setPositiveButton(R.string.ok) { _, _ ->
			TimerListStorage.removeTimer(this@makeDialogToDeleteTimer, duration)
			onTimerListUpdate()
		}
		setNegativeButton(R.string.cancel, null)
		setCancelable(true)

		with(create()) {
			setCanceledOnTouchOutside(true)
			show()
		}
	}
}
