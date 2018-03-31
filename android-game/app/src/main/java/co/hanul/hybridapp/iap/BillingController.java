package co.hanul.hybridapp.iap;

import android.app.Activity;
import android.support.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.hanul.hybridapp.JSCallback;

public class BillingController {

    private Activity activity;
    private BillingClient billingClient;

    private boolean isServiceConnected;

    private JSCallback loadPurchasedHandler;
    private JSCallback purchaseErrorHandler;
    private JSCallback purchaseCancelHandler;
    private JSCallback purchaseSuccessHandler;

    private Map<String, String> purchaseTokenMap = new HashMap<>();

    private void executeServiceRequest(Runnable runnable) {

        if (isServiceConnected == true) {
            if (runnable != null) {
                runnable.run();
            }
        } else {

            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponseCode) {
                    isServiceConnected = true;

                    JSONArray dataSet = new JSONArray();
                    for (Purchase purchase : billingClient.queryPurchases(BillingClient.SkuType.INAPP).getPurchasesList()) {

                        purchaseTokenMap.put(purchase.getSku(), purchase.getPurchaseToken());

                        JSONObject data = new JSONObject();
                        try {
                            data.put("productId", purchase.getSku());
                            data.put("purchaseToken", purchase.getPurchaseToken());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        dataSet.put(data);
                    }

                    if (dataSet.length() > 0) {
                        loadPurchasedHandler.callDataSet(dataSet);
                    }
                }

                @Override
                public void onBillingServiceDisconnected() {
                    isServiceConnected = false;
                }
            });
        }
    }

    public BillingController(Activity activity, JSCallback loadPurchasedHandler) {
        this.activity = activity;
        this.loadPurchasedHandler = loadPurchasedHandler;

        billingClient = BillingClient.newBuilder(activity).setListener(new PurchasesUpdatedListener() {

            @Override
            public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
                if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {

                    JSONObject data = new JSONObject();
                    for (Purchase purchase : purchases) {

                        purchaseTokenMap.put(purchase.getSku(), purchase.getPurchaseToken());

                        try {
                            data.put("productId", purchase.getSku());
                            data.put("purchaseToken", purchase.getPurchaseToken());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    }

                    purchaseSuccessHandler.call(data);
                }

                else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
                    purchaseCancelHandler.call(new JSONObject());
                }

                else {
                    purchaseErrorHandler.call(new JSONObject());
                }
            }
        }).build();

        executeServiceRequest(null);
    }

    public void purchase(final String productId, final JSCallback errorHandler, final JSCallback cancelHandler, final JSCallback successHandler) {

        this.purchaseErrorHandler = errorHandler;
        this.purchaseCancelHandler = cancelHandler;
        this.purchaseSuccessHandler = successHandler;

        executeServiceRequest(new Runnable() {
            @Override
            public void run() {

                billingClient.launchBillingFlow(activity, BillingFlowParams.newBuilder()
                        .setSku(productId)
                        .setType(BillingClient.SkuType.INAPP)
                        .build());
            }
        });
    }

    public void consumePurchase(final String productId, final JSCallback errorHandler, final JSCallback callback) {
        executeServiceRequest(new Runnable() {
            @Override
            public void run() {

                billingClient.consumeAsync(purchaseTokenMap.get(productId), new ConsumeResponseListener() {
                    @Override
                    public void onConsumeResponse(int responseCode, String purchaseToken) {
                        if (responseCode == BillingClient.BillingResponse.OK) {
                            purchaseTokenMap.remove(productId);
                            callback.call(new JSONObject());
                        } else {
                            errorHandler.call(new JSONObject());
                        }
                    }
                });
            }
        });
    }
}
