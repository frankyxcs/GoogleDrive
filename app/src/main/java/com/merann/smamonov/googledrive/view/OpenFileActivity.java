package com.merann.smamonov.googledrive.view;

import android.content.Intent;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_file);

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(new ListViewAdapter(this,
                LocalStorageManager.getInstance().getImagesList()));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Log.d(LOG_TAG, "onItemClick position:" + position);
                Image image = (Image) parent.getItemAtPosition(position);
                File file =LocalStorageManager.getInstance().getFileByFileName(image.getFileName());
                Intent intent = new Intent();
                intent.putExtra(File.class.getName(), file);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
