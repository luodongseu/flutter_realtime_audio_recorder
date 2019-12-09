//
//  DataHandler.m
//  realtime_audio_recorder
//
//  Created by luodong on 2019/12/9.
//

#import "DataHandler.h"

@interface DataHandler()

@end

@implementation DataHandler

- (FlutterError * _Nullable)onCancelWithArguments:(id _Nullable)arguments {
    self.sink = nil;
    return nil;
}

- (FlutterError * _Nullable)onListenWithArguments:(id _Nullable)arguments eventSink:(nonnull FlutterEventSink)events {
    self.sink = events;
    return nil;
}

@end
