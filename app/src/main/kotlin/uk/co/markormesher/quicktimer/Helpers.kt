package uk.co.markormesher.quicktimer

import android.content.Context
import android.util.TypedValue
import android.view.animation.Animation
import android.widget.Toast

fun Context.toast(message: Int) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
fun Context.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.getPrimaryColor(): Int {
	val a = obtainStyledAttributes(TypedValue().data, intArrayOf(R.attr.colorPrimary))
	val color = a.getColor(0, 0)
	a.recycle()
	return color
}

open class AbstractAnimationListener: Animation.AnimationListener {
	override fun onAnimationStart(animation: Animation?) {
	}

	override fun onAnimationRepeat(animation: Animation?) {
	}

	override fun onAnimationEnd(animation: Animation?) {
	}
}

