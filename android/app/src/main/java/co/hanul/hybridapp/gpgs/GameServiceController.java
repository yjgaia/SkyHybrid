package co.hanul.hybridapp.gpgs;

import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.games.Games;
import com.google.android.gms.tasks.OnSuccessListener;

public class GameServiceController {

    public static final int RC_SIGN_IN = 9001;
    private static final int RC_ACHIEVEMENT_UI = 9003;

    private Activity activity;

    public GameServiceController(Activity activity) {
        this.activity = activity;
    }

    public void login() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(activity, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        Intent intent = signInClient.getSignInIntent();
        activity.startActivityForResult(intent, RC_SIGN_IN);
    }

    public void showAchievements() {
        Games.getAchievementsClient(activity, GoogleSignIn.getLastSignedInAccount(activity))
                .getAchievementsIntent()
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        activity.startActivityForResult(intent, RC_ACHIEVEMENT_UI);
                    }
                });
    }
}
