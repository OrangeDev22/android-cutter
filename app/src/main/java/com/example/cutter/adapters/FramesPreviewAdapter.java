package com.example.cutter.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cutter.R;

import java.util.List;

public class FramesPreviewAdapter extends RecyclerView.Adapter<FramesPreviewAdapter.ViewHolder> {
    private List<Bitmap> previews;
    private OnItemSelected onItemSelected;
    private Context context;
    public FramesPreviewAdapter(OnItemSelected onItemSelected, List<Bitmap> previews, Context context){
        this.onItemSelected = onItemSelected;
        this.previews = previews;
        this.context = context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.frame_adapter_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), previews.get(position));
        holder.imageView.setBackground(bitmapDrawable);
        holder.imageViewRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemSelected.onRemoveButtonSelected(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return previews.size();
    }
    public interface OnItemSelected{
        void onPreviewSelected(int position);
        void onRemoveButtonSelected(int position);
    }
    class ViewHolder extends  RecyclerView.ViewHolder{
        ImageView imageView;
        ImageView imageViewRemove;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageview_preview_frame);
            imageViewRemove = itemView.findViewById(R.id.imageview_delete_preview);

        }
    }
}
