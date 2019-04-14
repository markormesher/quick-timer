package uk.co.markormesher.quicktimer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.dialog_duration_picker.view.*
import uk.co.markormesher.quicktimer.helpers.toast

@SuppressLint("InflateParams")
fun Context.createTimerCreationDialog(onTimerListUpdate: () -> Unit) {
	with(AlertDialog.Builder(this)) {
		val view = LayoutInflater.from(this@createTimerCreationDialog).inflate(R.layout.dialog_duration_picker, null)
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

		setTitle(R.string.duration_picker_title)
		setView(view)
		setPositiveButton(R.string.ok) { _, _ ->
			view.hour_picker.clearFocus()
			view.min_picker.clearFocus()
			view.sec_picker.clearFocus()
			val duration = (view.hour_picker.value * 60 * 60) + (view.min_picker.value * 60) + view.sec_picker.value
			if (TimerListStorage.getTimerList(this@createTimerCreationDialog).contains(duration)) {
				toast(R.string.duplicate_timer_error)
			} else {
				TimerListStorage.addTimer(this@createTimerCreationDialog, duration)
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

@SuppressLint("InflateParams")
fun Context.createTimerOptionsDialog(duration: Int, onTimerListUpdate: () -> Unit) {
	with(AlertDialog.Builder(this)) {
		setTitle(R.string.timer_options_title)
		setItems(arrayOf(
				getString(R.string.repeat_timer_option),
				getString(R.string.delete_timer_option)
		)) { _, which ->
			when (which) {
				0 -> {
					ActiveTimer.init(duration * 1000L, repeating = true)
					startService(Intent(this@createTimerOptionsDialog, TimerService::class.java))
				}
				1 -> createTimerDeleteDialog(duration, onTimerListUpdate)
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

fun Context.createTimerDeleteDialog(duration: Int, onTimerListUpdate: () -> Unit) {
	with(AlertDialog.Builder(this)) {
		setMessage(R.string.delete_timer_confirm)
		setPositiveButton(R.string.ok) { _, _ ->
			TimerListStorage.removeTimer(this@createTimerDeleteDialog, duration)
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
