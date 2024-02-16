import 'package:frontend/domain/model/client_settings.dart';
import 'package:json_annotation/json_annotation.dart';

part 'client.g.dart';

@JsonSerializable()
class Client {
  Client(this.tenantId, this.clientId, this.application, this.clientSecret,
      this.settings);

  int tenantId;
  String clientId;
  String application;
  String clientSecret;
  ClientSettings settings;

  factory Client.fromJson(Map<String, dynamic> json) => _$ClientFromJson(json);

  Map<String, dynamic> toJson() => _$ClientToJson(
        this,
      );
}
