import 'package:flutter/material.dart';

class Heading24 extends StatelessWidget {
  const Heading24({super.key, required this.text});

  final String text;

  @override
  Widget build(BuildContext context) {
    return Padding(
        padding: const EdgeInsets.fromLTRB(160, 0, 0, 0),
        child: Text(
          text,
          style: const TextStyle(
            color: Colors.deepPurple,
            fontSize: 24,
            fontWeight: FontWeight.w600,
          ),
        ));
  }
}

class Heading18 extends StatelessWidget {
  const Heading18({super.key, required this.text});

  final String text;

  @override
  Widget build(BuildContext context) {
    return Text(
      text,
      style: const TextStyle(color: Colors.deepPurple, fontSize: 18, fontWeight: FontWeight.w600),
    );
  }
}

class ErrorText14 extends StatelessWidget {
  const ErrorText14({super.key, required this.text, this.color});

  final String text;
  final Color? color;

  @override
  Widget build(BuildContext context) {
    return Text(
      text,
      style: TextStyle(color: color ?? Colors.deepPurple, fontSize: 14, fontWeight: FontWeight.normal),
    );
  }
}
