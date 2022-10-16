import 'package:flutter/material.dart';
import 'package:native_functions/services/services.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  var batteryLevel = 0;
  bool initialBatteryFetch = false;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        centerTitle: true,
        title: const Text('Native Functions'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: [
            FunctionWidget(
              functionName: 'Open Browser',
              onPressed: () {
                Services.launchBrowser();
              },
            ),
            FunctionWidget(
              functionName: 'Open Camera',
              onPressed: () {
                Services.openCamera();
              },
            ),
            Column(
              children: [
                FunctionWidget(
                  functionName: 'Get Battery Level',
                  onPressed: () async {
                    batteryLevel = await Services.getBatteryLevel();
                    if (batteryLevel != 0) {
                      initialBatteryFetch = true;
                    }
                    setState(() {});
                  },
                ),
                if (initialBatteryFetch)
                  Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Text('Battery Level : '),
                      Text(
                        batteryLevel.toString(),
                      ),
                    ],
                  ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class FunctionWidget extends StatelessWidget {
  const FunctionWidget({
    super.key,
    required this.functionName,
    required this.onPressed,
  });
  final String functionName;
  final void Function() onPressed;

  @override
  Widget build(BuildContext context) {
    return ElevatedButton(
      onPressed: onPressed,
      child: Text(functionName),
    );
  }
}
