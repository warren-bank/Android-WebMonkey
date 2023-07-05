package at.pardus.android.webview.gm.util;

public class UrlUtils {

  public static String removeHash(String url) {
    if (url == null) return url;

    int index = url.indexOf('#');

    return (index >= 0)
      ? url.substring(0, index)
      : url;
  }

}
