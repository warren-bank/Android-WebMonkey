package com.github.warren_bank.webmonkey.settings;

import android.content.Context;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class WebViewSettingsMgr {

  private static Context context = null;
  private static WebView webView = null;

  public static void initStaticResources(Context context, WebView webView) {
    WebViewSettingsMgr.context = context;
    WebViewSettingsMgr.webView = webView;
  }

  public static void initWebView() {
    if ((webView == null) || (context == null)) return;

    updateRemoteDebugger();

    WebSettings webSettings = webView.getSettings();
    webSettings.setLoadWithOverviewMode(true);
    webSettings.setSupportZoom(true);
    webSettings.setBuiltInZoomControls(true);
    webSettings.setDisplayZoomControls(true);
    webSettings.setUseWideViewPort(false);
    webSettings.setJavaScriptEnabled(true);
    webSettings.setDomStorageEnabled(true);
    if (Build.VERSION.SDK_INT >= 17) {
      webSettings.setMediaPlaybackRequiresUserGesture(false);
    }
    if (Build.VERSION.SDK_INT >= 21) {
      webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
    }
    updateUserAgent(webSettings);

    webView.setInitialScale(0);
    webView.setHorizontalScrollBarEnabled(false);
    webView.setVerticalScrollBarEnabled(false);
    webView.clearCache(true);
    webView.clearHistory();
  }

  public static String getDefaultUserAgent() {
    if ((webView == null) || (context == null)) return null;

    if (Build.VERSION.SDK_INT >= 17)
      return WebSettings.getDefaultUserAgent(context);

    // https://stackoverflow.com/a/10248817
    try {
      return System.getProperty("http.agent");
    }
    catch(Exception e) {}

    return null;
  }

  public static void updateUserAgent() {
    if ((webView == null) || (context == null)) return;

    WebSettings webSettings = webView.getSettings();
    updateUserAgent(webSettings);
  }

  private static void updateUserAgent(WebSettings webSettings) {
    if ((webView == null) || (context == null) || (webSettings == null)) return;

    String agent = SettingsUtils.getUserAgent(/* Context */ context, false);
    webSettings.setUserAgentString(agent);
  }

  public static void updateRemoteDebugger() {
    if ((webView == null) || (context == null)) return;

    boolean enabled = (Build.VERSION.SDK_INT >= 19)
      ? SettingsUtils.getEnableRemoteDebuggerPreference(/* Context */ context)
      : false;

    WebView.setWebContentsDebuggingEnabled(enabled);
  }

  public static void removeAllCookies() {
    CookieManager cookieMgr = CookieManager.getInstance();

    cookieMgr.removeSessionCookie();
    cookieMgr.removeAllCookie();
  }

}
