// ==UserScript==
// @name         test: regex @include pattern
// @namespace    WebViewWM
// @include      /^https?:.*$/
// @run-at       document-start
// ==/UserScript==

GM_toastLong("Hello from " + unsafeWindow.location.href);
