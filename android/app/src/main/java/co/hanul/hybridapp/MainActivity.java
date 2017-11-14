package co.hanul.hybridapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.games.Games;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import co.hanul.hybridapp.iap.BillingController;
import co.hanul.hybridapp.unityads.UnityAdsController;

public class MainActivity extends AppCompatActivity {

    private static final int RC_LOGIN = 9001;
    private static final int RC_LOGIN_FOR_ACHIEVEMENT = 9002;
    private static final int RC_LOGIN_FOR_LEADERBOARD = 9003;

    private static final int RC_ACHIEVEMENT_UI = 9011;
    private static final int RC_LEADERBOARD_UI = 9012;

    private WebView webView;

    private BillingController billingController;
    private UnityAdsController unityAdsController;

    public static String registeredPushKey;
    public static JSCallback registerPushKeyHandler;

    private JSCallback loginGameServiceErrorHandler;
    private JSCallback loginGameServiceCallback;
    private JSCallback logoutGameServiceCallback;
    private JSCallback showAchievementsErrorHandler;
    private JSCallback showLeaderboardsErrorHandler;
    private String leaderboardId;

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

        // 웹뷰 디버깅 모드 ON
        WebView.setWebContentsDebuggingEnabled(true);

        // 웹뷰 로드
        webView = findViewById(R.id.webView);

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
        });

        // JavaScript 인터페이스 등록
        webView.addJavascriptInterface(new WebAppInterface(this), "__Native");

        // 웹뷰 세팅
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    protected void onResume() {
        super.onResume();

        changeToFullscreen();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    private class WebAppInterface {
        Activity activity;
        WebAppInterface(Activity activity) {
            this.activity = activity;
        }

        @JavascriptInterface
        public void init(boolean isDevMode, String registerPushKeyHandlerName, String unityAdsGameId) {

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

            unityAdsController = new UnityAdsController(activity, unityAdsGameId, isDevMode);
        }

        @JavascriptInterface
        public void removePushKey() {
            try {
                FirebaseInstanceId.getInstance().deleteInstanceId();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public void generateNewPushKey() {
            FirebaseInstanceId.getInstance().getToken();
        }

        @JavascriptInterface
        public void initPurchaseService(String purchaseErrorHandlerName, String purchaseCancelHandlerName, String purchaseSuccessHandlerName) {
            billingController = new BillingController(activity, new JSCallback(webView, purchaseErrorHandlerName), new JSCallback(webView, purchaseCancelHandlerName), new JSCallback(webView, purchaseSuccessHandlerName));
        }

        @JavascriptInterface
        public void loadPurchased(String errorHandlerName, String callbackName) {
            billingController.loadPurchased(new JSCallback(webView, errorHandlerName), new JSCallback(webView, callbackName));
        }

        @JavascriptInterface
        public void requestPurchase(String skuId) {
            billingController.requestPurchase(skuId);
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
        public void loginGameService(String errorHandlerName, String callbackName) {
            loginGameServiceErrorHandler = new JSCallback(webView, errorHandlerName);
            loginGameServiceCallback = new JSCallback(webView, callbackName);

            GoogleSignInClient signInClient = GoogleSignIn.getClient(activity, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
            Intent intent = signInClient.getSignInIntent();
            activity.startActivityForResult(intent, RC_LOGIN);
        }

        @JavascriptInterface
        public void logoutGameService(String callbackName) {
            logoutGameServiceCallback = new JSCallback(webView, callbackName);

            GoogleSignInClient signInClient = GoogleSignIn.getClient(activity, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
            signInClient.signOut().addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    logoutGameServiceCallback.call(new JSONObject());
                }
            });
        }

        @JavascriptInterface
        public void showAchievements(String errorHandlerName) {
            showAchievementsErrorHandler = new JSCallback(webView, errorHandlerName);

            GoogleSignInClient signInClient = GoogleSignIn.getClient(activity, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
            Intent intent = signInClient.getSignInIntent();
            activity.startActivityForResult(intent, RC_LOGIN_FOR_ACHIEVEMENT);
        }

        @JavascriptInterface
        public void unlockAchievement(String achievementId) {
            Games.getAchievementsClient(activity, GoogleSignIn.getLastSignedInAccount(activity)).unlock(achievementId);
        }

        @JavascriptInterface
        public void incrementAchievement(String achievementId) {
            Games.getAchievementsClient(activity, GoogleSignIn.getLastSignedInAccount(activity)).increment(achievementId, 1);
        }

        @JavascriptInterface
        public void showLeaderboards(String _leaderboardId, String errorHandlerName) {
            leaderboardId = _leaderboardId;
            showLeaderboardsErrorHandler = new JSCallback(webView, errorHandlerName);

            GoogleSignInClient signInClient = GoogleSignIn.getClient(activity, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
            Intent intent = signInClient.getSignInIntent();
            activity.startActivityForResult(intent, RC_LOGIN_FOR_LEADERBOARD);
        }

        @JavascriptInterface
        public void updateLeaderboardScore(String leaderboardId, int score) {
            Games.getLeaderboardsClient(activity, GoogleSignIn.getLastSignedInAccount(activity)).submitScore(leaderboardId, score);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == RC_LOGIN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent);
            if (result.isSuccess()) {
                loginGameServiceCallback.call(new JSONObject());
            } else {
                loginGameServiceErrorHandler.call(new JSONObject());
            }
        }

        if (requestCode == RC_LOGIN_FOR_ACHIEVEMENT) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent);
            if (result.isSuccess()) {
                Games.getAchievementsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                        .getAchievementsIntent()
                        .addOnSuccessListener(new OnSuccessListener<Intent>() {
                            @Override
                            public void onSuccess(Intent intent) {
                                startActivityForResult(intent, RC_ACHIEVEMENT_UI);
                            }
                        });
            } else {
                showAchievementsErrorHandler.call(new JSONObject());
            }
        }

        if (requestCode == RC_LOGIN_FOR_LEADERBOARD) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent);
            if (result.isSuccess()) {
                Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                        .getLeaderboardIntent(leaderboardId)
                        .addOnSuccessListener(new OnSuccessListener<Intent>() {
                            @Override
                            public void onSuccess(Intent intent) {
                                startActivityForResult(intent, RC_LEADERBOARD_UI);
                            }
                        });
            } else {
                showLeaderboardsErrorHandler.call(new JSONObject());
            }
        }
    }
}
