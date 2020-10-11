// ==UserScript==
// @name         test: toast script version to monitor behavior of automatic script updates
// @namespace    WebViewWM
// @version      1.0.0
// @updateURL    http://192.168.0.100/0008-monitor-script-updates.user.js
// @match        *://*
// @run-at       document-start
// ==/UserScript==

// ===============
// where:
//   - IP of webserver on LAN is: "192.168.0.100"
// ===============

// ===============
// usage:
//   - on the webserver:
//       interactively change both 2x instances of the version string: "1.0.0"
//   - in the app:
//       interactively change script update interval setting and/or trigger an immediate script update
// ===============

GM_toastLong("v1.0.0");
