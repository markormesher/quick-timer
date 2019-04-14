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
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import uk.co.markormesher.quicktimer.helpers.Preferences
import uk.co.markormesher.quicktimer.helpers.doAlarmVibration
import uk.co.markormesher.quicktimer.helpers.formatDuration
import uk.co.markormesher.quicktimer.helpers.playAlarmSound

class TimerService : Service() {

	companion object {
		private const val NOTIFICATION_ID = 1415
		private const val NOTIFICATION_CHANNEL_ID = "timer_notifications"
		private const val UPDATE_PERIOD = 80L

		const val INTENT_TIMER_CANCEL_REQUESTED = "uk.co.markormesher.quicktimer.timer_cancel_requested"
		const val INTENT_TIMER_UPDATED = "uk.co.markormesher.quicktimer.timer_updated"
	}

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
	private val updateRunnable = Runnable { updateTimerState() }

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		updateTimerState()
		applicationContext.registerReceiver(timerCancelRequestedReceiver, IntentFilter(INTENT_TIMER_CANCEL_REQUESTED))
		return super.onStartCommand(intent, flags, startId)
	}

	override fun onDestroy() {
		applicationContext.unregisterReceiver(timerCancelRequestedReceiver)
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

		notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
		} else {
			@Suppress("DEPRECATION")
			NotificationCompat.Builder(applicationContext)
		}

		val openAppIntent = PendingIntent.getActivity(applicationContext, 0, Intent(applicationContext, MainActivity::class.java), 0)
		val cancelIntent = PendingIntent.getBroadcast(applicationContext, 0, Intent(INTENT_TIMER_CANCEL_REQUESTED), 0)
		with(notificationBuilder) {
			setSmallIcon(R.drawable.ic_circle_slice_8)
			setContentTitle(getString(R.string.app_name))
			setContentIntent(openAppIntent)
			addAction(NotificationCompat.Action.Builder(R.drawable.ic_close, getString(R.string.cancel), cancelIntent).build())
		}
	}

	private fun updateTimerState() {
		if (ActiveTimer.msRemaining > 0) {
			broadcastUpdate()
			updateNotification()
			updateHandler.postDelayed(updateRunnable, UPDATE_PERIOD)
		} else {
			if (Preferences.shouldVibrateOnTimerEnd(this)) {
				doAlarmVibration()
			}

			if (Preferences.shouldPlaySoundOnTimerEnd(this)) {
				playAlarmSound()
			}

			if (ActiveTimer.repeatingTimer) {
				ActiveTimer.repeat()
				broadcastUpdate()
				updateNotification()
				updateHandler.postDelayed(updateRunnable, UPDATE_PERIOD)
			} else {
				ActiveTimer.reset(ActiveTimer.State.FINISHED)
				broadcastUpdate()
				stopForeground(true)
				stopSelf()
			}
		}
	}

	private fun updateNotification() {
		initNotification()

		val icon = when {
			ActiveTimer.percentRemaining >= (7f / 8) -> R.drawable.ic_circle_slice_8
			ActiveTimer.percentRemaining >= (6f / 8) -> R.drawable.ic_circle_slice_7
			ActiveTimer.percentRemaining >= (5f / 8) -> R.drawable.ic_circle_slice_6
			ActiveTimer.percentRemaining >= (4f / 8) -> R.drawable.ic_circle_slice_5
			ActiveTimer.percentRemaining >= (3f / 8) -> R.drawable.ic_circle_slice_4
			ActiveTimer.percentRemaining >= (2f / 8) -> R.drawable.ic_circle_slice_3
			ActiveTimer.percentRemaining >= (1f / 8) -> R.drawable.ic_circle_slice_2
			else -> R.drawable.ic_circle_slice_1
		}

		// only update the notification if something changed
		if (ActiveTimer.secsRemaining != lastSecsRemainingNotified || icon != lastIconNotified) {
			with(notificationBuilder) {
				setContentText(formatDuration(ActiveTimer.secsRemaining))
				setSmallIcon(icon)
			}
			startForeground(NOTIFICATION_ID, notificationBuilder.build())

			lastSecsRemainingNotified = ActiveTimer.secsRemaining
			lastIconNotified = icon
		}
	}

	private fun broadcastUpdate() {
		localBroadcastManager.sendBroadcast(Intent(INTENT_TIMER_UPDATED))
	}

	private val timerCancelRequestedReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			ActiveTimer.reset(ActiveTimer.State.INACTIVE)
			broadcastUpdate()

			updateHandler.removeCallbacks(updateRunnable)
			stopForeground(true)
			stopSelf()
		}
	}
}
