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
import com.example.cutter.tools.ToolType;

import java.util.ArrayList;
import java.util.List;

public class CropToolsAdapter extends RecyclerView.Adapter<CropToolsAdapter.ViewHolder> {
    private List<CropToolsAdapter.ToolModel> toolList = new ArrayList<>();
    private CropToolsAdapter.onToolListener listener;
    private Context context;

    public CropToolsAdapter(CropToolsAdapter.onToolListener listener, Context context) {
        this.listener = listener;
        this.context = context;
        toolList.add(new CropToolsAdapter.ToolModel(context.getResources().getString(R.string.bottom_crop_menu_select_all), ToolType.SELECTALL));
        toolList.add(new CropToolsAdapter.ToolModel(1+":"+1, ToolType.ONEBYONE));
        toolList.add(new CropToolsAdapter.ToolModel(3+":"+4, ToolType.THREEBYFOUR));
        toolList.add(new CropToolsAdapter.ToolModel(4+":"+3, ToolType.FOURBYTHREE));
        toolList.add(new CropToolsAdapter.ToolModel(5+":"+4, ToolType.FIVEBYFOUR));
        toolList.add(new CropToolsAdapter.ToolModel(16+":"+9, ToolType.SIXTEENBYNINE));
        toolList.add(new CropToolsAdapter.ToolModel(9+":"+16, ToolType.NINEBYSIXTEEN));

    }

    @NonNull
    @Override
    public CropToolsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cropper_menu_item,parent,false);
        return new CropToolsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CropToolsAdapter.ViewHolder holder, int position) {
        CropToolsAdapter.ToolModel item = toolList.get(position);
        holder.textView.setText(item.toolName);
    }

    @Override
    public int getItemCount() {
        return toolList.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder{
        protected ImageView toolIcon;
        protected TextView textView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.txtToolCropper);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onToolSelected(toolList.get(getLayoutPosition()).toolType);
                }
            });
        }
    }
    public interface onToolListener{
        void onToolSelected(ToolType toolType);
    }
    class ToolModel {
        private String toolName;
        private ToolType toolType;

        ToolModel(String toolName, ToolType toolType) {
            this.toolName = toolName;
            this.toolType = toolType;
        }

    }
}

