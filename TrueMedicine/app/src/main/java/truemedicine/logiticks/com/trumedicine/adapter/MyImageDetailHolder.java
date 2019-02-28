package truemedicine.logiticks.com.trumedicine.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import truemedicine.logiticks.com.trumedicine.R;

public class MyImageDetailHolder extends RecyclerView.ViewHolder {

    TextView mTitleTextView;
    TextView mDateTextView;
    ImageView mItemImageView;
    TextView mItemTimeTextView;

    public MyImageDetailHolder(View itemView) {
        super(itemView);
        mTitleTextView = (TextView) itemView
                .findViewById(R.id.imageTitleTextView);
        mDateTextView = (TextView) itemView
                .findViewById(R.id.dateTextView);
        mItemImageView = (ImageView) itemView
                .findViewById(R.id.listItemImageView);
        mItemTimeTextView = (TextView) itemView
                .findViewById(R.id.timeTextView);
    }
}
