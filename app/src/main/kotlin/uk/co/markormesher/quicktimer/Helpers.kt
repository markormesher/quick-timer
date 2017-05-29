package uk.co.markormesher.quicktimer

import android.content.Context
import android.util.TypedValue
import android.widget.Toast


fun Context.toast(message: Int) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
fun Context.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.getPrimaryColor(): Int {
	val a = obtainStyledAttributes(TypedValue().data, intArrayOf(R.attr.colorPrimary))
	val color = a.getColor(0, 0)
	a.recycle()
	return color
}
