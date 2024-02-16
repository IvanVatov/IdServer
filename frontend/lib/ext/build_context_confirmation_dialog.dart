import 'package:flutter/material.dart';

extension ConfirmationDialog on BuildContext {
  void showConfirmationDialog(String question, VoidCallback onConfirmed) {
    showDialog(
      context: this,
      builder: (BuildContext context) => Dialog(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Wrap(
            spacing: 16,
            direction: Axis.vertical,
            crossAxisAlignment: WrapCrossAlignment.center,
            children: [
              Text(question),
              Row(
                children: [
                  TextButton(
                      onPressed: () {
                        Navigator.pop(context);
                        onConfirmed();
                      },
                      child: const Text('Yes!')),
                  TextButton(
                      onPressed: () {
                        Navigator.pop(context);
                      },
                      child: const Text('No'))
                ],
              )
            ],
          ),
        ),
      ),
    );
  }
}
