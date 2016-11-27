package sudoalgorithm.visahackathon;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class DisplayTextActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_text);
    }

    public void PayButton(View view){
        new AlertDialog.Builder(DisplayTextActivity.this)
                .setTitle("Visa Direct Pay")
                .setMessage("Please Wait, Waiting For Response")
                .show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(DisplayTextActivity.this, SuccessActivity.class));
            }
        },3000);

    }


}
