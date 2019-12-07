import 'dart:async';

import 'package:flutter/services.dart';

/// 实时录音器
/// @author luodongseu
class RealtimeAudioRecorder {
  static const MethodChannel _channel =
  const MethodChannel('realtime_audio_recorder');
  static const EventChannel _dataChannel =
  EventChannel('realtime_audio_recorder.dataChannel');

  /// 是否正在录音
  bool isRecording = false;

  /// 数据流
  Stream<dynamic> _dataStream;

  /// 开始录音
  /// 返回：录音文件mp3地址
  Future<String> start() async {
    isRecording = true;
    return _channel.invokeMethod(
      'startRecorder',
    );
  }

  /// 结束录音
  /// 返回：录音文件mp3地址
  Future<String> stop() async {
    isRecording = false;
    return _channel.invokeMethod(
      'stopRecorder',
    );
  }

  /// 获取数据流
  Stream<dynamic> get dataStream {
    if (_dataStream == null) {
      _dataStream = _dataChannel.receiveBroadcastStream();
    }
    return _dataStream;
  }
}
