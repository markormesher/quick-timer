package uk.co.markormesher.quicktimer.helpers

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.TypedValue
import android.widget.Toast
import uk.co.markormesher.quicktimer.R

fun Context.toast(message: Int) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
fun Context.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.getPrimaryColor(): Int {
	val a = obtainStyledAttributes(TypedValue().data, intArrayOf(R.attr.colorPrimary))
	val color = a.getColor(0, 0)
	a.recycle()
	return color
}

fun Context.doAlarmVibration() {
	val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
		vibrator.vibrate(VibrationEffect.createOneShot(1000L, -1))
	} else {
		@Suppress("DEPRECATION")
		vibrator.vibrate(longArrayOf(0L, 1000L), -1)
	}
}

fun Context.doAlarmSound() {
	val alarmTone: Uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.alarm)
	MediaPlayer.create(applicationContext, alarmTone).start()
}
