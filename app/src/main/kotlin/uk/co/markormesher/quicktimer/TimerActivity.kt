package uk.co.markormesher.quicktimer

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Vibrator
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_timer.*
import uk.co.markormesher.quicktimer.helpers.AbstractAnimationListener
import uk.co.markormesher.quicktimer.helpers.Preferences
import uk.co.markormesher.quicktimer.helpers.formatDuration
import uk.co.markormesher.quicktimer.helpers.toast


class TimerActivity: AppCompatActivity() {

	companion object {
		const val DURATION_KEY = "duration"
	}

	private var lastBackClick = 0L
	private val backClickThreshold = 1500L

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_timer)
		val duration = intent.extras?.getInt(DURATION_KEY, 0) ?: 0
		startTimer(duration)
	}

	override fun onResume() {
		super.onResume()
		if (Preferences.shouldKeepScreenOn(this)) {
			window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
		} else {
			window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
		}
	}

	override fun onBackPressed() {
		val now = System.currentTimeMillis()
		if (now - lastBackClick <= backClickThreshold) {
			super.onBackPressed()
		} else {
			toast(R.string.exit_timer_confirm)
			lastBackClick = now
		}
	}

	private fun startTimer(duration: Int) {
		val totalMillis = duration * 1000L
		object: CountDownTimer(totalMillis, 25) {
			override fun onTick(millisUntilFinished: Long) {
				val totalProgress = millisUntilFinished / totalMillis.toFloat()

				timer.text = this@TimerActivity.formatDuration(Math.ceil(millisUntilFinished / 1000.0).toInt())
				background_progress.scaleY = totalProgress
				background_progress.alpha = 0.2f + (totalProgress * 0.8f)
			}

			override fun onFinish() {
				finishTimer()
			}
		}.start()
	}

	private fun finishTimer() {
		timer.text = "0"

		background_progress.visibility = View.GONE
		background_done.visibility = View.VISIBLE
		with(AnimationUtils.loadAnimation(this, R.anim.timer_background_flash)) {
			setAnimationListener(object: AbstractAnimationListener() {
				override fun onAnimationEnd(animation: Animation?) = finish()
			})
			background_done.startAnimation(this)
		}

		if (Preferences.shouldVibrateOnTimerEnd(this)) {
			// TODO: use new vibration API
			(getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(longArrayOf(0L, 500L, 300L, 500L), -1)
		}

		if (Preferences.shouldPlaySoundOnTimerEnd(this)) {
			val alarmTone: Uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.alarm)
			MediaPlayer.create(applicationContext, alarmTone).start()
		}
	}

}
