package com.enjoy.gamelive;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private ScreenLive mScreenLive;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mScreenLive.onActivityResult(requestCode, resultCode, data);
    }

    public void startLive(View view) {
        mScreenLive = new ScreenLive();
        mScreenLive.startLive(this,
                //"rtmp://192.168.0.128/myapp/mystream");
                "rtmp://123.56.253.81:1935/myapp");
    }

    public void stopLive(View view) {
        mScreenLive.stoptLive();
    }

}
