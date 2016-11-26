package sudoalgorithm.visahackathon;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

public class CardActivity extends AppCompatActivity {

    private ImageView imageView1, imageView2;
    boolean isBackVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        imageView1 = (ImageView) findViewById(R.id.card1citi);
        imageView2 = (ImageView) findViewById(R.id.card2hsbc);

        final AnimatorSet setRightOut = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(),
                R.anim.flight_right_out);

        final AnimatorSet setLeftIn = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(),
                R.anim.flight_left_in);

        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isBackVisible){
                    setRightOut.setTarget(imageView1);
                    setLeftIn.setTarget(imageView2);
                    setRightOut.start();
                    setLeftIn.start();
                    isBackVisible = true;
                }
                else{
                    setRightOut.setTarget(imageView1);
                    setLeftIn.setTarget(imageView2);
                    setRightOut.start();
                    setLeftIn.start();
                    isBackVisible = false;
                }
            }
        });
    }
}
