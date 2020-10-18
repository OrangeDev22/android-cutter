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

public class EditToolsAdapter extends RecyclerView.Adapter<EditToolsAdapter.ViewHolder> {
    private List<ToolModel> toolList = new ArrayList<>();
    private onToolListener listener;
    private Context context;

    public EditToolsAdapter(onToolListener listener, Context context) {
        this.listener = listener;
        this.context = context;
        toolList.add(new ToolModel(context.getResources().getString(R.string.edit_bottom_menu_frame_color),R.drawable.color_circle,ToolType.COLOR));
        toolList.add(new ToolModel(context.getResources().getString(R.string.edit_bottom_menu_sticker),R.drawable.sticker,ToolType.STICKER));
        toolList.add(new ToolModel(context.getResources().getString(R.string.edit_bottom_menu_Text),R.drawable.ic_baseline_text_fields_24,ToolType.TEXT));
        toolList.add(new ToolModel(context.getResources().getString(R.string.edit_bottom_menu_add_frame),R.drawable.art_frame,ToolType.FRAME));
        toolList.add(new ToolModel(context.getResources().getString(R.string.edit_bottom_menu_add_filter),R.drawable.filer,ToolType.FILTER));
        toolList.add(new ToolModel(context.getResources().getString(R.string.edit_bottom_menu_add_brightness),R.drawable.brightness,ToolType.BRIGHTNESS));
        toolList.add(new ToolModel(context.getResources().getString(R.string.edit_bottom_menu_add_contrast),R.drawable.contrast,ToolType.CONTRAST));
        toolList.add(new ToolModel(context.getResources().getString(R.string.edit_bottom_menu_add_saturation),R.drawable.saturation,ToolType.SATURATION));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.editor_tools_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ToolModel item = toolList.get(position);
        holder.textView.setText(item.toolName);
        holder.toolIcon.setImageResource(item.toolIcon);
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
            toolIcon = itemView.findViewById(R.id.imgToolIcon);
            textView = itemView.findViewById(R.id.txtTool);
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
        private int toolIcon;
        private ToolType toolType;

        ToolModel(String toolName, int toolIcon, ToolType toolType) {
            this.toolName = toolName;
            this.toolIcon = toolIcon;
            this.toolType = toolType;
        }

    }
}
