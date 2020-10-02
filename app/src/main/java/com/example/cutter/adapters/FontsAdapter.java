package com.example.cutter.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cutter.R;

import java.util.List;

public class FontsAdapter extends RecyclerView.Adapter<FontsAdapter.ViewHolder> {

    private List<Typeface> mTypefaces;
    private OnItemSelected mOnItemSelected;
    private Context mContext;
    int index;
    public FontsAdapter(OnItemSelected onItemSelected, List<Typeface> typeFaces, Context context) {
        mOnItemSelected = onItemSelected;
        mTypefaces = typeFaces;
        mContext = context;

    }

    public interface OnItemSelected {
        void onFontSelected(int position);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.font_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.txtTool.setText("Logo Maker Font");
        holder.txtTool.setTypeface(mTypefaces.get(position));
        holder.txtTool.setBackgroundColor(ContextCompat.getColor(mContext, R.color.background));
        holder.txtTool.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        //If font is selected then background and text color will change
        if (index == position){
            Log.d("Font Position", index+"");
            holder.txtTool.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorAccent));
            holder.txtTool.setTextColor(ContextCompat.getColor(mContext, R.color.black));
        }
    }
    //Method Called to change the poisition of the selected font
    public void setSelectedFont (int position){
        index = position;
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return mTypefaces.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTool;
        ViewHolder(View itemView) {
            super(itemView);
            txtTool = itemView.findViewById(R.id.font_textView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemSelected.onFontSelected(getLayoutPosition());
                }
            });
        }
    }
}


