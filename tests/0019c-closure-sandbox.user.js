// ==UserScript==
// @name         test: with closure, with sandbox
// @namespace    WebViewWM
// @match        *://*
// @run-at       document-end
// ==/UserScript==

var clean_dom = function() {
  while(document.body.childNodes.length) {
    document.body.removeChild(document.body.childNodes[0]);
  }
}

var append_to_dom = function(text) {
  var div = document.createElement('div');
  var pre = document.createElement('pre');
  var hr  = document.createElement('hr');

  pre.innerText = text;
  div.appendChild(pre);
  document.body.appendChild(div);
  document.body.appendChild(hr);
}

clean_dom()
append_to_dom(`with closure, with sandbox:`)

append_to_dom(`(typeof window) = ${(typeof window)}`)
append_to_dom(`(typeof this) = ${(typeof this)}`)
append_to_dom(`(typeof self) = ${(typeof self)}`)
append_to_dom(`(typeof globalThis) = ${(typeof globalThis)}`)
append_to_dom(`(typeof unsafeWindow) = ${(typeof unsafeWindow)}`)

append_to_dom(`(window === this) = ${(window === this)}`)
append_to_dom(`(window === self) = ${(window === self)}`)
if (typeof globalThis !== 'undefined')
  append_to_dom(`(window === globalThis) = ${(window === globalThis)}`)

append_to_dom(`(window instanceof Window) = ${(window instanceof Window)}`)

if (typeof unsafeWindow !== 'undefined') {
  append_to_dom(`(unsafeWindow instanceof Window) = ${(unsafeWindow instanceof Window)}`)
  append_to_dom(`(window === unsafeWindow) = ${(window === unsafeWindow)}`)
}

append_to_dom(`(typeof GM_info) = ${(typeof GM_info)}`)
append_to_dom(`(typeof window.GM_info) = ${(typeof window.GM_info)}`)
if (typeof unsafeWindow !== 'undefined')
  append_to_dom(`(typeof unsafeWindow.GM_info) = ${(typeof unsafeWindow.GM_info)}`)
