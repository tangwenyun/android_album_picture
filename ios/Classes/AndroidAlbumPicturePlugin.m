#import "AndroidAlbumPicturePlugin.h"
#if __has_include(<android_album_picture/android_album_picture-Swift.h>)
#import <android_album_picture/android_album_picture-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "android_album_picture-Swift.h"
#endif

@implementation AndroidAlbumPicturePlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftAndroidAlbumPicturePlugin registerWithRegistrar:registrar];
}
@end
