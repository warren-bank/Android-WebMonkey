package com.github.warren_bank.webmonkey.file_input;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

public class CaptureImageFileHelper {

  private static int CHOOSE_CAMERA_IMAGE_FILE_REQUEST_CODE = 4;
  private static int SAVE_CAMERA_IMAGE_FILE_REQUEST_CODE   = 5;

  private static FileUriListener listener = null;
  private static Uri currentCameraImageFile = null;

  public static void setFileUriListener(FileUriListener fileUriListener) {
    listener = fileUriListener;
  }

  private static void useInputFileHelper(Activity activity) {
    if (listener != null) {
      InputFileHelper.setFileUriListener(listener);
    }

    InputFileHelper.chooseInputFile(activity, "image/*");
  }

  public static void chooseCameraImageFile(Activity activity) {
    chooseCameraImageFile(activity, "image/jpeg", "camera-photo.jpg");
  }

  public static void chooseCameraImageFile(Activity activity, String mimeType, String fileName) {
    if ((Build.VERSION.SDK_INT < 19) || (listener == null))
      return;

    if (!hasCameraApp(activity)) {
      useInputFileHelper(activity);
      return;
    }

    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.setType(mimeType);
    intent.putExtra(Intent.EXTRA_TITLE, fileName);

    activity.startActivityForResult(intent, CHOOSE_CAMERA_IMAGE_FILE_REQUEST_CODE);
  }

  private static boolean hasCameraApp(Activity activity) {
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    return (intent.resolveActivity(activity.getPackageManager()) != null);
  }

  private static void saveCameraImageFile(Activity activity) {
    if ((Build.VERSION.SDK_INT < 3) || (listener == null) || (currentCameraImageFile == null))
      return;

    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    intent.putExtra(MediaStore.EXTRA_OUTPUT, currentCameraImageFile);

    activity.startActivityForResult(intent, SAVE_CAMERA_IMAGE_FILE_REQUEST_CODE);
  }

  public static boolean onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
    if (requestCode == CHOOSE_CAMERA_IMAGE_FILE_REQUEST_CODE) {
      if ((resultCode == Activity.RESULT_OK) && (data != null)) {
          Uri file = data.getData();
          if (file != null) {
            currentCameraImageFile = file;
            saveCameraImageFile(activity);
            return true;
          }
      }

      useInputFileHelper(activity);
      return true;
    }

    if (requestCode == SAVE_CAMERA_IMAGE_FILE_REQUEST_CODE) {
      Uri file = currentCameraImageFile;
      currentCameraImageFile = null;

      if ((resultCode == Activity.RESULT_OK) && (listener != null) && (file != null)) {
        listener.onFileUri(
          new Uri[]{file}
        );
      }

      return true;
    }

    return false;
  }

}
