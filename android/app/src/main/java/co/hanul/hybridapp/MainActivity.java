package co.hanul.hybridapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.Arrays;

import co.hanul.hybridapp.iap.BillingController;
import co.hanul.hybridapp.unityads.UnityAdsController;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    private BillingController billingController;
    private UnityAdsController unityAdsController;

    private void changeToFullscreen() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 전체 화면 설정
        changeToFullscreen();
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    changeToFullscreen();
                }
            }
        });

        // 앱 실행중에는 화면이 꺼지지 않도록
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        billingController = new BillingController(this, Arrays.asList("iap_test_item"));

        // 웹뷰 디버깅 모드 ON
        WebView.setWebContentsDebuggingEnabled(true);

        // 웹뷰 로드
        webView = findViewById(R.id.webView);

        // alert 디자인 변경
        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(view.getContext())
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.cancel();
                            }
                        })
                        .create()
                        .show();
                return true;
            }
        });

        // JavaScript 인터페이스 등록
        webView.addJavascriptInterface(new WebAppInterface(this), "__Native");

        // 웹뷰 세팅
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        webView.loadUrl("file:///android_asset/index.html");
    }

    private class WebAppInterface {
        Context context;
        WebAppInterface(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void init(boolean isDevMode, String unityAdsGameId) {
            unityAdsController = new UnityAdsController((MainActivity) context, unityAdsGameId, isDevMode);
        }

        @JavascriptInterface
        public void loadPurchased(String errorHandlerName, String callbackName) {
            billingController.loadPurchased(new JSCallback(webView, errorHandlerName), new JSCallback(webView, callbackName));
        }

        @JavascriptInterface
        public void purchase(String skuId, String errorHandlerName, String callbackName) {
            billingController.purchase(skuId, new JSCallback(webView, errorHandlerName), new JSCallback(webView, callbackName));
        }

        @JavascriptInterface
        public void consumePurchase(String purchaseToken, String errorHandlerName, String callbackName) {
            billingController.consumePurchase(purchaseToken, new JSCallback(webView, errorHandlerName), new JSCallback(webView, callbackName));
        }

        @JavascriptInterface
        public void showUnityAd(String errorHandlerName, String callbackName) {
            unityAdsController.show(new JSCallback(webView, errorHandlerName), new JSCallback(webView, callbackName));
        }

        @JavascriptInterface
        public void loginGameService(String callbackName) {

        }
    }
}
