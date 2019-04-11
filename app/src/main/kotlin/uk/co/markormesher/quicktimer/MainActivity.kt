package uk.co.markormesher.quicktimer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.markormesher.quicktimer.helpers.AbstractAnimationListener
import uk.co.markormesher.quicktimer.helpers.Preferences
import uk.co.markormesher.quicktimer.helpers.formatDuration
import uk.co.markormesher.quicktimer.helpers.getPrimaryColor


class MainActivity: AppCompatActivity(), TimerRecyclerAdapter.TimerRecyclerClickListener {

	private val timerRecyclerLayoutManager by lazy {
		LinearLayoutManager(this, RecyclerView.VERTICAL, false)
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

		updateViews(skipEvents = true)
	}

	override fun onPause() {
		super.onPause()
		localBroadcastManager.unregisterReceiver(timerUpdatedReceiver)
	}

	private val timerUpdatedReceiver = object: BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) = updateViews()
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

	private fun updateViews(skipEvents: Boolean = false) {
		if (ActiveTimer.currentState == ActiveTimer.State.FINISHED && skipEvents) {
			ActiveTimer.currentState = ActiveTimer.State.INACTIVE
		}

		when (ActiveTimer.currentState) {
			ActiveTimer.State.INACTIVE -> {
				showSingleViewWrapper(timer_list_wrapper)

				val timerListEmpty = timerRecyclerAdapter.timers.isEmpty()
				timers_recycler.visibility = if (timerListEmpty) View.GONE else View.VISIBLE
				no_timers_message.visibility = if (timerListEmpty) View.VISIBLE else View.GONE
			}

			ActiveTimer.State.RUNNING -> {
				showSingleViewWrapper(timer_display_wrapper)

				timer_text.text = formatDuration(ActiveTimer.secsRemaining)
				background_progress.scaleY = ActiveTimer.percentRemaining
				background_progress.alpha = ActiveTimer.percentRemaining
			}

			ActiveTimer.State.FINISHED -> {
				showSingleViewWrapper(timer_finished_wrapper)
				startTimerEndAnimation(onComplete = {
					ActiveTimer.currentState = ActiveTimer.State.INACTIVE
					updateViews()
				})
			}
		}
	}

	private fun showSingleViewWrapper(wrapper: ViewGroup) {
		listOf(timer_list_wrapper, timer_display_wrapper, timer_finished_wrapper).forEach {
			it.visibility = if (it == wrapper) {
				View.VISIBLE
			} else {
				View.GONE
			}
		}
	}

	private fun updateTimerList() {
		timerRecyclerAdapter.timers.clear()
		timerRecyclerAdapter.timers.addAll(TimerListStorage.getTimerList(this))
		timerRecyclerAdapter.notifyDataSetChanged()

		updateViews()
	}

	private fun startTimerEndAnimation(onComplete: () -> Unit) {
		timer_text.text = getString(R.string.timer_done)

		val animation = AnimationUtils.loadAnimation(this, R.anim.timer_background_flash)
		animation.setAnimationListener(object: AbstractAnimationListener() {
			override fun onAnimationEnd(animation: Animation?) = onComplete()
		})
		background_done.startAnimation(animation)
	}

	override fun onTimerClick(duration: Int) {
		ActiveTimer.init(duration * 1000L)
		startService(Intent(this, TimerService::class.java))
	}

	override fun onTimerLongClick(duration: Int) {
		makeDialogToDeleteTimer(duration) { updateTimerList() }
	}
}
