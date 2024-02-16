import 'package:flutter/material.dart';
import 'package:frontend/ext/exception_parse_error.dart';

extension SetMountedState<T extends StatefulWidget> on State<T> {
  void showError(String message, Object exception) {
    if (!mounted) {
      return;
    }

    var snackBar = SnackBar(
      content: Text('$message: ${exception.parseMessage()}'),
      backgroundColor: Colors.red,
      padding: const EdgeInsets.all(24),
    );

    ScaffoldMessenger.of(context).showSnackBar(snackBar);
  }
}
