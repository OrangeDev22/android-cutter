package com.example.cutter.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cutter.Interface.FiltersListFragmentListener;
import com.example.cutter.R;
import com.example.cutter.adapters.ThumbnailAdapter;
import com.example.cutter.utils.ImageUtilities;
import com.example.cutter.utils.SpaceItemDecoration;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailCallback;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import java.util.ArrayList;
import java.util.List;

public class AddFilterDailog extends DialogFragment implements ThumbnailAdapter.FiltersListFragmentListener {
    private RecyclerView recyclerView;
    private List<ThumbnailItem> thumbnailItemList;
    private ThumbnailAdapter adapter;
    private Button positiveButton, negativeButton;
    private FiltersListFragmentListener listener;
    private onDialogFilterListener dialogListener;
    private boolean apply = false;
    public Bitmap bitmap;
    Filter filterSelected;
    public AddFilterDailog(List<ThumbnailItem> thumbnailItemList){
        this.thumbnailItemList = thumbnailItemList;
    }
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
        View view = inflater.inflate(R.layout.add_filter_dialog,null);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(),RecyclerView.HORIZONTAL,false);
        filterSelected = thumbnailItemList.get(0).filter;
        adapter = new ThumbnailAdapter(thumbnailItemList,this, getContext());
        positiveButton = view.findViewById(R.id.dialog_filter_positive_button);
        negativeButton = view.findViewById(R.id.dialog_filter_negative_button);
        recyclerView = view.findViewById(R.id.recycler_view_filters);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        int space = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,8,getResources().getDisplayMetrics());
        recyclerView.addItemDecoration(new SpaceItemDecoration(space));
        recyclerView.setAdapter(adapter);
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apply = false;
                dismiss();
            }
        });
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apply = true;
                dismiss();
            }
        });
        builder.setView(view)
                .setTitle("");
        return builder.create();
    }
    @Override
    public void onResume() {
        super.onResume();
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.dimAmount = 0;
        params.gravity = Gravity.BOTTOM | Gravity.CENTER;
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getContext(), R.color.colorPrimary)));
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            listener = (FiltersListFragmentListener) context;
            dialogListener = (onDialogFilterListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement FiltersListFragmentListener");
        }
    }

    @Override
    public void onFilterSelected(int position, Filter filter) {
        adapter.setSelectedFilter(position);
        filterSelected = filter;
        listener.onFilterSelected(filter);
    }
    public void setBitmap(Bitmap bitmap){
        this.bitmap = bitmap;
    }

    public interface onDialogFilterListener{
        void setFilter(Filter filter,boolean apply);
    }

    @Override
    public void onDestroyView() {
        dialogListener.setFilter(filterSelected,apply);
        super.onDestroyView();
    }
}
