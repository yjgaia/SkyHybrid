package co.hanul.hybridapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import co.hanul.hybridapp.iap.BillingController;

public class MainActivity extends Activity {
    private static final int RC_SELECT_FILE = 9013;

    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;

    private BillingController billingController;

    public static String registeredPushKey;
    public static JSCallback registerPushKeyHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(Settings.mainViewId);

        // 웹뷰 디버깅 모드 ON
        WebView.setWebContentsDebuggingEnabled(true);

        // 웹뷰 로드
        webView = findViewById(Settings.webViewId);
        webView.setBackgroundColor(Color.TRANSPARENT);

        // alert 디자인 변경
        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(view.getContext())
                        .setCancelable(false)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        })
                        .create()
                        .show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(view.getContext())
                        .setCancelable(false)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.cancel();
                            }
                        })
                        .create()
                        .show();
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {

                final EditText input = new EditText(view.getContext());

                new AlertDialog.Builder(view.getContext())
                        .setCancelable(false)
                        .setMessage(message)
                        .setView(input)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm(input.getText().toString());
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.cancel();
                            }
                        })
                        .create()
                        .show();
                return true;
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> _filePathCallback, FileChooserParams fileChooserParams) {

                if (filePathCallback != null) {
                    filePathCallback.onReceiveValue(null);
                }
                filePathCallback = _filePathCallback;

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Image Chooser"), RC_SELECT_FILE);

                return true;
            }
        });

        // JavaScript 인터페이스 등록
        webView.addJavascriptInterface(new WebAppInterface(this), "__Native");

        // 웹뷰 세팅
        WebSettings webSettings = webView.getSettings();
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setTextZoom(100);

        webView.loadUrl("file:///android_asset/index.html");

        if (registeredPushKey == null) {
            registeredPushKey = FirebaseInstanceId.getInstance().getToken();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class WebAppInterface {
        Activity activity;
        WebAppInterface(Activity activity) {
            this.activity = activity;
        }

        @JavascriptInterface
        public void init(boolean isDevMode, String pushChannelId, String pushChannelTitle, String registerPushKeyHandlerName) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                NotificationChannel channel = new NotificationChannel(pushChannelId, pushChannelTitle, NotificationManager.IMPORTANCE_LOW);

                channel.enableLights(true);

                channel.setLightColor(Color.RED);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.createNotificationChannel(channel);
            }

            if (registeredPushKey == null) {
                registerPushKeyHandler = new JSCallback(webView, registerPushKeyHandlerName);
            }

            else {

                JSONObject data = new JSONObject();
                try {
                    data.put("pushKey", registeredPushKey);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                new JSCallback(webView, registerPushKeyHandlerName).call(data);
            }
        }

        @JavascriptInterface
        public void initPurchaseService(String loadPurchasedHandlerName) {
            billingController = new BillingController(activity, new JSCallback(webView, loadPurchasedHandlerName));
        }

        @JavascriptInterface
        public void purchase(String productId, String errorHandlerName, String cancelHandlerName, String callbackName) {
            billingController.purchase(productId, new JSCallback(webView, errorHandlerName), new JSCallback(webView, cancelHandlerName), new JSCallback(webView, callbackName));
        }

        @JavascriptInterface
        public void consumePurchase(String productId, String errorHandlerName, String callbackName) {
            billingController.consumePurchase(productId, new JSCallback(webView, errorHandlerName), new JSCallback(webView, callbackName));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == RC_SELECT_FILE && filePathCallback != null) {
            if (resultCode == RESULT_OK) {
                filePathCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                filePathCallback = null;
            } else {
                filePathCallback.onReceiveValue(null);
                filePathCallback = null;
            }
        }
    }
}
