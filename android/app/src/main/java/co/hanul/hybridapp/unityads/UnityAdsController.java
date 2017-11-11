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

    public UnityAdsController(Activity activity, String gameId) {
        this.activity = activity;
        UnityAds.initialize(activity, gameId, this, true);
    }

    public void show(JSCallback callback) {
        showCallback = callback;

        if (UnityAds.isReady() == true) {
            UnityAds.show(activity, "rewardedVideo");
        }
    }

    @Override
    public void onUnityAdsReady(String s) {}

    @Override
    public void onUnityAdsStart(String s) {}

    @Override
    public void onUnityAdsFinish(String s, UnityAds.FinishState finishState) {
        Log.d("UnityAds", s);

        JSONObject json = new JSONObject();
        try {
            json.put("s", s);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        showCallback.call(json);
    }

    @Override
    public void onUnityAdsError(UnityAds.UnityAdsError unityAdsError, String s) {}
}
