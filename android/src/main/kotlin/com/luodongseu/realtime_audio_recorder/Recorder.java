package com.luodongseu.realtime_audio_recorder;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import com.luodongseu.realtime_audio_recorder.util.FileUtils;
import com.luodongseu.realtime_audio_recorder.util.Logger;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * 录音器
 *
 * @author luodong
 */
public class Recorder {
    private static final String TAG = Recorder.class.getSimpleName();
    private volatile static Recorder instance;
    private volatile RecordState state = RecordState.IDLE;
    private static final int RECORD_AUDIO_BUFFER_TIMES = 1;
    private RecordDataListener recordDataListener;
    private RecordVolumeListener recordVolumeListener;
    private RecordConfig currentConfig = new RecordConfig();
    private AudioRecordThread audioRecordThread;
    private Mp3EncoderHelper mp3EncoderHelper;

    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private File resultFile = null;

    private Recorder() {
    }

    static Recorder getInstance() {
        if (instance == null) {
            synchronized (Recorder.class) {
                if (instance == null) {
                    instance = new Recorder();
                }
            }
        }
        return instance;
    }

    /**
     * 监听器
     *
     * @param recordDataListener RecordDataListener
     */
    void setRecordDataListener(RecordDataListener recordDataListener) {
        this.recordDataListener = recordDataListener;
    }

    /**
     * 音量监听器
     *
     * @param recordVolumeListener RecordVolumeListener
     */
    public void setRecordVolumeListener(RecordVolumeListener recordVolumeListener) {
        this.recordVolumeListener = recordVolumeListener;
    }

    /**
     * 开始录音
     */
    public void start() {
        if (state != RecordState.IDLE && state != RecordState.STOP) {
            Logger.e(TAG, "状态异常当前状态： %s", state.name());
            return;
        }
        Logger.d(TAG, "----------------开始录制mp3------------------------");
        Logger.d(TAG, "参数： %s", currentConfig.toString());
        audioRecordThread = new AudioRecordThread();
        audioRecordThread.start();
    }

    /**
     * 结束录音
     */
    public void stop() {
        if (state == RecordState.IDLE) {
            Logger.e(TAG, "状态异常当前状态： %s", state.name());
            return;
        }
        if (state == RecordState.PAUSE) {
            state = RecordState.IDLE;
            if (null != audioRecordThread) {
                audioRecordThread = null;
            }
        } else {
            state = RecordState.STOP;
        }
    }

    /**
     * 通知数据变化
     *
     * @param data byte[]
     */
    private void notifyData(final byte[] data) {
        if (null == recordDataListener) {
            return;
        }
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                recordDataListener.onData(data);
            }
        });
    }

    /**
     * 通知声音变化
     *
     * @param v 音量大小
     */
    private void notifyVolume(final int v) {
        if (null == recordVolumeListener) {
            return;
        }
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                recordVolumeListener.onData(v);
            }
        });
    }


    /**
     * 单独的录音线程
     */
    private class AudioRecordThread extends Thread {
        private AudioRecord audioRecord;
        private int bufferSize;

        AudioRecordThread() {
            bufferSize = AudioRecord.getMinBufferSize(currentConfig.getSampleRate(),
                    currentConfig.getChannelConfig(), currentConfig.getEncodingConfig()) * RECORD_AUDIO_BUFFER_TIMES;
            Logger.d(TAG, "record buffer size = %s", bufferSize);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, currentConfig.getSampleRate(),
                    currentConfig.getChannelConfig(), currentConfig.getEncodingConfig(), bufferSize);

            // 初始化mp3 encode Thread
            mp3EncoderHelper = new Mp3EncoderHelper(currentConfig, bufferSize);
        }

        @Override
        public void run() {
            super.run();
            startMp3Recorder();
        }

        private void startMp3Recorder() {
            state = RecordState.RECORDING;
            try {
                audioRecord.startRecording();
                short[] byteBuffer = new short[bufferSize];

                while (state == RecordState.RECORDING) {
                    int end = audioRecord.read(byteBuffer, 0, byteBuffer.length);
                    if (mp3EncoderHelper != null) {
                        // 通知转换后的 mp3 数据
                        byte[] encodeData = mp3EncoderHelper.encode(new Mp3EncoderHelper.ChangeBuffer(byteBuffer, end));
                        notifyData(encodeData);

                        // 通知声音大小
                        notifyVolume(calcVolume(byteBuffer, byteBuffer.length));

                        // @TODO: 写入到本地文件
                    }
                }
                audioRecord.stop();
            } catch (Exception e) {
                Logger.e(e, TAG, e.getMessage());
            }
            if (state != RecordState.PAUSE) {
                state = RecordState.IDLE;
            } else {
                Logger.d(TAG, "暂停");
            }
        }

        /**
         * 计算声音大小
         *
         * @param buffer 声音数据
         * @param length 有效长度
         * @return double 0-100
         */
        private int calcVolume(short[] buffer, int length) {
            if (length <= 0 || null == buffer) {
                return 0;
            }
            long v = 0;
            for (int i = 0; i < buffer.length && i < length; i++) {
                v += buffer[i] * buffer[i];
            }
            int vol = Double.valueOf(10 * Math.log10(1.0 * v / length)).intValue();
            return Math.max(Math.min(vol, 100), 0);
        }

    }


    /**
     * 表示当前状态
     */
    public enum RecordState {
        /**
         * 空闲状态
         */
        IDLE,
        /**
         * 录音中
         */
        RECORDING,
        /**
         * 暂停中
         */
        PAUSE,
        /**
         * 正在停止
         */
        STOP,
        /**
         * 录音流程结束（转换结束）
         */
        FINISH
    }

    /**
     * 根据当前的时间生成相应的文件名
     * 实例 record_20160101_13_15_12
     */
    private static String getFilePath() {
        String fileDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (!FileUtils.createOrExistsDir(fileDir)) {
            Logger.w(TAG, "文件夹创建失败：%s", fileDir);
            return null;
        }
        String fileName = String.format(Locale.getDefault(), "record_%s", FileUtils.getNowString(new SimpleDateFormat("yyyyMMdd_HH_mm_ss", Locale.SIMPLIFIED_CHINESE)));
        return String.format(Locale.getDefault(), "%s%s.mp3", fileDir, fileName);
    }

}
