import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
import 'package:native_functions/services/services.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  final Completer<GoogleMapController> _controller = Completer();
  Map<MarkerId, Marker> markers = {};
  late StreamController<Location> streamController;

  final double cameraZoom = 20;
  final double cameraTilt = 50;
  final double cameraBearing = 0;

  late CameraPosition _currentPosition = CameraPosition(
    target: LatLng(37.42796133580664, -122.085749655962),
    zoom: cameraZoom,
    bearing: cameraBearing,
    tilt: cameraTilt,
  );

  @override
  void initState() {
    streamController = StreamController();
    streamController.stream.listen((location) {
      updateMarker(location);
    });
    SchedulerBinding.instance.addPostFrameCallback((_) async {
      await getCoords();
    });
    super.initState();
  }

  Future<void> getCoords() async {
    Services.getLocationData();
    Services.startSubscription(streamController);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        centerTitle: true,
        title: const Text('Native Functions'),
      ),
      body: GoogleMap(
        mapType: MapType.normal,
        initialCameraPosition: _currentPosition,
        onMapCreated: (GoogleMapController controller) {
          _controller.complete(controller);
        },
        markers: markers.values.toSet(),
      ),
    );
  }

  void updateMarker(Location location) async {
    if (!mounted) return;

    final mapController = await _controller.future;

    mapController.animateCamera(
      CameraUpdate.newCameraPosition(
        CameraPosition(
          target: LatLng(location.lat, location.long),
          zoom: cameraZoom,
          bearing: cameraBearing,
          tilt: cameraTilt,
        ),
      ),
    );

    const markerId = MarkerId('user_location');
    markers[markerId] = Marker(
      markerId: markerId,
      position: LatLng(location.lat, location.long),
      icon: BitmapDescriptor.defaultMarkerWithHue(
        BitmapDescriptor.hueAzure,
      ),
    );

    setState(() {});
  }
}
