import 'dart:io';

import 'package:android_album_picture/android_album_picture.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  /// 照片路径
  String _pickPath = "";

  @override
  void initState() {
    AndroidAlbumPicture.channel.setMethodCallHandler((call) => callBack(call));
  }

  /// 更新页面图片
  upDatePicture(MethodCall call) {
    if (!mounted) return;
    setState(() {
      _pickPath = call.arguments;
    });
  }

  /// 通道回调
  callBack(MethodCall call) {
    switch (call.method) {
      case "callback_photo":
        upDatePicture(call);
        break;
    }
  }

  /// 请求相机
  openCamera() async {
    await AndroidAlbumPicture.openCamera();
  }

  /// 请求相册
  openAlbum() async {
    await AndroidAlbumPicture.openAlbum();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('调用安卓原相机、相册'),
        ),
        body: Center(
          child: Column(
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  ElevatedButton(
                    onPressed: openCamera,
                    child: const Text('打开相机'),
                  ),
                  ElevatedButton(
                    onPressed: openAlbum,
                    child: const Text('打开相册'),
                  ),
                ],
              ),
              Container(
                child: _pickPath == ""
                    ? Container()
                    : Container(
                        width: 250,
                        height: 300,
                        decoration: BoxDecoration(
                          image: DecorationImage(
                            image: FileImage(File(_pickPath)),
                            fit: BoxFit.fill,
                          ),
                        ),
                      ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
