package co.hanul.hybridapp;

import android.webkit.WebView;

import org.json.JSONObject;

public class JSCallback {

    private WebView webView;
    private String callbackName;

    public JSCallback(WebView webView, String callbackName) {
        this.webView = webView;
        this.callbackName = callbackName;
    }

    public void call(JSONObject json) {
        webView.evaluateJavascript(callbackName + "(" + json + ");", null);
    }
}