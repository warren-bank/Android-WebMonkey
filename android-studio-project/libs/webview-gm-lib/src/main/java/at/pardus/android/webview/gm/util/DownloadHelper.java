/*
 *    Copyright 2012 Werner Bayer
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package at.pardus.android.webview.gm.util;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Class offering static functions to download data from a given URL either as a
 * String or byte[]
 */
public class DownloadHelper {

  private static final String TAG = DownloadHelper.class.getName();

  private static final class DownloadData {
    public byte[] buffer;
    public String encoding;

    public DownloadData(byte[] _buffer, String _encoding) {
      buffer   = _buffer;
      encoding = _encoding;
    }
  }

  /**
   * Don't run on the UI thread!
   */
  private static DownloadData downloadData(String _url, Map<String, String> _headers) {
    DownloadData response = null;
    HttpURLConnection httpConn = null;
    try {
      URL url = new URL(_url);
      httpConn = (HttpURLConnection) url.openConnection();

      httpConn.setRequestMethod("GET");
      httpConn.setDoOutput(false);
      httpConn.setDoInput(true);
      httpConn.setUseCaches(false);
      httpConn.setReadTimeout(5000);

      if ((_headers != null) && !_headers.isEmpty()) {
        for (Map.Entry<String, String> header : _headers.entrySet()) {
          httpConn.setRequestProperty(header.getKey(), header.getValue());
        }
      }

      httpConn.connect();
      if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
        throw new Exception("HTTP Response: " + httpConn.getResponseCode());
      }

      byte[] buffer = null;
      String encoding = httpConn.getContentEncoding();

      InputStream inputStream = httpConn.getInputStream();
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      int bytesRead;
      byte[] tempBuffer = new byte[4096];

      while (true) {
        bytesRead = inputStream.read(tempBuffer);
        if (bytesRead == -1) {
          break;
        }
        byteArrayOutputStream.write(tempBuffer, 0, bytesRead);
      }

      buffer = byteArrayOutputStream.toByteArray();
      inputStream.close();

      response = new DownloadData(buffer, encoding);
    }
    catch (Exception e) {
      String message = e.getMessage();

      if ((e instanceof IOException) && (httpConn != null)) {
        try {
          InputStream errorStream = httpConn.getErrorStream();
          if (errorStream != null) {
            Reader in = new UnicodeReader(errorStream, null);
            int charsRead;
            char[] charsBuffer = new char[4096];
            StringBuilder errorStr = new StringBuilder();
            while ((charsRead = in.read(charsBuffer, 0, 4096)) != -1) {
              if (charsRead > 0) {
                errorStr.append(charsBuffer, 0, charsRead);
              }
            }
            in.close();
            if (errorStr.length() > 0) {
              message = errorStr.toString();
            }
          }
        }
        catch (Exception ignored) {
        }
      }

      Log.e(TAG, "Exception downloading url: " + _url);
      Log.e(TAG, message);
      Log.e(TAG, Log.getStackTraceString(e));
    }
    finally {
      try {
        if (httpConn != null) {
          httpConn.disconnect();
        }
      }
      catch (Exception ignored) {}
    }

    return response;
  }

  public static String downloadUrl(String url, Map<String, String> headers) {
    DownloadData response = downloadData(url, headers);

    if ((response == null) || (response.buffer == null))
      return null;

    StringBuilder out = new StringBuilder();
    Reader in = null;

    try {
      ByteArrayInputStream is = new ByteArrayInputStream(response.buffer);
      in = new UnicodeReader(is, response.encoding);
      int charsRead;
      char[] charsBuffer = new char[4096];
      while ((charsRead = in.read(charsBuffer, 0, 4096)) != -1) {
        if (charsRead > 0) {
          out.append(charsBuffer, 0, charsRead);
        }
      }
    }
    catch (Exception e) {
      Log.e(TAG, "Exception downloading url: " + url);
      Log.e(TAG, e.getMessage());
      Log.e(TAG, Log.getStackTraceString(e));
    }
    finally {
      try {
        if (in != null) {
          in.close();
        }
      }
      catch (Exception ignored) {}
    }

    return (out.length() > 0) ? out.toString() : null;
  }

  public static String downloadScript(String url) {
    Map<String, String> headers = null;
    return downloadUrl(url, headers);
  }

  public static byte[] downloadBytes(String url) {
    Map<String, String> headers = null;
    DownloadData response = downloadData(url, headers);

    return ((response == null) || (response.buffer == null)) ? null : response.buffer;
  }

  public static String resolveUrl(String relativeUrl, String baseUrl) {
    try {
      URL base     = (baseUrl == null) ? null : new URL(baseUrl);
      URL resolved = new URL(base, relativeUrl);

      return resolved.toURI().toString();
    }
    catch(Exception e) {
      return null;
    }
  }
}
