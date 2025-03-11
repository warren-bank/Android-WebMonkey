package com.github.warren_bank.webmonkey;

import com.github.warren_bank.webmonkey.R;
import com.github.warren_bank.webmonkey.util.SaveFileHelper;

import at.pardus.android.webview.gm.run.WebViewXmlHttpRequest;
import at.pardus.android.webview.gm.run.WebViewXmlHttpResponse;
import at.pardus.android.webview.gm.util.CacheFileHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebView;

public class WmDownloadListener implements DownloadListener {

  public Activity activity;
  public WebView  webview;

  public WmDownloadListener(Activity activity, WebView webview) {
    this.activity = activity;
    this.webview  = webview;
  }

  @Override
  public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
    if (TextUtils.isEmpty(url) || (url.length() < 6)) return;

    String protocol = url.substring(0, 6).toLowerCase();
    boolean is_http = protocol.startsWith("http:") || protocol.startsWith("https:");
    boolean is_data = protocol.startsWith("data:");
    if (!is_http && !is_data) return;

    String message = is_data ? "data: URI" : (
      (url.length() > 100)
        ? ( url.substring(0, 50) + "..." + url.substring(url.length() - 50, url.length()) )
        : url
    );

    new AlertDialog.Builder(activity)
      .setTitle(R.string.alertdialog_downloadlistener_title)
      .setMessage(message)
      .setPositiveButton(R.string.alertdialog_downloadlistener_label_button_positive, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          if (is_http) {
            Thread thread = new Thread() {
              @Override
              public void run() {
                String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
                downloadHttpUri(url, fileName);
              }
            };
            thread.start();
            return;
          }

          if (is_data) {
            String _contentDisposition = TextUtils.isEmpty(contentDisposition) ? "attachment;filename=\"data\"" : contentDisposition;
            String fileName = URLUtil.guessFileName(null, _contentDisposition, mimetype);
            downloadDataUri(url, fileName);
            return;
          }
        }
      })
      .setNegativeButton(R.string.alertdialog_downloadlistener_label_button_negative, null)
      .show();
  }

  private void downloadHttpUri(String url, String fileName) {
    try {
      String jsonRequestString        = "{\"url\": \"" + url + "\", \"method\": \"GET\", \"synchronous\": true}";
      WebViewXmlHttpRequest  request  = new WebViewXmlHttpRequest(webview, jsonRequestString);
      WebViewXmlHttpResponse response = request.execute();
      if (response == null) return;

      String cacheUUID = response.getResponseCacheUUID();
      String mimeType  = response.getMimeType();

      if (TextUtils.isEmpty(mimeType))
        mimeType = "*/*";

      SaveFileHelper.Download download = new SaveFileHelper.Download(cacheUUID, mimeType);
      SaveFileHelper.showFilePicker(activity, download, fileName);
    }
    catch(Exception e) {
    }
  }

  private void downloadDataUri(String url, String fileName) {
    try {
      String cacheUUID = String.valueOf(System.currentTimeMillis());
      String mimeType  = "*/*";

      int index_start, index_end;

      // MIME-type
      index_start = 5;
      index_end   = url.indexOf(';', index_start);
      if (index_end != -1) {
        mimeType = url.substring(index_start, index_end).trim();
      }

      // encoding
      boolean isBase64Encoding = false;
      index_end = url.indexOf(',', index_start);
      if (index_end != -1) {
        index_start = url.lastIndexOf(';', index_end);
        if (index_start != -1) {
          isBase64Encoding = url.substring(index_start + 1, index_end).trim().toLowerCase().equals("base64");
        }
      }

      // data
      String data = null;
      if (index_end != -1) {
        data = url.substring(index_end + 1);
      }
      if (TextUtils.isEmpty(data)) return;

      if (isBase64Encoding) {
        CacheFileHelper.write(activity, cacheUUID, data);
      }
      else {
        CacheFileHelper.write(activity, cacheUUID, data.getBytes("UTF-8"));
      }

      SaveFileHelper.Download download = new SaveFileHelper.Download(cacheUUID, mimeType);
      SaveFileHelper.showFilePicker(activity, download, fileName);
    }
    catch(Exception e) {
    }
  }

}
