package uk.co.markormesher.resttimer

import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_timer.*

class TimerActivity: AppCompatActivity() {

	companion object {
		val DURATION_KEY = "duration"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_timer)
		val duration = intent.extras?.getInt(DURATION_KEY, 0) ?: 0
		startTimer(duration)
	}

	private fun startTimer(duration: Int) {
		val totalMillis = duration * 1000L
		object: CountDownTimer(totalMillis, 25) {
			override fun onTick(millisUntilFinished: Long) {
				val remaining = Math.ceil(millisUntilFinished / 1000.0).toInt()
				timer.text = remaining.toString()
				background_progress.scaleY = millisUntilFinished / totalMillis.toFloat()
			}

			override fun onFinish() {
				finishTimer()
			}
		}.start()
	}

	private fun finishTimer() {
		timer.text = "0"
		background_progress.visibility = View.GONE
		object: CountDownTimer(2500, 250) {
			override fun onTick(millisUntilFinished: Long) {
				if (background_done.visibility == View.VISIBLE) {
					background_done.visibility = View.GONE
				} else {
					background_done.visibility = View.VISIBLE
				}
			}

			override fun onFinish() {
				finish()
			}
		}.start()
	}

}
