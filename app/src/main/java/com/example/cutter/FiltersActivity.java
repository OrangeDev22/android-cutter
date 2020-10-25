package com.example.cutter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.Toast;

import com.example.cutter.Interface.EditFragnentListener;
import com.example.cutter.Interface.FiltersListFragmentListener;
import com.example.cutter.adapters.ViewPagerAdapter;
import com.example.cutter.utils.BitmapUtils;
import com.example.cutter.utils.ImageUtilities;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter;

import java.util.List;

public class FiltersActivity extends AppCompatActivity implements FiltersListFragmentListener , EditFragnentListener {
    //private StickerView stickerView;
    public static String picturePath = "";
    ImageView img_preview;
    TabLayout tabLayout;
    ViewPager viewPager;
    CoordinatorLayout coordinatorLayout;
    public static Bitmap oroginalBitmap, filteredBitmap, finalBitmap;
    FilterListFragment filterListFragment;
    EditImageFragment editImageFragment;
    int brightnessFinal = 0;
    float saturationFinal = 1.0f;
    float constrantFinal = 1.0f;
    static{
        System.loadLibrary("NativeImageProcessor");
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters);
        //stickerView = findViewById(R.id.sticker_view);
        init();

    }

    private void init(){
        picturePath = getIntent().getExtras().getString("bitmap_CropActivity");
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setTitle("FUCK YOU TUTORIAL");
        img_preview = findViewById(R.id.image_preview);
        tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewpager);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);
        setupViewPager(viewPager);
        //tabLayout.setSelectedTabIndicatorColor(R.color.white);
        tabLayout.setTabTextColors(Color.parseColor("#FFFFFF"),Color.parseColor("#BB86FC"));
        tabLayout.setupWithViewPager(viewPager);
        /*WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screen_width = metrics.widthPixels;
        int screen_height = displayMetrics.heightPixels;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(screen_width,(int)(screen_height*0.5));
        img_preview.setLayoutParams(layoutParams);*/
        loadImage();
    }

    private void loadImage() {

        oroginalBitmap = ImageUtilities.decodeImage(picturePath);
        filteredBitmap = oroginalBitmap.copy(Bitmap.Config.ARGB_8888,true);
       // filteredBitmap = Bitmap.createScaledBitmap(oroginalBitmap,(int) (oroginalBitmap.getWidth()*0.5),(int) (oroginalBitmap.getHeight()*0.5),false);
        finalBitmap = oroginalBitmap.copy(Bitmap.Config.ARGB_8888,true);
        img_preview.setImageBitmap(filteredBitmap);
        //filterListFragment.displayThumbnail(oroginalBitmap);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        filterListFragment = new FilterListFragment();
        filterListFragment.setListener(this);
        editImageFragment = new EditImageFragment();
        editImageFragment.setListener(this);
        adapter.addFragment(filterListFragment,"Filters");
        adapter.addFragment(editImageFragment,"EDIT");
        viewPager.setAdapter(adapter);

    }

    @Override
    public void onBrightnessChanged(int brightness) {
        brightnessFinal = brightness;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new BrightnessSubFilter(brightness));
        img_preview.setImageBitmap(myFilter.processFilter(filteredBitmap.copy(Bitmap.Config.ARGB_8888,true)));
    }

    @Override
    public void onSaturationChanged(float saturation) {
        saturationFinal = saturation;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new SaturationSubfilter(saturation));
        img_preview.setImageBitmap(myFilter.processFilter(filteredBitmap.copy(Bitmap.Config.ARGB_8888,true)));
    }

    @Override
    public void onContrastChanged(float contrast) {
        constrantFinal = contrast;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new ContrastSubFilter(contrast));
        img_preview.setImageBitmap(myFilter.processFilter(filteredBitmap.copy(Bitmap.Config.ARGB_8888,true)));
    }

    @Override
    public void onEditStarted() {

    }

    @Override
    public void onEditCompleted() {
        Bitmap bitmap = filteredBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new BrightnessSubFilter(brightnessFinal));
        myFilter.addSubFilter(new SaturationSubfilter(saturationFinal));
        myFilter.addSubFilter(new ContrastSubFilter(constrantFinal));

        finalBitmap = myFilter.processFilter(bitmap);
    }

    @Override
    public void onFilterSelected(Filter filter) {
        resetControl();
        filteredBitmap = oroginalBitmap.copy(Bitmap.Config.ARGB_8888,true);
        img_preview.setImageBitmap(filter.processFilter(filteredBitmap));
        finalBitmap = filteredBitmap.copy(Bitmap.Config.ARGB_8888,true);
    }

    private void resetControl() {
        if(editImageFragment != null){
            editImageFragment.resetControls();
        }
        brightnessFinal = 0;
        saturationFinal = 1.0f;
        constrantFinal = 1.0f;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_filter_editor,menu);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_save){
            Intent intent = new Intent(FiltersActivity.this,EditActivity.class);
            finalBitmap = ImageUtilities.trim(filteredBitmap);
            //String ImagePath = ImageUtilities.encodeImage(finalBitmap, Bitmap.CompressFormat.PNG,100);
            //intent.putExtra("bitmap_FilterActivity",ImagePath);
            startActivity(intent);
            //saveImageToGallery();
            //ImageUtilities.saveAsFile(".png",filteredBitmap,FiltersActivity.this);
            this.finish();
            //saveImageToGallery();
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveImageToGallery() {
       /* Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener(){

                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if(report.areAllPermissionsGranted()){
                            final String path = BitmapUtils.insertImage(getContentResolver(),finalBitmap,System.currentTimeMillis()+".jpg",null);
                            if(!TextUtils.isEmpty(path)){
                                Toast.makeText(FiltersActivity.this,"se guardo puto",Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(FiltersActivity.this,"No se pudo guardar wey",Toast.LENGTH_SHORT).show();
                            }
                        }
                        else{
                            Toast.makeText(FiltersActivity.this,"Permission no se pudo",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                });*/
        final String path = BitmapUtils.insertImage(getContentResolver(),finalBitmap,System.currentTimeMillis()+".jpg",null);
        Log.e("Image_save_status",path);
        if(!TextUtils.isEmpty(path)){
            Toast.makeText(FiltersActivity.this,"se guardo puto",Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(FiltersActivity.this,"No se pudo guardar wey",Toast.LENGTH_SHORT).show();
        }
    }

}