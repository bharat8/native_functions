import 'dart:async';
import 'dart:developer';

import 'package:flutter/services.dart';

class Services {
  static const platformChannel = MethodChannel('NativeFunctionsChannel');

  static const streamChannel = EventChannel('NativeFunctionsStreamChannel');

  static StreamSubscription? _nativeSubscription;

  static void getLocationData() {
    try {
      platformChannel.invokeMethod('locationData');
    } catch (e) {
      log(e.toString());
    }
  }

  static void startSubscription(StreamController streamController) {
    _nativeSubscription ??= streamChannel.receiveBroadcastStream().listen(
          (event) => eventListener(
            event,
            streamController,
          ),
        );
  }

  static void eventListener(dynamic event, StreamController streamController) {
    streamChannel.receiveBroadcastStream();
    streamController.sink.add(
      Location(
        lat: event['lat'] ?? 0.0,
        long: event['long'] ?? 0.0,
      ),
    );
    print("Data Receieved -> $event");
  }

  static void cancelSubscription() {
    if (_nativeSubscription != null) {
      _nativeSubscription?.cancel();
      _nativeSubscription = null;
    }
  }
}

class Location {
  final double lat;
  final double long;

  Location({required this.lat, required this.long});
}
