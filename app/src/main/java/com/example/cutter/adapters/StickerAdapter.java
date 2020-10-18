package com.example.cutter.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.TimedText;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cutter.R;
import com.example.cutter.utils.BitmapUtils;

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
        Bitmap bitmap = BitmapUtils.getBitmapFromAsset(context, list.get(position));
        //int rawId = context.getResources().getIdentifier(list.get(position),"raw",context.getPackageName());
        /*String resourcePath = "android.resource://" + context.getPackageName() + "/drawable/" + list.get(position) ;
        Uri uri = Uri.parse(resourcePath);
        Picasso.get()
                .load(uri)
                .resize(80, 80)
                .centerCrop()
                .into(holder.imageView);*/
        /*bitmap =BitmapUtils.scaleBitmap(bitmap,(int)(bitmap.getWidth()*0.5),(int)(bitmap.getHeight()*0.5));
        BitmapDrawable drawable = new BitmapDrawable(context.getResources(),bitmap);
        holder.imageView.setImageBitmap(bitmap);*/
        Drawable drawable = null;
        try {
            drawable = Drawable.createFromStream(context.getAssets().open(list.get(position)), null);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        holder.imageView.setBackground(drawable);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    public interface onStickerListener{
        void onStickerSelected(int position);
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        ViewHolder(View itemView){
            super(itemView);
            imageView = itemView.findViewById(R.id.imgStickerIcon);
            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    listener.onStickerSelected(getLayoutPosition());
                }
            });
        }
    }
}
