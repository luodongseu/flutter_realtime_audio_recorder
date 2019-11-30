import 'dart:io';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:realtime_audio_recorder/realtime_audio_recorder.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  RealtimeAudioRecorder recorder = RealtimeAudioRecorder();

  WebSocket webSocket;

  @override
  void initState() {
    super.initState();

    init();
  }

  void init() async {
    webSocket = await WebSocket.connect(
        'ws://192.168.1.99:8840/ksb-live/inspection-socket/100');
    webSocket.listen((data) {
      print('server data: $data');
    }, onDone: () {
      print('server done');
    }, onError: (e) {
      print('server error: $e');
    });
    recorder.dataStream.listen((data) {
      print('data: ${List.from(data).length}');
      webSocket.add(List<int>.from(data));
    });
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await recorder.start();
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: InkWell(
            child: Text("btn"),
            onTap: () {
              if (recorder.isRecording) {
                recorder.stop();
              } else {
                recorder.start();
              }
            },
          ),
        ),
      ),
    );
  }
}
