// ==UserScript==
// @name         test: GM_xmlhttpRequest
// @namespace    WebViewWM
// @match        *://*
// @run-at       document-end
// ==/UserScript==

var generic_event_handler = function(event_type, response) {
  var div = document.createElement('div');
  var pre = document.createElement('pre');
  var hr  = document.createElement('hr');

  pre.innerText = event_type + ":\n" + JSON.stringify(response, null, 2);
  div.appendChild(pre);
  document.body.appendChild(div);
  document.body.appendChild(hr);
};

var generic_details = {
  'onabort':            generic_event_handler.bind(null, 'onabort'),
  'onerror':            generic_event_handler.bind(null, 'onerror'),
  'onload':             generic_event_handler.bind(null, 'onload'),
  'onprogress':         generic_event_handler.bind(null, 'onprogress'),
  'onreadystatechange': generic_event_handler.bind(null, 'onreadystatechange'),
  'ontimeout':          generic_event_handler.bind(null, 'ontimeout')
};

while(document.body.childNodes.length) {
  document.body.removeChild(document.body.childNodes[0]);
}

// 0 DNS_PROBE_FINISHED_NXDOMAIN
GM_xmlhttpRequest(Object.assign({}, generic_details, {
  'method': 'GET',
  'url':    'https://does-not-exist.httpbin.org/'
}));
