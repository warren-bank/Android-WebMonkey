package com.github.warren_bank.webmonkey;

import com.github.warren_bank.webmonkey.WmScriptBrowserWebViewClient_AdBlock;

import at.pardus.android.webview.gm.run.WebViewClientGm;
import at.pardus.android.webview.gm.run.WebViewGm;

public class BrowserActivity extends BrowserActivity_Base {
  @Override
  protected void customizeWebView(WebViewGm webViewGm) {
    try {
      WebViewClientGm webViewClient = (WebViewClientGm) new WmScriptBrowserWebViewClient_AdBlock(BrowserActivity.this, webViewGm);

      webViewGm.setWebViewClient(webViewClient);
    }
    catch(Exception e) {}
  }
}
