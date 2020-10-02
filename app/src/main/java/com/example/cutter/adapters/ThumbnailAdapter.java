package com.example.cutter.adapters;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.nfc.Tag;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cutter.FilterListFragment;
import com.example.cutter.Interface.FiltersListFragmentListener;
import com.example.cutter.R;
import com.zomato.photofilters.utils.ThumbnailItem;

import java.util.List;

public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.MyViewHolder> {
    private static final String TAG = "THUMBNAILS_ADAPTER";
    private List <ThumbnailItem> thumbnailItems;
    private FiltersListFragmentListener listener;
    private Context context;
    private int selectedIndext = 0;

    public ThumbnailAdapter(List<ThumbnailItem> thumbnailItems, FiltersListFragmentListener listener, Context context) {
        this.thumbnailItems = thumbnailItems;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.thubmnail_item,parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        Log.v(TAG, "On Bind View Called");
        final ThumbnailItem thumbnailItem = thumbnailItems.get(position);
        BitmapDrawable drawable = new BitmapDrawable(context.getResources(), thumbnailItem.image);
        holder.thumbnail.setBackground(drawable);
        //holder.thumbnail.setScaleType(ImageView.ScaleType.FIT_START);

        holder.thumbnail.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                listener.onFilterSelected(thumbnailItem.filter);
                selectedIndext = position;
                notifyDataSetChanged();
            }
        });
        holder.filter_name.setText(thumbnailItem.filterName);
        if(selectedIndext == position){
            holder.filter_name.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        }
        else{
            holder.filter_name.setTextColor(ContextCompat.getColor(context, R.color.white));
        }
    }

    @Override
    public int getItemCount() {
        return thumbnailItems.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView thumbnail;
        TextView filter_name;
        public MyViewHolder(View itemView){
            super(itemView);
            thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
            filter_name = (TextView)itemView.findViewById(R.id.filter_name);
        }
    }
}
