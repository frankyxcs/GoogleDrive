package com.merann.smamonov.googledrive.view;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.merann.smamonov.googledrive.R;
import com.merann.smamonov.googledrive.model.Image;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by samam_000 on 26.11.2015.
 */
public class ListViewAdapter extends BaseAdapter {

    public final String LOG_TAG = "ListViewAdapter";

    class ViewHolder {
        TextView mTextView;
        ImageView mImageView;
        ProgressBar mProgressBar;
        Image mImage;

        public TextView getTextView() {
            return mTextView;
        }

        public void setTextView(TextView mTextView) {
            this.mTextView = mTextView;
        }

        public ImageView getImageView() {
            return mImageView;
        }

        public void setImageView(ImageView mImageView) {
            this.mImageView = mImageView;
        }

        public Image getImage() {
            return mImage;
        }

        public void setImage(Image mImage) {
            this.mImage = mImage;
        }

        public ProgressBar getProgressBar() {
            return mProgressBar;
        }

        public void setProgressBar(ProgressBar progressBar) {
            this.mProgressBar = progressBar;
        }
    }

    private List<Image> mImages = new ArrayList<>();
    private final Context mContext;

    public ListViewAdapter(Context context,
                           List<Image> images) {

        mContext = context;
        mImages = images;
    }

    @Override
    public int getCount() {
        return mImages.size();
    }

    @Override
    public Object getItem(int position) {
        return mImages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position,
                        View convertView,
                        ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(R.layout.image_item, parent, false);


            viewHolder = new ViewHolder();
            viewHolder.setTextView((TextView) convertView.findViewById(R.id.image_text));
            viewHolder.setImageView((ImageView) convertView.findViewById(R.id.image));
            viewHolder.setProgressBar((ProgressBar) convertView.findViewById(R.id.progress));
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.setImage((Image) getItem(position));
        viewHolder.getTextView().setText(viewHolder.getImage().getFileName());


        if (viewHolder.getImage().getBitmap() != null) {
            ImageView imageView = viewHolder.getImageView();
            imageView.setImageBitmap(viewHolder.getImage().getBitmap());
            imageView.setVisibility(View.VISIBLE);

            ProgressBar progressBar = viewHolder.getProgressBar();
            progressBar.setVisibility(View.INVISIBLE);
        } else {
            ImageView imageView = viewHolder.getImageView();
            imageView.setVisibility(View.INVISIBLE);

            ProgressBar progressBar = viewHolder.getProgressBar();
            progressBar.setVisibility(View.VISIBLE);
        }
        return convertView;
    }
}
