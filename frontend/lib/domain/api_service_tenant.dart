import 'dart:io';

import 'package:dio/dio.dart';
import 'package:frontend/domain/api_service.dart';

import '../main.dart';
import 'model/result.dart';
import 'model/rsa_key.dart';
import 'model/tenant.dart';
import 'model/valid_keys.dart';

extension ApiServiceTenant on ApiService {
  Future<List<Tenant>> getTenants() async {
    Response response = await dio.get('$baseUrl/admin/tenant/list');

    var tenants =
        (response.data as List).map((i) => Tenant.fromJson(i as Map<String, dynamic>)).toList(growable: false);

    return tenants;
  }

  Future<Tenant> createTenant(String name, String host) async {
    Response response = await dio.post('$baseUrl/admin/tenant/create',
        options: Options(headers: {
          HttpHeaders.contentTypeHeader: "application/json",
        }),
        data: {'name': name, 'host': host});

    return Tenant.fromJson(response.data as Map<String, dynamic>);
  }

  Future<Result> deleteTenant(int tenantId) async {
    Response response = await dio.post('$baseUrl/admin/tenant/delete',
        options: Options(headers: {
          HttpHeaders.contentTypeHeader: "application/json",
        }),
        data: {'tenantId': tenantId});

    return Result.fromJson(response.data as Map<String, dynamic>);
  }

  Future<ValidKeys> getKeys(int tenantId) async {
    Response response = await dio.get('$baseUrl/admin/tenant/keys', queryParameters: {'tenantId': tenantId});

    return ValidKeys.fromJson(response.data as Map<String, dynamic>);
  }

  Future<RsaKey> rotateKey(int tenantId) async {
    Response response = await dio.post('$baseUrl/admin/tenant/keys',
        options: Options(headers: {
          HttpHeaders.contentTypeHeader: "application/json",
        }),
        data: {'tenantId': tenantId});

    return RsaKey.fromJson(response.data as Map<String, dynamic>);
  }

  Future<Result> deleteKey(int tenantId, String keyId) async {
    Response response = await dio.post('$baseUrl/admin/tenant/keys/delete',
        options: Options(headers: {
          HttpHeaders.contentTypeHeader: "application/json",
        }),
        data: {'tenantId': tenantId, 'keyId': keyId});

    return Result.fromJson(response.data as Map<String, dynamic>);
  }
}
