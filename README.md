### [WebMonkey](https://github.com/warren-bank/Android-WebMonkey)

No-frills light-weight Android web browser with support for Greasemonkey userscripts.

Builds upon the [WebView GM library](https://github.com/wbayer/webview-gm) demo application.

#### Background

* the [WebView GM library](https://github.com/wbayer/webview-gm) enhances the native Android System [WebView](https://developer.chrome.com/multidevice/webview/overview)
  - with userscript management:
    * detecting and downloading `*.user.js` URLs
    * parsing and saving to a DB
    * automatic updates
  - with userscript injection:
    * on top-level HTML pages that match URL patterns
  - with support for Greasemonkey API ( [1](https://wiki.greasespot.net/Greasemonkey_Manual:API), [2](https://www.tampermonkey.net/documentation.php), [3](https://violentmonkey.github.io/api/gm/) ) functions:
    * `GM_addStyle`
    * `GM_deleteValue`
    * `GM_getResourceText`
    * `GM_getResourceURL`
    * `GM_getValue`
    * `GM_listValues`
    * `GM_log`
    * `GM_setValue`
    * `GM_xmlhttpRequest`

#### Improvements

* supplements the list of supported Greasemonkey API functions:
  - legacy:
    * `GM_addElement`
    * `GM_fetch`
      - drop-in replacement for `window.fetch` that uses `GM_xmlhttpRequest` to make network requests
    * `GM_registerMenuCommand`
    * `GM_unregisterMenuCommand`
  - [GM 4](https://www.greasespot.net/2017/09/greasemonkey-4-for-script-authors.html):
    * `GM.addElement`
    * `GM.addStyle`
    * `GM.deleteValue`
    * `GM.fetch`
    * `GM.getResourceText`
    * `GM.getResourceUrl`
    * `GM.getValue`
    * `GM.listValues`
    * `GM.log`
    * `GM.registerMenuCommand`
    * `GM.setValue`
    * `GM.unregisterMenuCommand`
    * `GM.xmlHttpRequest`
* adds an additional Javascript API interface to expose Android-specific capabilities:
  - `GM_toastLong(message)`
  - `GM_toastShort(message)`
  - `GM_getUrl()`
    * returns a String containing the URL that is currently loaded in the WebView
    * use case:
      - allows the userscript to detect whether the page has been redirected
        * server response status codes: 301, 302
    * example:
      - `var is_redirect = (GM_getUrl() !== unsafeWindow.location.href)`
  - `GM_resolveUrl(urlRelative, urlBase)`
    * returns a String containing `urlRelative` resolved relative to `urlBase`
    * where:
      - [required] `urlRelative` is a String URL: relative path
      - [optional] `urlBase`     is a String URL: absolute path
        * default value: the URL that is currently loaded in the WebView
    * examples:
      - `('video.mp4', 'http://example.com/iframe_window.html')`
      - `('video.mp4')`
  - `GM_startIntent(action, data, type, ...extras)`
    * starts an implicit [Intent](https://developer.android.com/training/basics/intents/sending)
    * where:
      - [required, can be empty] `action` is a String
      - [required, can be empty] `data`   is a String URL
      - [required, can be empty] `type`   is a String mime-type for format of `data`
      - [optional] `extras` is a list of String name/value pairs
    * example:
      - `('android.intent.action.VIEW', 'http://example.com/video.mp4', 'video/mp4', 'referUrl', 'http://example.com/videos.html')`
  - `GM_loadUrl(url, ...headers)`
    * loads a URL into the WebView with additional HTTP request headers
    * where:
      - [required] `url`     is a String URL
      - [optional] `headers` is a list of String name/value pairs
    * example:
      - `('http://example.com/iframe_window.html', 'Referer', 'http://example.com/parent_window.html')`
  - `GM_loadFrame(urlFrame, urlParent, proxyFrame)`
    * loads an iframe into the WebView
    * where:
      - [required] `urlFrame`   is a String URL: the page loaded into the iframe
      - [required] `urlParent`  is a String URL: value for `window.top.location.href` and `window.parent.location.href` as observed from within the iframe
      - [optional] `proxyFrame` is a boolean: a truthy value causes `urlFrame` to be downloaded in Java
        * `urlParent` is sent in the _Referer_ header
        * a successful (200-299) response is dynamically loaded into [_iframe.srcdoc_](https://developer.mozilla.org/en-US/docs/Web/API/HTMLIFrameElement/srcdoc)
        * the benefit:
          - same-origin policy does not apply
          - when `urlParent` and `urlFrame` belong to different domains, a userscript running in the top window can access the DOM within the iframe window
        * special use case:
          - when `urlFrame` only serves the desired web page content if `urlParent` is sent in the _Referer_ header
    * example:
      - `('http://example.com/iframe_window.html', 'http://example.com/parent_window.html')`
    * use case:
      - _"parent_window.html"_ contains:
        * an iframe to display _"iframe_window.html"_
        * other content that is not wanted
      - though a userscript could easily do the necessary housekeeping:
        * detach the iframe
        * remove all other DOM elements from body
        * reattach the iframe
      - this method provides a better solution:
        * removes all scripts that are loaded into the parent window
        * handles all the css needed to resize the iframe to maximize its display within the parent window
        * makes it easy to handle this common case
    * why this is a common case:
      - _"iframe_window.html"_ performs a check to verify that it is loaded in the proper parent window
      - example 1:
        ```javascript
          const urlParent = 'http://example.com/parent_window.html'
          try {
            // will throw when either:
            // - `top` is loaded from a different domain
            // - `top` is loaded from the same origin, but the URL path does not match 'parent_window.html'
            if(window.top.location.href !== urlParent)
              throw ''
          }
          catch(e) {
            // will redirect `top` window to the proper parent window
            window.top.location = urlParent
          }
        ```
      - example 2:
        ```javascript
          const urlParent = 'http://example.com/parent_window.html'
          {
            // will redirect to proper parent window when 'iframe_window.html' is loaded without a `top` window
            if(window === window.top)
              window.location = urlParent
          }
        ```
  - `GM_exit()`
    * causes [WebMonkey](https://github.com/warren-bank/Android-WebMonkey) to close

#### Settings

* default browser home page
  - _Continue where you left off_
  - [_Blank page_](about:blank)
  - [_Userscripts by developer_](https://warren-bank.github.io/Android-WebMonkey/index.html)
  - [_Userscripts at Greasy Fork_](https://greasyfork.org/)
* page load behavior on HTTPS certificate error
  - cancel
  - proceed
  - ask
* script update interval
  - number of days to wait between checks
  - special case: `0` disables automatic script updates
* shared secret for JS to access low-level API method:
  ```javascript
    window.WebViewWM.getUserscriptJS(secret, url)
  ```
  - specific use case: _mitmproxy_ script to inject JS code to bootstrap userscripts in [iframes](./IFRAMES.md)

#### Caveats

* userscripts only run in the top window
  - a _mitmproxy_ script is required to load userscripts into [iframes](./IFRAMES.md)

#### Legal:

* copyright: [Warren Bank](https://github.com/warren-bank)
* license: [GPL-2.0](https://www.gnu.org/licenses/old-licenses/gpl-2.0.txt)
