import 'package:flutter/widgets.dart';

extension SetMountedState<T extends StatefulWidget> on State<T> {
  void setMountedState(VoidCallback fn) {
    if (!mounted) return;
    setState(fn);
  }
}
