package uk.co.markormesher.resttimer

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.NumberPicker
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity: AppCompatActivity(), TimerRecyclerAdapter.TimerRecyclerClickListener {

	private val timerRecyclerLayoutManager by lazy { LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false) }
	private val timerRecyclerAdapter by lazy { TimerRecyclerAdapter(this, this) }
	private val timerRecyclerDecoration by lazy { DividerItemDecoration(timers_recycler.context, timerRecyclerLayoutManager.orientation) }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		initViews()
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

	private fun updateTimerList() {
		with(timerRecyclerAdapter.timers) {
			clear()
			addAll(getTimerList())
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
			val picker = NumberPicker(this@MainActivity)
			picker.minValue = 1
			picker.maxValue = 600
			picker.value = 30

			setTitle(R.string.select_timer_duration)
			setView(picker)
			setPositiveButton(R.string.ok, { _, _ ->
				picker.clearFocus()
				val duration = picker.value
				if (getTimerList().contains(duration)) {
					toast(R.string.duplicate_timer)
				} else {
					addTimer(duration)
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
		toast(duration.toString())
	}

	override fun onTimerLongClick(duration: Int): Boolean {
		with(AlertDialog.Builder(this)) {
			setTitle(R.string.confirm_timer_delete)
			setPositiveButton(R.string.confirm_timer_delete, { _, _ ->
				removeTimer(duration)
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
