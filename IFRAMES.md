### How to run userscripts in &lt;iframe&gt; tags

#### API method:

```javascript
  window.WebViewWM.getUserscriptJS(secret, url)
```

#### where:

* the 1st parameter is a user-defined shared secret
  - in WebMonkey, this value is entered in: `Settings`

#### use case:

* for use by JS code injected by a `mitmproxy` script
  - to allow userscripts to run in iframe windows

#### related tools:

* [PCAPdroid](https://github.com/emanuele-f/PCAPdroid)
  - summary:
    * simulates a VPN to capture network traffic without root
* [PCAPdroid mitm](https://github.com/emanuele-f/PCAPdroid-mitm)
  - summary:
    * addon for PCAPdroid.
    * runs an instance of `mitmproxy` server in SOCKS5 mode.
    * decrypts TLS/SSL connections.
    * supports running user-supplied scripts.
* [mitmproxy scripts](https://github.com/warren-bank/mitmproxy-scripts)
  - summary:
    * my collection of various `mitmproxy` scripts.
* mitmproxy script: [`disable_csp`](https://github.com/warren-bank/mitmproxy-scripts/blob/master/mitmproxy-scripts/disable_csp.py)
  - summary:
    * can be used to disable all `content-security-policy` rules.
  - comments:
    * CSP rules are set by server HTTP response headers.
    * CSP rules can prevent WebMonkey userscripts from running.
* mitmproxy script: [`modify_html_files`](https://github.com/warren-bank/mitmproxy-scripts/blob/master/mitmproxy-scripts/modify_html_files.py)
  - summary:
    * can be used to inject JS code into iframes.
    * if this code runs in WebMonkey and has the shared secret,<br>
      then it can obtain the JS code for all matching userscripts,<br>
      and then dynamically add it to the page.
  - [example](https://github.com/warren-bank/mitmproxy-scripts/tree/master/mitmproxy-scripts/modify_html_files/input)
  - comments:
    * routing all network traffic through a simulated VPN tunnel isn't ideal,<br>
      but Android System WebView doesn't provide an API interface to access iframe windows.
    * when userscripts only need to run in top-level windows, then this isn't necessary.
    * when userscripts need to run in all iframe windows, then this provides a workable solution.
