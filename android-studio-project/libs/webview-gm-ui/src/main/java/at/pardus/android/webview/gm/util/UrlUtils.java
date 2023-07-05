package at.pardus.android.webview.gm.util;

public class UrlUtils {

  public static String removeHash(String url) {
    if (url == null) return url;

    int index = url.indexOf('#');

    return (index >= 0)
      ? url.substring(0, index)
      : url;
  }

  public static boolean areEqual(String url_1, String url_2) {
    if ((url_1 == null) && (url_2 == null)) return true;
    if ((url_1 == null) || (url_2 == null)) return false;

    url_1 = removeHash(url_1);
    url_2 = removeHash(url_2);

    return url_1.equals(url_2);
  }

}
