import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:android_album_picture/android_album_picture.dart';

void main() {
  const MethodChannel channel = MethodChannel('android_album_picture');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });
}
