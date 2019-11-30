import Flutter
import UIKit



public class SwiftRealtimeAudioRecorderPlugin: NSObject, FlutterPlugin {
    
  var recorder : ZXLRecorder = ZXLRecorder()
    
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "realtime_audio_recorder", binaryMessenger: registrar.messenger())
    let instance = SwiftRealtimeAudioRecorderPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }
//
//   override init () {
//        recorder = ZXLRecorder()
//    }
//    
      public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        if(call.method == "startRecorder") {
            // 开始录音
            recorder.start()
        }else if(call.method == "stopRecorder") {
            // 停止录音
            recorder.stop()
        }else {
            result(FlutterMethodNotImplemented)
        }
      }
}
