package com.example.cutter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cutter.R;


import java.util.ArrayList;
import java.util.List;

public class TextToolsAdapter extends RecyclerView.Adapter<TextToolsAdapter.ViewHolder> {

    private List<ToolModel> mToolList = new ArrayList<>();
    private OnItemSelected mOnItemSelected;

    private int index=-1;
    public TextToolsAdapter(OnItemSelected onItemSelected, Context context) {
        mOnItemSelected = onItemSelected;
        mToolList.add(new ToolModel("TEXT", R.drawable.ic_baseline_text_fields_24, "TEXT"));
        mToolList.add(new ToolModel("FONTS", R.drawable.fonts, "FONTS"));
        mToolList.add(new ToolModel("COLORS", R.drawable.paint_can, "COLOR"));
        mToolList.add(new ToolModel("STYLE", R.drawable.typography,"STYLE"));
        mToolList.add(new ToolModel("", R.drawable.ic_baseline_check_circle_outline_24, "DONE"));
    }

    public interface OnItemSelected {
        void onToolSelected(String toolType);
    }

    class ToolModel {
        private String mToolName;
        private int mToolIcon;
        private String mToolType;

        ToolModel(String toolName, int toolIcon, String toolType) {
            mToolName = toolName;
            mToolIcon = toolIcon;
            mToolType = toolType;
        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.text_tools_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //holder.selector.setVisibility(View.GONE);
        ToolModel item = mToolList.get(position);
        holder.txtTool.setText(item.mToolName);
        holder.imgToolIcon.setImageResource(item.mToolIcon);
        //if text tool is selected set triangle visible
        //if (index == position)holder.selector.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return mToolList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgToolIcon;
        TextView txtTool;
        ViewHolder(View itemView) {
            super(itemView);
            imgToolIcon = itemView.findViewById(R.id.imgToolIcon);
            txtTool = itemView.findViewById(R.id.txtTool);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemSelected.onToolSelected(mToolList.get(getLayoutPosition()).mToolType);
                }
            });
        }
    }
    //Method to set the selected tool
    public void setSelectedTool (int position){
        index = position;
        notifyDataSetChanged();
    }
}

