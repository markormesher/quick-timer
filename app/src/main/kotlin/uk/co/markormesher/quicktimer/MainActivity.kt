package uk.co.markormesher.quicktimer

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_duration_picker.view.*
import uk.co.markormesher.quicktimer.helpers.*


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

		localBroadcastManager.registerReceiver(timerUpdateReceiver, IntentFilter(TimerService.UPDATE_INTENT_ACTION))
	}

	override fun onPause() {
		super.onPause()

		localBroadcastManager.unregisterReceiver(timerUpdateReceiver)
	}

	private fun initViews() {
		timers_recycler.layoutManager = timerRecyclerLayoutManager
		timers_recycler.adapter = timerRecyclerAdapter
		timers_recycler.addItemDecoration(timerRecyclerDecoration)

		fab.setButtonIconResource(R.drawable.ic_add)
		fab.setButtonBackgroundColour(getPrimaryColor())
		fab.setOnClickListener { addTimer() }

		updateTimerList()
	}

	private fun updateView() {
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

	private fun finishTimer() {
		timer_text.text = getString(R.string.timer_done)

		background_progress.visibility = View.GONE
		background_done.visibility = View.VISIBLE

		val animation = AnimationUtils.loadAnimation(this, R.anim.timer_background_flash)
		animation.setAnimationListener(object: AbstractAnimationListener() {
			override fun onAnimationEnd(animation: Animation?) {
				currentTimerActive = false
				updateView()

				background_progress.visibility = View.VISIBLE
				background_done.visibility = View.GONE
			}
		})
		background_done.startAnimation(animation)

		if (Preferences.shouldVibrateOnTimerEnd(this)) {
			doAlarmVibration()
		}

		if (Preferences.shouldPlaySoundOnTimerEnd(this)) {
			doAlarmSound()
		}
	}

	private fun updateTimerList() {
		timerRecyclerAdapter.timers.clear()
		timerRecyclerAdapter.timers.addAll(TimerListStorage.getTimerList(this))
		timerRecyclerAdapter.notifyDataSetChanged()

		updateView()
	}

	@SuppressLint("InflateParams")
	private fun addTimer() {
		with(AlertDialog.Builder(this)) {
			val view = LayoutInflater.from(this@MainActivity).inflate(R.layout.dialog_duration_picker, null)
			view.h_picker.minValue = 0
			view.h_picker.maxValue = 9
			view.h_picker.value = 0
			view.m_picker.minValue = 0
			view.m_picker.maxValue = 59
			view.m_picker.value = 0
			view.s_picker.minValue = 0
			view.s_picker.maxValue = 59
			view.s_picker.value = 30
			view.s_picker.requestFocus()

			setTitle(R.string.select_timer_duration)
			setView(view)
			setPositiveButton(R.string.ok) { _, _ ->
				view.h_picker.clearFocus()
				view.m_picker.clearFocus()
				view.s_picker.clearFocus()
				val duration = (view.h_picker.value * 60 * 60) + (view.m_picker.value * 60) + view.s_picker.value
				if (TimerListStorage.getTimerList(this@MainActivity).contains(duration)) {
					toast(R.string.duplicate_timer)
				} else {
					TimerListStorage.addTimer(this@MainActivity, duration)
					updateTimerList()
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

	override fun onTimerClick(duration: Int) {
		val intent = Intent(this, TimerService::class.java)
		intent.putExtra(TimerService.DURATION_KEY, duration)
		startService(intent)
	}

	override fun onTimerLongClick(duration: Int): Boolean {
		with(AlertDialog.Builder(this)) {
			setMessage(R.string.confirm_timer_delete)
			setPositiveButton(R.string.ok) { _, _ ->
				TimerListStorage.removeTimer(this@MainActivity, duration)
				updateTimerList()
			}
			setNegativeButton(R.string.cancel, null)
			setCancelable(true)

			with(create()) {
				setCanceledOnTouchOutside(true)
				show()
			}
		}
		return true
	}

	private val timerUpdateReceiver = object: BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			val durationRemaining = intent?.extras?.getInt(TimerService.DURATION_REMAINING_KEY)
			val percentRemaining = intent?.extras?.getFloat(TimerService.PERCENT_REMAINING_KEY)

			if (durationRemaining != null && percentRemaining != null) {
				if (percentRemaining == 0f) {
					finishTimer()
				} else {
					currentTimerDurationRemaining = durationRemaining
					currentTimerPercentRemaining = percentRemaining
					currentTimerActive = true
					updateView()
				}
			}
		}
	}
}
