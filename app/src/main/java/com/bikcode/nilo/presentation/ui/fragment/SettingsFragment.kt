package com.bikcode.nilo.presentation.ui.fragment

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.bikcode.nilo.R
import com.bikcode.nilo.presentation.util.showToast
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val switchPreferenceCompat =
            findPreference<SwitchPreferenceCompat>(getString(R.string.pref_offers_key))

        val topic = getString(R.string.settings_topic_offers)

        switchPreferenceCompat?.setOnPreferenceChangeListener { preference, newValue ->
            (newValue as? Boolean)?.let { isChecked ->
                if (isChecked) {
                    Firebase.messaging.subscribeToTopic(topic)
                        .addOnSuccessListener {
                            context?.showToast(R.string.notifications_activated)
                        }
                } else {
                    Firebase.messaging.unsubscribeFromTopic(topic)
                        .addOnSuccessListener {
                            context?.showToast(R.string.notifications_deactivated)
                        }
                }
            }
            true
        }
    }
}