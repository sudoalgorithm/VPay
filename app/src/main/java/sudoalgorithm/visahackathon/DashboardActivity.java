package sudoalgorithm.visahackathon;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ActionBar mActionBar;
    private ListView mListView;
    private CustomAdapter mCustomAdapter;
    private Integer[] img1, img2;
    private Button btn;
    private ImageButton im1, im2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayShowTitleEnabled(false);
        }

        img1 = new Integer[]{
                R.drawable.careemlogo,
                R.drawable.burjlogo,
                R.drawable.mcd,
                R.drawable.duber,
                R.drawable.starbuckslogo

        };

        img2 = new Integer[]{
                R.drawable.careemface,
                R.drawable.burjyface,
                R.drawable.mcdonface,
                R.drawable.uberface,
                R.drawable.starryface

        };


        List<ImageData> items = new ArrayList<ImageData>();
        for (int i = 0; i < img1.length; i++) {
            ImageData item = new ImageData(img1[i], img2[i]);
            items.add(item);
        }

        mListView = (ListView) findViewById(R.id.listView);
        mCustomAdapter = new CustomAdapter(this, items);

        mListView.setAdapter(mCustomAdapter);

        mListView.setAdapter(mCustomAdapter);

        im1 = (ImageButton) findViewById(R.id.scanQrCode);
        im2 = (ImageButton) findViewById(R.id.AccountButton);

        im1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, QRScannerActivity.class));
            }
        });

        im2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //IBM Watson Alchemy concept & keywords to find personal INTERESTS

        //call VISA Merchant offers to find matching personalized offer recommendations


        //call VISA mobile payment API



        /*btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callVisaAPI();
            }
        });*/

    }

    public void btnBuy(View view){

    }




}
