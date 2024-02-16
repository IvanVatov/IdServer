// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'server_configuration.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ServerConfiguration _$ServerConfigurationFromJson(Map<String, dynamic> json) =>
    ServerConfiguration(
      json['jwtSigningKeySize'] as int,
      json['jwtSigningAlgorithm'] as String,
    );

Map<String, dynamic> _$ServerConfigurationToJson(
        ServerConfiguration instance) =>
    <String, dynamic>{
      'jwtSigningKeySize': instance.jwtSigningKeySize,
      'jwtSigningAlgorithm': instance.jwtSigningAlgorithm,
    };
