package sudoalgorithm.visahackathon;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by kunalmalhotra on 11/26/16.
 */

public class ViewHolderRV extends RecyclerView.ViewHolder {
    public ImageView mImageView1, mImageView2;
    public ViewHolderRV(View itemView) {
        super(itemView);
        mImageView1 = (ImageView) itemView.findViewById(R.id.imageView1);
        mImageView2 = (ImageView) itemView.findViewById(R.id.imageView2);
    }
}
