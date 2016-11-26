package sudoalgorithm.visahackathon;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by kunalmalhotra on 11/26/16.
 */

public class RVAdapter extends RecyclerView.Adapter<ViewHolderRV> {

    private Integer[] imgDataSet1;
    private Integer[] imgDataSet2;

    public RVAdapter(Integer[] imgDataSet1, Integer[] imgDataSet2){
        this.imgDataSet1 = imgDataSet1;
        this.imgDataSet2 = imgDataSet2;
    }


    @Override
    public ViewHolderRV onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_layout, parent, false);
        ViewHolderRV vh = new ViewHolderRV(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolderRV holder, int position) {
        holder.mImageView1.setImageResource(imgDataSet1[position]);
        holder.mImageView2.setImageResource(imgDataSet2[position]);

    }

    @Override
    public int getItemCount() {
        return imgDataSet1.length;
    }
}
