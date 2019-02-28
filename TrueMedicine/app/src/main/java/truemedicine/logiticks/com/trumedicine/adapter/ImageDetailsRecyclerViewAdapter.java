package truemedicine.logiticks.com.trumedicine.adapter;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.Locale;

import truemedicine.logiticks.com.trumedicine.R;
import truemedicine.logiticks.com.trumedicine.activity.ImageDetailActivity;
import truemedicine.logiticks.com.trumedicine.model.ImageListModel;
import truemedicine.logiticks.com.trumedicine.network.Urls;
import truemedicine.logiticks.com.trumedicine.utils.AppUtils;


public class ImageDetailsRecyclerViewAdapter extends RecyclerView.Adapter<MyImageDetailHolder> {

    private ArrayList<ImageListModel> mList;
    private ArrayList<ImageListModel> mCopyList;
    private Context mContext;

    public ImageDetailsRecyclerViewAdapter(Context mContext, ArrayList<ImageListModel> list) {
        this.mList = list;
        mCopyList = new ArrayList<>();

        this.mContext = mContext;
    }

    public void notifyFilterList() {
        mCopyList.clear();
        mCopyList.addAll(mList);
        notifyDataSetChanged();
    }

    @Override
    public MyImageDetailHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_image_list_item, null);

        return new MyImageDetailHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyImageDetailHolder holder,  int position) {
        holder.mTitleTextView.setText(mList.get(position).getmTitle());
        holder.mDateTextView.setText(mList.get(position)
                .getmDescription());
        if (mList.get(position).getUploadStatus() == 0&& mList.get(position).getmImagePathLocal().contains("/")) {
            Glide.with(mContext)
                    .load(mList.get(position).getmImagePathLocal())
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE).

                    into(holder.mItemImageView);

        } else {
            Glide.with(mContext)
                    .load(Urls.IMAGE_BLOB_URL + mList.get(position).getmImagePath())
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE).
                    into(holder.mItemImageView);

        }
        holder.mDateTextView.setText(mList.get(position).getmDate());
        holder.mItemTimeTextView.setText(mList.get(position).getmTime());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ImageDetailActivity.class);
                intent.putExtra("model", mList.get(holder.getAdapterPosition()));
                mContext.startActivity(intent);

            }
        });

    }

    private void previewCapturedImage(Uri selectedImage, ImageView mItemImageView) {
        try {
            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();
            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 4;
            final Bitmap bitmap = BitmapFactory.decodeFile(selectedImage.getPath(),
                    options);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 500, 500,
                    false);
            // rotated
            Bitmap thumbnail_r = AppUtils.imageOreintationValidator(resizedBitmap,
                    selectedImage.getPath());
            mItemImageView.setImageBitmap(thumbnail_r);
            //   new ImageUpload(getApplicationContext(), Urls.IMAGE_SEARCH_URL, fileUri).execute();

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public int filter(String newText) {
        String query = newText.toLowerCase(Locale.getDefault());
        mList.clear();
        if (query.length() == 0) {
            mList.addAll(mCopyList);
        } else {
            for (ImageListModel imageListModel : mCopyList) {

                if (imageListModel.getmTitle().toLowerCase(Locale.getDefault()).contains(query)) {
                    mList.add(imageListModel);
                }

            }
        }
        notifyDataSetChanged();
        return mList.size();
    }
}
