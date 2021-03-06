#import "Recorder.h"
#import <AVFoundation/AVFoundation.h>
#import <lame/lame.h>
#import <Accelerate/Accelerate.h>

@interface Recorder()
@property (nonatomic, strong) AVAudioEngine *engine;
@end


@implementation Recorder

// 存储mp3的字节
unsigned char mp3_buffer[4096];

// 存储的文件
NSMutableData* file;

// mp3库
lame_t lame;

- (instancetype)init
{
    self = [super init];
    if (self) {
        file = [[NSMutableData alloc]init];
    }
    return self;
}

-(void)dealloc{
    
}
/**
 *  设置音频会话
 */
-(void)initAudioSession{
    NSLog(@"Initialize audio session...");
    AVAudioSession *audioSession=[AVAudioSession sharedInstance];
    //设置为播放和录音状态，以便可以在录制完之后播放录音
    NSError *error;
    [audioSession setCategory:AVAudioSessionCategoryRecord error:&error];
    if(error != noErr) {
        NSLog(@"AudioSession setCategory error %@", error.description);
    }
    [audioSession setActive:YES error:&error];
    if(error != noErr) {NSLog(@"AudioSession setActive error %@", error.description);}
}

- (void)start{
    if ([self isRecording]) return;
    NSLog(@"Start to record....");

    AVAudioFrameCount bufferSize = 4096;
    self.engine = [[AVAudioEngine alloc]init];
    AVAudioInputNode* inputNode = self.engine.inputNode;

    // fix: com.apple.coreaudio.avfaudio: required condition is false: format.sampleRate == hwFormat.sampleRate
    AudioOutputUnitStop((self.engine.inputNode.audioUnit));
    AudioUnitUninitialize((self.engine.inputNode.audioUnit));

    [self initAudioSession];

    // 加载lame
    lame = lame_init();
    lame_set_num_channels(lame, 1);//通道
    lame_set_in_samplerate(lame, [inputNode inputFormatForBus:0].sampleRate / 2);//采样率
    // lame_set_brate(lame, 96);//比特率
    lame_set_quality(lame, 2);//音质
    lame_set_out_samplerate(lame, 0);
    //    lame_set_mode(lame, 3);
    lame_set_VBR(lame, vbr_default/*vbr_off*/);
    lame_init_params(lame);
    
    // 监听录音数据
    [inputNode installTapOnBus:0 bufferSize:bufferSize format:[inputNode inputFormatForBus:0] block:^(AVAudioPCMBuffer * _Nonnull buffer, AVAudioTime * _Nonnull when) {
        float*  channel1Buffer = buffer.floatChannelData[0];
        int frameLength = buffer.frameLength / 2;
        NSLog(@"Record data.... %d", buffer.frameLength);
        int bytesWritten = lame_encode_buffer_interleaved_ieee_float(lame, channel1Buffer, frameLength, mp3_buffer, bufferSize);
        if(nil != self.dataListener) {
            [self.dataListener onData:mp3_buffer length:bytesWritten];
        }
        [file appendBytes:mp3_buffer length:bytesWritten];
        
        // 声音大小
        if(nil != self.volumeListener) {
            [self.volumeListener onData:[self calcVolume:channel1Buffer size:bufferSize frameLength:frameLength]];
        }
    }];
    
    // 开始
    [self.engine prepare];
    
    NSError *error;
    [self.engine startAndReturnError:&error];
    if(error != noErr) {
        NSLog(@"Audio engine start error: %@", [error description]);
    }
}

// 返回0-1.0的数据
-(int)calcVolume:(float*)buffer size:(int)bufferSize frameLength:(int)frameLength {
    Float32 avgValue = 0.0;
    // 计算平方平均值
    vDSP_meamgv(buffer, 1, &avgValue, bufferSize);
    Float32 v = (20 * log10f(avgValue)) + 100;
    return MAX(MIN((int)v, 100), 0);
}

- (void)stop{
    if (![self isRecording]) return;
    NSLog(@"Stop recording....");
    
    [self.engine.inputNode removeTapOnBus:0];
    [self.engine stop];
    self.engine = nil;
    
    if(nil != lame) {
       lame_close(lame);
    }
    
    // 存储mp3到文件中
    NSFileManager *fm = [NSFileManager defaultManager];
    NSURL *doc = [fm URLForDirectory:NSDocumentDirectory inDomain:NSUserDomainMask appropriateForURL:nil create:YES error:nil];
    NSURL *outurl = [doc URLByAppendingPathComponent:@"last_record.mp3" isDirectory:NO];
    [file writeToURL:outurl atomically:true];
}

-(BOOL)isRecording{
    return self.engine.isRunning;
}

@end
