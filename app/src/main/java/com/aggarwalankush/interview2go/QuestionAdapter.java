package com.aggarwalankush.interview2go;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aggarwalankush.interview2go.QuestionAdapter.QuestionAdapterViewHolder;
import com.aggarwalankush.interview2go.data.InterviewContract.InterviewEntry;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapterViewHolder> {

    private Cursor mCursor;
    final private Context mContext;
    final private QuestionAdapterOnClickHandler mClickHandler;
    final private View mEmptyView;

    public QuestionAdapter(Context context, QuestionAdapterOnClickHandler clickHandler, View emptyView) {
        mContext = context;
        mClickHandler = clickHandler;
        mEmptyView = emptyView;
    }

    public interface QuestionAdapterOnClickHandler {
        void onClick(String question, QuestionAdapterViewHolder vh);
    }

    public class QuestionAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView mQuestionView;

        public QuestionAdapterViewHolder(View view) {
            super(view);
            mQuestionView = (TextView) view.findViewById(R.id.tv_question);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int question_index = mCursor.getColumnIndex(InterviewEntry.COLUMN_QUESTION);
            mClickHandler.onClick(mCursor.getString(question_index), this);
        }
    }


    @Override
    public QuestionAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewGroup instanceof RecyclerView) {
            int layoutId = R.layout.list_item_question;
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
            view.setFocusable(true);
            return new QuestionAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(QuestionAdapterViewHolder questionAdapterViewHolder, int position) {
        mCursor.moveToPosition(position);
        String question = mCursor.getString(QuestionActivityFragment.COL_QUESTION);
        questionAdapterViewHolder.mQuestionView.setText(question);
    }


    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

}
