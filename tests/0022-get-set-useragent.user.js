// ==UserScript==
// @name         test: GM_getUserAgent and GM_setUserAgent
// @namespace    WebViewWM
// @match        *://*
// @run-at       document-start
// @grant        GM_setUserAgent
// ==/UserScript==

var agents = ['WebView', 'Chrome', 'WebMonkey']
var index = 0

const rotateAgent = function() {
  GM_setUserAgent(agents[index])

  index = (index + 1) % agents.length

  GM_toastLong(
    GM_getUserAgent()
  )
}

setInterval(rotateAgent, 5000)
