package com.github.warren_bank.webmonkey;

import com.github.warren_bank.webmonkey.file_input.CaptureImageFileHelper;
import com.github.warren_bank.webmonkey.file_input.FileUriListener;
import com.github.warren_bank.webmonkey.file_input.InputFileHelper;

import android.app.Activity;
import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class WmWebChromeClient extends WebChromeClient implements FileUriListener {
  private Activity activity;
  private ValueCallback<Uri[]> currentFilePathCallback;

  public WmWebChromeClient(Activity activity) {
    this.activity = activity;

    CaptureImageFileHelper.setFileUriListener(this);
    InputFileHelper.setFileUriListener(this);
  }

  // API 21+
  @Override
  public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
    if (activity == null)
      return false;

    if (currentFilePathCallback != null) {
      currentFilePathCallback.onReceiveValue(null);
    }
    currentFilePathCallback = filePathCallback;

    if ((fileChooserParams != null) && fileChooserParams.isCaptureEnabled()) {
      String[] acceptTypes = fileChooserParams.getAcceptTypes();
      if (acceptTypes != null) {
        for (int i=0; i < acceptTypes.length; i++) {
          if (acceptTypes[i].equals("image/*") || acceptTypes[i].equals("image/jpeg")) {
            CaptureImageFileHelper.chooseCameraImageFile(activity);
            return true;
          }
        }
      }
    }

    InputFileHelper.chooseInputFile(activity);
    return true;
  }

  @Override
  public void onFileUri(Uri[] files) {
    if (currentFilePathCallback != null) {
      currentFilePathCallback.onReceiveValue(files);
      currentFilePathCallback = null;
    }
  }
}
