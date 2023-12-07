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
    String pref_value = getUnresolvedHomePageValue(context, prefs);

    if (pref_value.equals(context.getString(R.string.pref_lasturl))) {
      return getLastUrl(context, prefs);
    }

    if (pref_value.equals(context.getString(R.string.pref_custom_homepage_key))) {
      return getCustomHomePage(context, prefs);
    }

    return pref_value;
  }

  protected static String getUnresolvedHomePageValue(Context context, SharedPreferences prefs) {
    String pref_key     = context.getString(R.string.pref_homepage_key);
    String pref_default = context.getString(R.string.pref_homepage_default);
    String pref_value   = prefs.getString(pref_key, pref_default);

    return pref_value;
  }

  // --------------------

  public static String getCustomHomePage(Context context) {
    return getCustomHomePage(context, getPrefs(context));
  }

  private static String getCustomHomePage(Context context, SharedPreferences prefs) {
    String pref_key     = context.getString(R.string.pref_custom_homepage_key);
    String pref_default = context.getString(R.string.pref_custom_homepage_default);
    String pref_value   = prefs.getString(pref_key, pref_default);

    return pref_value;
  }

  // --------------------

  public static void setUserAgent(Context context, String value) {
    setUserAgent(context, value, true);
  }

  public static void setUserAgent(Context context, String value, boolean updateWebViewSettings) {
    SharedPreferences.Editor editor = getPrefsEditor(context);
    String pref_key   = null;
    String pref_value = null;

    if (value != null)
      value = value.trim();

    // WebView
    if ((value == null) || value.isEmpty() || value.toLowerCase().equals("webview")) {
      pref_key   = context.getString(R.string.pref_useragent_key);
      pref_value = context.getString(R.string.pref_useragent_array_values_1);

      editor.putString(pref_key, pref_value);
    }

    // Chrome desktop
    else if (value.toLowerCase().equals("chrome")) {
      pref_key   = context.getString(R.string.pref_useragent_key);
      pref_value = context.getString(R.string.pref_useragent_array_values_2);

      editor.putString(pref_key, pref_value);
    }

    // Custom URL
    else {
      pref_key   = context.getString(R.string.pref_useragent_key);
      pref_value = context.getString(R.string.pref_useragent_array_values_3);

      editor.putString(pref_key, pref_value);

      pref_key   = context.getString(R.string.pref_custom_useragent_key);
      pref_value = value;

      editor.putString(pref_key, pref_value);
    }

    editor.commit();

    if (updateWebViewSettings)
      WebViewSettingsMgr.updateUserAgent();
  }

  public static String getUserAgent(Context context) {
    return getUserAgent(context, true);
  }

  public static String getUserAgent(Context context, boolean resolveDefaultUserAgent) {
    return getUserAgent(context, resolveDefaultUserAgent, getPrefs(context));
  }

  private static String getUserAgent(Context context, boolean resolveDefaultUserAgent, SharedPreferences prefs) {
    String pref_value = getUnresolvedUserAgentValue(context, prefs);

    if (pref_value.equals(context.getString(R.string.pref_useragent_array_names_1))) {
      return getDefaultUserAgent(context, resolveDefaultUserAgent);
    }

    if (pref_value.equals(context.getString(R.string.pref_custom_useragent_key))) {
      return getCustomUserAgent(context, prefs);
    }

    return pref_value;
  }

  protected static String getUnresolvedUserAgentValue(Context context, SharedPreferences prefs) {
    String pref_key     = context.getString(R.string.pref_useragent_key);
    String pref_default = context.getString(R.string.pref_useragent_default);
    String pref_value   = prefs.getString(pref_key, pref_default);

    return pref_value;
  }

  private static String getDefaultUserAgent(Context context, boolean resolveDefaultUserAgent) {
    // https://developer.android.com/reference/android/webkit/WebSettings.html#setUserAgentString(java.lang.String)
    //   If the string is null or empty, the system default value will be used.

    return (resolveDefaultUserAgent)
      ? WebViewSettingsMgr.getDefaultUserAgent()
      : null
    ;
  }

  // --------------------

  public static String getCustomUserAgent(Context context) {
    return getCustomUserAgent(context, getPrefs(context));
  }

  private static String getCustomUserAgent(Context context, SharedPreferences prefs) {
    String pref_key     = context.getString(R.string.pref_custom_useragent_key);
    String pref_default = context.getString(R.string.pref_custom_useragent_default);
    String pref_value   = prefs.getString(pref_key, pref_default);

    return pref_value;
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

  // --------------------

  public static String getSharedSecretPreference(Context context) {
    return getSharedSecretPreference(context, getPrefs(context));
  }

  private static String getSharedSecretPreference(Context context, SharedPreferences prefs) {
    String pref_key     = context.getString(R.string.pref_sharedsecret_key);
    String pref_default = "";
    String pref_value   = prefs.getString(pref_key, pref_default);

    return pref_value;
  }

  // --------------------

  public static boolean getEnableRemoteDebuggerPreference(Context context) {
    return getEnableRemoteDebuggerPreference(context, getPrefs(context));
  }

  private static boolean getEnableRemoteDebuggerPreference(Context context, SharedPreferences prefs) {
    String pref_key     = context.getString(R.string.pref_enableremotedebugger_key);
    String pref_default = context.getString(R.string.pref_enableremotedebugger_default);
    boolean val_default = "true".equals(pref_default);

    return prefs.getBoolean(pref_key, val_default);
  }

}
