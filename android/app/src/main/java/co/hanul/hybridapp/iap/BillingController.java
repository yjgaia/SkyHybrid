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

import java.util.List;

import co.hanul.hybridapp.JSCallback;

public class BillingController {

    private Activity activity;
    private BillingClient billingClient;

    public BillingController(Activity activity, List<String> skuIds) {
        this.activity = activity;

        billingClient = BillingClient.newBuilder(activity).setListener(new PurchasesUpdatedListener() {

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

        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuIds).setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                // Process the result.

                if (responseCode == BillingClient.BillingResponse.OK
                        && skuDetailsList != null) {

                    for (SkuDetails skuDetails : skuDetailsList) {
                    }
                }
            }
        });
    }

    public void loadPurchased(final JSCallback errorHandler, final JSCallback callback) {

        billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, new PurchaseHistoryResponseListener() {
            @Override
            public void onPurchaseHistoryResponse(int responseCode, List<Purchase> purchasesList) {
                if (responseCode == BillingClient.BillingResponse.OK && purchasesList != null) {

                    JSONArray dataSet = new JSONArray();
                    for (Purchase purchase : purchasesList) {
                        // Process the result.
                        Log.d("HybridApp", "purchase: " + purchase);

                        JSONObject data = new JSONObject();
                        try {
                            data.put("skuId", purchase.getSku());
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

    public void purchase(String skuId, JSCallback errorHandler, JSCallback callback) {

        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSku(skuId)
                .setType(BillingClient.SkuType.INAPP)
                .build();

        int responseCode = billingClient.launchBillingFlow(activity, flowParams);

        Log.d("HybridApp", "responseCode: " + responseCode);
    }

    public void consumePurchase(String purchaseToken, JSCallback errorHandler, JSCallback callback) {

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
}
