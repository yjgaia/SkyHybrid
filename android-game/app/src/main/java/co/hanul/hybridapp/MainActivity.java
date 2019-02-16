package co.hanul.hybridapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
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

import co.hanul.hybridapp.iap.BillingController;

public class MainActivity extends Activity {

    private static final int RC_LOGIN = 9001;
    private static final int RC_LOGIN_FOR_ACHIEVEMENT = 9002;
    private static final int RC_LOGIN_FOR_LEADERBOARD = 9003;

    private static final int RC_ACHIEVEMENT_UI = 9011;
    private static final int RC_LEADERBOARD_UI = 9012;

    private static final int RC_SELECT_FILE = 9013;

    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;

    private BillingController billingController;

    public static String registeredPushKey;
    public static JSCallback registerPushKeyHandler;

    private JSCallback loginGameServiceErrorHandler;
    private JSCallback loginGameServiceCallback;
    private JSCallback showAchievementsErrorHandler;
    private JSCallback showLeaderboardsErrorHandler;
    private String leaderboardId;

    private boolean isSignedGameService;

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
        setContentView(Settings.mainViewId);

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

        // 네비게이션 바를 검정색으로
        getWindow().setNavigationBarColor(Color.BLACK);

        // 웹뷰 디버깅 모드 ON
        WebView.setWebContentsDebuggingEnabled(true);

        // 웹뷰 로드
        webView = findViewById(Settings.webViewId);
        webView.setBackgroundColor(Color.TRANSPARENT);

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

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> _filePathCallback, FileChooserParams fileChooserParams) {

                if (filePathCallback != null) {
                    filePathCallback.onReceiveValue(null);
                }
                filePathCallback = _filePathCallback;

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Image Chooser"), RC_SELECT_FILE);

                return true;
            }
        });

        // JavaScript 인터페이스 등록
        webView.addJavascriptInterface(new WebAppInterface(this), "__Native");

        // 웹뷰 세팅
        WebSettings webSettings = webView.getSettings();
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setTextZoom(100);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        webView.loadUrl("file:///android_asset/index.html");

        if (registeredPushKey == null) {
            registeredPushKey = FirebaseInstanceId.getInstance().getToken();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();

        changeToFullscreen();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class WebAppInterface {
        Activity activity;
        WebAppInterface(Activity activity) {
            this.activity = activity;
        }

        @JavascriptInterface
        public void init(boolean isDevMode, String pushChannelId, String pushChannelTitle, String registerPushKeyHandlerName) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                NotificationChannel channel = new NotificationChannel(pushChannelId, pushChannelTitle, NotificationManager.IMPORTANCE_LOW);

                channel.enableLights(true);

                channel.setLightColor(Color.RED);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.createNotificationChannel(channel);
            }

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
        }

        @JavascriptInterface
        public void initPurchaseService(String loadPurchasedHandlerName) {
            billingController = new BillingController(activity, new JSCallback(webView, loadPurchasedHandlerName));
        }

        @JavascriptInterface
        public void purchase(String productId, String errorHandlerName, String cancelHandlerName, String callbackName) {
            billingController.purchase(productId, new JSCallback(webView, errorHandlerName), new JSCallback(webView, cancelHandlerName), new JSCallback(webView, callbackName));
        }

        @JavascriptInterface
        public void consumePurchase(String productId, String errorHandlerName, String callbackName) {
            billingController.consumePurchase(productId, new JSCallback(webView, errorHandlerName), new JSCallback(webView, callbackName));
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
        public void logoutGameService(final String callbackName) {
            GoogleSignIn.getClient(activity, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).signOut().addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    isSignedGameService = false;
                    new JSCallback(webView, callbackName).call(new JSONObject());
                }
            });
        }

        @JavascriptInterface
        public void showAchievements(String errorHandlerName) {

            if (isSignedGameService == true) {
                Games.getAchievementsClient(activity, GoogleSignIn.getLastSignedInAccount(activity))
                        .getAchievementsIntent()
                        .addOnSuccessListener(new OnSuccessListener<Intent>() {
                            @Override
                            public void onSuccess(Intent intent) {
                                startActivityForResult(intent, RC_ACHIEVEMENT_UI);
                            }
                        });
            }

            else {
                showAchievementsErrorHandler = new JSCallback(webView, errorHandlerName);

                GoogleSignInClient signInClient = GoogleSignIn.getClient(activity, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
                Intent intent = signInClient.getSignInIntent();
                activity.startActivityForResult(intent, RC_LOGIN_FOR_ACHIEVEMENT);
            }
        }

        @JavascriptInterface
        public void unlockAchievement(String achievementId) {
            if (isSignedGameService == true) {
                Games.getAchievementsClient(activity, GoogleSignIn.getLastSignedInAccount(activity)).unlock(achievementId);
            }
        }

        @JavascriptInterface
        public void incrementAchievement(String achievementId) {
            if (isSignedGameService == true) {
                Games.getAchievementsClient(activity, GoogleSignIn.getLastSignedInAccount(activity)).increment(achievementId, 1);
            }
        }

        @JavascriptInterface
        public void showLeaderboards(String _leaderboardId, String errorHandlerName) {

            if (isSignedGameService == true) {
                Games.getLeaderboardsClient(activity, GoogleSignIn.getLastSignedInAccount(activity))
                        .getLeaderboardIntent(_leaderboardId)
                        .addOnSuccessListener(new OnSuccessListener<Intent>() {
                            @Override
                            public void onSuccess(Intent intent) {
                                startActivityForResult(intent, RC_LEADERBOARD_UI);
                            }
                        });
            }

            else {
                leaderboardId = _leaderboardId;
                showLeaderboardsErrorHandler = new JSCallback(webView, errorHandlerName);

                GoogleSignInClient signInClient = GoogleSignIn.getClient(activity, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
                Intent intent = signInClient.getSignInIntent();
                activity.startActivityForResult(intent, RC_LOGIN_FOR_LEADERBOARD);
            }
        }

        @JavascriptInterface
        public void updateLeaderboardScore(String leaderboardId, String score) {
            if (isSignedGameService == true) {
                Games.getLeaderboardsClient(activity, GoogleSignIn.getLastSignedInAccount(activity)).submitScore(leaderboardId, Integer.parseInt(score));
            }
        }

        @JavascriptInterface
        public void exit() {
            activity.finishAndRemoveTask();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == RC_LOGIN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent);
            if (result.isSuccess()) {
                Games.getGamesClient(this, result.getSignInAccount()).setViewForPopups(webView);
                isSignedGameService = true;

                loginGameServiceCallback.call(new JSONObject());
            } else {
                loginGameServiceErrorHandler.call(new JSONObject());
            }
        }

        if (requestCode == RC_LOGIN_FOR_ACHIEVEMENT) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent);
            if (result.isSuccess()) {
                Games.getGamesClient(this, result.getSignInAccount()).setViewForPopups(webView);
                isSignedGameService = true;

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
                Games.getGamesClient(this, result.getSignInAccount()).setViewForPopups(webView);
                isSignedGameService = true;

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

        if (requestCode == RC_SELECT_FILE && filePathCallback != null) {
            if (resultCode == RESULT_OK) {
                filePathCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                filePathCallback = null;
            } else {
                filePathCallback.onReceiveValue(null);
                filePathCallback = null;
            }
        }
    }
}
