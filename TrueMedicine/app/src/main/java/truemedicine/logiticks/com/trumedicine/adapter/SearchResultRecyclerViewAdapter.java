package truemedicine.logiticks.com.trumedicine.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Locale;

import truemedicine.logiticks.com.trumedicine.R;
import truemedicine.logiticks.com.trumedicine.model.ImageSearchResponseModel;
import truemedicine.logiticks.com.trumedicine.network.Urls;


public class SearchResultRecyclerViewAdapter extends RecyclerView.Adapter<SearchResultRecyclerViewAdapter.UserDetailHolder> {

    private ArrayList<ImageSearchResponseModel> mList;
    private Context mContext;
    private OnItemClickListener onItemClickListener;

    public SearchResultRecyclerViewAdapter(Context mContext, ArrayList<ImageSearchResponseModel> list, OnItemClickListener onItemClickListener) {
        this.mList = list;
        this.mContext = mContext;
        this.onItemClickListener = onItemClickListener;
    }


    @Override
    public UserDetailHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, null);

        return new UserDetailHolder(view);
    }

    @Override
    public void onBindViewHolder(UserDetailHolder holder, final int position) {
        final ImageSearchResponseModel searchResponseModel = mList.get(position);
        holder.mTitleTextView.setText(searchResponseModel.name);
        holder.mDiscriptionTextView.setText(searchResponseModel.description);
        if (searchResponseModel.percentage == null) {
            holder.mPercentageTextView.setVisibility(View.GONE);
        } else {
            float per = Float.parseFloat(searchResponseModel.percentage);
            if (Math.round(per) == 1)
            {
                holder.mPercentageTextView.setText("Closest Match");
            }
            else
            {
                holder.mPercentageTextView.setText("Rank #" + String.format(Locale.getDefault(),"%.0f", per));
            }
        }
        Glide.with(mContext).load(searchResponseModel.imageurl).into(holder.mImage);
        holder.mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onPlayClicked(searchResponseModel);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public class UserDetailHolder extends RecyclerView.ViewHolder {

        TextView mTitleTextView;
        TextView mDiscriptionTextView;
        TextView mPercentageTextView;
        ImageView mImage;
        ImageButton mPlay;

        public UserDetailHolder(View itemView) {
            super(itemView);
            mTitleTextView = (TextView) itemView
                    .findViewById(R.id.title);
            mDiscriptionTextView = (TextView) itemView
                    .findViewById(R.id.discription);
            mPercentageTextView = (TextView) itemView.findViewById(R.id.percentage);
            mImage = (ImageView) itemView.findViewById(R.id.image);
            mPlay = (ImageButton) itemView.findViewById(R.id.playButton);
        }
    }

    public interface OnItemClickListener {
        void onPlayClicked(ImageSearchResponseModel imageSearchResponseModel);
    }

}
