package com.luodongseu.realtime_audio_recorder

import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.StreamHandler

/**
 * 实时录音的Android插件
 *
 * @author luodong
 */
class RealtimeAudioRecorderPlugin : MethodCallHandler {

    // 数据的SINK
    private var dataSink: EventChannel.EventSink? = null
    // 音量的SINK
    private var volumeSink: EventChannel.EventSink? = null
    // 是否正在录音
    private var isRecording = false
    // 录音器
    private var recorder: Recorder = Recorder.getInstance()

    init {
        // 初始化录音
        recorder.setRecordDataListener { data ->
            // 获取到录音数据 mp3
            if (null != data && data.isNotEmpty()) {
                println("Send mp3 data(length)...." + data.size)
                dataSink?.success(data)
            }
        }
        // 初始化录音
        recorder.setRecordVolumeListener { data ->
            // 获取到录音数据 mp3
            println("Send mp3 volume data....$data")
            volumeSink?.success(data)
        }
    }

    companion object {
        @JvmStatic
        lateinit var methodChannel: MethodChannel
        lateinit var registrar: Registrar

        @JvmStatic
        fun registerWith(_registrar: Registrar) {
            registrar = _registrar
            val plugin = RealtimeAudioRecorderPlugin()
            methodChannel = MethodChannel(_registrar.messenger(), "realtime_audio_recorder")
            methodChannel.setMethodCallHandler(plugin)
            val eventChannel = EventChannel(_registrar.messenger(), "realtime_audio_recorder.dataChannel")
            eventChannel.setStreamHandler(object : StreamHandler {
                override fun onCancel(p0: Any?) {
                    plugin.dataSink = null
                }

                override fun onListen(p0: Any?, p1: EventChannel.EventSink?) {
                    plugin.dataSink = p1
                }
            })
            val volumeSink = EventChannel(_registrar.messenger(), "realtime_audio_recorder.volumeChannel")
            volumeSink.setStreamHandler(object : StreamHandler {
                override fun onCancel(p0: Any?) {
                    plugin.volumeSink = null
                }

                override fun onListen(p0: Any?, p1: EventChannel.EventSink?) {
                    plugin.volumeSink = p1
                }
            })
        }
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when {
            call.method == "startRecorder" -> {
                // 开始录音
                if (isRecording) {
                    recorder.stop()
                }
                isRecording = true
                recorder.start()
                result.success("ok")
            }
            call.method == "stopRecorder" -> {
                // 结束录音
                if (isRecording) {
                    isRecording = false
                    recorder.stop()
                }
                result.success("ok")
            }
            else -> result.notImplemented()
        }
    }
}
