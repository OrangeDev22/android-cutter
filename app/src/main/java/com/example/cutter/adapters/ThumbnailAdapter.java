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
import com.example.cutter.R;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;

import java.util.List;

public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ViewHolder> {
    private static final String TAG = "THUMBNAILS_ADAPTER";
    private List <ThumbnailItem> thumbnailItems;
    public FiltersListFragmentListener listener;
    private Context context;

    private int selectedIndext = 0;

    public ThumbnailAdapter(List<ThumbnailItem> thumbnailItems, FiltersListFragmentListener listener, Context context) {
        this.thumbnailItems = thumbnailItems;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.thubmnail_item,parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.v(TAG, "On Bind View Called");
        final ThumbnailItem thumbnailItem = thumbnailItems.get(position);
        BitmapDrawable drawable = new BitmapDrawable(context.getResources(), thumbnailItem.image);
        //holder.thumbnail.setScaleType(ImageView.ScaleType.FIT_START);
        holder.filter_name.setText(thumbnailItem.filterName);
        holder.thumbnail.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                listener.onFilterSelected(position,thumbnailItems.get(position).filter);
                //notifyDataSetChanged();
            }
        });
        if(selectedIndext == position){
            holder.filter_name.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
            holder.thumbnail.setBackground(drawable);
        }
        else{
            holder.filter_name.setTextColor(ContextCompat.getColor(context, R.color.white));
            holder.thumbnail.setBackground(drawable);
        }
    }

    @Override
    public int getItemCount() {
        return thumbnailItems.size();
    }
    public interface OnItemSelected{
        void onFilterSelected(int position);
    }
    public  class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView thumbnail;
        TextView filter_name;
        public ViewHolder(View itemView){
            super(itemView);
            thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
            filter_name = (TextView)itemView.findViewById(R.id.filter_name);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }
    public void setSelectedFilter(int position){
        selectedIndext = position;
        notifyDataSetChanged();
    }
    public interface  FiltersListFragmentListener  {
        void onFilterSelected(int position, Filter filter);
    }
}
