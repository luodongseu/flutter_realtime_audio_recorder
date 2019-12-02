#import <Foundation/Foundation.h>

// 数据监听器
@protocol DataListener <NSObject>

// 接收到数据
- (void)onData:(unsigned char*_Nullable)data
        length: (int)length;

@end

@interface Recorder : NSObject
@property (nonatomic, weak) id<DataListener> _Nullable dataListener;

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
@end
