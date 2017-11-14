package co.hanul.hybridapp.iap;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.util.Log;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import co.hanul.hybridapp.JSCallback;

public class BillingController {

    private Activity activity;
    private BillingClient billingClient;

    private boolean isServiceConnected;

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
                }

                @Override
                public void onBillingServiceDisconnected() {
                    isServiceConnected = false;
                }
            });
        }
    }

    public BillingController(Activity activity, final JSCallback purchaseErrorHandler, final JSCallback purchaseCancelHandler, final JSCallback purchaseSuccessHandler) {
        this.activity = activity;

        billingClient = BillingClient.newBuilder(activity).setListener(new PurchasesUpdatedListener() {

            @Override
            public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
                if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {

                    JSONArray dataSet = new JSONArray();
                    for (Purchase purchase : purchases) {
                        JSONObject data = new JSONObject();
                        try {
                            data.put("productId", purchase.getSku());
                            data.put("purchaseToken", purchase.getPurchaseToken());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        dataSet.put(data);
                    }

                    purchaseSuccessHandler.callDataSet(dataSet);
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

    public void loadPurchased(final JSCallback errorHandler, final JSCallback callback) {
        executeServiceRequest(new Runnable() {
            @Override
            public void run() {

                billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, new PurchaseHistoryResponseListener() {
                    @Override
                    public void onPurchaseHistoryResponse(int responseCode, List<Purchase> purchases) {
                        if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {

                            JSONArray dataSet = new JSONArray();
                            for (Purchase purchase : purchases) {
                                JSONObject data = new JSONObject();
                                try {
                                    data.put("productId", purchase.getSku());
                                    data.put("purchaseToken", purchase.getPurchaseToken());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                dataSet.put(data);
                            }

                            callback.callDataSet(dataSet);
                        }

                        else {
                            errorHandler.call(new JSONObject());
                        }
                    }
                });
            }
        });
    }

    public void requestPurchase(final String productId) {
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

    public void consumePurchase(final String purchaseToken, final JSCallback errorHandler, final JSCallback callback) {
        executeServiceRequest(new Runnable() {
            @Override
            public void run() {

                billingClient.consumeAsync(purchaseToken, new ConsumeResponseListener() {
                    @Override
                    public void onConsumeResponse(int responseCode, String purchaseToken) {
                        if (responseCode == BillingClient.BillingResponse.OK) {
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
