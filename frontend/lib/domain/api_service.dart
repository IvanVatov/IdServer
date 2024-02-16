import 'dart:async';

import 'package:dio/dio.dart';
import 'package:frontend/domain/model/server_configuration.dart';
import 'package:frontend/domain/model/user.dart';
import 'package:frontend/main.dart';
import 'package:injectable/injectable.dart';

@Singleton()
class ApiService {

  final Dio dio = authenticationManager.client;

  User? _user;

  User? get user => _user;

  ApiService();

  Future<ServerConfiguration> getConfiguration() async {
    Response response = await dio.get('$baseUrl/admin/configuration');

    return ServerConfiguration.fromJson(response.data);
  }

  Future<User> whoAmI() async {
    Response response = await dio.get('$baseUrl/admin/whoami');

    var user = User.fromJson(response.data);
    _user = user;

    return user;
  }
}
