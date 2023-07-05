// ==UserScript==
// @name         test: window.History
// @namespace    WebViewWM
// @match        *://*
// @run-at       document-end
// ==/UserScript==

const urls = [
  '/foo/',
  '/bar/',
  '/baz/?foo=bar',
  '/baz/?foo=bar#baz'
]

setTimeout(
  function() {
    var loc = unsafeWindow.location
    var url = loc.pathname + (loc.search || '') + (loc.hash || '')
    var this_index = urls.indexOf(url)
    var next_index = this_index + 1
    var islast = (next_index === urls.length)

    if (islast) {
      unsafeWindow.history.back()
    }
    else {
      url = urls[next_index]
      if (next_index % 2 === 0)
        unsafeWindow.history.pushState({}, '', url)
      else
        unsafeWindow.history.replaceState({}, '', url)
    }
  },
  3000
)

GM_toastShort(unsafeWindow.location.href)
