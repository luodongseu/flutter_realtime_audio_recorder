//
//  DataHandler.h
//  realtime_audio_recorder
//
//  Created by luodong on 2019/12/9.
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>

NS_ASSUME_NONNULL_BEGIN

@interface DataHandler : NSObject<FlutterStreamHandler>
@property (nonatomic, copy) FlutterEventSink _Nullable sink;
@end

NS_ASSUME_NONNULL_END
