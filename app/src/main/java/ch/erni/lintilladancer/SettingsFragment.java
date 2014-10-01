package ch.erni.lintilladancer;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Shows the preferences screen. Gets its info from /xml/preferences.xml
 * Created by gus on 24.08.14.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
