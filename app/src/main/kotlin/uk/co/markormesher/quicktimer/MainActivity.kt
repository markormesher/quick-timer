package uk.co.markormesher.quicktimer

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_duration_picker.view.*
import uk.co.markormesher.quicktimer.helpers.Preferences
import uk.co.markormesher.quicktimer.helpers.getPrimaryColor
import uk.co.markormesher.quicktimer.helpers.toast


class MainActivity: AppCompatActivity(), TimerRecyclerAdapter.TimerRecyclerClickListener {

	private val timerRecyclerLayoutManager by lazy { LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false) }
	private val timerRecyclerAdapter by lazy { uk.co.markormesher.quicktimer.TimerRecyclerAdapter(this, this) }
	private val timerRecyclerDecoration by lazy { DividerItemDecoration(timers_recycler.context, timerRecyclerLayoutManager.orientation) }

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
	}

	private fun initViews() {
		timers_recycler.layoutManager = timerRecyclerLayoutManager
		timers_recycler.adapter = timerRecyclerAdapter
		timers_recycler.addItemDecoration(timerRecyclerDecoration)

		fab.setIcon(R.drawable.ic_add)
		fab.setBackgroundColour(getPrimaryColor())
		fab.setOnClickListener { addTimer() }

		updateTimerList()
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

	private fun updateTimerList() {
		with(timerRecyclerAdapter.timers) {
			clear()
			addAll(TimerListStorage.getTimerList(this@MainActivity))
		}
		timerRecyclerAdapter.notifyDataSetChanged()

		if (timerRecyclerAdapter.timers.isNotEmpty()) {
			timers_recycler.visibility = View.VISIBLE
			no_timers_message.visibility = View.GONE
		} else {
			timers_recycler.visibility = View.GONE
			no_timers_message.visibility = View.VISIBLE
		}
	}

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
			setPositiveButton(R.string.ok, { _, _ ->
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
			})
			setNegativeButton(R.string.cancel, null)
			setCancelable(true)

			with(create()) {
				setCanceledOnTouchOutside(true)
				show()
			}
		}
	}

	override fun onTimerClick(duration: Int) {
		val gotoTimerIntent = Intent(this, TimerActivity::class.java)
		gotoTimerIntent.putExtra(TimerActivity.Companion.DURATION_KEY, duration)
		startActivity(gotoTimerIntent)
	}

	override fun onTimerLongClick(duration: Int): Boolean {
		with(AlertDialog.Builder(this)) {
			setMessage(R.string.confirm_timer_delete)
			setPositiveButton(R.string.ok, { _, _ ->
				TimerListStorage.removeTimer(this@MainActivity, duration)
				updateTimerList()
			})
			setNegativeButton(R.string.cancel, null)
			setCancelable(true)

			with(create()) {
				setCanceledOnTouchOutside(true)
				show()
			}
		}
		return true
	}
}
