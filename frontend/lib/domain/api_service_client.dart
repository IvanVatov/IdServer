import 'dart:io';

import 'package:dio/dio.dart';
import 'package:frontend/domain/api_service.dart';
import 'package:frontend/main.dart';

import 'model/client.dart';
import 'model/result.dart';

extension ApiServiceClient on ApiService {
  Future<Client> getClient(int tenantId, String clientId) async {
    Response response =
        await dio.get('$baseUrl/admin/client', queryParameters: {'tenantId': tenantId, 'clientId': clientId});

    var client = Client.fromJson(response.data as Map<String, dynamic>);

    return client;
  }

  Future<List<Client>> getClients(int tenantId) async {
    Response response = await dio.get('$baseUrl/admin/client/list', queryParameters: {'tenantId': tenantId});

    var clients =
        (response.data as List).map((i) => Client.fromJson(i as Map<String, dynamic>)).toList(growable: false);

    return clients;
  }

  Future<Client> createClient(int tenantId, String clientId, String application) async {
    Response response = await dio.post('$baseUrl/admin/client/create',
        options: Options(headers: {
          HttpHeaders.contentTypeHeader: "application/json",
        }),
        data: {'tenantId': tenantId, 'clientId': clientId, 'application': application});

    return Client.fromJson(response.data as Map<String, dynamic>);
  }

  Future<Result> updateClient(Client updatedClient) async {
    Response response = await dio.post('$baseUrl/admin/client/put',
        options: Options(headers: {
          HttpHeaders.contentTypeHeader: "application/json",
        }),
        data: updatedClient);

    return Result.fromJson(response.data as Map<String, dynamic>);
  }

  Future<Result> deleteClient(int tenantId, String clientId) async {
    Response response = await dio.post('$baseUrl/admin/client/delete',
        options: Options(headers: {
          HttpHeaders.contentTypeHeader: "application/json",
        }),
        data: {'tenantId': tenantId, 'clientId': clientId});

    return Result.fromJson(response.data as Map<String, dynamic>);
  }
}
