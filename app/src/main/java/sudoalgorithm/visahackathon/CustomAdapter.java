package sudoalgorithm.visahackathon;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by kunalmalhotra on 11/26/16.
 */

public class CustomAdapter extends BaseAdapter {

    private Context context;
    private List<ImageData> rowItems;
    private Button btn;

    public CustomAdapter(Context context, List<ImageData> items) {
        this.context = context;
        rowItems = items;
    }

    private class ViewHolder {
        ImageView imageView1,imageView2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        LayoutInflater mInflater = (LayoutInflater)
                context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.cardview_layout, null);
            holder = new ViewHolder();
            holder.imageView1 = (ImageView) convertView.findViewById(R.id.imageView1);
            holder.imageView2 = (ImageView) convertView.findViewById(R.id.imageView2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ImageData rowItem = (ImageData) getItem(position);


        holder.imageView1.setImageResource(rowItem.getImage1());
        holder.imageView2.setImageResource(rowItem.getImage2());

        return convertView;
    }



    @Override
    public int getCount() {
        return rowItems.size();
    }

    @Override
    public Object getItem(int position) {
        return rowItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return rowItems.indexOf(getItem(position));
    }
}
