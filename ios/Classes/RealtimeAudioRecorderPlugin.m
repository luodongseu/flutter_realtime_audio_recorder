#import "RealtimeAudioRecorderPlugin.h"
#import <realtime_audio_recorder/realtime_audio_recorder-Swift.h>

@implementation RealtimeAudioRecorderPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftRealtimeAudioRecorderPlugin registerWithRegistrar:registrar];
}
@end
