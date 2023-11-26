// ==UserScript==
// @name         test: GM_cookie.set, GM_cookie.delete, GM_cookie.list
// @namespace    WebViewWM
// @match        *://*
// @run-at       document-start
// @grant        GM_cookie
// ==/UserScript==

var callback = console.log.bind(console)

GM_cookie.delete({'name': 'foo'})
GM_cookie.set({'name': 'foo', 'value': 'hello, foo'})
GM_cookie.list(callback)

GM_cookie.delete({'name': 'bar'})
GM_cookie.set({'name': 'bar', 'value': 'hello, bar'})
GM_cookie.list(callback)

GM_cookie.list({'name': 'foo'}, callback)
GM_cookie.list({'name': 'bar'}, callback)

GM_cookie.delete({'name': 'foo'})
GM_cookie.list(callback)

GM_cookie.delete({'name': 'bar'})
GM_cookie.list(callback)

GM_cookie.delete({'name': 'baz'})
GM_cookie.set({'name': 'baz', 'value': 'hello, baz; hello = baz', 'encode': true})
GM_cookie.list({'decode': true}, callback)
