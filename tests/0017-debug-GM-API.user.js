// ==UserScript==
// @name         debug GM API: add to global window, and log all usage
// @namespace    WebViewWM
// @match        *://*
// @run-at       document-start
// ==/UserScript==

if (!window.GM && GM && (typeof GM === 'object')) {
  window.GM = GM

  for (let key in GM) {
    const val = GM[key]

    GM[key] = (...args) => {
      const result = val(...args)

      result.then(resolved => {
        console.log(`GM.${key}(${args.map(arg => JSON.stringify(arg, null, 2)).join(', ')}) = ${JSON.stringify(resolved, null, 2)}`)
      })

      return result
    }
  }
}
