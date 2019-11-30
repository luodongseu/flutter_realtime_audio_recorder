package com.luodongseu.realtime_audio_recorder;


import android.annotation.SuppressLint;
import android.app.Application;

import com.luodongseu.realtime_audio_recorder.util.Logger;

/**
 * @author zhaolewei on 2018/7/10.
 */
public class RecordManager {
    private static final String TAG = RecordManager.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private volatile static RecordManager instance;
    private Application context;

    private RecordManager() {
    }

    public static RecordManager getInstance() {
        if (instance == null) {
            synchronized (RecordManager.class) {
                if (instance == null) {
                    instance = new RecordManager();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化
     *
     * @param application Application
     * @param showLog     是否开启日志
     */
    public void init(Application application, boolean showLog) {
        this.context = application;
        Logger.IsDebug = showLog;
    }

    public void start() {
        if (context == null) {
            Logger.e(TAG, "未进行初始化");
            return;
        }
        Logger.i(TAG, "start...");
        RecordService.startRecording(context);
    }

    public void stop() {
        if (context == null) {
            return;
        }
        RecordService.stopRecording(context);
    }

    public void resume() {
        if (context == null) {
            return;
        }
        RecordService.resumeRecording(context);
    }

    public void pause() {
        if (context == null) {
            return;
        }
        RecordService.pauseRecording(context);
    }

    /**
     * 录音数据监听回调
     */
    public void setRecordDataListener(RecordDataListener listener) {
        RecordService.setRecordDataListener(listener);
    }


    public RecordConfig getRecordConfig() {
        return RecordService.getRecordConfig();
    }

    /**
     * 修改录音文件存放路径
     */
    public void changeRecordDir(String recordDir) {
        RecordService.changeRecordDir(recordDir);
    }

    /**
     * 获取当前的录音状态
     *
     * @return 状态
     */
    public RecordHelper.RecordState getState() {
        return RecordService.getState();
    }

}
