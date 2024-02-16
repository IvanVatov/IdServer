// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'client_settings.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ClientSettings _$ClientSettingsFromJson(Map<String, dynamic> json) =>
    ClientSettings(
      (json['grantTypes'] as List<dynamic>).map((e) => e as String).toList(),
      (json['redirectUris'] as List<dynamic>).map((e) => e as String).toList(),
      (json['scope'] as List<dynamic>).map((e) => e as String).toList(),
      (json['audience'] as List<dynamic>).map((e) => e as String).toList(),
      json['tokenExpiration'] as int,
      json['refreshTokenExpiration'] as int,
      json['refreshTokenAbsoluteExpiration'] as int,
    );

Map<String, dynamic> _$ClientSettingsToJson(ClientSettings instance) =>
    <String, dynamic>{
      'grantTypes': instance.grantTypes,
      'redirectUris': instance.redirectUris,
      'scope': instance.scope,
      'audience': instance.audience,
      'tokenExpiration': instance.tokenExpiration,
      'refreshTokenExpiration': instance.refreshTokenExpiration,
      'refreshTokenAbsoluteExpiration': instance.refreshTokenAbsoluteExpiration,
    };
