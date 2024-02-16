import 'package:flutter/material.dart';
import 'package:frontend/di.dart';
import 'package:frontend/domain/api_service.dart';
import 'package:frontend/main.dart';
import 'package:go_router/go_router.dart';

abstract class ProtectedScreenState<T extends StatefulWidget> extends State<T> {
  final ApiService service = getIt<ApiService>();

  @override
  void initState() {
    _validateAuthentication();
    super.initState();
  }

  @override
  void reassemble() {
    _validateAuthentication();
    super.reassemble();
  }

  void _validateAuthentication() {
    if (isAuthenticated != true) {
      context.pushReplacementNamed(RouteName.login);
    }
  }
}
