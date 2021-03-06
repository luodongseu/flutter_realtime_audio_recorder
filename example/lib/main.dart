import 'dart:async';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:realtime_audio_recorder/realtime_audio_recorder.dart';
import 'package:simple_permissions/simple_permissions.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  RealtimeAudioRecorder recorder = RealtimeAudioRecorder();

  WebSocket webSocket;
  Timer timer;
  int volume = 0;

  @override
  void initState() {
    super.initState();

    init();
  }

  void init() async {
    PermissionStatus status =
        await SimplePermissions.getPermissionStatus(Permission.RecordAudio);
    if (status != PermissionStatus.authorized) {
      await SimplePermissions.requestPermission(Permission.RecordAudio);
    }

    try {
      webSocket = await WebSocket.connect(
          'ws://192.168.1.99:8840/socket',
          headers: {"inspectionNo": "IN1911291429466187084"});
    } catch (e) {
      print('Cannot connect to server socket!!! $e');
      return;
    }
    webSocket.listen((data) {
      print('server data: $data');
    }, onDone: () {
      print('server done');
    }, onError: (e) {
      print('server error: $e');
    });
    recorder.dataStream?.listen((data) {
      print('data: ${List.from(data).length}');
      webSocket.add(List<int>.from(data));
    });
    recorder.volumeStream?.listen((v) {
      print('vol: $v');
      if (v > volume) {
        volume = v;
      }
    });
    timer = Timer.periodic(Duration(seconds: 1), (t) {
      if (recorder.isRecording) {
        webSocket.add('V:$volume');
        volume = 0;
      }
    });
  }

  @override
  void dispose() {
    super.dispose();
    timer?.cancel();
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
          title: const Text('Plugin example ap1p'),
        ),
        body: Center(
          child: Column(
            children: <Widget>[
              FlatButton(
                child: Text(recorder.isRecording ? "停止" : "开始录音"),
                onPressed: () {
                  if (recorder.isRecording) {
                    recorder.stop();
                  } else {
                    recorder.start();
                  }
                  setState(() {});
                },
              ),
              FlatButton(
                child: Text('发送事件'),
                onPressed: () {
                  webSocket?.add("E:2-3");
                },
              )
            ],
          ),
        ),
      ),
    );
  }
}
