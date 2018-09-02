package uk.co.markormesher.quicktimer

object ActiveTimer {

	var totalDurationInMs = 0L
	var startTimeInMs = 0L
	var currentState = State.INACTIVE

	fun init(durationInMs: Long) {
		totalDurationInMs = durationInMs
		startTimeInMs = System.currentTimeMillis()
		currentState = State.RUNNING
	}

	fun reset(state: State) {
		totalDurationInMs = 0
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
				" startTimeInMs=$startTimeInMs," +
				" currentState=$currentState" +
				" ]"
	}

	enum class State {
		INACTIVE, RUNNING, FINISHED
	}
}
