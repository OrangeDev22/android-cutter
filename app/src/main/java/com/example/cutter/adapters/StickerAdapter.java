package com.example.cutter.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cutter.R;
import java.io.IOException;
import java.util.List;

public class StickerAdapter extends RecyclerView.Adapter<StickerAdapter.ViewHolder> {
    private onStickerListener listener;
    private List<String> list;
    private Context context;

    public StickerAdapter(onStickerListener listener, List<String> list, Context context) {
        this.listener = listener;
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sticker_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Drawable drawable = null;
        try {
            drawable = Drawable.createFromStream(context.getAssets().open(list.get(position)), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        holder.imageViewStickerIcon.setBackground(drawable);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    public interface onStickerListener{
        void onStickerSelected(int position);
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageViewStickerIcon;
        ViewHolder(View itemView){
            super(itemView);
            imageViewStickerIcon = itemView.findViewById(R.id.imgStickerIcon);
            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    listener.onStickerSelected(getLayoutPosition());
                }
            });
        }
    }
}
