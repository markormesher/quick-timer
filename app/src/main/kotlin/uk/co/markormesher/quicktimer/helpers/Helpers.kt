package uk.co.markormesher.quicktimer.helpers

import android.content.Context
import android.util.TypedValue
import android.widget.Toast
import uk.co.markormesher.quicktimer.R

fun Context.toast(message: Int) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
fun Context.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.getPrimaryColor(): Int {
	val a = obtainStyledAttributes(TypedValue().data, intArrayOf(R.attr.colorPrimary))
	val color = a.getColor(0, 0)
	a.recycle()
	return color
}
