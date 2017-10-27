package com.example.ftkj.zoomimageview;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ViewPager mVp;
    private List<Integer> mImages;
    private MyPagerAdapter mMyPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVp = (ViewPager) findViewById(R.id.ac_vp);
        mImages = new ArrayList<>();
        mMyPagerAdapter = new MyPagerAdapter(this,mImages);
        mVp.setAdapter(mMyPagerAdapter);
        mImages.add(R.mipmap.beauty00);
        mImages.add(R.mipmap.beauty01);
        mImages.add(R.mipmap.beauty02);
        mImages.add(R.mipmap.beauty03);
        mImages.add(R.mipmap.beauty04);
        mMyPagerAdapter.notifyDataSetChanged();

    }


    class MyPagerAdapter extends PagerAdapter {
        private List<Integer> mList;
        private Context mContext;

        public MyPagerAdapter(Context context, List<Integer> list) {
            mList = list;
            mContext = context;

        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(((View) object));
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ZoomImageView zoomImageView = new ZoomImageView(mContext);
            zoomImageView.setImageResource(mList.get(position));
            container.addView(zoomImageView);
            return zoomImageView;
        }
    }

}
