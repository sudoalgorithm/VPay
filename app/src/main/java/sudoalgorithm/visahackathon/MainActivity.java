package sudoalgorithm.visahackathon;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.entity.mime.Header;

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


    protected void callVisaAPI() {
        try {
            AsyncHttpClient client = new AsyncHttpClient();

            StringEntity se = new StringEntity("{\n" +
                    "  \"method\": \"POST\",\n" +
                    "  \"uri\": \"https://sandbox.api.visa.com/visadirect/mvisa/v1/merchantpushpayments\",\n" +
                    "  \"content\": {\n" +
                    "  \"acquirerCountryCode\": \"356\",\n" +
                    "  \"acquiringBin\": \"408972\",\n" +
                    "  \"amount\": \"124.05\",\n" +
                    "  \"businessApplicationId\": \"MP\",\n" +
                    "  \"cardAcceptor\": {\n" +
                    "    \"address\": {\n" +
                    "      \"city\": \"KOLKATA\",\n" +
                    "      \"country\": \"IND\"\n" +
                    "    },\n" +
                    "    \"idCode\": \"CA-IDCode-77765\",\n" +
                    "    \"name\": \"Visa Inc. USA-Foster City\"\n" +
                    "  },\n" +
                    "  \"feeProgramIndicator\": \"123\",\n" +
                    "  \"localTransactionDateTime\": \"2016-11-26T02:36:41\",\n" +
                    "  \"purchaseIdentifier\": {\n" +
                    "    \"referenceNumber\": \"REF_123456789123456789123\",\n" +
                    "    \"type\": \"1\"\n" +
                    "  },\n" +
                    "  \"recipientName\": \"Jasper\",\n" +
                    "  \"recipientPrimaryAccountNumber\": \"4123640062698797\",\n" +
                    "  \"retrievalReferenceNumber\": \"412770451035\",\n" +
                    "  \"secondaryId\": \"123TEST\",\n" +
                    "  \"senderAccountNumber\": \"4027290077881587\",\n" +
                    "  \"senderName\": \"Jasper\",\n" +
                    "  \"senderReference\": \"\",\n" +
                    "  \"systemsTraceAuditNumber\": \"451035\",\n" +
                    "  \"transactionCurrencyCode\": \"INR\",\n" +
                    "  \"transactionIdentifier\": \"381228649430015\"\n" +
                    "}\n" +
                    "}");
            client.post(getApplicationContext(), "http://vdpwrapper.herokuapp.com/auth/api", se, "application/json",new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    Log.d("mainActivity", response.toString());
                    Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_SHORT).show(); //display in long period of time
                }
            } );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
