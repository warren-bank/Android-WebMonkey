package com.github.warren_bank.webmonkey.settings;

import com.github.warren_bank.webmonkey.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingsUtils {

  private static SharedPreferences getPrefs(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context);
  }

  private static SharedPreferences.Editor getPrefsEditor(Context context) {
    SharedPreferences prefs = getPrefs(context);
    return prefs.edit();
  }

  public static void setLastUrl(Context context, String url) {
    SharedPreferences.Editor editor = getPrefsEditor(context);
    String pref_key = context.getString(R.string.pref_lasturl);

    editor.putString(pref_key, url);
    editor.commit();
  }

  public static String getLastUrl(Context context) {
    return getLastUrl(context, getPrefs(context));
  }

  private static String getLastUrl(Context context, SharedPreferences prefs) {
    String pref_key = context.getString(R.string.pref_lasturl);

    return prefs.getString(pref_key, "about:blank");
  }

  public static String getHomePage(Context context) {
    return getHomePage(context, getPrefs(context));
  }

  private static String getHomePage(Context context, SharedPreferences prefs) {
    String pref_key     = context.getString(R.string.pref_homepage_key);
    String pref_default = context.getString(R.string.pref_homepage_default);
    String pref_value   = prefs.getString(pref_key, pref_default);

    return (pref_value.equals(context.getString(R.string.pref_lasturl)))
      ? getLastUrl(context, prefs)
      : pref_value
    ;
  }

}
