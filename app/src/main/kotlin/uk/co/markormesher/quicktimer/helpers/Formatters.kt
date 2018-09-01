package uk.co.markormesher.quicktimer.helpers

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import uk.co.markormesher.quicktimer.R

fun Context.formatDuration(duration: Int): SpannableString {
	val hours = duration / (60 * 60)
	val mins = (duration / 60).rem(60)
	val secs = duration.rem(60)
	val str: String
	str = when {
		hours > 0 -> getString(R.string.timer_template_hms, hours, mins, secs)
		mins > 0 -> getString(R.string.timer_template_ms, mins, secs)
		else -> getString(R.string.timer_template_s, secs)
	}

	val output = SpannableString(str)
	val hIndex = str.indexOf("h")
	val mIndex = str.indexOf("m")
	val sIndex = str.indexOf("s")
	if (hIndex >= 0) output.setSpan(RelativeSizeSpan(0.7f), hIndex, hIndex + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
	if (mIndex >= 0) output.setSpan(RelativeSizeSpan(0.7f), mIndex, mIndex + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
	output.setSpan(RelativeSizeSpan(0.7f), sIndex, sIndex + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

	return output
}
