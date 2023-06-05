package com.github.warren_bank.webmonkey.settings;

import com.github.warren_bank.webmonkey.BuildConfig;
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

  // --------------------

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

  // --------------------

  public static void setLastUpdateTimestamp(Context context, long timestamp) {
    SharedPreferences.Editor editor = getPrefsEditor(context);
    String pref_key = context.getString(R.string.pref_lastupdate);

    editor.putLong(pref_key, timestamp);
    editor.commit();
  }

  public static long getLastUpdateTimestamp(Context context) {
    return getLastUpdateTimestamp(context, getPrefs(context));
  }

  private static long getLastUpdateTimestamp(Context context, SharedPreferences prefs) {
    String pref_key = context.getString(R.string.pref_lastupdate);

    return prefs.getLong(pref_key, 0l);
  }

  // --------------------

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

  // --------------------

  public static String getBadSslPageloadBehavior(Context context) {
    return getBadSslPageloadBehavior(context, getPrefs(context));
  }

  private static String getBadSslPageloadBehavior(Context context, SharedPreferences prefs) {
    String pref_key     = context.getString(R.string.pref_badssl_pageloadbehavior_key);
    String pref_default = context.getString(R.string.pref_badssl_pageloadbehavior_default);
    String pref_value   = prefs.getString(pref_key, pref_default);

    return pref_value;
  }

  // --------------------

  public static int getUpdateIntervalDays(Context context) {
    return getUpdateIntervalDays(context, getPrefs(context));
  }

  private static int getUpdateIntervalDays(Context context, SharedPreferences prefs) {
    String pref_key     = context.getString(R.string.pref_updateinterval_key);
    String pref_default = context.getString(R.string.pref_updateinterval_default);
    String pref_value   = prefs.getString(pref_key, pref_default);

    return Integer.parseInt(pref_value, 10);
  }

  // --------------------

  public static boolean getEnableAdBlockPreference(Context context) {
    return getEnableAdBlockPreference(context, getPrefs(context));
  }

  private static boolean getEnableAdBlockPreference(Context context, SharedPreferences prefs) {
    if (!BuildConfig.ALLOW_ADBLOCK) return false;

    String pref_key         = context.getString(R.string.pref_enableadblock_key);
    String pref_default     = context.getString(R.string.pref_enableadblock_default);
    boolean val_default     = "true".equals(pref_default);

    return prefs.getBoolean(pref_key, val_default);
  }

}
