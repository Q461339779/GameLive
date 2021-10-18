package com.enjoy.gamelive;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.AsyncTask;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Lance
 * @date 2019/5/9
 */
public class ScreenLive implements Runnable {

    static {
        System.loadLibrary("native-lib");
    }

    private String url;
    private MediaProjectionManager mediaProjectionManager;
    private boolean isLiving;
    private LinkedBlockingQueue<RTMPPackage> queue = new LinkedBlockingQueue<>();
    private MediaProjection mediaProjection;


    public void startLive(Activity activity, String url) {
        this.url = url;
        // 投屏管理器
        this.mediaProjectionManager = (MediaProjectionManager) activity
                .getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        // 创建截屏请求intent
        Intent captureIntent = mediaProjectionManager.createScreenCaptureIntent();
        activity.startActivityForResult(captureIntent, 100);
    }

    public void stoptLive() {
        addPackage(RTMPPackage.EMPTY_PACKAGE);
        isLiving = false;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 用户授权
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            // 获得截屏器
            mediaProjection = mediaProjectionManager.getMediaProjection
                    (resultCode, data);
            LiveTaskManager.getInstance().execute(this);
        }
    }

    public void addPackage(RTMPPackage rtmpPackage) {
        if (!isLiving) {
            return;
        }
        queue.add(rtmpPackage);
    }


    @Override
    public void run() {
        //1、连接服务器  斗鱼rtmp服务器
        if (!connect(url)) {
            return;
        }
        isLiving = true;
        VideoCodec videoCodec = new VideoCodec(this);
        videoCodec.startLive(mediaProjection);
        AudioCodec audioCodec = new AudioCodec(this);
        audioCodec.startLive();
        boolean isSend = true;
        while (isLiving && isSend) {
            RTMPPackage rtmpPackage = null;
            try {
                rtmpPackage = queue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (null == rtmpPackage) {
                break;
            }
            if (rtmpPackage.getBuffer() != null && rtmpPackage.getBuffer().length != 0) {
                isSend = sendData(rtmpPackage.getBuffer(), rtmpPackage.getBuffer()
                        .length, rtmpPackage
                        .getType(), rtmpPackage.getTms());
            }
        }
        isLiving = false;
        videoCodec.stopLive();
        audioCodec.stopLive();
        queue.clear();
        disConnect();
    }

    private native boolean connect(String url);

    private native void disConnect();

    private native boolean sendData(byte[] data, int len, int type, long tms);

}
