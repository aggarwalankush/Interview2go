package com.aggarwalankush.interview2go;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.aggarwalankush.interview2go.data.InterviewContract.InterviewEntry;

public class SolutionActivity extends AppCompatActivity {

    private static Uri mUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solution);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mUri = getIntent().getData();
        String question = InterviewEntry.getQuestionFromUri(mUri);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(question);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        if (Utility.isDarkMode(this)) {
            mViewPager.setBackgroundColor(ContextCompat.getColor(this,R.color.dark_mode_color));
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return SolutionFragment.newInstance(mUri, getSectionNameFromPosition(position));
        }

        public String getSectionNameFromPosition(int position) {
            switch (position) {
                case 0:
                    return SolutionFragment.SOLUTION;
                case 1:
                    return SolutionFragment.OUTPUT;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return SolutionFragment.SOLUTION;
                case 1:
                    return SolutionFragment.OUTPUT;
            }
            return null;
        }
    }
}
