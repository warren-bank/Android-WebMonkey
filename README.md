### [WebMonkey](https://github.com/warren-bank/Android-WebMonkey)

No-frills light-weight Android web browser with support for Greasemonkey user scripts.

Minor improvement to the [WebView GM library](https://github.com/wbayer/webview-gm) demo application.

#### Background

* the [WebView GM library](https://github.com/wbayer/webview-gm) enhances the native Android System [WebView](https://developer.chrome.com/multidevice/webview/overview) with support for Greasemonkey functions and the management of user scripts

#### Improvements

* an additional Javascript API interface to provide the following functions to user scripts:
  - `GM_toastLong(message)`
  - `GM_toastShort(message)`
  - `GM_startIntent(action, data, type, ...extras)`
    * starts an implicit [Intent](https://developer.android.com/training/basics/intents/sending)
    * where:
      - `action` is a String
      - `data`   is a String URL
      - `type`   is a String mime-type for format of `data`
      - `extras` is a list of String name/value pairs
    * example:
      - `('android.intent.action.VIEW', 'http://example.com/video.mp4', 'video/mp4', 'referUrl', 'http://example.com/videos.html')`
  - `GM_loadUrl(url, ...headers)`
    * loads a URL into the WebView with additional HTTP request headers
    * where:
      - `url`     is a String URL
      - `headers` is a list of String name/value pairs
    * example:
      - `('http://example.com/iframe_window.html', 'Referer', 'http://example.com/parent_window.html')`
  - `GM_exit()`
    * causes [WebMonkey](https://github.com/warren-bank/Android-WebMonkey) to close

#### Caveats

* userscripts only run in the top window
  - they are __not__ loaded into iframes
* issue [#1](https://github.com/warren-bank/Android-WebMonkey/issues/1)
  - a userscript will __not__ run when its `@name` begins with a numeric character

#### Legal:

* copyright: [Warren Bank](https://github.com/warren-bank)
* license: [GPL-2.0](https://www.gnu.org/licenses/old-licenses/gpl-2.0.txt)
