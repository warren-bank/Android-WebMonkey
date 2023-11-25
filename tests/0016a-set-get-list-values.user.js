// ==UserScript==
// @name         test: GM_setValue, GM_getValue, GM_listValues
// @namespace    WebViewWM
// @match        *://*
// @run-at       document-start
// @grant        GM_setValue
// ==/UserScript==

console.log(GM_deleteValue('foo'))
console.log(GM_setValue('foo', 'hello, foo'))
console.log(GM_getValue('foo'))
console.log(GM_listValues())

console.log(GM_deleteValue('bar'))
console.log(GM_setValue('bar', {hello: 'bar'}))
console.log(GM_getValue('bar'))
console.log(GM_listValues())

console.log(GM_deleteValue('baz'))
console.log(GM_getValue('baz', {hello: 'baz'}))
console.log(GM_listValues())
