package uk.co.markormesher.quicktimer

import android.os.Bundle
import android.preference.PreferenceFragment
import android.support.v7.app.AppCompatActivity


class SettingsActivity: AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_settings)

		fragmentManager.beginTransaction()
				.add(R.id.root_layout, SettingsFragment(), "preferences")
				.commit()
	}

	class SettingsFragment: PreferenceFragment() {

		override fun onCreate(savedInstanceState: Bundle?) {
			super.onCreate(savedInstanceState)
			addPreferencesFromResource(R.xml.preferences)
		}
	}
}
