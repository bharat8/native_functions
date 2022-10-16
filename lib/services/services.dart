import 'dart:developer';

import 'package:flutter/services.dart';

class Services {
  static const platformChannel = MethodChannel('NativeFunctionsChannel');

  static Future<void> launchBrowser() async {
    try {
      await platformChannel.invokeMethod('launchBrowser', <String, String>{
        'url': 'https://flutter.dev',
      });
    } catch (e) {
      log(e.toString());
    }
  }

  static Future<void> openCamera() async {
    try {
      await platformChannel.invokeMethod('openCamera');
    } catch (e) {
      log(e.toString());
    }
  }

  static Future<int> getBatteryLevel() async {
    try {
      return await platformChannel.invokeMethod('getBatteryLevel');
    } catch (e) {
      log(e.toString());
      return -1;
    }
  }
}
