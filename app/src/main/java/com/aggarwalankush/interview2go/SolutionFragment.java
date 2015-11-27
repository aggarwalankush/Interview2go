package com.aggarwalankush.interview2go;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aggarwalankush.interview2go.data.InterviewContract.InterviewEntry;

public class SolutionFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = SolutionFragment.class.getSimpleName();

    static final String QUESTION_URI = "QUESTION_URI";
    static final String SECTION_NAME = "SECTION_NAME";
    private static final String SHARE_HASHTAG = "#Interview2go-App";
    private String mShareString;
    private Uri mUri;
    private String mSectionName;

    static final String SOLUTION = "SOLUTION";
    static final String OUTPUT = "OUTPUT";


    private static final int SOL_OUT_LOADER = 0;

    private static final String[] SOL_OUT_COLUMNS = {
            InterviewEntry._ID,
            InterviewEntry.COLUMN_SOLUTION,
            InterviewEntry.COLUMN_OUTPUT
    };

    public static final int COL_ID = 0;
    public static final int COL_SOLUTION = 1;
    public static final int COL_OUTPUT = 2;

    private TextView mSectionView;

    public SolutionFragment() {
        setHasOptionsMenu(true);
    }

    public static SolutionFragment newInstance(Uri questionUri, String sectionName) {
        SolutionFragment fragment = new SolutionFragment();
        Bundle args = new Bundle();
        args.putParcelable(QUESTION_URI, questionUri);
        args.putString(SECTION_NAME, sectionName);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(SolutionFragment.QUESTION_URI);
            mSectionName = arguments.getString(SolutionFragment.SECTION_NAME);
        }
        View rootView = inflater.inflate(R.layout.fragment_solution, container, false);
        mSectionView = (TextView) rootView.findViewById(R.id.tv_section);
        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (getActivity() instanceof SolutionActivity) {
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.menu_solution, menu);
            finishCreatingMenu(menu);
        }
    }

    private void finishCreatingMenu(Menu menu) {
        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);
        menuItem.setIntent(createShareForecastIntent());
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mShareString + "\n" + SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(SOL_OUT_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    SOL_OUT_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {

            String solution = cursor.getString(COL_SOLUTION);
            String output = cursor.getString(COL_OUTPUT);

            switch (mSectionName) {
                case SOLUTION:
                    mSectionView.setText(solution);
                    break;
                case OUTPUT:
                    mSectionView.setText(output);
                    break;
                default:
                    mSectionView.setText(getContext().getString(R.string.empty_section));
            }

            String topic = InterviewEntry.getTopicFromUri(mUri);
            String question = InterviewEntry.getQuestionFromUri(mUri);

            mShareString =
                    String.format(getContext().getString(R.string.share_format),
                            topic, question, solution, output);

            // reset the menu in case it was created before data loading
            Toolbar toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
            if (null != toolbar) {
                Menu menu = toolbar.getMenu();
                if (null != menu) menu.clear();
                toolbar.inflateMenu(R.menu.menu_solution);
                finishCreatingMenu(toolbar.getMenu());
            }

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}