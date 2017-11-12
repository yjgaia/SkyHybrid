package co.hanul.hybridapp.push;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONException;
import org.json.JSONObject;

import co.hanul.hybridapp.MainActivity;

public class HybridAppFirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {

        if (MainActivity.registerPushKeyHandler == null) {
            MainActivity.registeredPushKey = FirebaseInstanceId.getInstance().getToken();
        }

        else {

            JSONObject data = new JSONObject();
            try {
                data.put("pushKey", FirebaseInstanceId.getInstance().getToken());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            MainActivity.registerPushKeyHandler.call(data);
        }
    }
}
