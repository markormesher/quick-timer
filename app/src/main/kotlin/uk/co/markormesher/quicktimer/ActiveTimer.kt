package uk.co.markormesher.quicktimer

object ActiveTimer {

	var totalDurationInMs = 0L
	var repeatingTimer = false
	var startTimeInMs = 0L
	var currentState = State.INACTIVE

	fun init(durationInMs: Long, repeating: Boolean = false) {
		totalDurationInMs = durationInMs
		repeatingTimer = repeating
		startTimeInMs = System.currentTimeMillis()
		currentState = State.RUNNING
	}

	fun repeat() {
		if (currentState !== ActiveTimer.State.RUNNING) {
			throw IllegalStateException("Cannot repeat a non-running timer")
		}

		startTimeInMs = System.currentTimeMillis()
	}

	fun reset(state: State) {
		totalDurationInMs = 0
		repeatingTimer = false
		startTimeInMs = 0L
		currentState = state
	}

	val msRemaining: Long
		get() = totalDurationInMs + startTimeInMs - System.currentTimeMillis()

	val secsRemaining: Int
		get() = (msRemaining / 1000).toInt()

	val percentRemaining: Float
		get() = msRemaining.toFloat() / totalDurationInMs.toFloat()

	override fun toString(): String {
		return "ActiveTimer[" +
				" totalDurationInMs=$totalDurationInMs," +
				" repeatingTimer=$repeatingTimer," +
				" startTimeInMs=$startTimeInMs," +
				" currentState=$currentState" +
				" ]"
	}

	enum class State {
		INACTIVE, RUNNING, FINISHED
	}
}
