// ==UserScript==
// @name         resolve relative URLs in script parser
// @namespace    WebViewWM
// @version      1.0.0
// @updateURL    http://192.168.0.100/0009-resolve-relative-URLs-in-script-parser.user.js
// @match        *://*
// @require      0009-resolve-relative-URLs-in-script-parser/js/jquery-2.2.4.min.js
// @icon         0009-resolve-relative-URLs-in-script-parser/img/icon.png
// @resource     image_001 0009-resolve-relative-URLs-in-script-parser/img/OK.png
// @run-at       document-end
// ==/UserScript==

// ----------------------------------------------------------------------------- constants

unsafeWindow.alert('typeof unsafeWindow.jQuery is: ' + (typeof unsafeWindow.jQuery))

var image_data_uri = GM_getResourceURL("image_001")
unsafeWindow.alert('typeof GM_getResourceURL("image_001") is: ' + (typeof image_data_uri))
unsafeWindow.alert('value of GM_getResourceURL("image_001") is: ' + "\n\n" + image_data_uri.substr(0, 40))

unsafeWindow.jQuery(unsafeWindow.document).ready(function($) {
  var $body = $("body")

  unsafeWindow.alert('$("body").length is: ' + $body.length)
  unsafeWindow.alert('$("body").get(0) instanceof HTMLBodyElement is: ' + ($body.get(0) instanceof HTMLBodyElement))

  $body
    .html('<div><img src="' + image_data_uri + '" style="max-width: 90%" /><br /><br /><span>Hello from jQuery</span></div>')
    .css({textAlign: 'center', backgroundColor: 'white'})
})
