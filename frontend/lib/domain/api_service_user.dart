import 'dart:io';

import 'package:dio/dio.dart';
import 'package:frontend/domain/api_service.dart';

import '../main.dart';
import 'model/user.dart';

extension ApiServiceUser on ApiService {
  Future<List<User>> getUsers(int tenantId) async {
    Response response = await dio.get('$baseUrl/admin/user/list', queryParameters: {'tenantId': tenantId});

    var users = (response.data as List).map((i) => User.fromJson(i as Map<String, dynamic>)).toList(growable: false);

    return users;
  }

  Future<User> getUser(int tenantId, String userId) async {
    Response response = await dio.get('$baseUrl/admin/user', queryParameters: {'tenantId': tenantId, 'userId': userId});

    return User.fromJson(response.data as Map<String, dynamic>);
  }

  Future<User> createUser(int tenantId, String account, String password) async {
    Response response = await dio.post('$baseUrl/admin/user',
        queryParameters: {'tenantId': tenantId},
        options: Options(headers: {
          HttpHeaders.contentTypeHeader: "application/json",
        }),
        data: {'account': account, 'password': password});

    return User.fromJson(response.data as Map<String, dynamic>);
  }

  Future<User> updateUser(int tenantId, String userId, Map<String, dynamic> userMap) async {
    Response response = await dio.post('$baseUrl/admin/user/patch',
        queryParameters: {'tenantId': tenantId, 'userId': userId},
        options: Options(headers: {
          HttpHeaders.contentTypeHeader: "application/json",
        }),
        data: userMap);

    return User.fromJson(response.data as Map<String, dynamic>);
  }
}
