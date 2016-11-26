package sudoalgorithm.visahackathon;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class DisplayTextActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_text);
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
            client.post(getApplicationContext(), "http://vdpwrapper.herokuapp.com/auth/api", se, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String text = " ";

                    try {
                        text = new JSONObject(new String(responseBody)).toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d("mainActivity", text + " ");
                    Toast.makeText(getApplicationContext(), responseBody.toString(), Toast.LENGTH_SHORT).show(); //display in long period of time

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.d("mainActivity" + "Hello", responseBody.toString());
                }


            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
}
