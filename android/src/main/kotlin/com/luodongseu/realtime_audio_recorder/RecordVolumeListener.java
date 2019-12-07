package com.luodongseu.realtime_audio_recorder;

/**
 * 录音声音大小监听器
 *
 * @author luodong
 */
public interface RecordVolumeListener {

    /**
     * 录音mp3数据
     *
     * @param volume 录音大小
     */
    void onData(double volume);

}
