import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';

class AndroidAlbumPicture {
  static const MethodChannel channel = MethodChannel('android_album_picture');
  static Future<String?> openCamera() async {
    return await channel.invokeMethod("openCamera", Directory.systemTemp.parent.path + "/photo${DateTime.now().toString()}.jpg");
  }
  static Future<String?> openAlbum() async {
    return await channel.invokeMethod("openAlbum");
  }
}