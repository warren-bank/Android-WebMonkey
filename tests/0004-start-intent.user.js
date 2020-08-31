// ==UserScript==
// @name         test: GM_startIntent
// @namespace    WebViewWM
// @match        *://*
// @run-at       document-start
// ==/UserScript==

// ==========================================
// https://www.bioscopelive.com/en/faq
// https://www.bioscopelive.com/en/originals
// ==========================================
// * legal streaming service
// * offers both free and premium content
// * offers Bangla movies, dramas, and serials
// * Bengal is a region in South Asia
// ==========================================
// * video is HLS
// * video is restricted by HTTP Referer
// ==========================================
// * ExoAirPlayer reads "referUrl" extra,
//   and sends HTTP Referer header,
//   which permits access to HLS video stream
// ==========================================

GM_startIntent(/* action= */ "android.intent.action.VIEW", /* data= */ "https://global.bioscopelive.com/vod/vod/j/f/jfnoj6o2wGB/jfnoj6o2wGB.m3u8", /* type= */ "application/x-mpegurl", /* extras: */ "referUrl", "https://www.bioscopelive.com/en/prime-details?type=channel&slug=channel-24");
