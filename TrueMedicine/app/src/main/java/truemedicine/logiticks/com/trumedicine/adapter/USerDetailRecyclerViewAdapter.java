package truemedicine.logiticks.com.trumedicine.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import truemedicine.logiticks.com.trumedicine.R;
import truemedicine.logiticks.com.trumedicine.model.UserDetailModel;
import truemedicine.logiticks.com.trumedicine.utils.AppConstants;
import truemedicine.logiticks.com.trumedicine.utils.AppUtils;


public class USerDetailRecyclerViewAdapter extends RecyclerView.Adapter<USerDetailRecyclerViewAdapter.UserDetailHolder> {

    private ArrayList<UserDetailModel> mList;
    private Context mContext;
    private OnItemClickListener onItemClickListener;

    public USerDetailRecyclerViewAdapter(Context mContext, ArrayList<UserDetailModel> list, OnItemClickListener onItemClickListener) {
        this.mList = list;
        this.mContext = mContext;
        this.onItemClickListener = onItemClickListener;
    }


    @Override
    public UserDetailHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_detail, null);

        return new UserDetailHolder(view);
    }

    @Override
    public void onBindViewHolder(UserDetailHolder holder, final int position) {
        final UserDetailModel userDetailModel = mList.get(position);
        holder.mNameTextView.setText(userDetailModel.firstName + " " + userDetailModel.lastName);
        holder.mEmailTextView.setText(userDetailModel.email);
        holder.mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemDelete(userDetailModel);

            }
        });
        if (AppUtils.getStringSharedPreference(mContext, AppConstants.SIGNIN_KEY_USERNAME).equalsIgnoreCase(userDetailModel.email)) {
            holder.mDeleteButton.setVisibility(View.INVISIBLE);
            holder.mEmailTextView.append("  ( Admin )");
        } else {
            holder.mDeleteButton.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public class UserDetailHolder extends RecyclerView.ViewHolder {

        TextView mNameTextView;
        TextView mEmailTextView;
        ImageButton mDeleteButton;

        public UserDetailHolder(View itemView) {
            super(itemView);
            mNameTextView = (TextView) itemView
                    .findViewById(R.id.name);
            mEmailTextView = (TextView) itemView
                    .findViewById(R.id.email);
            mDeleteButton = (ImageButton) itemView.findViewById(R.id.deleteButton);
        }
    }

    public interface OnItemClickListener {
        void onItemDelete(UserDetailModel userDetailModel);
    }

}
