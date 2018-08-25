package co.hanul.hybridapp.admob;

import android.app.Activity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import org.json.JSONObject;

import co.hanul.hybridapp.JSCallback;

public class AdMobController implements RewardedVideoAdListener {

    private Activity activity;
    private boolean isDevMode;
    private String testDeviceId;

    private InterstitialAd interstitialAd;
    private String rewardedVideoAdId;
    private RewardedVideoAd rewardedVideoAd;
    private JSCallback showRewardedVideoAdCallback;
    
    public AdMobController(Activity activity, String adMobAppId, boolean isDevMode, String testDeviceId) {
        this.activity = activity;
        this.isDevMode = isDevMode;
        this.testDeviceId = testDeviceId;

        MobileAds.initialize(activity, adMobAppId);
    }

    public void initInterstitialAd(final String adId) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                interstitialAd = new InterstitialAd(activity);

                if (isDevMode == true) {
                    interstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
                    interstitialAd.loadAd(new AdRequest.Builder().addTestDevice(testDeviceId).build());
                } else {
                    interstitialAd.setAdUnitId(adId);
                    interstitialAd.loadAd(new AdRequest.Builder().build());
                }
                interstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        if (isDevMode == true) {
                            interstitialAd.loadAd(new AdRequest.Builder().addTestDevice(testDeviceId).build());
                        } else {
                            interstitialAd.loadAd(new AdRequest.Builder().build());
                        }
                    }
                });
            }
        });
    }

    public void showInterstitialAd() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (interstitialAd.isLoaded() == true) {
                    interstitialAd.show();
                }
            }
        });
    }

    public void initRewardedVideoAd(final String adId, JSCallback callback) {

        rewardedVideoAdId = adId;
        showRewardedVideoAdCallback = callback;

        final RewardedVideoAdListener that = this;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(activity);
                rewardedVideoAd.setRewardedVideoAdListener(that);
                if (isDevMode == true) {
                    rewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().addTestDevice(testDeviceId).build());
                } else {
                    rewardedVideoAd.loadAd(rewardedVideoAdId, new AdRequest.Builder().build());
                }
            }
        });
    }

    public void showRewardedVideoAd() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (rewardedVideoAd.isLoaded() == true) {
                    rewardedVideoAd.show();
                }
            }
        });
    }

    @Override
    public void onRewarded(RewardItem reward) {
        showRewardedVideoAdCallback.call(new JSONObject());
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {}

    @Override
    public void onRewardedVideoAdClosed() {
        if (isDevMode == true) {
            rewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().addTestDevice(testDeviceId).build());
        } else {
            rewardedVideoAd.loadAd(rewardedVideoAdId, new AdRequest.Builder().build());
        }
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {}

    @Override
    public void onRewardedVideoAdLoaded() {}

    @Override
    public void onRewardedVideoAdOpened() {}

    @Override
    public void onRewardedVideoStarted() {}
}
