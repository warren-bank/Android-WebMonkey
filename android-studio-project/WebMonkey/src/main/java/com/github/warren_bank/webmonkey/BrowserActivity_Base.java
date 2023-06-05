package com.github.warren_bank.webmonkey;

import com.github.warren_bank.webmonkey.settings.SettingsActivity;

import at.pardus.android.webview.gm.demo.WebViewGmImpl;
import at.pardus.android.webview.gm.run.WebViewClientGm;
import at.pardus.android.webview.gm.run.WebViewGm;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebSettings;

public class BrowserActivity_Base extends WebViewGmImpl implements IBrowser {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    scriptStore = new WmScriptStore(this);
    scriptStore.open();

    super.onCreate(savedInstanceState);

    if (Build.VERSION.SDK_INT >= 19)
      WebViewGm.setWebContentsDebuggingEnabled(true);

    WebViewGm webViewGm = scriptBrowser.getWebView();
    customizeWebView(webViewGm);
    String secret = webViewGm.getWebViewClient().getSecret();

    WmJsApi jsApi = new WmJsApi(secret, /* Activity */ this, /* WebView */ webViewGm, /* IBrowser */ this);

    webViewGm.addJavascriptInterface(jsApi.getGlobalJsApi(), WmJsApi.GlobalJsApiNamespace);
    ((WmScriptStore) scriptStore).addScript(jsApi.getWrappedJsApi());

    initWebView(webViewGm);

    processIntent(getIntent());

    WmScriptUpdateMgr.run_check_interval(this, scriptStore);
  }

  @Override
  public void onNewIntent (Intent in) {
    super.onNewIntent(in);
    processIntent(in);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.menu_update_scripts) {
      WmScriptUpdateMgr.run_check_now(BrowserActivity_Base.this, scriptStore);
      return true;
    } else if (item.getItemId() == R.id.menu_settings) {
      Intent in = new Intent(BrowserActivity_Base.this, SettingsActivity.class);
      startActivity(in);
      return true;
    } else if (item.getItemId() == R.id.menu_test_pattern) {
      Intent in = new Intent(BrowserActivity_Base.this, TestPatternActivity.class);
      startActivity(in);
      return true;
    } else if (item.getItemId() == R.id.menu_exit) {
      exit();
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }

  protected void customizeWebView(WebViewGm webViewGm) {
    try {
      WebViewClientGm webViewClient = (WebViewClientGm) new WmScriptBrowserWebViewClient_Base(BrowserActivity_Base.this, webViewGm);

      webViewGm.setWebViewClient(webViewClient);
    }
    catch(Exception e) {}
  }

  protected void exit() {
    if (Build.VERSION.SDK_INT >= 21)
      finishAndRemoveTask();
    else
      finish();
  }

  private void processIntent(Intent in) {
    String url = in.getDataString();

    if ((url != null) && (url.length() > 0))
      scriptBrowser.loadUrl(url);
  }

  // ---------------------------------------------------------------------------------------------
  // WebView:
  // ---------------------------------------------------------------------------------------------

  private void initWebView(WebViewGm webView) {
    WebSettings webSettings = webView.getSettings();
    webSettings.setLoadWithOverviewMode(true);
    webSettings.setSupportZoom(true);
    webSettings.setBuiltInZoomControls(true);
    webSettings.setDisplayZoomControls(true);
    webSettings.setUseWideViewPort(false);
    webSettings.setJavaScriptEnabled(true);
    webSettings.setDomStorageEnabled(true);
    webSettings.setUserAgentString(
      getString(R.string.user_agent)
    );
    if (Build.VERSION.SDK_INT >= 17) {
      webSettings.setMediaPlaybackRequiresUserGesture(false);
    }
    if (Build.VERSION.SDK_INT >= 21) {
      webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
    }

    webView.setInitialScale(0);
    webView.setHorizontalScrollBarEnabled(false);
    webView.setVerticalScrollBarEnabled(false);
    webView.clearCache(true);
    webView.clearHistory();
  }

  // ---------------------------------------------------------------------------------------------
  // IBrowser:
  // ---------------------------------------------------------------------------------------------

  public String getCurrentUrl() {
    return scriptBrowser.getCurrentUrl();
  }

  public void setCurrentUrl(String url) {
    scriptBrowser.changeAddressField(url);
  }

}
