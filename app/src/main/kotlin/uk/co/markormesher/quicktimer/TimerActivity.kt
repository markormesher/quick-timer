package uk.co.markormesher.quicktimer

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Vibrator
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_timer.*


class TimerActivity: AppCompatActivity() {

	companion object {
		val DURATION_KEY = "duration"
	}

	private var lastBackClick = 0L
	private val backClickThreshold = 1500L

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_timer)
		val duration = intent.extras?.getInt(DURATION_KEY, 0) ?: 0
		startTimer(duration)
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
				val secProgress = (millisUntilFinished / 1000.0f).rem(1)
				val totalProgress = millisUntilFinished / totalMillis.toFloat()

				timer.text = Math.ceil(millisUntilFinished / 1000.0).toInt().toString()
				timer.alpha = 0.5f + (secProgress * 0.5f)
				timer.scaleX = 0.8f + (secProgress * 0.2f)
				timer.scaleY = 0.8f + (secProgress * 0.2f)

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
		timer.alpha = 1.0f
		timer.scaleX = 1.0f
		timer.scaleY = 1.0f

		background_progress.visibility = View.GONE
		background_done.visibility = View.VISIBLE
		with(AnimationUtils.loadAnimation(this, R.anim.timer_background_flash)) {
			setAnimationListener(object: AbstractAnimationListener() {
				override fun onAnimationEnd(animation: Animation?) = finish()
			})
			background_done.startAnimation(this)
		}

		if (Preferences.shouldVibrateOnTimerEnd(this)) {
			(getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(longArrayOf(0L, 500L, 300L, 500L), -1)
		}

		if (Preferences.shouldPlaySoundOnTimerEnd(this)) {
			val alarmTone: Uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.alarm)
			MediaPlayer.create(applicationContext, alarmTone).start()
		}
	}

}
