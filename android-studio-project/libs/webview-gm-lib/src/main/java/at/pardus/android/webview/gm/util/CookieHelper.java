package at.pardus.android.webview.gm.util;

import android.webkit.CookieManager;

import org.json.JSONArray;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CookieHelper {

  public static String getCookieString(String url) {
    CookieManager cookieMgr = CookieManager.getInstance();
    String        cookieStr = cookieMgr.getCookie(url);

    return cookieStr;
  }

  public static Map<String, String> getCookieMap(String url) {
    String              cookieStr = CookieHelper.getCookieString(url);
    Map<String, String> cookieMap = new HashMap<String, String>();

    if (cookieStr != null) {
      String[] cookieArr = cookieStr.split(";");

      String[] cookieParts;
      String name, value;

      for (int i=0; i < cookieArr.length; i++) {
        cookieParts = cookieArr[i].split("=");

        if (cookieParts.length == 2) {
          name  = cookieParts[0].trim();
          value = cookieParts[1].trim();

          try {
            value = URLDecoder.decode(value, "UTF-8");
          }
          catch(Exception e){}

          cookieMap.put(name, value);
        }
      }
    }

    return cookieMap;
  }

  public static List<Map<String, String>> getCookieList(String url) {
    Map<String, String>       cookieMap  = CookieHelper.getCookieMap(url);
    List<Map<String, String>> cookieList = new ArrayList<Map<String, String>>();

    String name, value;
    Map<String, String> cookie;

    for (Map.Entry<String, String> entry : cookieMap.entrySet()) {
      name  = entry.getKey();
      value = entry.getValue();

      cookie = new HashMap<String, String>();
      cookie.put("name",  name);
      cookie.put("value", value);

      cookieList.add(cookie);
    }

    return cookieList;
  }

  public static JSONArray getCookieJSONArray(String url) {
    List<Map<String, String>> cookieList      = CookieHelper.getCookieList(url);
    JSONArray                 cookieJSONArray = new JSONArray(cookieList);

    return cookieJSONArray;
  }

  public static String getCookieJSON(String url) {
    JSONArray cookieJSONArray = CookieHelper.getCookieJSONArray(url);

    return cookieJSONArray.toString();
  }

  public static void setCookie(String url, String name, String value, boolean secure, boolean httpOnly, int maxAge) {
    try {
      value = URLEncoder.encode(value, "UTF-8");
    }
    catch(Exception e){}

    StringBuffer cookieStrBuf = new StringBuffer();
    cookieStrBuf.append(name + "=" + value);
    if (secure)
      cookieStrBuf.append("; Secure");
    if (httpOnly)
      cookieStrBuf.append("; HttpOnly");
    if (maxAge >= 0)
      cookieStrBuf.append("; Max-Age=" + maxAge);

    CookieManager cookieMgr = CookieManager.getInstance();
    String        cookieStr = cookieStrBuf.toString();

    cookieMgr.setCookie(url, cookieStr);
  }

  public static void deleteCookie(String url, String name) {
    CookieHelper.setCookie(url, name, "", false, false, 0);
  }

}
