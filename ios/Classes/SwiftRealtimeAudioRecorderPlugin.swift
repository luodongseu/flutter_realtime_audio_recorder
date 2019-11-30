import Flutter
import UIKit

public class SwiftRealtimeAudioRecorderPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "realtime_audio_recorder", binaryMessenger: registrar.messenger())
    let instance = SwiftRealtimeAudioRecorderPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    result("iOS " + UIDevice.current.systemVersion)
  }
}
