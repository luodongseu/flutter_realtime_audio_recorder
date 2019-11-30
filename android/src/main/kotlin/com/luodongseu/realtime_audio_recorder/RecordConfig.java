package com.luodongseu.realtime_audio_recorder;

import android.media.AudioFormat;
import android.os.Environment;

import java.io.Serializable;
import java.util.Locale;

/**
 * @author zhaolewei on 2018/7/11.
 */
public class RecordConfig implements Serializable {
    /**
     * 通道数:默认单通道
     */
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;

    /**
     * 位宽
     */
    private int encodingConfig = AudioFormat.ENCODING_PCM_16BIT;

    /**
     * 采样率
     */
    private int sampleRate = 44100;

    /*
     * 录音文件存放路径，默认sdcard/Record
     */
    private String recordDir = String.format(Locale.getDefault(),
            "%s/Record/",
            Environment.getExternalStorageDirectory().getAbsolutePath());

    public RecordConfig() {
    }


    public String getRecordDir() {
        return recordDir;
    }

    public void setRecordDir(String recordDir) {
        this.recordDir = recordDir;
    }

    /**
     * 获取当前录音的采样位宽 单位bit
     *
     * @return 采样位宽 0: error
     */
    public int getEncoding() {
        return 16;
    }

    /**
     * 获取当前录音的采样位宽 单位bit
     *
     * @return 采样位宽 0: error
     */
    public int getRealEncoding() {
        if (encodingConfig == AudioFormat.ENCODING_PCM_8BIT) {
            return 8;
        } else if (encodingConfig == AudioFormat.ENCODING_PCM_16BIT) {
            return 16;
        } else {
            return 0;
        }
    }

    /**
     * 当前的声道数
     *
     * @return 声道数： 0：error
     */
    public int getChannelCount() {
        if (channelConfig == AudioFormat.CHANNEL_IN_MONO) {
            return 1;
        } else if (channelConfig == AudioFormat.CHANNEL_IN_STEREO) {
            return 2;
        } else {
            return 0;
        }
    }

    public int getChannelConfig() {
        return channelConfig;
    }

    public RecordConfig setChannelConfig(int channelConfig) {
        this.channelConfig = channelConfig;
        return this;
    }

    public int getEncodingConfig() {
        return AudioFormat.ENCODING_PCM_16BIT;
    }

    public RecordConfig setEncodingConfig(int encodingConfig) {
        this.encodingConfig = encodingConfig;
        return this;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public RecordConfig setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
        return this;
    }


    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "录制格式：mp3,采样率：%sHz,位宽：%s bit,声道数：%s", sampleRate, getEncoding(), getChannelCount());
    }
}
