package com.luodongseu.realtime_audio_recorder;

import com.luodongseu.realtime_audio_recorder.util.Logger;
import com.zlw.main.recorderlib.recorder.mp3.Mp3Encoder;

import java.util.Arrays;

/**
 * MP3编码帮助类
 *
 * @author luodong
 */
public class Mp3EncoderHelper {
    private static final String TAG = Mp3EncoderHelper.class.getSimpleName();
    private byte[] mp3Buffer;

    public Mp3EncoderHelper(RecordConfig currentConfig, int bufferSize) {
        mp3Buffer = new byte[(int) (7200 + (bufferSize * 2 * 1.25))];
        int sampleRate = currentConfig.getSampleRate();
        Logger.w(TAG, "in_sampleRate:%s，getChannelCount:%s ，out_sampleRate：%s 位宽： %s,",
                sampleRate, currentConfig.getChannelCount(), sampleRate, currentConfig.getRealEncoding());
        Mp3Encoder.init(sampleRate, currentConfig.getChannelCount(), sampleRate, currentConfig.getRealEncoding());
    }

    /**
     * 转码
     *
     * @param changeBuffer ChangeBuffer
     * @return byte[]
     */
    public byte[] encode(ChangeBuffer changeBuffer) {
        if (changeBuffer == null) {
            return new byte[0];
        }
        short[] buffer = changeBuffer.getData();
        int readSize = changeBuffer.getReadSize();
        if (readSize > 0) {
            int encodedSize = Mp3Encoder.encode(buffer, buffer, readSize, mp3Buffer);
            if (encodedSize < 0) {
                Logger.e(TAG, "Lame encoded size: " + encodedSize);
            }
            return Arrays.copyOfRange(mp3Buffer, 0, encodedSize);
        }
        return new byte[0];
    }

    public static class ChangeBuffer {
        private short[] rawData;
        private int readSize;

        public ChangeBuffer(short[] rawData, int readSize) {
            this.rawData = rawData.clone();
            this.readSize = readSize;
        }

        short[] getData() {
            return rawData;
        }

        int getReadSize() {
            return readSize;
        }
    }
}
