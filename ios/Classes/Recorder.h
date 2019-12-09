#import <Foundation/Foundation.h>

// 数据监听器
@protocol DataListener <NSObject>

// 接收到数据
- (void)onData:(unsigned char*_Nullable)data
        length: (int)length;

@end

// 声音监听器
@protocol VolumeListener <NSObject>

// 接收到数据
- (void)onData:(double)data;

@end

@interface Recorder : NSObject
@property (nonatomic, weak) id<DataListener> _Nullable dataListener;
@property (nonatomic, weak) id<VolumeListener> _Nullable volumeListener;

/**
 是否正在录音
 */
-(BOOL)isRecording;

/**
 录音开始
 */
- (void)start;

/**
 录音停止
 */
- (void)stop;

/**
 计算音量
 */
- (double)calcVolume:(float*)buffer size:(int)bufferSize;
@end
