#import "RealtimeAudioRecorderPlugin.h"
#import "Recorder.h"
#import "DataHandler.h"

@interface RealtimeAudioRecorderPlugin()<DataListener,VolumeListener>
@property (nonatomic, strong) DataHandler* dataSinkHandler;
@property (nonatomic, strong) DataHandler* volumeSinkHandler;
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
    
    // 注册dataChannel
    FlutterEventChannel* dataChannel =[FlutterEventChannel
                                       eventChannelWithName:@"realtime_audio_recorder.dataChannel"
                                       binaryMessenger: [registrar messenger]];
    DataHandler *dataHandler = [[DataHandler alloc]init];
    [dataChannel setStreamHandler:dataHandler];
    instance.dataSinkHandler = dataHandler;
    
    // 注册volumeChannel
    FlutterEventChannel* volumeChannel =[FlutterEventChannel
                                         eventChannelWithName:@"realtime_audio_recorder.volumeChannel"
                                         binaryMessenger: [registrar messenger]];
    DataHandler *volumeHandler = [[DataHandler alloc]init];
    [volumeChannel setStreamHandler:dataHandler];
    instance.volumeSinkHandler = volumeHandler;
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

/// 接收到音频数据
- (void)onData:(unsigned char * _Nullable)data length:(int)length {
    if(nil != self.dataSinkHandler && nil != self.dataSinkHandler.sink && nil != data) {
        NSData *dataArr = [NSData dataWithBytes:data  length:length];
        self.dataSinkHandler.sink(dataArr);
    }
}

/// 声音监听
- (void)onData:(double)data {
    if(nil != self.volumeSinkHandler && nil != self.volumeSinkHandler.sink) {\
        self.volumeSinkHandler.sink(@(data));
    }
}

@end

