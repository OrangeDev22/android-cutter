 package com.example.cutter;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.cutter.Interface.FiltersListFragmentListener;
import com.example.cutter.adapters.ThumbnailAdapter;
import com.example.cutter.utils.BitmapUtils;
import com.example.cutter.utils.ImageUtilities;
import com.example.cutter.utils.SpaceItemDecoration;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import java.util.ArrayList;
import java.util.List;

 public class FilterListFragment extends Fragment implements  ThumbnailAdapter.FiltersListFragmentListener {
    RecyclerView recyclerView;
    ThumbnailAdapter adapter;
    List<ThumbnailItem> thumbnailItems;
    FiltersListFragmentListener listener;
    public void setListener(FiltersListFragmentListener listener){
        this.listener = listener;
    }
    public FilterListFragment(){

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_filter_list, container, false);
        thumbnailItems = new ArrayList<>();

        adapter = new ThumbnailAdapter(thumbnailItems, this,getActivity());
        recyclerView = itemView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL,false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        int space = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,8,getResources().getDisplayMetrics());
        recyclerView.addItemDecoration(new SpaceItemDecoration(space));
        recyclerView.setAdapter(adapter);

        Log.e("filter_list","before display");
        displayThumbnail(ImageUtilities.decodeImage(FiltersActivity.picturePath));
        return itemView;
    }
    public void displayThumbnail(final Bitmap bitmap){

        Runnable r = new Runnable() {
            @Override
            public void run() {
                Log.e("filter list","on display");
                Bitmap thumbImg;
                int width = (int) (bitmap.getWidth()*0.6);
                int height = (int)(bitmap.getHeight()*0.6);
                float dp = 80;
                Resources r = getResources();

                float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
                thumbImg = ImageUtilities.createSquaredBitmap(bitmap,(int) width);
               //thumbImg = ImageUtilities.getResizedBitmap(bitmap,width,height);
                if(thumbImg == null)
                    return;
                Log.e("thumb_nails_dims",width+"x"+height);
                ThumbnailsManager.clearThumbs();
                thumbnailItems.clear();
                ThumbnailItem thumbnailItem = new ThumbnailItem();
                thumbnailItem.image = thumbImg;
                thumbnailItem.filterName = "normal";
                ThumbnailsManager.addThumb(thumbnailItem);
                List<Filter> filters = FilterPack.getFilterPack(getActivity());
                for(Filter filter:filters){

                    ThumbnailItem tI = new ThumbnailItem();
                    tI.image = thumbImg;
                    tI.filter = filter;
                    tI.filterName = filter.getName();
                    ThumbnailsManager.addThumb(tI);
                }
                thumbnailItems.addAll(ThumbnailsManager.processThumbs(getActivity()));
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        };

        new Thread(r).start();
    }

     @Override
     public void onFilterSelected(int position,Filter filter) {
         adapter.setSelectedFilter(position);
         listener.onFilterSelected(filter);
        if(listener!= null){

        }
     }


 }