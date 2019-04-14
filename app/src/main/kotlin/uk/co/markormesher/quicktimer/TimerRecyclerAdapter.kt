package uk.co.markormesher.quicktimer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item_timer.view.*
import uk.co.markormesher.quicktimer.helpers.formatDuration

class TimerRecyclerAdapter(private val context: Context, private val listener: TimerRecyclerClickListener? = null) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	private val layoutInflater by lazy { LayoutInflater.from(context)!! }
	val timers = ArrayList<Int>()

	override fun getItemCount(): Int = timers.size

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		return TimerViewHolder(layoutInflater.inflate(R.layout.list_item_timer, parent, false))
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		with(holder as TimerViewHolder) {
			val timer = timers[position]
			duration.text = context.formatDuration(timer)
			rootView.setOnClickListener { listener?.onTimerClick(timer) }
			rootView.setOnLongClickListener {
				listener?.onTimerLongClick(timer)
				return@setOnLongClickListener true
			}
		}
	}

	class TimerViewHolder(v: View) : RecyclerView.ViewHolder(v) {
		val rootView = v
		val duration = v.duration!!
	}

	interface TimerRecyclerClickListener {
		fun onTimerClick(duration: Int)
		fun onTimerLongClick(duration: Int)
	}

}
