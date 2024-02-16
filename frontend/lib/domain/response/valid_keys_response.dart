import 'package:json_annotation/json_annotation.dart';

part 'valid_keys_response.g.dart';

@JsonSerializable()
class ValidKeysResponse {
  ValidKeysResponse(
      this.tenantId,
      this.clientId,
      this.clientSecret,
      );

  int tenantId;
  String clientId;
  String clientSecret;

  factory ValidKeysResponse.fromJson(Map<String, dynamic> json) => _$ValidKeysResponseFromJson(json);

  Map<String, dynamic> toJson() => _$ValidKeysResponseToJson(
    this,
  );
}
