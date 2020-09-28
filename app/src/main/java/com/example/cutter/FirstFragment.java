package com.example.cutter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cutter.views.DrawView;


public class FirstFragment extends Fragment {
    private DrawView drawView;
    private FragmentAListener listener;
    public interface  FragmentAListener{

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_first, container,false);
        drawView = v.findViewById(R.id.im_crop_image);
        return v;
    }


}