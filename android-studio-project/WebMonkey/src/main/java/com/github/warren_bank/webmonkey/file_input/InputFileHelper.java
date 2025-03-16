package com.github.warren_bank.webmonkey.file_input;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import java.util.ArrayList;

public class InputFileHelper {

  private static int CHOOSE_INPUT_FILE_REQUEST_CODE = 3;

  private static FileUriListener listener = null;

  public static void setFileUriListener(FileUriListener fileUriListener) {
    listener = fileUriListener;
  }

  public static void chooseInputFile(Activity activity) {
    chooseInputFile(activity, "*/*");
  }

  public static void chooseInputFile(Activity activity, String mimeType) {
    if ((Build.VERSION.SDK_INT < 19) || (listener == null))
      return;

    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
    intent.setType(mimeType);

    activity.startActivityForResult(intent, CHOOSE_INPUT_FILE_REQUEST_CODE);
  }

  public static boolean onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == CHOOSE_INPUT_FILE_REQUEST_CODE) {
      if ((resultCode == Activity.RESULT_OK) && (data != null) && (listener != null)) {
        ClipData clipData = data.getClipData();

        if (clipData != null) {
          // multiple files selected
          ArrayList<Uri> arrayList = new ArrayList<Uri>();

          for (int i=0; i < clipData.getItemCount(); i++) {
            Uri file = clipData.getItemAt(i).getUri();

            if (file != null) {
              arrayList.add(file);
            }
          }

          if (!arrayList.isEmpty()) {
            listener.onFileUri(
              arrayList.toArray(
                new Uri[arrayList.size()]
              )
            );
          }
        }
        else {
          // single file selected
          Uri file = data.getData();
          if (file != null) {
            listener.onFileUri(
              new Uri[]{file}
            );
          }
        }
      }

      return true;
    }

    return false;
  }

}
