package com.luodongseu.realtime_audio_recorder;

/**
 * 录音数据监听器
 *
 * @author luodong
 */
public interface RecordDataListener {

    /**
     * 录音mp3数据
     *
     * @param data 当前音频数据字节byte[]
     */
    void onData(byte[] data);

}
