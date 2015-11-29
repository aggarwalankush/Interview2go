package com.aggarwalankush.interview2go;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aggarwalankush.interview2go.data.InterviewContract.InterviewEntry;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.TopicAdapterViewHolder> {

    private Cursor mCursor;
    final private Context mContext;
    final private TopicAdapterOnClickHandler mClickHandler;
    final private View mEmptyView;

    public TopicAdapter(Context context, TopicAdapterOnClickHandler clickHandler, View emptyView) {
        mContext = context;
        mClickHandler = clickHandler;
        mEmptyView = emptyView;
    }


    @Override
    public TopicAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewGroup instanceof RecyclerView) {
            int layoutId = R.layout.list_item_topic;
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
            view.setFocusable(true);
            return new TopicAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(TopicAdapterViewHolder topicAdapterViewHolder, int position) {
        mCursor.moveToPosition(position);
        String topic = mCursor.getString(TopicFragment.COL_TOPIC);
        int total = mCursor.getInt(TopicFragment.COL_TOTAL);
        Integer done = TopicFragment.topicToDoneQues.get(topic);
        if (null == done) {
            done = 0;
        }

        topicAdapterViewHolder.mTopicView.setText(Utility.getTopicName(topic));
        topicAdapterViewHolder.mIconView.setImageResource(Utility.getImageResouce(topic));
        String detailString = String.format(mContext.getString(R.string.share_topic_detail), total, done);
        topicAdapterViewHolder.mTopicDetailView.setText(detailString);
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

    public interface TopicAdapterOnClickHandler {
        void onClick(String topic, TopicAdapterViewHolder vh);
    }

    public class TopicAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView mTopicView;
        public final ImageView mIconView;
        public final TextView mTopicDetailView;

        public TopicAdapterViewHolder(View view) {
            super(view);
            mTopicView = (TextView) view.findViewById(R.id.tv_topic);
            mIconView = (ImageView) view.findViewById(R.id.list_item_icon);
            mTopicDetailView = (TextView) view.findViewById(R.id.tv_topic_detail);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int topic_index = mCursor.getColumnIndex(InterviewEntry.COLUMN_TOPIC);
            mClickHandler.onClick(mCursor.getString(topic_index), this);
        }
    }

}
