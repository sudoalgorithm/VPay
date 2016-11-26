package sudoalgorithm.visahackathon;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

public class DashboardActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ActionBar mActionBar;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutmanager;
    private RecyclerView.Adapter mAdapter;
    private Integer[] imageDataSet1, imageDataSet2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();
        if (mActionBar != null){
            mActionBar.setDisplayShowTitleEnabled(false );
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_layout);
        mLayoutmanager = new LinearLayoutManager(this);
        mAdapter = new RVAdapter(imageDataSet1,imageDataSet2);
        mRecyclerView.setAdapter(mAdapter);
    }
}
