package com.github.warren_bank.webmonkey.settings;

import com.github.warren_bank.webmonkey.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

  private PreferenceCategory prefsCategory;

  private Preference customHomePagePref;
  private Preference customUserAgentPref;

  private boolean isVisible_customHomePagePref;
  private boolean isVisible_customUserAgentPref;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);

    prefsCategory       = (PreferenceCategory) findPreference(getString(R.string.pref_settings_key));

    customHomePagePref  = findPreference(getString(R.string.pref_custom_homepage_key));
    customUserAgentPref = findPreference(getString(R.string.pref_custom_useragent_key));

    isVisible_customHomePagePref  = true;
    isVisible_customUserAgentPref = true;

    initCustomPrefs();

    getPrefs().registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();

    getPrefs().unregisterOnSharedPreferenceChangeListener(this);
  }

  private void initCustomPrefs() {
    SharedPreferences prefs = getPrefs();

    updateVisibilityOf_customHomePagePref(prefs);
    updateVisibilityOf_customUserAgentPref(prefs);
  }

  private SharedPreferences getPrefs() {
    return getPreferenceManager().getSharedPreferences();
  }

  private void updateVisibilityOf_customHomePagePref(SharedPreferences prefs) {
    String pref_value = SettingsUtils.getUnresolvedHomePageValue(getContext(), prefs);

    if (pref_value.equals(getString(R.string.pref_custom_homepage_key))) {
      // ensure that Custom URL text preference is: visible

      if (!isVisible_customHomePagePref) {
        prefsCategory.addPreference(customHomePagePref);
        isVisible_customHomePagePref = true;
      }
    }
    else {
      // ensure that Custom URL text preference is: hidden

      if (isVisible_customHomePagePref) {
        prefsCategory.removePreference(customHomePagePref);
        isVisible_customHomePagePref = false;
      }
    }
  }

  private void updateVisibilityOf_customUserAgentPref(SharedPreferences prefs) {
    String pref_value = SettingsUtils.getUnresolvedUserAgentValue(getContext(), prefs);

    if (pref_value.equals(getString(R.string.pref_custom_useragent_key))) {
      // ensure that Custom User Agent text preference is: visible

      if (!isVisible_customUserAgentPref) {
        prefsCategory.addPreference(customUserAgentPref);
        isVisible_customUserAgentPref = true;
      }
    }
    else {
      // ensure that Custom User Agent text preference is: hidden

      if (isVisible_customUserAgentPref) {
        prefsCategory.removePreference(customUserAgentPref);
        isVisible_customUserAgentPref = false;
      }
    }
  }

  // -------------------------------------------------------
  // implementation:
  //   SharedPreferences.OnSharedPreferenceChangeListener
  // -------------------------------------------------------

  @Override
  public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
    if ((key == null) || key.isEmpty()) return;

    if (key.equals(getString(R.string.pref_homepage_key))) {
      updateVisibilityOf_customHomePagePref(prefs);
      return;
    }

    if (key.equals(getString(R.string.pref_useragent_key))) {
      updateVisibilityOf_customUserAgentPref(prefs);
      WebViewSettingsMgr.updateUserAgent();
      return;
    }

    if (key.equals(getString(R.string.pref_custom_useragent_key))) {
      WebViewSettingsMgr.updateUserAgent();
      return;
    }

    if (key.equals(getString(R.string.pref_enableremotedebugger_key))) {
      WebViewSettingsMgr.updateRemoteDebugger();
      return;
    }
  }
}
