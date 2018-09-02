package uk.co.markormesher.quicktimer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.markormesher.quicktimer.helpers.AbstractAnimationListener
import uk.co.markormesher.quicktimer.helpers.Preferences
import uk.co.markormesher.quicktimer.helpers.formatDuration
import uk.co.markormesher.quicktimer.helpers.getPrimaryColor


class MainActivity: AppCompatActivity(), TimerRecyclerAdapter.TimerRecyclerClickListener {

	private val timerRecyclerLayoutManager by lazy {
		LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
	}
	private val timerRecyclerAdapter by lazy {
		TimerRecyclerAdapter(this, this)
	}
	private val timerRecyclerDecoration by lazy {
		DividerItemDecoration(timers_recycler.context, timerRecyclerLayoutManager.orientation)
	}

	private val localBroadcastManager by lazy {
		LocalBroadcastManager.getInstance(applicationContext)
	}

	private var currentTimerActive = false
	private var currentTimerDurationRemaining = 0
	private var currentTimerPercentRemaining = 0f

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.menu, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		when (item?.itemId) {
			R.id.goto_settings -> startActivity(Intent(this, SettingsActivity::class.java))
		}
		return super.onOptionsItemSelected(item)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		initViews()
	}

	override fun onResume() {
		super.onResume()

		if (Preferences.shouldKeepScreenOn(this)) {
			window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
		} else {
			window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
		}

		localBroadcastManager.registerReceiver(timerUpdatedReceiver, IntentFilter(TimerService.INTENT_TIMER_UPDATED))
		localBroadcastManager.registerReceiver(timerCancelledReceiver, IntentFilter(TimerService.INTENT_TIMER_CANCELLED))
	}

	override fun onPause() {
		super.onPause()

		localBroadcastManager.unregisterReceiver(timerUpdatedReceiver)
		localBroadcastManager.unregisterReceiver(timerCancelledReceiver)
	}

	private fun initViews() {
		timers_recycler.layoutManager = timerRecyclerLayoutManager
		timers_recycler.adapter = timerRecyclerAdapter
		timers_recycler.addItemDecoration(timerRecyclerDecoration)

		fab.setButtonIconResource(R.drawable.ic_add)
		fab.setButtonBackgroundColour(getPrimaryColor())
		fab.setOnClickListener { makeDialogToCreateTimer { updateTimerList() } }

		updateTimerList()
	}

	private fun updateViews() {
		timer_list_wrapper.visibility = if (currentTimerActive) View.GONE else View.VISIBLE
		timer_display_wrapper.visibility = if (currentTimerActive) View.VISIBLE else View.GONE

		if (currentTimerActive) {
			timer_text.text = formatDuration(currentTimerDurationRemaining)
			background_progress.scaleY = currentTimerPercentRemaining
			background_progress.alpha = currentTimerPercentRemaining
		} else {
			val timerListEmpty = timerRecyclerAdapter.timers.isEmpty()
			timers_recycler.visibility = if (timerListEmpty) View.GONE else View.VISIBLE
			no_timers_message.visibility = if (timerListEmpty) View.VISIBLE else View.GONE
		}
	}

	private fun updateTimerList() {
		timerRecyclerAdapter.timers.clear()
		timerRecyclerAdapter.timers.addAll(TimerListStorage.getTimerList(this))
		timerRecyclerAdapter.notifyDataSetChanged()

		updateViews()
	}

	private val timerUpdatedReceiver = object: BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			val durationRemaining = intent?.extras?.getInt(TimerService.DURATION_REMAINING_KEY)
			val percentRemaining = intent?.extras?.getFloat(TimerService.PERCENT_REMAINING_KEY)

			if (durationRemaining != null && percentRemaining != null) {
				if (percentRemaining == 0f) {
					startTimerEndAnimation(onComplete = {
						currentTimerActive = false
						updateViews()
					})
				} else {
					currentTimerActive = true
					currentTimerDurationRemaining = durationRemaining
					currentTimerPercentRemaining = percentRemaining
					updateViews()
				}
			}
		}
	}

	private val timerCancelledReceiver = object: BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			currentTimerActive = false
			updateViews()
		}
	}

	private fun startTimerEndAnimation(onComplete: () -> Unit) {
		timer_text.text = getString(R.string.timer_done)

		background_progress.visibility = View.GONE
		background_done.visibility = View.VISIBLE

		val animation = AnimationUtils.loadAnimation(this, R.anim.timer_background_flash)
		animation.setAnimationListener(object: AbstractAnimationListener() {
			override fun onAnimationEnd(animation: Animation?) {
				background_progress.visibility = View.VISIBLE
				background_done.visibility = View.GONE
				onComplete()
			}
		})
		background_done.startAnimation(animation)
	}

	override fun onTimerClick(duration: Int) {
		val intent = Intent(this, TimerService::class.java)
		intent.putExtra(TimerService.DURATION_KEY, duration)
		startService(intent)
	}

	override fun onTimerLongClick(duration: Int) {
		makeDialogToDeleteTimer(duration) { updateTimerList() }
	}
}
