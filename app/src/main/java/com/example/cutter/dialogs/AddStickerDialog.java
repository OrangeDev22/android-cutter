package com.example.cutter.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cutter.R;
import com.example.cutter.adapters.StickerAdapter;

import java.util.ArrayList;
import java.util.List;

public class AddStickerDialog extends DialogFragment implements StickerAdapter.onStickerListener {
    private RecyclerView recyclerView;
    private onStickerListener listener;
    private List<String> stickerPaths;
    private StickerAdapter stickerAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.fragmentNavBarColor);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_sticker_dialog,null);
        createAssetsPaths();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(),RecyclerView.HORIZONTAL,false);
        recyclerView = view.findViewById(R.id.sticker_recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(stickerAdapter);
        builder.setView(view)
                .setTitle("");
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getDialog().getWindow().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.dimAmount = 0;
        params.gravity = Gravity.BOTTOM | Gravity.CENTER;
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getContext(),R.color.colorPrimary)));
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (AddStickerDialog.onStickerListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement onStickerListener");
        }
    }

    @Override
    public void onStickerSelected(int position) {
        listener.onStickerSelected(stickerPaths.get(position));
        dismiss();
    }

    public interface onStickerListener {
        void onStickerSelected(String fileName);
    }
    private void createAssetsPaths(){
        stickerPaths = new ArrayList<>();
        for (int i = 1; i<=47;i++){
            stickerPaths.add("sticker_"+i+".webp");
        }
        stickerAdapter = new StickerAdapter(this,stickerPaths,getContext());
    }

}
