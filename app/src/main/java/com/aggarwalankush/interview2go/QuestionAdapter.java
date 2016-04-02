package com.aggarwalankush.interview2go;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
        String questionDetail = mCursor.getString(QuestionActivityFragment.COL_QUESTION_DETAIL);
        String[] str = questionDetail.split("\n");
        questionAdapterViewHolder.mQuestionDetailView.setLines(str.length + 2);
        questionAdapterViewHolder.mQuestionDetailView.setText(questionDetail);
        String topic = mCursor.getString(QuestionActivityFragment.COL_TOPIC);
        questionAdapterViewHolder.mIconView.setImageResource(Utility.getImageResouce(topic));
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


    public interface QuestionAdapterOnClickHandler {
        void onClick(String question, QuestionAdapterViewHolder vh);
    }

    public class QuestionAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView mQuestionView;
        public final ImageView mIconView;
        public final ImageView mQuestionIconView;
        public final TextView mQuestionDetailView;
        public final LinearLayout mQuestionDetailLinearLayout;
        public final FrameLayout mQuestionIconLayout;

        public QuestionAdapterViewHolder(View view) {
            super(view);
            mIconView = (ImageView) view.findViewById(R.id.list_item_icon);
            mQuestionIconView = (ImageView) view.findViewById(R.id.question_expand_collapse);
            mQuestionView = (TextView) view.findViewById(R.id.tv_question);
            mQuestionDetailView = (TextView) view.findViewById(R.id.tv_question_detail);
            mQuestionDetailLinearLayout = (LinearLayout) view.findViewById(R.id.ll_question_detail);
            mQuestionIconLayout = (FrameLayout) view.findViewById(R.id.question_detail_icon);
            mQuestionDetailLinearLayout.setVisibility(View.GONE);

            mQuestionIconLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mQuestionDetailLinearLayout.getVisibility() == View.GONE) {
                        expand();
                        mQuestionIconView.setImageResource(R.drawable.collapse);
                    } else {
                        collapse();
                        mQuestionIconView.setImageResource(R.drawable.expand);
                    }
                }
            });

            view.setOnClickListener(this);
        }


        private void expand() {
            //set Visible
            mQuestionDetailLinearLayout.setVisibility(View.VISIBLE);

            final int widthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            final int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            mQuestionDetailLinearLayout.measure(widthSpec, heightSpec);

            ValueAnimator mAnimator = slideAnimator(0, mQuestionDetailLinearLayout.getMeasuredHeight());
            mAnimator.start();
        }

        private void collapse() {
            int finalHeight = mQuestionDetailLinearLayout.getHeight();

            ValueAnimator mAnimator = slideAnimator(finalHeight, 0);

            mAnimator.addListener(new AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    //Height=0, but it set visibility to GONE
                    mQuestionDetailLinearLayout.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            mAnimator.start();
        }

        private ValueAnimator slideAnimator(int start, int end) {

            ValueAnimator animator = ValueAnimator.ofInt(start, end);

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    //Update Height
                    int value = (Integer) valueAnimator.getAnimatedValue();
                    LayoutParams layoutParams = mQuestionDetailLinearLayout.getLayoutParams();
                    layoutParams.height = value;
                    mQuestionDetailLinearLayout.setLayoutParams(layoutParams);
                }
            });
            return animator;
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int question_index = mCursor.getColumnIndex(InterviewEntry.COLUMN_QUESTION);
            mClickHandler.onClick(mCursor.getString(question_index), this);
        }


    }


}
