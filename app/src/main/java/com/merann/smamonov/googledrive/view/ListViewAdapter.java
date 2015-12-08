package com.merann.smamonov.googledrive.view;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.merann.smamonov.googledrive.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by samam_000 on 26.11.2015.
 */
public class ListViewAdapter extends BaseAdapter {

    class ViewHolder {
        TextView text;
        ImageView image;
        File file;

        public ViewHolder(TextView text, ImageView image, File file) {
            this.text = text;
            this.image = image;
            this.file = file;
        }

        public ViewHolder(File file) {
            this.file = file;
        }

        public TextView getText() {
            return text;
        }

        public void setText(TextView text) {
            this.text = text;
        }

        public ImageView getImage() {
            return image;
        }

        public void setImage(ImageView image) {
            this.image = image;
        }

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }
    }

    final private ArrayList<ViewHolder> viewHolders = new ArrayList<>();
    private final Context context;

    public ListViewAdapter(Context context,
                           List<File> files) {
        this.context = context;

        for (File file : files) {
            ViewHolder viewHolder = new ViewHolder(file);
            viewHolders.add(viewHolder);
        }
    }

    @Override
    public int getCount() {
        return viewHolders.size();
    }

    @Override
    public Object getItem(int position) {
        return viewHolders.get(position).getFile();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position,
                        View convertView,
                        ViewGroup parent) {

        ViewHolder viewHolder = viewHolders.get(position);

        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.image_item, parent, false);

            viewHolder.setText((TextView) convertView.findViewById(R.id.image_text));
            viewHolder.setImage((ImageView) convertView.findViewById(R.id.image));
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (viewHolder != null) {
            viewHolder.getText().setText(viewHolder.getFile().getName());
        }

        return convertView;
    }
}
