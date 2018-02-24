package co.hanul.hybridapp;

import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSCallback {

    private WebView webView;
    private String callbackName;

    public JSCallback(WebView webView, String callbackName) {
        this.webView = webView;
        this.callbackName = callbackName;
    }

    public void call(final JSONObject json) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.evaluateJavascript(callbackName + "(" + json + ");", null);
            }
        });
    }

    public void callDataSet(final JSONArray jsonArray) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.evaluateJavascript(callbackName + "(" + jsonArray + ");", null);
            }
        });
    }
}