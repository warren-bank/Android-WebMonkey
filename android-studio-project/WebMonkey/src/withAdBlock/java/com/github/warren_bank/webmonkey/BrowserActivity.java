package com.github.warren_bank.webmonkey;

import com.github.warren_bank.webmonkey.WmScriptBrowserWebViewClient_AdBlock;

import at.pardus.android.webview.gm.run.WebViewClientGm;
import at.pardus.android.webview.gm.run.WebViewGm;

public class BrowserActivity extends BrowserActivity_Base {
  @Override
  protected void customizeWebView(WebViewGm webViewGm) {
    try {
      WmScriptBrowserWebViewClient_AdBlock webViewClient = WmScriptBrowserWebViewClient_AdBlock.getInstance(BrowserActivity.this, webViewGm);

      webViewGm.setWebViewClient((WebViewClientGm) webViewClient);
    }
    catch(Exception e) {}
  }
}
