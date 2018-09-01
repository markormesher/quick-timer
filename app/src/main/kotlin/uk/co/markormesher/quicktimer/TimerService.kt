package uk.co.markormesher.quicktimer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import uk.co.markormesher.quicktimer.helpers.Preferences
import uk.co.markormesher.quicktimer.helpers.doAlarmVibration
import uk.co.markormesher.quicktimer.helpers.formatDuration
import uk.co.markormesher.quicktimer.helpers.playAlarmSound


class TimerService: Service() {

	companion object {
		private const val NOTIFICATION_ID = 1415
		private const val NOTIFICATION_CHANNEL_ID = "timer_notifications"

		// TODO: clean up intent naming
		const val CANCEL_CLICKED_INTENT_ACTION = "uk.co.markormesher.quicktimer.timercancelclicked"
		const val CANCEL_INTENT_ACTION = "uk.co.markormesher.quicktimer.timercancel"
		const val UPDATE_INTENT_ACTION = "uk.co.markormesher.quicktimer.timerupdate"
		private const val UPDATE_PERIOD = 80L

		const val DURATION_KEY = "duration"
		const val DURATION_REMAINING_KEY = "duration_remaining"
		const val PERCENT_REMAINING_KEY = "percent_remaining"
	}

	private var timerDurationInMs = 0L
	private var timerStartTime = 0L

	private var lastSecsRemainingNotified = -1
	private var lastIconNotified = -1

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

		applicationContext.registerReceiver(cancelRequestedReceiver, IntentFilter(CANCEL_CLICKED_INTENT_ACTION))

		return super.onStartCommand(intent, flags, startId)
	}

	override fun onDestroy() {
		applicationContext.unregisterReceiver(cancelRequestedReceiver)

		super.onDestroy()
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

		val openAppIntent = PendingIntent.getActivity(applicationContext, 0, Intent(applicationContext, MainActivity::class.java), 0)
		val cancelIntent = PendingIntent.getBroadcast(applicationContext, 0, Intent(CANCEL_CLICKED_INTENT_ACTION), 0)
		notificationBuilder = builder
				.setSmallIcon(R.drawable.ic_circle_slice_8)
				.setContentTitle(getString(R.string.app_name))
				.setContentIntent(openAppIntent)
				.addAction(NotificationCompat.Action.Builder(R.drawable.ic_close, "CANCEL", cancelIntent).build())
	}

	private fun updateNotification() {
		initNotification()

		val msElapsed = System.currentTimeMillis() - timerStartTime
		val msRemaining = timerDurationInMs - msElapsed
		val secsRemaining = (msRemaining / 1000).toInt()
		val percentRemaining = msRemaining.toFloat() / timerDurationInMs

		if (msRemaining > 0) {

			val icon = when {
				percentRemaining >= (7f/8) -> R.drawable.ic_circle_slice_8
				percentRemaining >= (6f/8) -> R.drawable.ic_circle_slice_7
				percentRemaining >= (5f/8) -> R.drawable.ic_circle_slice_6
				percentRemaining >= (4f/8) -> R.drawable.ic_circle_slice_5
				percentRemaining >= (3f/8) -> R.drawable.ic_circle_slice_4
				percentRemaining >= (2f/8) -> R.drawable.ic_circle_slice_3
				percentRemaining >= (1f/8) -> R.drawable.ic_circle_slice_2
				else -> R.drawable.ic_circle_slice_1
			}

			// only update the notification if something changed
			if (secsRemaining != lastSecsRemainingNotified || icon != lastIconNotified) {
				notificationBuilder
						.setContentText(formatDuration(secsRemaining))
						.setSmallIcon(icon)
				startForeground(NOTIFICATION_ID, notificationBuilder.build())

				lastSecsRemainingNotified = secsRemaining
				lastIconNotified = icon
			}

			broadcastUpdate(secsRemaining, percentRemaining)
			updateHandler.postDelayed(updateRunnable, UPDATE_PERIOD)
		} else {
			broadcastUpdate(0, 0f)

			if (Preferences.shouldVibrateOnTimerEnd(this)) {
				doAlarmVibration()
			}

			if (Preferences.shouldPlaySoundOnTimerEnd(this)) {
				playAlarmSound()
			}

			stopForeground(true)
			stopSelf()
		}
	}

	private fun broadcastUpdate(durationRemaining: Int, percentRemaining: Float) {
		val updateIntent = Intent(UPDATE_INTENT_ACTION)
		updateIntent.putExtra(DURATION_KEY, (timerDurationInMs / 1000).toInt())
		updateIntent.putExtra(DURATION_REMAINING_KEY, durationRemaining)
		updateIntent.putExtra(PERCENT_REMAINING_KEY, percentRemaining)
		localBroadcastManager.sendBroadcast(updateIntent)
	}

	private val cancelRequestedReceiver = object: BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			localBroadcastManager.sendBroadcast(Intent(CANCEL_INTENT_ACTION))
			updateHandler.removeCallbacks(updateRunnable)
			stopForeground(true)
			stopSelf()
		}
	}
}
