#import "RealtimeAudioRecorderPlugin.h"
#import "Recorder.h"

@interface RealtimeAudioRecorderPlugin()<FlutterStreamHandler,DataListener>
@property (nonatomic, copy) FlutterEventSink dataSink;
@property (nonatomic, strong) Recorder *recorder;
@property (nonatomic, strong) NSTimer         *timer;
@property (nonatomic, assign) BOOL         isStopRecord;//结束录音控制
@end

@implementation RealtimeAudioRecorderPlugin

// 是否正在录音
BOOL isRecording = false;

- (instancetype)init
{
    self = [super init];
    if (self) {
        self.recorder = [[Recorder alloc]init];
        self.recorder.dataListener=self;
    }
    return self;
}

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    // 注册methodChannel
    FlutterMethodChannel* channel = [FlutterMethodChannel
                                     methodChannelWithName:@"realtime_audio_recorder"
                                     binaryMessenger:[registrar messenger]];
    RealtimeAudioRecorderPlugin* instance = [[RealtimeAudioRecorderPlugin alloc] init];
    [registrar addMethodCallDelegate:instance channel:channel];
    
    // 注册eventChannel
    FlutterEventChannel* dataChannel =[FlutterEventChannel
                                       eventChannelWithName:@"realtime_audio_recorder.dataChannel"
                                       binaryMessenger: [registrar messenger]];
    [dataChannel setStreamHandler:instance];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    if ([@"startRecorder" isEqualToString:call.method]) {
        NSLog(@"started");
        // 开始录音
        if(isRecording) {
            [self.recorder stop];
        }
        isRecording = true;
        [self.recorder start];
        result(@"ok");
    } else  if ([@"stopRecorder" isEqualToString:call.method]) {
        NSLog(@"stoped");
        if(isRecording) {
            [self.recorder stop];
        }
        isRecording = false;
        result(@"ok");
    } else {
        result(FlutterMethodNotImplemented);
    }
}

- (FlutterError * _Nullable)onCancelWithArguments:(id _Nullable)arguments {
    self.dataSink = nil;
    return nil;
}

- (FlutterError * _Nullable)onListenWithArguments:(id _Nullable)arguments eventSink:(nonnull FlutterEventSink)events {
    self.dataSink = events;
    return nil;
}

/// 接收到音频数据
- (void)onData:(unsigned char * _Nullable)data length:(int)length {
    if(nil != self.dataSink && nil != data) {
        NSData *dataArr = [NSData dataWithBytes:data  length:length];
        self.dataSink(dataArr);
    }
}

@end

