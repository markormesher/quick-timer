package uk.co.markormesher.quicktimer.helpers

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import uk.co.markormesher.quicktimer.R

fun Context.formatDuration(duration: Int): SpannableString {
	val h = duration / (60 * 60)
	val m = (duration / 60).rem(60)
	val s = duration.rem(60)
	val str: String
	if (h > 0) {
		str = getString(R.string.timer_template_hms, h, m, s)
	} else if (m > 0) {
		str =  getString(R.string.timer_template_ms, m, s)
	} else {
		str = getString(R.string.timer_template_s, s)
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
