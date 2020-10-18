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
    private List<ThumbnailItem> list;
    private ThumbnailAdapter adapter;
    private Button positiveButton, negativeButton;
    private FiltersListFragmentListener listener;
    private onDialogFilterListener dialogListener;
    private boolean apply = false;
    public Bitmap bitmap;
    Filter filterSelected;
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
        list = new ArrayList<>();
        displayThumbnail(bitmap);
        Log.e("adapter_size",adapter.getItemCount()+"");
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
                //filterSelected = list.get(0).filter;
                //dialogListener.setFilter(filterSelected);
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
    public void displayThumbnail(final Bitmap bitmap){

        Runnable r = new Runnable() {
            @Override
            public void run() {

            }
        };
        Log.e("filter list","on display");
        Bitmap thumbImg;
        int width = (int) (bitmap.getWidth()*0.6);
        int height = (int)(bitmap.getHeight()*0.6);
        float dp = 80;
        //Resources r = getResources();

        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
        thumbImg = ImageUtilities.createSquaredBitmap(bitmap,width);
        //thumbImg = ImageUtilities.getResizedBitmap(bitmap,width,height);

        Log.e("thumb_nails_dims",width+"x"+height);
        ThumbnailsManager.clearThumbs();
        //list.clear();
        ThumbnailItem thumbnailItem = new ThumbnailItem();
        thumbnailItem.image = thumbImg;
        thumbnailItem.filterName = "normal";
        ThumbnailsManager.addThumb(thumbnailItem);
        List<Filter> filters = FilterPack.getFilterPack(getContext());
        for(Filter filter:filters){
            Log.e("puta","la que te pario"+filters.size());
            ThumbnailItem tI = new ThumbnailItem();
            tI.image = thumbImg;
            tI.filter = filter;
            tI.filterName = filter.getName();
            ThumbnailsManager.addThumb(tI);
        }

        list.addAll(ThumbnailsManager.processThumbs(getActivity()));
        Log.e("filters_size",list.size()+"");
        /*getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });*/
        filterSelected = list.get(0).filter;
        adapter = new ThumbnailAdapter(list,this, getContext());
        //new Thread(r).start();
    }
    public interface onDialogFilterListener{
        void setFilter(Filter filter,boolean apply);
    }

    @Override
    public void onDestroyView() {
        //filterSelected = list.get(0).filter;
        dialogListener.setFilter(filterSelected,apply);
        super.onDestroyView();
    }
}
