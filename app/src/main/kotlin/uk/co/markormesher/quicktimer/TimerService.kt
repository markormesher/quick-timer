package uk.co.markormesher.quicktimer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import uk.co.markormesher.quicktimer.helpers.formatDuration


class TimerService: Service() {

	companion object {
		private const val NOTIFICATION_ID = 1415
		private const val NOTIFICATION_CHANNEL_ID = "timer_notifications"

		const val UPDATE_INTENT_ACTION = "uk.co.markormesher.quicktimer.timerupdate"
		private const val UPDATE_PERIOD = 80L

		const val DURATION_KEY = "duration"
		const val DURATION_REMAINING_KEY = "duration_remaining"
		const val PERCENT_REMAINING_KEY = "percent_remaining"
	}

	private var timerDurationInMs = 0L
	private var timerStartTime = 0L

	private val notificationManager by lazy {
		getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
	}
	private lateinit var notificationBuilder: NotificationCompat.Builder
	private var notificationInitialised = false

	private val localBroadcastManager by lazy {
		LocalBroadcastManager.getInstance(applicationContext)
	}

	private val updateHandler by lazy { Handler(mainLooper) }
	private val updateRunnable = Runnable { updateNotification() }

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		if (intent?.extras?.containsKey(DURATION_KEY) == true) {
			timerDurationInMs = (intent.extras[DURATION_KEY] as Int) * 1000L
			timerStartTime = System.currentTimeMillis()
			updateNotification()
		}

		return super.onStartCommand(intent, flags, startId)
	}

	override fun onBind(intent: Intent) = null

	private fun initNotification() {
		if (notificationInitialised) {
			return
		}
		notificationInitialised = true

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			notificationManager.createNotificationChannel(NotificationChannel(
					NOTIFICATION_CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW
			))
		}

		val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
		} else {
			@Suppress("DEPRECATION")
			NotificationCompat.Builder(applicationContext)
		}

		notificationBuilder = builder
				.setSmallIcon(R.mipmap.ic_launcher)
				.setContentTitle(getString(R.string.app_name))
	}

	private fun updateNotification() {
		initNotification()

		val msElapsed = System.currentTimeMillis() - timerStartTime
		val msRemaining = timerDurationInMs - msElapsed
		val secsRemaining = (msRemaining / 1000).toInt()
		val percentRemaining = msRemaining.toFloat() / timerDurationInMs

		if (msRemaining > 0) {
			notificationBuilder.setContentText(formatDuration(secsRemaining))
			startForeground(NOTIFICATION_ID, notificationBuilder.build())
			broadcastUpdate(secsRemaining, percentRemaining)
			updateHandler.postDelayed(updateRunnable, UPDATE_PERIOD)
		} else {
			broadcastUpdate(0, 0f)
			stopForeground(true)
		}
	}

	private fun broadcastUpdate(durationRemaining: Int, percentRemaining: Float) {
		val updateIntent = Intent(UPDATE_INTENT_ACTION)
		updateIntent.putExtra(DURATION_KEY, (timerDurationInMs / 1000).toInt())
		updateIntent.putExtra(DURATION_REMAINING_KEY, durationRemaining)
		updateIntent.putExtra(PERCENT_REMAINING_KEY, percentRemaining)
		localBroadcastManager.sendBroadcast(updateIntent)
	}
}
