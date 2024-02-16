import 'package:flutter/material.dart';

class ErrorView extends StatelessWidget {
  const ErrorView({super.key, required this.message, this.closeCallback});

  final String message;
  final Function? closeCallback;

  @override
  Widget build(BuildContext context) {
    return ListTile(
      tileColor: Colors.redAccent.shade700,
      shape: const RoundedRectangleBorder(
          borderRadius: BorderRadius.all(Radius.circular(8))),
      title: Text(message, style: const TextStyle(color: Colors.white)),
      // subtitle: Text(
      //     'Created on: ${DateFormat.yMMMd().format(i.second.startedAt)}'),
      leading: const Icon(
        Icons.warning,
        color: Colors.white,
      ),
      trailing: IconButton(
          onPressed: () {
            closeCallback?.call();
          },
          icon: const Icon(
            Icons.close,
            color: Colors.white,
          )),
    );
  }
}
