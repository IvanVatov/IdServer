import 'package:json_annotation/json_annotation.dart';

part 'server_configuration.g.dart';

@JsonSerializable()
class ServerConfiguration {
  ServerConfiguration(

      this.jwtSigningKeySize,
      this.jwtSigningAlgorithm,
      );

  int jwtSigningKeySize;
  String jwtSigningAlgorithm;

  factory ServerConfiguration.fromJson(Map<String, dynamic> json) => _$ServerConfigurationFromJson(json);

  Map<String, dynamic> toJson() => _$ServerConfigurationToJson(
    this,
  );
}
