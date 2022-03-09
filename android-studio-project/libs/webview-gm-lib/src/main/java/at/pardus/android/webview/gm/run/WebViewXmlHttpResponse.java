/*
 *    Copyright 2015 Richard Broker
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

package at.pardus.android.webview.gm.run;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class WebViewXmlHttpResponse {

  private static final String TAG = WebViewXmlHttpResponse.class.getName();

  public static final int READY_STATE_UNSENT = 0;
  public static final int READY_STATE_OPENED = 1;
  public static final int READY_STATE_HEADERS_RECEIVED = 2;
  public static final int READY_STATE_LOADING = 3;
  public static final int READY_STATE_DONE = 4;

  private int readyState; // Java has no unsigned types, this should be
              // unsigned short.
  private String responseHeaders;
  private String responseText;
  private int status; // This should also be an unsigned short.
  private String statusText;

  private JSONObject context;
  private String finalUrl;

  // @see <tt><a
  // href="https://developer.mozilla.org/en-US/docs/Web/API/ProgressEvent">ProgressEvent</a></tt>
  private boolean lengthComputable;
  private long loaded; // Should be unsigned long long
  private long total; // Should also be unsigned long long.

  public WebViewXmlHttpResponse(JSONObject context) {
    this.readyState = READY_STATE_UNSENT;
    this.context = context;
  }

  public String toJSONString() {
    JSONObject self = toJSONObject();

    if (self == null) {
      return "";
    }

    return JSONObject.quote(self.toString());
  }

  public JSONObject toJSONObject() {
    JSONObject self = new JSONObject();

    try {
      self.put("readyState", this.readyState);
      self.put("responseHeaders", this.responseHeaders);
      self.put("responseText", this.responseText);
      self.put("status", this.status);
      self.put("statusText", this.statusText);
      self.put("finalUrl", this.finalUrl);
      self.put("lengthComputable", this.lengthComputable);
      self.put("loaded", this.loaded);
      self.put("total", this.total);

      if (this.context != null) {
        self.put("context", this.context);
      }
    } catch (JSONException e) {
      Log.e(TAG,
          "Failed to generate JSON response object:" + e.getMessage());
      return null;
    }

    return self;
  }

  public void setReadyState(int newReadyState) {
    this.readyState = newReadyState;
  }

  public void setResponseHeaders(String newResponseHeaders) {
    this.responseHeaders = newResponseHeaders;
  }

  public void setResponseText(String newResponseText) {
    this.responseText = newResponseText;
  }

  public void setStatus(int newStatus) {
    this.status = newStatus;
  }

  public void setStatusText(String newStatusText) {
    this.statusText = newStatusText;
  }

  public void setFinalUrl(String newFinalUrl) {
    this.finalUrl = newFinalUrl;
  }

  public void setLengthComputable(boolean isComputable) {
    this.lengthComputable = isComputable;
  }

  public void setLoaded(int newLoaded) {
    this.loaded = newLoaded;
  }

  public void setTotal(int newTotal) {
    this.total = newTotal;
  }
}
