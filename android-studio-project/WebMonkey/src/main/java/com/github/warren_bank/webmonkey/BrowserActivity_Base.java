package com.github.warren_bank.webmonkey;

import com.github.warren_bank.webmonkey.settings.SettingsActivity;
import com.github.warren_bank.webmonkey.settings.WebViewSettingsMgr;
import com.github.warren_bank.webmonkey.util.BackupRestoreHelper;
import com.github.warren_bank.webmonkey.util.SaveFileHelper;

import at.pardus.android.webview.gm.demo.WebViewGmImpl;
import at.pardus.android.webview.gm.run.WebViewClientGm;
import at.pardus.android.webview.gm.run.WebViewGm;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class BrowserActivity_Base extends WebViewGmImpl implements IBrowser {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    WebViewGm webViewGm = scriptBrowser.getWebView();
    customizeWebView(webViewGm);

    WebViewClientGm webViewClient = webViewGm.getWebViewClient();
    String secret = webViewClient.getSecret();
    WmJsApi jsApi = new WmJsApi(secret, /* Activity */ this, /* WebViewGm */ webViewGm, /* IBrowser */ this);

    WmScriptJsCode.initStaticResources(/* Context */ this);
    WmScriptJsCode scriptJsCode = new WmScriptJsCode(jsApi);
    webViewGm.addJavascriptInterface(jsApi.getJsInterface(), WmJsApi.GlobalJsApiNamespace);
    webViewClient.setScriptJsCode(scriptJsCode);

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
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);

    // only enable backup and restore when SAF is available
    if (Build.VERSION.SDK_INT < 19) {
      MenuItem menu_database_backup  = menu.findItem(R.id.menu_database_backup);
      MenuItem menu_database_restore = menu.findItem(R.id.menu_database_restore);

      menu_database_backup.setVisible(false);
      menu_database_restore.setVisible(false);
    }

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.menu_update_scripts) {
      WmScriptUpdateMgr.run_check_now(BrowserActivity_Base.this, scriptStore);
      return true;
    } else if (item.getItemId() == R.id.menu_remove_cookies) {
      WebViewSettingsMgr.removeAllCookies();
      return true;
    } else if (item.getItemId() == R.id.menu_settings) {
      Intent in = new Intent(BrowserActivity_Base.this, SettingsActivity.class);
      startActivity(in);
      return true;
    } else if (item.getItemId() == R.id.menu_database_backup) {
      BackupRestoreHelper.chooseBackupFile(BrowserActivity_Base.this, getDbFilename());
      return true;
    } else if (item.getItemId() == R.id.menu_database_restore) {
      BackupRestoreHelper.chooseRestoreFile(BrowserActivity_Base.this, getDbFilename());
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

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    boolean handled;

    handled = BackupRestoreHelper.onActivityResult(BrowserActivity_Base.this, scriptStore, requestCode, resultCode, data);
    if (handled) return;

    handled = SaveFileHelper.onActivityResult(BrowserActivity_Base.this, requestCode, resultCode, data);
    if (handled) return;
  }

  private String getDbFilename() {
    return getString(R.string.app_name) + ".db";
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
    WmDownloadListener downloadListener = new WmDownloadListener(/* Activity */ this, /* WebView */ webView);
    webView.setDownloadListener(downloadListener);

    WebViewSettingsMgr.initStaticResources(/* Context */ this, /* WebView */ webView);
    WebViewSettingsMgr.initWebView();
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
