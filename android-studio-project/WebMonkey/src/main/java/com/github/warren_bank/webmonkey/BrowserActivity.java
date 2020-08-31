package com.github.warren_bank.webmonkey;

import at.pardus.android.webview.gm.demo.WebViewGmImpl;
import at.pardus.android.webview.gm.run.WebViewGm;

import android.os.Bundle;

public class BrowserActivity extends WebViewGmImpl {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    scriptStore = new WmScriptStore(this);
    scriptStore.open();

    super.onCreate(savedInstanceState);

    WebViewGm webViewGm = scriptBrowser.getWebView();
    String secret = webViewGm.getWebViewClient().getSecret();

    WmJsApi jsApi = new WmJsApi(secret, this);

    webViewGm.addJavascriptInterface(jsApi.getGlobalJsApi(), WmJsApi.GlobalJsApiNamespace);
    ((WmScriptStore) scriptStore).addScript(jsApi.getWrappedJsApi());
  }

}
