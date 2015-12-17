package com.merann.smamonov.googledrive.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.merann.smamonov.googledrive.R;
import com.merann.smamonov.googledrive.model.Image;
import com.merann.smamonov.googledrive.service.DriveServiceProxyForActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    public final String LOG_TAG = "HomeActivity";

    DriveServiceProxyForActivity mDriveServiceProxy;
    private static final int OPEN_FILE_DIALOG_REQUEST = 101;
    List<Image> mImages = new ArrayList<>();
    ListViewAdapter mListViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileDialog();
            }
        });

        mDriveServiceProxy = new DriveServiceProxyForActivity(this,
                new DriveServiceProxyForActivity.DriveServiceProxyListener() {

                    @Override
                    public void onConnectionStateChange(boolean isConneted) {
                        Log.d(LOG_TAG, "onConnectionStateChange");
                        onServiceConnected(isConneted);
                    }

                    @Override
                    public void onNewFileNotification() {
                        Log.d(LOG_TAG, "onNewFileNotification");
                        updateImageList();
                    }

                    @Override
                    public void onFileUploadNotification(String fileName, Boolean isSuccess) {
                        Log.d(LOG_TAG, "onFileUploadNotification");
                        String message;

                        if (isSuccess) {
                            message = "File " + fileName + " was successfully uploaded";
                        } else

                        {
                            message = "File " + fileName + " upload was failed";
                        }
                        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });

        onServiceConnected(false);
    }

    private void updateListView() {
        Log.d(LOG_TAG, "updateListView");
        ListView listView = (ListView) findViewById(R.id.listView);
        mListViewAdapter = new ListViewAdapter(this, mImages);
        listView.setAdapter(mListViewAdapter);
    }

    @Override
    protected void onStart() {
        Log.d(LOG_TAG, "onStart");
        super.onStart();
        mDriveServiceProxy.bind();
        updateListView();
    }

    @Override
    protected void onStop() {
        Log.d(LOG_TAG, "onStop");
        super.onStop();
        mDriveServiceProxy.unBind();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(LOG_TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(LOG_TAG, "onOptionsItemSelected");

        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Log.d(LOG_TAG, "onOptionsItemSelected: action_settings");
                showSettingsActivity();
                break;
            case R.id.action_sync:
                Log.d(LOG_TAG, "onOptionsItemSelected: action_sync");
                mDriveServiceProxy.doSync();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onServiceConnected(boolean isConnected) {
        Log.d(LOG_TAG, "onServiceConnected");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (isConnected) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.toolBarColorConnected));
        } else {
            toolbar.setBackgroundColor(getResources().getColor(R.color.toolBarColorDisconnected));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG, "onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode);
        if (!mDriveServiceProxy.onActivityResultHandler(requestCode, resultCode, data)) {
            switch (requestCode) {
                case OPEN_FILE_DIALOG_REQUEST: {
                    switch (resultCode) {
                        case RESULT_OK:
                            Log.d(LOG_TAG, "onActivityResult: OPEN_FILE_DIALOG_REQUEST : RESULT_OK");
                            File file = (File) data.getSerializableExtra(File.class.getName());
                            mDriveServiceProxy.uploadFile(file);
                            break;
                        default:
                            break;
                    }
                    break;
                }
                default:
                    Log.e(LOG_TAG, "unable to handle onActivityResult requestCode:"
                            + requestCode
                            + " resultCode:"
                            + resultCode);
                    break;
            }
        }
    }

    private void showSettingsActivity() {
        Log.d(LOG_TAG, "showSettingsActivity");
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void openFileDialog() {
        Intent intent = new Intent(this, OpenFileActivity.class);
        startActivityForResult(intent, OPEN_FILE_DIALOG_REQUEST);
    }

    private void updateImageList() {
        Log.d(LOG_TAG, "updateImageList");
        mImages = mDriveServiceProxy.getImagesList();
        updateListView();
    }
}
