package com.github.warren_bank.webmonkey;

import com.github.warren_bank.webmonkey.R;
import com.github.warren_bank.webmonkey.settings.SettingsUtils;

import at.pardus.android.webview.gm.run.WebViewClientGm;
import at.pardus.android.webview.gm.run.WebViewGm;
import at.pardus.android.webview.gm.store.ScriptStore;
import at.pardus.android.webview.gm.store.ui.ScriptBrowser;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.http.SslError;
import android.os.Build;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;

public class WmScriptBrowserWebViewClient_Base extends ScriptBrowser.ScriptBrowserWebViewClientGm {
  protected Context context;

  public WmScriptBrowserWebViewClient_Base(Context context, WebViewGm webView) throws Exception {
    this(
      context,
      (ScriptBrowser.ScriptBrowserWebViewClientGm) webView.getWebViewClient()
    );
  }

  public WmScriptBrowserWebViewClient_Base(Context context, ScriptBrowser.ScriptBrowserWebViewClientGm webViewClient) {
    this(
      context,
      webViewClient.getScriptStore(),
      webViewClient.getJsBridgeName(),
      webViewClient.getSecret(),
      webViewClient.getScriptBrowser()
    );
  }

  public WmScriptBrowserWebViewClient_Base(Context context, ScriptStore scriptStore, String jsBridgeName, String secret, ScriptBrowser scriptBrowser) {
    super(scriptStore, jsBridgeName, secret, scriptBrowser);

    this.context = context;
  }

  @Override
  public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
    String behavior = SettingsUtils.getBadSslPageloadBehavior(context);

    if (behavior.equals(context.getString(R.string.pref_pageloadbehavior_array_value_ask)))
      askBadSslPageloadBehavior(handler, error);
    else if (behavior.equals(context.getString(R.string.pref_pageloadbehavior_array_value_proceed)))
      handler.proceed();
    else if (behavior.equals(context.getString(R.string.pref_pageloadbehavior_array_value_cancel)))
      handler.cancel();
    else
      handler.cancel();
  }

  private void askBadSslPageloadBehavior(SslErrorHandler handler, SslError error) {
    AlertDialog.Builder builder = new AlertDialog.Builder(context);

    StringBuilder sb = new StringBuilder();

    int error_code = error.getPrimaryError();
    switch(error_code) {
      case SslError.SSL_DATE_INVALID:
        sb.append(context.getString(R.string.alertdialog_badssl_pageloadbehavior_reason_ssl_date_invalid));
        break;
      case SslError.SSL_EXPIRED:
        sb.append(context.getString(R.string.alertdialog_badssl_pageloadbehavior_reason_ssl_expired));
        break;
      case SslError.SSL_IDMISMATCH:
        sb.append(context.getString(R.string.alertdialog_badssl_pageloadbehavior_reason_ssl_idmismatch));
        break;
      case SslError.SSL_INVALID:
        sb.append(context.getString(R.string.alertdialog_badssl_pageloadbehavior_reason_ssl_invalid));
        break;
      case SslError.SSL_NOTYETVALID:
        sb.append(context.getString(R.string.alertdialog_badssl_pageloadbehavior_reason_ssl_notyetvalid));
        break;
      case SslError.SSL_UNTRUSTED:
        sb.append(context.getString(R.string.alertdialog_badssl_pageloadbehavior_reason_ssl_untrusted));
        break;
    }

    if (Build.VERSION.SDK_INT >= 14) {
      String url = error.getUrl();

      if ((url != null) && !url.isEmpty()) {
        if (sb.length() > 0) {
          sb.append("\n\n");
        }

        sb.append(context.getString(R.string.alertdialog_badssl_pageloadbehavior_label_url));
        sb.append("\n");
        sb.append(url);
      }
    }

    if (sb.length() > 0) {
      builder.setMessage(sb.toString());
    }

    builder
      .setTitle(
        R.string.alertdialog_badssl_pageloadbehavior_title
      )
      .setPositiveButton(
        R.string.alertdialog_badssl_pageloadbehavior_label_button_positive,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            handler.proceed();
          }
        }
      )
      .setNegativeButton(
        R.string.alertdialog_badssl_pageloadbehavior_label_button_negative,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            handler.cancel();
          }
        }
      )
      .show();
  }
}
