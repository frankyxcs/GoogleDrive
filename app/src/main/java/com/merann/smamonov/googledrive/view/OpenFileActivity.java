package com.merann.smamonov.googledrive.view;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.merann.smamonov.googledrive.R;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class OpenFileActivity extends AppCompatActivity {

    public final String LOG_TAG = "OpenFileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_file);

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(new ListViewAdapter(this, getImagesList()));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Log.d(LOG_TAG, "onItemClick position:" + position);
                File file = (File)parent.getItemAtPosition(position);
                Log.d(LOG_TAG, "onItemClick position:" + position + " file:" + file.getName());
                Intent intent = new Intent();
                intent.putExtra(File.class.getName(), file);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private List<File> getImagesList() {
        //TODO: get the folder by global name
        List<File> result = null;
        File picture_folder = new File("/mnt/extSdCard/DCIM/Camera");
        if (picture_folder != null) {
            result = Arrays.asList(picture_folder.listFiles());
        }
        return result;
    }
}
