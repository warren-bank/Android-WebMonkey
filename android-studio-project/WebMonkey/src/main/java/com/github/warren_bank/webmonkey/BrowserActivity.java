package com.github.warren_bank.webmonkey;

import at.pardus.android.webview.gm.demo.WebViewGmImpl;
import at.pardus.android.webview.gm.run.WebViewGm;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

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

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.menu_exit) {
      exit();
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }

  protected void exit() {
    if (Build.VERSION.SDK_INT >= 21)
      finishAndRemoveTask();
    else
      finish();
  }

}
