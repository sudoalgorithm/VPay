package sudoalgorithm.visahackathon;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;

public class MainActivity extends AppCompatActivity {


    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private RelativeLayout mRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_main);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.activity_main);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                /* make the Facebook User Feed API call */
                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/me/feed",
                        null,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                Log.d("mainActivityFACEBOOK", response.toString());
                                try {
                                    Log.d("mainActivityFACEBOOK ", response.getJSONObject().getJSONArray("data").toString());
                                    JSONArray fbPostsText = response.getJSONObject().getJSONArray("data");

                                    for (int i = 0; i < fbPostsText.length(); ++i) {

                                        Log.d("mainActivityFACEBOOK " + i, fbPostsText.getJSONObject(i).getString("messages"));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                ).executeAsync();
            }

            @Override
            public void onCancel() {
                Snackbar.make(mRelativeLayout, "Transaction Canceled", Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Snackbar.make(mRelativeLayout, "Network Error, Please Try Again", Snackbar.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
