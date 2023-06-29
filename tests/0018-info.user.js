// ==UserScript==
// @name         test: GM_info
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
append_to_dom('(GM_info === GM.info) = ' + (GM_info === GM.info))
append_to_dom('(typeof GM_info) = ' + (typeof GM_info))
append_to_dom('GM_info = ' + JSON.stringify(GM_info, null, 2))
