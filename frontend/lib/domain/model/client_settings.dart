import 'package:json_annotation/json_annotation.dart';

part 'client_settings.g.dart';

@JsonSerializable()
class ClientSettings {
  ClientSettings(this.grantTypes, this.redirectUris, this.scope, this.audience,
      this.tokenExpiration, this.refreshTokenExpiration, this.refreshTokenAbsoluteExpiration);

  List<String> grantTypes;
  List<String> redirectUris;
  List<String> scope;
  List<String> audience;
  int tokenExpiration;
  int refreshTokenExpiration;
  int refreshTokenAbsoluteExpiration;

  factory ClientSettings.fromJson(Map<String, dynamic> json) =>
      _$ClientSettingsFromJson(json);

  Map<String, dynamic> toJson() => _$ClientSettingsToJson(
        this,
      );
}
