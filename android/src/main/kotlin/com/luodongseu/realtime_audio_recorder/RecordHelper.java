package com.luodongseu.realtime_audio_recorder;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import com.luodongseu.realtime_audio_recorder.util.FileUtils;
import com.luodongseu.realtime_audio_recorder.util.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * @author zhaolewei on 2018/7/10.
 */
public class RecordHelper {
    private static final String TAG = RecordHelper.class.getSimpleName();
    private volatile static RecordHelper instance;
    private volatile RecordState state = RecordState.IDLE;
    private static final int RECORD_AUDIO_BUFFER_TIMES = 1;

    private RecordDataListener recordDataListener;
    private RecordConfig currentConfig;
    private AudioRecordThread audioRecordThread;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private File resultFile = null;
    private Mp3EncodeThread mp3EncodeThread;

    private RecordHelper() {
    }

    static RecordHelper getInstance() {
        if (instance == null) {
            synchronized (RecordHelper.class) {
                if (instance == null) {
                    instance = new RecordHelper();
                }
            }
        }
        return instance;
    }

    RecordState getState() {
        return state;
    }

    void setRecordDataListener(RecordDataListener recordDataListener) {
        this.recordDataListener = recordDataListener;
    }

    public void start(RecordConfig config) {
        this.currentConfig = config;
        if (state != RecordState.IDLE && state != RecordState.STOP) {
            Logger.e(TAG, "状态异常当前状态： %s", state.name());
            return;
        }
        Logger.d(TAG, "----------------开始录制mp3------------------------");
        Logger.d(TAG, "参数： %s", currentConfig.toString());
        audioRecordThread = new AudioRecordThread();
        audioRecordThread.start();
    }

    public void stop() {
        if (state == RecordState.IDLE) {
            Logger.e(TAG, "状态异常当前状态： %s", state.name());
            return;
        }

        if (state == RecordState.PAUSE) {
            state = RecordState.IDLE;
            stopMp3Encoded();
        } else {
            state = RecordState.STOP;
        }
    }

    void pause() {
        if (state != RecordState.RECORDING) {
            Logger.e(TAG, "状态异常当前状态： %s", state.name());
            return;
        }
        state = RecordState.PAUSE;
    }

    void resume() {
        if (state != RecordState.PAUSE) {
            Logger.e(TAG, "状态异常当前状态： %s", state.name());
            return;
        }
        audioRecordThread = new AudioRecordThread();
        audioRecordThread.start();
    }

    private void notifyData(final byte[] data) {
        if (recordDataListener == null) {
            return;
        }
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (recordDataListener != null) {
                    recordDataListener.onData(data);
                }
            }
        });
    }

    /**
     * 获取音量
     */
    private int getDb(byte[] data) {
        double sum = 0;
        double ave;
        int length = data.length > 128 ? 128 : data.length;
        int offsetStart = 8;
        for (int i = offsetStart; i < length; i++) {
            sum += data[i];
        }
        ave = (sum / (length - offsetStart)) * 65536 / 128f;
        int i = (int) (Math.log10(ave) * 20);
        return i < 0 ? 27 : i;
    }

    private void initMp3EncoderThread(int bufferSize) {
        try {
            mp3EncodeThread = new Mp3EncodeThread(resultFile, bufferSize);
            mp3EncodeThread.setOnDataEncodedListner(new Mp3EncodeThread.OnDataEncodedListener() {
                @Override
                public void onEncodeData(byte[] data) {
                    notifyData(data);
                }
            });
            mp3EncodeThread.start();
        } catch (Exception e) {
            Logger.e(e, TAG, e.getMessage());
        }
    }

    private class AudioRecordThread extends Thread {
        private AudioRecord audioRecord;
        private int bufferSize;

        AudioRecordThread() {
            bufferSize = AudioRecord.getMinBufferSize(currentConfig.getSampleRate(),
                    currentConfig.getChannelConfig(), currentConfig.getEncodingConfig()) * RECORD_AUDIO_BUFFER_TIMES;
            Logger.d(TAG, "record buffer size = %s", bufferSize);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, currentConfig.getSampleRate(),
                    currentConfig.getChannelConfig(), currentConfig.getEncodingConfig(), bufferSize);
            initMp3EncoderThread(bufferSize);
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
                    if (mp3EncodeThread != null) {
                        mp3EncodeThread.addChangeBuffer(new Mp3EncodeThread.ChangeBuffer(byteBuffer, end));
                    }
//                    notifyData(ByteUtils.toBytes(byteBuffer));
                }
                audioRecord.stop();
            } catch (Exception e) {
                Logger.e(e, TAG, e.getMessage());
            }
            if (state != RecordState.PAUSE) {
                state = RecordState.IDLE;
                stopMp3Encoded();
            } else {
                Logger.d(TAG, "暂停");
            }
        }
    }

    private void stopMp3Encoded() {
        if (mp3EncodeThread != null) {
            mp3EncodeThread.stopSafe(new Mp3EncodeThread.EncordFinishListener() {
                @Override
                public void onFinish() {
                    mp3EncodeThread = null;
                }
            });
        } else {
            Logger.e(TAG, "mp3EncodeThread is null, 代码业务流程有误，请检查！！ ");
        }
    }

    /**
     * 根据当前的时间生成相应的文件名
     * 实例 record_20160101_13_15_12
     */
    private String getTempFilePath() {
        String fileDir = String.format(Locale.getDefault(), "%s/Record/", Environment.getExternalStorageDirectory().getAbsolutePath());
        if (!FileUtils.createOrExistsDir(fileDir)) {
            Logger.e(TAG, "文件夹创建失败：%s", fileDir);
        }
        String fileName = String.format(Locale.getDefault(), "record_tmp_%s", FileUtils.getNowString(new SimpleDateFormat("yyyyMMdd_HH_mm_ss", Locale.SIMPLIFIED_CHINESE)));
        return String.format(Locale.getDefault(), "%s%s.pcm", fileDir, fileName);
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

}
