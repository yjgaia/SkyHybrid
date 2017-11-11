package co.hanul.hybridapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;

import co.hanul.hybridapp.unityads.UnityAdsController;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private BillingClient billingClient;
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

        // 결제 관련 설정
        billingClient = BillingClient.newBuilder(this).setListener(new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
                if (responseCode == BillingClient.BillingResponse.OK
                        && purchases != null) {
                    for (Purchase purchase : purchases) {
                        handlePurchase(purchase);
                    }
                } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
                    // Handle an error caused by a user cancelling the purchase flow.
                } else {
                    // Handle any other error codes.
                }
            }

            private void handlePurchase(Purchase purchase) {
                //TODO: 구현하기
            }
        }).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponseCode) {
                if (billingResponseCode == BillingClient.BillingResponse.OK) {
                    // The billing client is ready. You can query purchases here.
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });

        List<String> skuList = new ArrayList<>();
        skuList.add("premium_upgrade");
        skuList.add("gas");
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                // Process the result.

                if (responseCode == BillingClient.BillingResponse.OK
                        && skuDetailsList != null) {
                    for (SkuDetails skuDetails : skuDetailsList) {
                        String sku = skuDetails.getSku();
                        String price = skuDetails.getPrice();
                        if ("iap_test_item".equals(sku)) {
                            Log.d("HybridApp", "price: " + price);
                        }
                    }
                }
            }
        });

        billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, new PurchaseHistoryResponseListener() {
            @Override
            public void onPurchaseHistoryResponse(int responseCode, List<Purchase> purchasesList) {
                if (responseCode == BillingClient.BillingResponse.OK && purchasesList != null) {
                    for (Purchase purchase : purchasesList) {
                        // Process the result.
                        Log.d("HybridApp", "purchase: " + purchase);
                    }
                }
            }
        });

        unityAdsController = new UnityAdsController(this, "1604164");

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
                            public void onClick(DialogInterface dialog, int which) {}
                        })
                        .create()
                        .show();
                result.confirm();
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
        public void purchase(String skuId) {
            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                    .setSku(skuId)
                    .setType(BillingClient.SkuType.INAPP)
                    .build();
            int responseCode = billingClient.launchBillingFlow((MainActivity) context, flowParams);
            Log.d("HybridApp", "responseCode: " + responseCode);
        }

        @JavascriptInterface
        public void consumePurchase(String purchaseToken) {
            billingClient.consumeAsync(purchaseToken, new ConsumeResponseListener() {
                @Override
                public void onConsumeResponse(int responseCode, String purchaseToken) {
                    if (responseCode == BillingClient.BillingResponse.OK) {
                        // Handle the success of the consume operation.
                        // For example, increase the number of coins inside the user's basket.
                    }
                }
            });
        }

        @JavascriptInterface
        public void showUnityAd(String callbackName) {
            unityAdsController.show(new JSCallback(webView, callbackName));
        }
    }
}
