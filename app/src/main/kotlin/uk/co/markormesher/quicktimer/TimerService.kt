package uk.co.markormesher.quicktimer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.support.v4.app.NotificationCompat
import uk.co.markormesher.quicktimer.helpers.formatDuration


class TimerService: Service() {

	companion object {
		private const val NOTIFICATION_ID = 1415
		private const val NOTIFICATION_CHANNEL_ID = "timer_notifications"
		const val DURATION_KEY = "duration"
	}

	private var timerDurationInMs = 0L
	private var timerStartTime = 0L

	private val notificationManager by lazy {
		getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
	}
	private lateinit var notificationBuilder: NotificationCompat.Builder
	private var notificationInitialised = false

	private val updateHandler by lazy { Handler(mainLooper) }
	private val updateRunnable = Runnable { updateNotification() }

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		if (intent != null
				&& intent.extras != null
				&& intent.extras.containsKey(DURATION_KEY)) {
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

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val notificationChannel = NotificationChannel(
					NOTIFICATION_CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW)
			notificationManager.createNotificationChannel(notificationChannel)
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
		val percentRemaining = msRemaining.toDouble() / timerDurationInMs

		if (msRemaining > 0) {
			notificationBuilder.setContentText(formatDuration(secsRemaining))
			startForeground(NOTIFICATION_ID, notificationBuilder.build())
			broadcastUpdate(secsRemaining, percentRemaining)
			updateHandler.postDelayed(updateRunnable, 250L)
		} else {
			broadcastUpdate(0, 0.0)
			stopForeground(true)
		}
	}

	private fun broadcastUpdate(durationRemaining: Int, percentRemaining: Double) {
		// TODO
	}
}
