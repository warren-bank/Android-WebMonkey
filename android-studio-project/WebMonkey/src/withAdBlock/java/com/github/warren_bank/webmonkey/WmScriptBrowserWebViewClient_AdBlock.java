package com.github.warren_bank.webmonkey;

import com.github.warren_bank.webmonkey.R;
import com.github.warren_bank.webmonkey.WmScriptBrowserWebViewClient_Base;
import com.github.warren_bank.webmonkey.settings.SettingsUtils;

import at.pardus.android.webview.gm.run.WebViewGm;
import at.pardus.android.webview.gm.store.ScriptStore;
import at.pardus.android.webview.gm.store.ui.ScriptBrowser;

import android.content.Context;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.TreeMap;

public class WmScriptBrowserWebViewClient_AdBlock extends WmScriptBrowserWebViewClient_Base {
  private boolean isPopulatingHosts;
  private TreeMap<String, Object> blockedHosts;

  public WmScriptBrowserWebViewClient_AdBlock(Context context, WebViewGm webView) throws Exception {
    this(
      context,
      (ScriptBrowser.ScriptBrowserWebViewClientGm) webView.getWebViewClient()
    );
  }

  public WmScriptBrowserWebViewClient_AdBlock(Context context, ScriptBrowser.ScriptBrowserWebViewClientGm webViewClient) {
    this(
      context,
      webViewClient.getScriptStore(),
      webViewClient.getJsBridgeName(),
      webViewClient.getSecret(),
      webViewClient.getScriptBrowser()
    );
  }

  public WmScriptBrowserWebViewClient_AdBlock(Context context, ScriptStore scriptStore, String jsBridgeName, String secret, ScriptBrowser scriptBrowser) {
    super(context, scriptStore, jsBridgeName, secret, scriptBrowser);

    if (SettingsUtils.getEnableAdBlockPreference(context)) {
      isPopulatingHosts = true;
      populateBlockedHosts(context);
    }
    isPopulatingHosts = false;
  }

  @Override
  public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
    return shouldBlockRequest(url);
  }

  @Override
  public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
    String url = request.getUrl().toString();
    return shouldBlockRequest(url);
  }

  private void populateBlockedHosts(Context context) {
    InputStream is = context.getResources().openRawResource(R.raw.adblock_serverlist);
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    String line;

    if (is != null) {
      try {
        blockedHosts = new TreeMap<String, Object>();

        while ((line = br.readLine()) != null) {
          line = line.toLowerCase().trim();

          if (!line.isEmpty() && !line.startsWith("#")) {
            blockedHosts.put(line, null);
          }
        }
      } catch (IOException e) {
        blockedHosts = null;
      }
    }
  }

  private boolean isHostBlocked(String url) {
    if ((blockedHosts == null) || isPopulatingHosts) return false;

    try {
      Uri uri = Uri.parse(url);
      String host = uri.getHost().toLowerCase().trim();
      return blockedHosts.containsKey(host);
    }
    catch(Exception e) {
      return false;
    }
  }

  private WebResourceResponse shouldBlockRequest(String url) {
    if (isHostBlocked(url)) {
      ByteArrayInputStream EMPTY = new ByteArrayInputStream("".getBytes());
      return new WebResourceResponse("text/plain", "utf-8", EMPTY);
    }
    else {
      return null;
    }
  }
}
