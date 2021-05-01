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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Class offering static functions to download data from a given URL either as a
 * String or byte[]
 */
public class DownloadHelper {

	private static final String TAG = DownloadHelper.class.getName();

	/**
	 * Downloads and returns a file as String.
	 *
	 * Not to be run on the UI thread.
	 *
	 * @param url
	 *            the http address to get
	 * @return the downloaded file as String or null on any error
	 */
	public static String downloadScript(String url) {
		StringBuilder out = new StringBuilder();
		Reader in = null;
		HttpURLConnection con = null;
		char[] buffer = new char[4096];
		try {
			URL u = new URL(url);
			con = (HttpURLConnection) u.openConnection();
			con.setReadTimeout(5000);
			con.setRequestMethod("GET");
			con.setUseCaches(false);
			con.connect();
			InputStream is = con.getInputStream();
			in = new UnicodeReader(is, con.getContentEncoding());
			int bytesRead;
			while ((bytesRead = in.read(buffer, 0, 4096)) != -1) {
				if (bytesRead > 0) {
					out.append(buffer, 0, bytesRead);
				}
			}
		} catch (MalformedURLException e) {
			Log.e(TAG, Log.getStackTraceString(e));
			return null;
		} catch (IOException e) {
			Log.e(TAG, Log.getStackTraceString(e));
			try {
				InputStream errorStream = con != null ? con.getErrorStream() : null;
				if (errorStream != null) {
					in = new UnicodeReader(errorStream, null);
					int bytesRead;
					StringBuilder errorStr = new StringBuilder();
					while ((bytesRead = in.read(buffer, 0, 4096)) != -1) {
						if (bytesRead > 0) {
							errorStr.append(buffer, 0, bytesRead);
						}
					}
					in.close();
					Log.e(TAG, errorStr.toString());
				}
			} catch (Exception ignored) {
			}
			return null;
		} catch (Exception e) {
			Log.e(TAG, Log.getStackTraceString(e));
			return null;
		} finally {
			try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ignored) {
			}
			if (con != null) {
				con.disconnect();
			}
		}
		return out.toString();
	}

	/**
	 * Downloads and returns a file as an array of bytes.
	 *
	 * Not to be run on the UI thread.
	 *
	 * @param downloadUrl
	 *            the http address to get
	 * @return the downloaded file as byte[] or null on any error
	 */
	public static byte[] downloadBytes(String downloadUrl) {
		try {
			URL url = new URL(downloadUrl);
			HttpURLConnection httpConn = (HttpURLConnection) url
					.openConnection();

			if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				Log.e(TAG, "Exception downloading url: " + downloadUrl
						+ " HTTP Response " + httpConn.getResponseCode());
				httpConn.disconnect();
				return null;
			}

			InputStream inputStream = httpConn.getInputStream();
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			int bytesRead;
			byte[] buffer;
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
			return buffer;
		} catch (IOException e) {
			Log.e(TAG, "Exception downloading url: " + downloadUrl
					+ " as file: " + e.getMessage());
		}

		return null;
	}
}
