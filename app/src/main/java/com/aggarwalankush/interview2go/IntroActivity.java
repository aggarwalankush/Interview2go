package com.aggarwalankush.interview2go;

import android.graphics.Color;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

public class IntroActivity extends AppIntro2 {


    @Override
    public void init(Bundle savedInstanceState) {

        addSlide(SampleSlide.newInstance(R.layout.intro));
        addSlide(AppIntroFragment.newInstance("Dark Mode", "Enable Dark Mode from Settings\n", R.drawable.dark_mode, Color.parseColor("#311759")));
        addSlide(AppIntroFragment.newInstance("Light Mode", "Disable Dark Mode from Settings\n", R.drawable.light_mode, Color.parseColor("#311759")));
        addSlide(AppIntroFragment.newInstance("Done Question", "Save question in Done list by sliding it to right\n", R.drawable.slide_right_done, Color.parseColor("#311759")));
        addSlide(AppIntroFragment.newInstance("Bookmark Question", "Save question in Bookmark list by sliding it to left\n", R.drawable.slide_left_bookmark, Color.parseColor("#311759")));
    }


    @Override
    public void onDonePressed() {
        finish();
    }

    @Override
    public void onNextPressed() {

    }

    @Override
    public void onSlideChanged() {

    }

}
