package co.hanul.hybridapp.unityads;

import android.app.Activity;
import android.util.Log;

import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;

import org.json.JSONException;
import org.json.JSONObject;

import co.hanul.hybridapp.JSCallback;

public class UnityAdsController implements IUnityAdsListener {

    private Activity activity;
    private JSCallback showCallback;

    public UnityAdsController(Activity activity, String gameId, boolean isDevMode) {
        this.activity = activity;
        UnityAds.initialize(activity, gameId, this, isDevMode);
    }

    public void show(JSCallback errorHandler, JSCallback callback) {
        showCallback = callback;

        if (UnityAds.isReady() == true) {
            UnityAds.show(activity, "rewardedVideo");
        } else {
            errorHandler.call(new JSONObject());
        }
    }

    @Override
    public void onUnityAdsFinish(String s, UnityAds.FinishState finishState) {
        showCallback.call(new JSONObject());
    }

    @Override
    public void onUnityAdsReady(String s) {}

    @Override
    public void onUnityAdsStart(String s) {}

    @Override
    public void onUnityAdsError(UnityAds.UnityAdsError unityAdsError, String s) {}
}
