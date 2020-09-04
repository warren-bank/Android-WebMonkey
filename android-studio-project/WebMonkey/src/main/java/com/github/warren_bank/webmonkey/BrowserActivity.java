package com.github.warren_bank.webmonkey;

import com.github.warren_bank.webmonkey.settings.SettingsActivity;

import at.pardus.android.webview.gm.demo.WebViewGmImpl;
import at.pardus.android.webview.gm.run.WebViewGm;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

public class BrowserActivity extends WebViewGmImpl {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    scriptStore = new WmScriptStore(this);
    scriptStore.open();

    super.onCreate(savedInstanceState);

    if (Build.VERSION.SDK_INT >= 19)
      WebViewGm.setWebContentsDebuggingEnabled(true);

    WebViewGm webViewGm = scriptBrowser.getWebView();
    String secret = webViewGm.getWebViewClient().getSecret();

    WmJsApi jsApi = new WmJsApi(secret, this, webViewGm);

    webViewGm.addJavascriptInterface(jsApi.getGlobalJsApi(), WmJsApi.GlobalJsApiNamespace);
    ((WmScriptStore) scriptStore).addScript(jsApi.getWrappedJsApi());

    processIntent(getIntent());
  }

  @Override
  public void onNewIntent (Intent in) {
    super.onNewIntent(in);
    processIntent(in);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.menu_settings) {
      Intent in = new Intent(BrowserActivity.this, SettingsActivity.class);
      startActivity(in);
      return true;
    } else if (item.getItemId() == R.id.menu_exit) {
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

  private void processIntent(Intent in) {
    String url = in.getDataString();

    if ((url != null) && (url.length() > 0))
      scriptBrowser.loadUrl(url);
  }

}
