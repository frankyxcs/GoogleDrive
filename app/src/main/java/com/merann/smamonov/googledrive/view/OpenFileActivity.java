package com.merann.smamonov.googledrive.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.merann.smamonov.googledrive.R;
import com.merann.smamonov.googledrive.model.Image;
import com.merann.smamonov.googledrive.service.LocalStorageManager;

import java.io.File;

public class OpenFileActivity extends AppCompatActivity {

    public final String LOG_TAG = "OpenFileActivity";
    private ListViewAdapter mListViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_file);

        final ListView listView = (ListView) findViewById(R.id.listView);
        mListViewAdapter = new ListViewAdapter(this,
                LocalStorageManager
                        .getInstance()
                        .getImagesList(new LocalStorageManager.BitmapLoadedListener() {
                            @Override
                            public void onBitmapLoaded(String fileName) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateListView();
                                    }
                                });
                            }
                        }));

        listView.setAdapter(mListViewAdapter);

        listView.setOnItemClickListener(new AdapterView
                                                .OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view,
                                                                    int position, long id) {

                                                Log.d(LOG_TAG, "onItemClick position:" + position);
                                                Image image = (Image) parent.getItemAtPosition(position);
                                                File file = LocalStorageManager.getInstance().getFileByFileName(image.getFileName());
                                                Intent intent = new Intent();
                                                intent.putExtra(File.class.getName(), file);
                                                setResult(RESULT_OK, intent);
                                                finish();
                                            }
                                        }

        );
    }

    private void updateListView() {
        if (mListViewAdapter != null) {
            mListViewAdapter.notifyDataSetChanged();
        }
    }
}
