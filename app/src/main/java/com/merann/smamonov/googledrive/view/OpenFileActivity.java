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
import com.merann.smamonov.googledrive.managers.LocalStorageManager;

import java.io.File;

public class OpenFileActivity extends AppCompatActivity {

    public final String LOG_TAG = "OpenFileActivity";
    private ListViewAdapter mListViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(LOG_TAG, "onCreate");

        setContentView(R.layout.activity_open_file);

        final LocalStorageManager localStorageManager
                = new LocalStorageManager(LocalStorageManager.MEDIA_STORAGE,
                new LocalStorageManager.BitmapLoadedListener() {
                    @Override
                    public void onBitmapLoaded(String fileName) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                updateListView();
                            }
                        });
                    }
                });

        final ListView listView = (ListView) findViewById(R.id.listView);
        mListViewAdapter = new ListViewAdapter(this, localStorageManager.getImagesList());

        listView.setAdapter(mListViewAdapter);

        listView.setOnItemClickListener(new AdapterView
                                                .OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view,
                                                                    int position, long id) {

                                                Log.d(LOG_TAG, "onItemClick position:" + position);
                                                Image image = (Image) parent.getItemAtPosition(position);
                                                File file = localStorageManager.getFileByFileName(image.getFileName());
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
